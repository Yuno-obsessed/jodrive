package sanity.nil.metadata;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sanity.nil.grpc.block.BlockService;
import sanity.nil.grpc.block.Code;
import sanity.nil.grpc.block.DeleteBlocksRequest;
import sanity.nil.grpc.block.DeleteBlocksResponse;
import sanity.nil.meta.consts.FileState;
import sanity.nil.meta.consts.TaskStatus;
import sanity.nil.meta.model.FileJournalModel;
import sanity.nil.meta.model.TaskModel;
import sanity.nil.meta.model.UserModel;
import sanity.nil.meta.model.WorkspaceModel;
import sanity.nil.meta.service.FileJournalRepo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@JBossLog
@QuarkusTest
@QuarkusTestResource.List({
        @QuarkusTestResource(IntegrationTestResource.class),
        @QuarkusTestResource(OidcWiremockTestResource.class)
})
public class DeleteFileTest {

    @Inject
    FileJournalRepo fileJournalRepo;
    @InjectMock
    @GrpcClient("blockService")
    BlockService blockClient;
    @Inject
    EntityManager entityManager;
    @Inject
    UserTransaction userTransaction;
    @ConfigProperty(name = "application.security.default-user-id")
    UUID defaultUserID;

    @BeforeEach
    public void setup() throws Exception {
        userTransaction.begin();
        entityManager.createQuery("DELETE FROM FileJournalModel f").executeUpdate();
        entityManager.createQuery("DELETE FROM TaskModel t").executeUpdate();
        userTransaction.commit();
    }

    @Test
    public void given_Delete_File_When_Requested_Then_File_State_Changed() throws Exception {
        var file = generateTestData("testFile.png");

        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .when().delete("/api/v1/metadata/file/{id}", file.getFileID())
                .then()
                .statusCode(204)
                .extract().response();

        var expectedFile = getFileJournals("testFile.png").getFirst();
        assertThat(expectedFile.getState()).isEqualTo(FileState.DELETED);
    }

    @Test
    public void given_Delete_File_When_Delete_Job_Executed_And_Block_Service_Returns_Error_Then_Rollback_Changes() throws Exception {
        var file = generateTestData("testFile.png");

        var blocklist = Arrays.asList(file.getBlocklist().split(","));
        var request = DeleteBlocksRequest.newBuilder().addAllHash(blocklist).build();
        var response = DeleteBlocksResponse.newBuilder().setCode(Code.failure).build();

        when(blockClient.deleteBlocks(request))
                .thenReturn(Uni.createFrom().item(response));

        given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .when().delete("/api/v1/metadata/file/{id}", file.getFileID())
                .then()
                .statusCode(204)
                .extract().response();

        // imitate performAt trigger
        userTransaction.begin();
        entityManager.createQuery("UPDATE TaskModel t SET t.performAt = :beforeNow " +
                        "WHERE t.objectID = :fileID")
                .setParameter("beforeNow", LocalDateTime.now().minusDays(1L))
                .setParameter("fileID", file.getFileID())
                .executeUpdate();
        userTransaction.commit();

        Awaitility.await()
                .pollInterval(Duration.ofMillis(500L))
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> getTask(file.getFileID()).getStatus().equals(TaskStatus.FAILED));
        assertThat(getFileJournals("testFile.png")).isNotEmpty();
    }

    @Test
    public void given_Delete_File_When_Delete_Job_Executed_And_Block_Service_Returns_Success_Then_Consider_Task_Finished() throws Exception {
        var file = generateTestData("testFile.png");

        var blocklist = Arrays.asList(file.getBlocklist().split(","));
        var request = DeleteBlocksRequest.newBuilder().addAllHash(blocklist).build();
        var response = DeleteBlocksResponse.newBuilder().setCode(Code.success).build();

        when(blockClient.deleteBlocks(request))
                .thenReturn(Uni.createFrom().item(response));

        given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .when().delete("/api/v1/metadata/file/{id}", file.getFileID())
                .then()
                .statusCode(204)
                .extract().response();

        // imitate performAt trigger
        userTransaction.begin();
        entityManager.createQuery("UPDATE TaskModel t SET t.performAt = :beforeNow " +
                        "WHERE t.objectID = :fileID")
                .setParameter("beforeNow", LocalDateTime.now().minusDays(1L))
                .setParameter("fileID", file.getFileID())
                .executeUpdate();
        userTransaction.commit();

        Awaitility.await()
                .pollInterval(Duration.ofMillis(500L))
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> getFileJournals("testFile.png").isEmpty());

        var task = entityManager.createQuery("SELECT t FROM TaskModel t " +
                        "WHERE t.objectID = :fileID", TaskModel.class)
                .setParameter("fileID", file.getFileID())
                .getSingleResult();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.FINISHED);
    }

    @Transactional
    protected List<FileJournalModel> getFileJournals(String path) {
        return entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE f.id.workspaceID = :wsID AND f.path = :path", FileJournalModel.class)
                .setParameter("wsID", 1L)
                .setParameter("path", path)
                .getResultList();
    }

    @Transactional
    protected TaskModel getTask(Long fileID) {
        return entityManager.createQuery("SELECT t FROM TaskModel t " +
                        "WHERE t.objectID = :fileID", TaskModel.class)
                .setParameter("fileID", fileID)
                .getSingleResult();
    }

    private FileJournalModel generateTestData(String path) throws Exception {
        userTransaction.begin();
        var userUploader = entityManager.find(UserModel.class, defaultUserID);
        var workspace = entityManager.find(WorkspaceModel.class, 1L);
        var journal = new FileJournalModel(workspace, "testFile.png", userUploader, FileState.UPLOADED,
                4256400L, UUID.randomUUID().toString(), 0);
        fileJournalRepo.insert(journal);
        userTransaction.commit();
        return journal;
    }
}

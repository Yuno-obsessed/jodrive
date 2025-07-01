package sanity.nil.metadata;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sanity.nil.grpc.block.BlockService;
import sanity.nil.grpc.block.Code;
import sanity.nil.grpc.block.DeleteBlocksRequest;
import sanity.nil.grpc.block.DeleteBlocksResponse;
import sanity.nil.meta.consts.FileState;
import sanity.nil.meta.consts.TaskStatus;
import sanity.nil.meta.db.tables.Tasks;
import sanity.nil.meta.db.tables.records.FileJournalRecord;
import sanity.nil.meta.db.tables.records.TasksRecord;
import sanity.nil.meta.model.FileJournalEntity;
import sanity.nil.meta.service.FileJournalRepo;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static sanity.nil.meta.db.tables.FileJournal.FILE_JOURNAL;

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
    DSLContext dslContext;
    @Inject
    UserTransaction userTransaction;
    @ConfigProperty(name = "application.security.default-user-id")
    UUID defaultUserID;
    private final String testFile = "/testFile.png";

    @BeforeEach
    public void setup() throws Exception {
        userTransaction.begin();
        dslContext.deleteFrom(FILE_JOURNAL).execute();
        dslContext.deleteFrom(Tasks.TASKS).execute();
        userTransaction.commit();
    }

    @Test
    public void given_Delete_File_When_Requested_Then_File_State_Changed() throws Exception {
        var file = generateTestData(testFile);

        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .when().delete("/api/v1/metadata/file/{id}", file.getFileId())
                .then()
                .statusCode(204)
                .extract().response();

        var expectedFile = getFileJournals(testFile).getFirst();
        assertThat(expectedFile.getState()).isEqualTo(FileState.DELETED.name());
    }

    @Test
    public void given_Delete_File_When_Delete_Job_Executed_And_Block_Service_Returns_Error_Then_Rollback_Changes() throws Exception {
        var file = generateTestData(testFile);

        var blocklist = Arrays.asList(file.getBlocklist().split(","));
        var request = DeleteBlocksRequest.newBuilder().addAllHash(blocklist).build();
        var response = DeleteBlocksResponse.newBuilder().setCode(Code.failure).build();

        when(blockClient.deleteBlocks(request))
                .thenReturn(Uni.createFrom().item(response));

        given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .when().delete("/api/v1/metadata/file/{id}", file.getFileId())
                .then()
                .statusCode(204)
                .extract().response();

        // imitate performAt trigger
        userTransaction.begin();
        dslContext.update(Tasks.TASKS)
                .set(Tasks.TASKS.PERFORM_AT, OffsetDateTime.now().minusDays(1L))
                .where(Tasks.TASKS.OBJECT_ID.eq(String.valueOf(file.getFileId())))
                .execute();
//        entityManager.createQuery("UPDATE TaskModel t SET t.performAt = :beforeNow " +
//                        "WHERE t.objectID = :fileID")
//                .setParameter("beforeNow", LocalDateTime.now().minusDays(1L))
//                .setParameter("fileID", file.getFileID())
//                .executeUpdate();
        userTransaction.commit();

        Awaitility.await()
                .pollInterval(Duration.ofMillis(500L))
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> getTask(file.getFileId()).getStatus().equals(TaskStatus.FAILED.name()));
        assertThat(getFileJournals(testFile)).isNotEmpty();
    }

    @Test
    public void given_Delete_File_When_Delete_Job_Executed_And_Block_Service_Returns_Success_Then_Consider_Task_Finished() throws Exception {
        var file = generateTestData(testFile);

        var blocklist = Arrays.asList(file.getBlocklist().split(","));
        var request = DeleteBlocksRequest.newBuilder().addAllHash(blocklist).build();
        var response = DeleteBlocksResponse.newBuilder().setCode(Code.success).build();

        when(blockClient.deleteBlocks(request))
                .thenReturn(Uni.createFrom().item(response));

        given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .when().delete("/api/v1/metadata/file/{id}", file.getFileId())
                .then()
                .statusCode(204)
                .extract().response();

        // imitate performAt trigger
        userTransaction.begin();
        dslContext.update(Tasks.TASKS)
                .set(Tasks.TASKS.PERFORM_AT, OffsetDateTime.now().minusDays(1L))
                .where(Tasks.TASKS.OBJECT_ID.eq(String.valueOf(file.getFileId())))
                .execute();
//        entityManager.createQuery("UPDATE TaskModel t SET t.performAt = :beforeNow " +
//                        "WHERE t.objectID = :fileID")
//                .setParameter("beforeNow", LocalDateTime.now().minusDays(1L))
//                .setParameter("fileID", file.getFileID())
//                .executeUpdate();
        userTransaction.commit();

        Awaitility.await()
                .pollInterval(Duration.ofMillis(500L))
                .atMost(10, TimeUnit.SECONDS)
                .until(() -> getFileJournals(testFile).isEmpty());

        var task = getTask(file.getFileId());
        assertThat(task.getStatus()).isEqualTo(TaskStatus.FINISHED.name());
    }

    @Transactional
    protected List<FileJournalRecord> getFileJournals(String path) {
        return dslContext.selectFrom(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(1L))
                .and(FILE_JOURNAL.PATH.eq(path))
                .fetch();
//        return entityManager.createQuery("SELECT f FROM FileJournalModel f " +
//                        "WHERE f.id.workspaceID = :wsID AND f.path = :path", FileJournalModel.class)
//                .setParameter("wsID", 1L)
//                .setParameter("path", path)
//                .getResultList();
    }

    @Transactional
    protected TasksRecord getTask(Long fileID) {
        return dslContext.selectFrom(Tasks.TASKS)
                .where(Tasks.TASKS.OBJECT_ID.eq(String.valueOf(fileID)))
                .fetchOne();
//        return entityManager.createQuery("SELECT t FROM TaskModel t " +
//                        "WHERE t.objectID = :fileID", TaskModel.class)
//                .setParameter("fileID", fileID)
//                .getSingleResult();
    }

    private FileJournalRecord generateTestData(String path) throws Exception {
        userTransaction.begin();
        var journal = new FileJournalEntity(1L, testFile, 4256400L, FileState.UPLOADED,
                List.of(UUID.randomUUID().toString()), defaultUserID);
        var savedJournal = fileJournalRepo.insert(journal);
        userTransaction.commit();
        return savedJournal;
    }
}

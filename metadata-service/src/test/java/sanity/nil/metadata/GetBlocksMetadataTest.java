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
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sanity.nil.grpc.block.BlockService;
import sanity.nil.grpc.block.CheckBlocksExistenceRequest;
import sanity.nil.grpc.block.CheckBlocksExistenceResponse;
import sanity.nil.meta.consts.FileState;
import sanity.nil.meta.consts.Quota;
import sanity.nil.meta.dto.block.BlockMetadata;
import sanity.nil.meta.dto.block.GetBlocksMetadata;
import sanity.nil.meta.model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static sanity.nil.meta.consts.Constants.BLOCK_SIZE;

@JBossLog
@QuarkusTest
@QuarkusTestResource.List({
        @QuarkusTestResource(IntegrationTestResource.class),
        @QuarkusTestResource(OidcWiremockTestResource.class)
})
public class GetBlocksMetadataTest {

    @InjectMock
    @GrpcClient("blockService")
    BlockService blockClient;
    @Inject
    EntityManager entityManager;
    @Inject
    UserTransaction userTransaction;
    @ConfigProperty(name = "application.security.default-user-id")
    UUID defaultUserID;
    private final static String DEFAULT_STATISTICS_VALUE = "0";

    @BeforeEach
    public void setUp() throws Exception {
        userTransaction.begin();
        entityManager.createQuery("DELETE FROM FileJournalModel f").executeUpdate();
        entityManager.createQuery("DELETE FROM FileModel f").executeUpdate();
        entityManager.createQuery("UPDATE UserStatisticsModel us " +
                        "SET us.value = :defaultValue WHERE us.id.userID = :userID")
                .setParameter("defaultValue", DEFAULT_STATISTICS_VALUE)
                .setParameter("userID", defaultUserID)
                .executeUpdate();
        userTransaction.commit();
    }

    @Test
    public void given_Valid_Request_When_New_File_Then_Create_FileJournal_And_File_With_Correct_Size() {

        var lastBlockSize = 12000;
        var blocks = IntStream.range(1, 100)
                .mapToObj(e -> new GetBlocksMetadata.BlockInfo(UUID.randomUUID().toString(), e))
                .sorted(Comparator.comparing(a -> a.position()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var newBlocks = blocks.stream().map(e -> e.hash())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        var expectedFileSize = 411053792L;
        var expectedBlockList = String.join(",", newBlocks);

        var blockServiceRequest = CheckBlocksExistenceRequest.newBuilder()
                .addAllHash(newBlocks).build();
        var blockServiceResponse = CheckBlocksExistenceResponse.newBuilder()
                .addAllMissingBlocks(newBlocks).build();
        Mockito.when(blockClient.checkBlocksExistence(blockServiceRequest))
                .thenReturn(Uni.createFrom().item(blockServiceResponse));

        var request = new GetBlocksMetadata(UUID.randomUUID(), 1L, "newFile.png", blocks, lastBlockSize);
        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(200)
                .extract().body().as(BlockMetadata.class);

        assertThat(response.missingBlocks().size()).isEqualTo(blocks.size());

        var insertedJournal = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE f.id.workspaceID = :wsID AND f.file.filename = :filename", FileJournalModel.class)
                .setParameter("wsID", 1L)
                .setParameter("filename", "newFile")
                .getSingleResult();

        assertThat(insertedJournal).isNotNull();
        assertThat(insertedJournal.getBlocklist()).isEqualTo(expectedBlockList);

        var insertedFile = insertedJournal.getFile();
        assertThat(insertedFile.getSize()).isEqualTo(expectedFileSize);
        assertThat(insertedFile.getContentType()).isEqualTo("png");
        assertThat(insertedFile.getUploader().getId()).isEqualTo(defaultUserID);
        assertThat(insertedFile.getVersion()).isEqualTo(0L);
        assertThat(getStorageLimitQuota()).isEqualTo(expectedFileSize);
    }

    @Test
    public void given_Valid_Request_When_File_Exists_Then_Update() throws Exception {
        userTransaction.begin();
        var existingFile = createExistingFile(0);
        userTransaction.commit();

        var lastBlockSize = 14000;

        var existingFileBlocks = Arrays.stream(existingFile.getBlocklist().split(","))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> addedBlocks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            var block = UUID.randomUUID().toString();
            addedBlocks.add(block);
        }

        Set<String> newFileVersionBlocks = Stream.concat(existingFileBlocks.stream().limit(existingFileBlocks.size()-10), addedBlocks.stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> orderedBlocks = new ArrayList<>(newFileVersionBlocks);
        var blocks = IntStream.range(0, orderedBlocks.size())
                .mapToObj(i -> new GetBlocksMetadata.BlockInfo(orderedBlocks.get(i), i))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var expectedFileSize = (newFileVersionBlocks.size()-1) * BLOCK_SIZE + lastBlockSize;
        var expectedBlockList = String.join(",", newFileVersionBlocks);

        var blockServiceRequest = CheckBlocksExistenceRequest.newBuilder()
                .addAllHash(newFileVersionBlocks).build();
        var blockServiceResponse = CheckBlocksExistenceResponse.newBuilder()
                .addAllMissingBlocks(addedBlocks).build();
        Mockito.when(blockClient.checkBlocksExistence(blockServiceRequest))
                .thenReturn(Uni.createFrom().item(blockServiceResponse));

        var request = new GetBlocksMetadata(UUID.randomUUID(), 1L, "testFile.png", blocks, lastBlockSize);
        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(200)
                .extract().body().as(BlockMetadata.class);

        assertThat(response.missingBlocks().size()).isEqualTo(addedBlocks.size());

        var journals = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE f.id.workspaceID = :wsID AND f.file.filename = :filename", FileJournalModel.class)
                .setParameter("wsID", 1L)
                .setParameter("filename", "testFile")
                .getResultList();

        var updatedJournal = journals.stream().filter(e -> e.getHistoryID() > 0).findFirst().get();
        assertThat(updatedJournal).isNotNull();
        assertThat(updatedJournal.getBlocklist()).isEqualTo(expectedBlockList);
        assertThat(updatedJournal.getHistoryID()).isEqualTo(1);

        var updatedFile = updatedJournal.getFile();
        assertThat(updatedFile.getSize()).isEqualTo(expectedFileSize);
        assertThat(updatedFile.getContentType()).isEqualTo("png");
        assertThat(updatedFile.getUploader().getId()).isEqualTo(defaultUserID);
        assertThat(updatedFile.getVersion()).isEqualTo(1);
        assertThat(getStorageLimitQuota()).isEqualTo(expectedFileSize);
    }

    @Test
    public void given_Valid_Request_When_Exceeded_Subscription_Limit_Then_Return_Insufficient_Quota_Exception() throws Exception {
        var almostExceededLimit = 10735418240L;
        userTransaction.begin();
        entityManager.createQuery("UPDATE UserStatisticsModel us SET us.value = :currValue " +
                        "WHERE us.id.userID = :userID AND us.id.statisticsID = :statisticsID")
                .setParameter("currValue", String.valueOf(almostExceededLimit))
                .setParameter("userID", defaultUserID)
                .setParameter("statisticsID", Quota.USER_STORAGE_USED.id())
                .executeUpdate();
        userTransaction.commit();

        var lastBlockSize = 12000;
        var blocks = IntStream.range(1, 101)
                .mapToObj(e -> new GetBlocksMetadata.BlockInfo(UUID.randomUUID().toString(), e))
                .sorted(Comparator.comparing(a -> a.position()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var newBlocks = blocks.stream().map(e -> e.hash())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var blockServiceRequest = CheckBlocksExistenceRequest.newBuilder()
                .addAllHash(newBlocks).build();
        var blockServiceResponse = CheckBlocksExistenceResponse.newBuilder()
                .addAllMissingBlocks(newBlocks).build();
        Mockito.when(blockClient.checkBlocksExistence(blockServiceRequest))
                .thenReturn(Uni.createFrom().item(blockServiceResponse));

        var request = new GetBlocksMetadata(UUID.randomUUID(), 1L, "newFile.png", blocks, lastBlockSize);
        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(400)
                .extract().asString();

        assertThat(response).contains("Exceeded quota USER_STORAGE_USED with limit ");

        var insertedJournal = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE f.id.workspaceID = :wsID AND f.file.filename = :filename", FileJournalModel.class)
                .setParameter("wsID", 1L)
                .setParameter("filename", "newFile")
                .getResultList();

        assertThat(insertedJournal).isEmpty();
    }

    @Test
    public void given_Valid_Request_When_Max_File_Versions_Limit_Reached_Override_Oldest_Version_With_New() throws Exception {
        userTransaction.begin();
        var existingFileVersion1 = createExistingFile(0);
        var existingFileVersion2 = createExistingFile(1);
        var existingFileVersion3 = createExistingFile(2);
        userTransaction.commit();

        var lastBlockSize = 14000;

        var existingFileBlocks = Arrays.stream(existingFileVersion3.getBlocklist().split(","))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> addedBlocks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            var block = UUID.randomUUID().toString();
            addedBlocks.add(block);
        }

        Set<String> newFileVersionBlocks = Stream.concat(existingFileBlocks.stream().limit(existingFileBlocks.size()-10), addedBlocks.stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> orderedBlocks = new ArrayList<>(newFileVersionBlocks);
        var blocks = IntStream.range(0, orderedBlocks.size())
                .mapToObj(i -> new GetBlocksMetadata.BlockInfo(orderedBlocks.get(i), i))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var expectedFileSize = (newFileVersionBlocks.size()-1) * BLOCK_SIZE + lastBlockSize;
        var expectedBlockList = String.join(",", newFileVersionBlocks);

        var blockServiceRequest = CheckBlocksExistenceRequest.newBuilder()
                .addAllHash(newFileVersionBlocks).build();
        var blockServiceResponse = CheckBlocksExistenceResponse.newBuilder()
                .addAllMissingBlocks(addedBlocks).build();
        Mockito.when(blockClient.checkBlocksExistence(blockServiceRequest))
                .thenReturn(Uni.createFrom().item(blockServiceResponse));

        var request = new GetBlocksMetadata(UUID.randomUUID(), 1L, "testFile.png", blocks, lastBlockSize);
        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(200)
                .extract().body().as(BlockMetadata.class);

        assertThat(response.missingBlocks().size()).isEqualTo(addedBlocks.size());

        var journals = entityManager.createQuery("SELECT f FROM FileJournalModel f " +
                        "WHERE f.id.workspaceID = :wsID AND f.file.filename = :filename", FileJournalModel.class)
                .setParameter("wsID", 1L)
                .setParameter("filename", "testFile")
                .getResultList();

        var latestInsertedVersion = journals.stream().max((a, b) -> a.getHistoryID() - b.getHistoryID()).get();
        assertThat(journals.stream().filter(j -> j.getFile().getVersion() == 0).findAny()).isEmpty(); // oldest was deleted
        assertThat(latestInsertedVersion).isNotNull();
        assertThat(latestInsertedVersion.getBlocklist()).isEqualTo(expectedBlockList);
        assertThat(latestInsertedVersion.getHistoryID()).isEqualTo(3);
        assertThat(latestInsertedVersion.getFile().getVersion()).isEqualTo(3);

        var latestInsertedFile = latestInsertedVersion.getFile();
        assertThat(latestInsertedFile.getSize()).isEqualTo(expectedFileSize);
        assertThat(latestInsertedFile.getContentType()).isEqualTo("png");
        assertThat(latestInsertedFile.getUploader().getId()).isEqualTo(defaultUserID);
        assertThat(getStorageLimitQuota()).isEqualTo(expectedFileSize);
    }

    // TODO: add test with imitated failed upload and retry upload

    private FileJournalModel createExistingFile(Integer version) {
        var workspace = entityManager.find(WorkspaceModel.class, 1L);
        var uploadUser = entityManager.find(UserModel.class, defaultUserID);
        var blockList = IntStream.range(1, 101)
                .mapToObj(e -> UUID.randomUUID().toString())
                .collect(Collectors.joining(","));
        var file = new FileModel(version, uploadUser, FileState.IN_UPLOAD, "testFile", "png", 411053792L);

        var fileJournal = new FileJournalModel(workspace, file, blockList, version);
        entityManager.persist(fileJournal);
        return fileJournal;
    }

    private Long getStorageLimitQuota() {
        var userStatistics = entityManager.find(UserStatisticsModel.class, new UserStatisticsModel.UserStatisticsIDModel(
                defaultUserID, Quota.USER_STORAGE_USED.id())
        );
        return Long.parseLong(userStatistics.getValue());
    }
}

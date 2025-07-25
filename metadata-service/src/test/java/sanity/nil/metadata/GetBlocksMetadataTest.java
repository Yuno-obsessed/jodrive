package sanity.nil.metadata;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sanity.nil.grpc.block.BlockService;
import sanity.nil.grpc.block.CheckBlocksExistenceRequest;
import sanity.nil.grpc.block.CheckBlocksExistenceResponse;
import sanity.nil.meta.consts.FileState;
import sanity.nil.meta.consts.Quota;
import sanity.nil.meta.db.tables.records.FileJournalRecord;
import sanity.nil.meta.dto.block.BlockMetadata;
import sanity.nil.meta.dto.block.GetBlocksMetadata;
import sanity.nil.meta.model.FileJournalEntity;
import sanity.nil.meta.service.FileJournalRepo;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static sanity.nil.meta.db.tables.FileJournal.FILE_JOURNAL;
import static sanity.nil.meta.db.tables.UserStatistics.USER_STATISTICS;

@JBossLog
@QuarkusTest
@QuarkusTestResource.List({
        @QuarkusTestResource(IntegrationTestResource.class),
        @QuarkusTestResource(OidcWiremockTestResource.class)
})
public class GetBlocksMetadataTest {

    @Inject
    FileJournalRepo fileJournalRepo;
    @Inject
    RedisDataSource redisDataSource;
    @InjectMock
    @GrpcClient("blockService")
    BlockService blockClient;
    @Inject
    DSLContext dslContext;
    @Inject
    UserTransaction userTransaction;
    @ConfigProperty(name = "application.security.default-user-id")
    UUID defaultUserID;
    private final static String DEFAULT_STATISTICS_VALUE = "0";
    private final static String defaultPath = "/testFile.png";

    @BeforeEach
    public void setUp() throws Exception {
        userTransaction.begin();
        dslContext.deleteFrom(FILE_JOURNAL).execute();
        dslContext.update(USER_STATISTICS)
                .set(USER_STATISTICS.VALUE, DEFAULT_STATISTICS_VALUE)
                .where(USER_STATISTICS.USER_ID.eq(defaultUserID))
                .execute();
        redisDataSource.flushall();
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

        var request = new GetBlocksMetadata(UUID.randomUUID(), 1L, defaultPath, blocks, lastBlockSize);
        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(200)
                .extract().body().as(BlockMetadata.class);

        assertThat(response.missingBlocks().size()).isEqualTo(blocks.size());

        var insertedFile = getFileJournal(defaultPath);

        assertFileState(insertedFile, defaultPath, expectedBlockList, expectedFileSize, defaultUserID, FileState.IN_UPLOAD, 1);
        assertThat(getStorageLimitQuota()).isEqualTo(expectedFileSize);
    }

    @Test
    public void given_Valid_Request_When_File_Exists_Then_Update() throws Exception {
        userTransaction.begin();
        var existingFile = createExistingFile(FileState.UPLOADED);
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

        var expectedFileSize = 448816528L;
        var expectedBlockList = String.join(",", newFileVersionBlocks);

        var blockServiceRequest = CheckBlocksExistenceRequest.newBuilder()
                .addAllHash(newFileVersionBlocks).build();
        var blockServiceResponse = CheckBlocksExistenceResponse.newBuilder()
                .addAllMissingBlocks(addedBlocks).build();
        Mockito.when(blockClient.checkBlocksExistence(blockServiceRequest))
                .thenReturn(Uni.createFrom().item(blockServiceResponse));

        var request = new GetBlocksMetadata(UUID.randomUUID(), 1L, defaultPath, blocks, lastBlockSize);
        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(200)
                .extract().body().as(BlockMetadata.class);

        assertThat(response.missingBlocks().size()).isEqualTo(addedBlocks.size());

        var files = getFileJournals(defaultPath);
        var updatedJournal = files.stream().filter(file -> file.getHistoryId() > 0).findFirst().get();

        assertFileState(updatedJournal, defaultPath, expectedBlockList, expectedFileSize, defaultUserID, FileState.IN_UPLOAD, 2);
        assertThat(getStorageLimitQuota()).isEqualTo(expectedFileSize);
    }

    @Test
    public void given_Valid_Request_When_Exceeded_Subscription_Limit_Then_Return_Insufficient_Quota_Exception() throws Exception {
        var almostExceededLimit = 10735418240L;
        userTransaction.begin();
        dslContext.update(USER_STATISTICS)
                .set(USER_STATISTICS.VALUE, String.valueOf(almostExceededLimit))
                .where(USER_STATISTICS.USER_ID.eq(defaultUserID))
                .and(USER_STATISTICS.STATISTICS_ID.eq(Quota.USER_STORAGE_USED.id())).execute();
//        entityManager.createQuery("UPDATE UserStatisticsModel us SET us.value = :currValue " +
//                        "WHERE us.id.userID = :userID AND us.id.statisticsID = :statisticsID")
//                .setParameter("currValue", String.valueOf(almostExceededLimit))
//                .setParameter("userID", defaultUserID)
//                .setParameter("statisticsID", Quota.USER_STORAGE_USED.id())
//                .executeUpdate();
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

        var request = new GetBlocksMetadata(UUID.randomUUID(), 1L, defaultPath, blocks, lastBlockSize);
        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(400)
                .extract().asString();

        assertThat(response).contains("Exceeded quota USER_STORAGE_USED with limit ");

        var insertedJournal = getFileJournals(defaultPath);

        assertThat(insertedJournal).isEmpty();
    }

    @Test
    public void given_Valid_Request_When_Max_File_Versions_Limit_Reached_Override_Oldest_Version_With_New() throws Exception {
        userTransaction.begin();
        var existingFileVersion1 = createExistingFile(FileState.UPLOADED);
        var existingFileVersion2 = createExistingFile(FileState.UPLOADED);
        var existingFileVersion3 = createExistingFile(FileState.UPLOADED);
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

        var expectedFileSize = 448816528L;
        var expectedBlockList = String.join(",", newFileVersionBlocks);

        var blockServiceRequest = CheckBlocksExistenceRequest.newBuilder()
                .addAllHash(newFileVersionBlocks).build();
        var blockServiceResponse = CheckBlocksExistenceResponse.newBuilder()
                .addAllMissingBlocks(addedBlocks).build();
        Mockito.when(blockClient.checkBlocksExistence(blockServiceRequest))
                .thenReturn(Uni.createFrom().item(blockServiceResponse));

        var request = new GetBlocksMetadata(UUID.randomUUID(), 1L, defaultPath, blocks, lastBlockSize);
        var response = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(200)
                .extract().body().as(BlockMetadata.class);

        assertThat(response.missingBlocks().size()).isEqualTo(addedBlocks.size());

        var files = getFileJournals(defaultPath);

        var latestInsertedVersion = files.stream().max(Comparator.comparingInt(FileJournalRecord::getHistoryId)).get();
        assertThat(files.stream().filter(j -> j.getHistoryId() == 0).findAny()).isEmpty(); // oldest was deleted

        assertFileState(latestInsertedVersion, defaultPath, expectedBlockList, expectedFileSize, defaultUserID, FileState.IN_UPLOAD, 4);
        assertThat(getStorageLimitQuota()).isEqualTo(expectedFileSize);
    }

    @Test
    public void given_Valid_Request_To_Upload_File_When_Called_Second_Time_Update_File_State_To_Uploaded() throws Exception {
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

        var request = new GetBlocksMetadata(UUID.randomUUID(), 1L, defaultPath, blocks, lastBlockSize);
        var responseFirst = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(200)
                .extract().body().as(BlockMetadata.class);

        var blockServiceFinalResponse = CheckBlocksExistenceResponse.newBuilder()
                .addAllMissingBlocks(Collections.EMPTY_LIST).build();
        Mockito.when(blockClient.checkBlocksExistence(blockServiceRequest))
                .thenReturn(Uni.createFrom().item(blockServiceFinalResponse));

        var responseFinal = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(200)
                .extract().body().as(BlockMetadata.class);

        assertThat(responseFirst.missingBlocks().size()).isEqualTo(blocks.size());
        assertThat(responseFinal.missingBlocks()).isNullOrEmpty();

        var insertedJournal = getFileJournal(defaultPath);

        assertFileState(insertedJournal, defaultPath, expectedBlockList, expectedFileSize, defaultUserID, FileState.UPLOADED, 1);
        assertThat(getStorageLimitQuota()).isEqualTo(expectedFileSize);
    }

    @Test
    public void given_Valid_Request_To_Upload_File_When_Called_Third_Time_Get_From_Cache_And_Update_File() throws Exception {
        var lastBlockSize = 12000;
        var blocks = IntStream.range(1, 100)
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

        var request = new GetBlocksMetadata(UUID.randomUUID(), 1L, defaultPath, blocks, lastBlockSize);
        // creates a file with in_upload state
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(200)
                .extract().body().as(BlockMetadata.class);

        blockServiceRequest = CheckBlocksExistenceRequest.newBuilder()
                .addAllHash(newBlocks).build();
        var blockServiceFinalResponse = CheckBlocksExistenceResponse.newBuilder()
                .addAllMissingBlocks(Collections.EMPTY_LIST).build();
        Mockito.when(blockClient.checkBlocksExistence(blockServiceRequest))
                .thenReturn(Uni.createFrom().item(blockServiceFinalResponse));

        // updates file state to uploaded and places it in cache
        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(200)
                .extract().body().as(BlockMetadata.class);

        var newBlock = new GetBlocksMetadata.BlockInfo(UUID.randomUUID().toString(), request.blocks().size()+1);
        request.blocks().add(newBlock);
        var newFinalBlocks = request.blocks().stream().map(e -> e.hash())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var responseFinal = given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().post("/api/v1/metadata")
                .then()
                .statusCode(200)
                .extract().body().as(BlockMetadata.class);

        var expectedBlockList = String.join(",", newFinalBlocks);
        assertThat(responseFinal.missingBlocks().size()).isEqualTo(1);

        var insertedJournal = getFileJournal(defaultPath);
        var expectedFileSize = 411_065_792L;

        assertFileState(insertedJournal, defaultPath, expectedBlockList, expectedFileSize, defaultUserID, FileState.IN_UPLOAD, 2);
        assertThat(getStorageLimitQuota()).isEqualTo(expectedFileSize);
    }

    private void assertFileState(FileJournalRecord file, String name, String blocklist, Long filesize, UUID uploader, FileState state, Integer historyID) {
        assertThat(file.getPath()).isEqualTo(name);
        assertThat(file.getBlocklist()).isEqualTo(blocklist);
        assertThat(file.getSize()).isEqualTo(filesize);
        assertThat(file.getUploaderId()).isEqualTo(uploader);
        assertThat(file.getState()).isEqualTo(state.name());
        assertThat(file.getHistoryId()).isEqualTo(historyID);
    }

    private FileJournalRecord createExistingFile(FileState state) {
        var blockList = IntStream.range(1, 101)
                .mapToObj(e -> UUID.randomUUID().toString()).toList();
        var journal = new FileJournalEntity(1L, defaultPath, 411053792L, state,
                blockList, defaultUserID);
        var savedJournal = fileJournalRepo.insert(journal);
        dslContext.update(USER_STATISTICS)
                .set(USER_STATISTICS.VALUE, savedJournal.getSize().toString())
                .where(USER_STATISTICS.USER_ID.eq(defaultUserID))
                .and(USER_STATISTICS.STATISTICS_ID.eq(Quota.USER_STORAGE_USED.id())).execute();
//        var statistics = entityManager.find(UserStatisticsModel.class, new UserStatisticsModel.UserStatisticsIDModel(
//                defaultUserID, Quota.USER_STORAGE_USED.id())
//        );
//        statistics.setValue(fileJournal.getSize().toString());
        return savedJournal;
    }

    private FileJournalRecord getFileJournal(String path) {
        return dslContext.selectFrom(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(1L))
                .and(FILE_JOURNAL.PATH.eq(path))
                .orderBy(FILE_JOURNAL.HISTORY_ID.desc())
                .limit(1L).fetchOne();
//        return entityManager.createQuery("SELECT f FROM FileJournalModel f " +
//                        "WHERE f.id.workspaceID = :wsID AND f.path = :path " +
//                        "ORDER BY f.historyID DESC", FileJournalModel.class)
//                .setParameter("wsID", 1L)
//                .setParameter("path", path)
//                .setMaxResults(1)
//                .getSingleResult();
    }

    private List<FileJournalRecord> getFileJournals(String path) {
        return dslContext.selectFrom(FILE_JOURNAL)
                .where(FILE_JOURNAL.WS_ID.eq(1L))
                .and(FILE_JOURNAL.PATH.eq(path))
                .orderBy(FILE_JOURNAL.HISTORY_ID.desc()).fetch();
//        return entityManager.createQuery("SELECT f FROM FileJournalModel f " +
//                        "WHERE f.id.workspaceID = :wsID AND f.path = :path " +
//                        "ORDER BY f.historyID DESC", FileJournalModel.class)
//                .setParameter("wsID", 1L)
//                .setParameter("path", path)
//                .getResultList();
    }

    private Long getStorageLimitQuota() {
        return dslContext.select(USER_STATISTICS.VALUE).from(USER_STATISTICS)
                .where(USER_STATISTICS.USER_ID.eq(defaultUserID))
                .and(USER_STATISTICS.STATISTICS_ID.eq(Quota.USER_STORAGE_USED.id()))
                .fetchOne().into(Long.class);
    }
}

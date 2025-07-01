package sanity.nil.metadata;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sanity.nil.grpc.meta.MetadataServiceGrpc;
import sanity.nil.grpc.meta.VerifyLinkRequest;
import sanity.nil.meta.consts.FileState;
import sanity.nil.meta.db.tables.FileJournal;
import sanity.nil.meta.db.tables.records.FileJournalRecord;
import sanity.nil.meta.db.tables.records.LinksRecord;
import sanity.nil.meta.model.FileJournalEntity;
import sanity.nil.meta.security.LinkEncoder;
import sanity.nil.meta.service.FileJournalRepo;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@JBossLog
@QuarkusTest
@QuarkusTestResource.List({
        @QuarkusTestResource(IntegrationTestResource.class),
        @QuarkusTestResource(OidcWiremockTestResource.class)
})
public class LinkIntegrationTest {

    @Inject
    FileJournalRepo fileJournalRepo;
    @ConfigProperty(name = "application.security.default-user-id")
    UUID defaultUserID;
    @Inject
    DSLContext dslContext;
    @Inject
    LinkEncoder linkEncoder;
    @Inject
    UserTransaction userTransaction;
    private final static String defaultPath = "/testFile.png";

    @GrpcClient("metadataService")
    MetadataServiceGrpc.MetadataServiceBlockingStub stub;

    @BeforeAll
    static void beforeAll() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    public void setup() throws Exception {
        userTransaction.begin();
        dslContext.deleteFrom(FileJournal.FILE_JOURNAL).execute();
        userTransaction.commit();
    }

    @Test
    public void given_Valid_Params_When_Construct_Link_Then_Returned_Encrypted_Link_Equal_To_Its_Decrypted_Value() throws Exception {
        var file = generateTestFile();

        String workspaceID = "1";
        var expiresAt = LocalDateTime.now().plusHours(4);
        var expiresAtExpected = expiresAt.toEpochSecond(ZoneOffset.UTC);

        var expectedLink = String.format("%s:%s:%s:%s", defaultUserID, file.getFileId(), workspaceID, expiresAtExpected);

        String constructedLink = given()
                .queryParam("wsID", workspaceID)
                .queryParam("expiresAt", expiresAtExpected)
                .when()
                .post("/api/v1/metadata/file/{id}/share", file.getFileId())
                .then()
                .statusCode(200)
                .extract().asString();

        var plainLink = linkEncoder.decrypt(constructedLink);

        assertThat(constructedLink).doesNotStartWith(expectedLink);
        assertThat(plainLink).isEqualTo(expectedLink);
    }

    @Test
    public void given_Valid_And_Not_Expired_Link_When_Verify_Then_Return_Valid() throws Exception {
        userTransaction.begin();
        var bareLink = String.format("%s:%s:%s:%s", defaultUserID, 1, 1, 12000L);
        var link = new LinksRecord(bareLink, defaultUserID, 0, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(2));
        dslContext.attach(link);
        link.store();
        userTransaction.commit();

        var encryptedLink = linkEncoder.encrypt(bareLink);

        var request = VerifyLinkRequest.newBuilder()
                .setLink(encryptedLink)
                .build();

        var response = stub.verifyLink(request);

        assertThat(response.getValid()).isTrue();
        assertThat(response.getExpired()).isFalse();
    }

    @Test
    public void given_Valid_But_Expired_Link_When_Verify_Then_Return_Valid_And_Expired() throws Exception {
        userTransaction.begin();
        var bareLink = String.format("%s:%s:%s:%s", defaultUserID, 1, 1, 36000L);
        var link = new LinksRecord(bareLink, defaultUserID, 0, OffsetDateTime.now(), OffsetDateTime.now().minusHours(2));
        dslContext.attach(link);
        link.store();
        userTransaction.commit();

        var encryptedLink = linkEncoder.encrypt(bareLink);

        var request = VerifyLinkRequest.newBuilder()
                .setLink(encryptedLink)
                .build();

        var response = stub.verifyLink(request);

        assertThat(response.getValid()).isTrue();
        assertThat(response.getExpired()).isTrue();
    }

    @Test
    public void given_Previously_Valid_But_Tampered_Link_When_Verify_Then_Return_Invalid() throws Exception {
        userTransaction.begin();
        var bareLink = String.format("%s:%s:%s:%s", defaultUserID, 1, 1, 18000L);
        var link = new LinksRecord(bareLink, defaultUserID, 0, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(3));
        dslContext.attach(link);
        link.store();
        userTransaction.commit();

        var encryptedLink = linkEncoder.encrypt(bareLink);
        var tamperedLink = encryptedLink.substring(1);

        var request = VerifyLinkRequest.newBuilder()
                .setLink(tamperedLink)
                .build();

        var response = stub.verifyLink(request);

        assertThat(response.getValid()).isFalse();
        assertThat(response.getExpired()).isFalse();
    }

    private FileJournalRecord generateTestFile() throws Exception {
        userTransaction.begin();
        var journal = new FileJournalEntity(1L, defaultPath, 4256400L, FileState.UPLOADED,
                List.of(UUID.randomUUID().toString()), defaultUserID);
        var savedJournal = fileJournalRepo.insert(journal);
        userTransaction.commit();
        return savedJournal;
    }

}

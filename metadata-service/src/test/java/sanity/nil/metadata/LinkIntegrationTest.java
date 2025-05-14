package sanity.nil.metadata;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sanity.nil.grpc.meta.MetadataServiceGrpc;
import sanity.nil.grpc.meta.VerifyLinkRequest;
import sanity.nil.meta.consts.FileState;
import sanity.nil.meta.consts.TimeUnit;
import sanity.nil.meta.model.FileJournalModel;
import sanity.nil.meta.model.LinkModel;
import sanity.nil.meta.model.UserModel;
import sanity.nil.meta.model.WorkspaceModel;
import sanity.nil.meta.security.LinkEncoder;
import sanity.nil.meta.service.FileJournalRepo;

import java.time.LocalDateTime;
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
    EntityManager entityManager;
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
        entityManager.createQuery("DELETE FROM FileJournalModel f").executeUpdate();
        userTransaction.commit();
    }

    @Test
    public void given_Valid_Params_When_Construct_Link_Then_Returned_Encrypted_Link_Equal_To_Its_Decrypted_Value() throws Exception {
        var file = generateTestFile(defaultPath);

        String workspaceID = "1";
        var expiration = 60000L;

        var expectedLink = String.format("%s:%s:%s:%s", defaultUserID, file.getFileID(), workspaceID, expiration);

        String constructedLink = given()
                .queryParam("wsID", workspaceID)
                .queryParam("timeUnit", TimeUnit.MINUTE.name())
                .queryParam("expiresIn", 1)
                .when()
                .post("/api/v1/metadata/file/{id}/share", file.getFileID())
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
        var link = new LinkModel(defaultUserID, bareLink, LocalDateTime.now().plusMinutes(2));
        entityManager.persist(link);
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
        var link = new LinkModel(defaultUserID, bareLink, LocalDateTime.now().minusHours(2));
        entityManager.persist(link);
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
        var link = new LinkModel(defaultUserID, bareLink, LocalDateTime.now().plusMinutes(3));
        entityManager.persist(link);
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

    private FileJournalModel generateTestFile(String path) throws Exception {
        userTransaction.begin();
        var userUploader = entityManager.find(UserModel.class, defaultUserID);
        var workspace = entityManager.find(WorkspaceModel.class, 1L);
        var journal = new FileJournalModel(workspace, UUID.randomUUID().toString(), userUploader, FileState.UPLOADED,
                4256400L, UUID.randomUUID().toString(), 0);
        fileJournalRepo.insert(journal);
        userTransaction.commit();
        return journal;
    }

}

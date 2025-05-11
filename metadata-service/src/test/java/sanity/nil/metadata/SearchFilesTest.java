package sanity.nil.metadata;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sanity.nil.meta.consts.FileState;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.file.FileInfo;
import sanity.nil.meta.model.FileJournalModel;
import sanity.nil.meta.model.UserModel;
import sanity.nil.meta.model.WorkspaceModel;
import sanity.nil.meta.service.FileJournalRepo;

import java.util.UUID;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@JBossLog
@QuarkusTest
@QuarkusTestResource.List({
        @QuarkusTestResource(IntegrationTestResource.class),
        @QuarkusTestResource(OidcWiremockTestResource.class)
})
public class SearchFilesTest {

    @Inject
    EntityManager entityManager;
    @Inject
    FileJournalRepo fileJournalRepo;
    @Inject
    UserTransaction userTransaction;
    @ConfigProperty(name = "application.security.default-user-id")
    UUID defaultUserID;

    @BeforeEach
    public void setup() throws Exception {
        userTransaction.begin();
        entityManager.createQuery("DELETE FROM FileJournalModel f").executeUpdate();
        userTransaction.commit();
    }

    @Test
    public void given_Three_Files_When_Queried_Then_Return_All_Elements_And_Correct_Pagination() throws Exception {
        generateTestData(3, false);
        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .queryParam("userID", defaultUserID)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/api/v1/metadata/file/search")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<Paged<FileInfo>>() {});

        assertThat(response.elements).hasSize(3);
        assertThat(response.next).isFalse();
        assertThat(response.previous).isFalse();
        assertThat(response.totalPages).isEqualTo(1);
    }

    @Test
    public void given_Many_Files_When_Queried_With_Pagination_Then_Return_All_Elements_And_Correct_Pagination() throws Exception {
        generateTestData(50, false);
        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .queryParam("userID", defaultUserID)
                .queryParam("page", 0)
                .queryParam("size", 50)
                .when().get("/api/v1/metadata/file/search")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<Paged<FileInfo>>() {});

        assertThat(response.elements).hasSize(50);
        assertThat(response.next).isFalse();
        assertThat(response.previous).isFalse();
        assertThat(response.totalPages).isEqualTo(1);
    }

    @Test
    public void given_Three_Files_And_One_Deleted_When_Queried_Without_Deleted_Flag_Then_Omit_Deleted_From_Return() throws Exception {
        generateTestData(3, false);
        generateTestData(1, true);
        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .queryParam("userID", defaultUserID)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/api/v1/metadata/file/search")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<Paged<FileInfo>>() {});

        assertThat(response.elements).hasSize(3);
        assertThat(response.next).isFalse();
        assertThat(response.previous).isFalse();
        assertThat(response.totalPages).isEqualTo(1);
    }

    @Test
    public void given_One_File_And_Five_Deleted_When_Queried_With_Deleted_Flag_Then_Return_Only_Deleted() throws Exception {
        generateTestData(3, false);
        generateTestData(5, true);
        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .queryParam("userID", defaultUserID)
                .queryParam("deleted", true)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/api/v1/metadata/file/search")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<Paged<FileInfo>>() {});

        assertThat(response.elements).hasSize(5);
        assertThat(response.next).isFalse();
        assertThat(response.previous).isFalse();
        assertThat(response.totalPages).isEqualTo(1);
    }


    private void generateTestData(int quantity, boolean deleted) throws Exception {
        userTransaction.begin();
        var userUploader = entityManager.find(UserModel.class, defaultUserID);
        var workspace = entityManager.find(WorkspaceModel.class, 1L);
        IntStream.range(0, quantity)
                .forEach(i -> {
                    var state = deleted ? FileState.DELETED : FileState.UPLOADED;
                    var journal = new FileJournalModel(workspace, UUID.randomUUID().toString(), userUploader, state,
                            4256400L, UUID.randomUUID().toString(), 0);
                    fileJournalRepo.insert(journal);
                });
        userTransaction.commit();
    }
}

package sanity.nil.metadata;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.oidc.server.OidcWiremockTestResource;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;
import lombok.extern.jbosslog.JBossLog;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sanity.nil.meta.consts.TimeUnit;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.block.BlockMetadata;
import sanity.nil.meta.dto.file.FileInfo;
import sanity.nil.meta.model.FileJournalModel;
import sanity.nil.meta.model.FileModel;
import sanity.nil.meta.model.UserModel;
import sanity.nil.meta.model.WorkspaceModel;

import java.io.IOException;
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
    UserTransaction userTransaction;
    @ConfigProperty(name = "application.security.default-user-id")
    UUID defaultUserID;

    @Test
    public void given_Three_Files_When_Queried_Then_Return_All_Elements_And_Correct_Pagination() throws Exception {
        generateTestData(3);
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
        generateTestData(50);
        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .queryParam("userID", defaultUserID)
                .queryParam("page", 2)
                .queryParam("size", 10)
                .when().get("/api/v1/metadata/file/search")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<Paged<FileInfo>>() {});

        assertThat(response.elements).hasSize(50);
        assertThat(response.next).isTrue();
        assertThat(response.previous).isTrue();
        assertThat(response.totalPages).isEqualTo(5);
    }

    private void generateTestData(int quantity) throws Exception {
        userTransaction.begin();
        var userUploader = entityManager.find(UserModel.class, defaultUserID);
        var workspace = entityManager.find(WorkspaceModel.class, 1L);
        IntStream.range(0, quantity)
                .forEach(i -> {
                    var file = new FileModel(userUploader, UUID.randomUUID().toString().substring(0, 30), "png", 4256400L);
                    var journal = new FileJournalModel(workspace, file, UUID.randomUUID().toString(), 0, 0);
                    entityManager.persist(journal);
                });
        userTransaction.commit();
    }
}

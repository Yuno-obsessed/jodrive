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
import sanity.nil.meta.dto.file.FileNode;
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
public class ListFileTreeTest {

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
        generateFile("/", 0L); // base directory
        userTransaction.commit();
    }

    @Test
    public void given_Two_Dirs_In_Root_When_Queried_For_Files_In_One_Then_Return_Only_Them() throws Exception{
        generateDirectoryWithFiles("/home/", 5);
        generateDirectoryWithFiles("/etc/", 3);

        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .queryParam("path", "/home/")
                .when().get("/api/v1/metadata/file/tree")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<FileNode>() {});

        assertThat(response.name).isEqualTo("/home/");
        assertThat(response.children).hasSize(5);
    }

    @Test
    public void given_Two_Dirs_In_Root_When_Queried_For_Root_Then_Return_All() throws Exception{
        generateDirectoryWithFiles("/home/", 5);
        generateDirectoryWithFiles("/etc/", 5);

        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .when().get("/api/v1/metadata/file/tree")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<FileNode>() {});

        assertThat(response.name).isEqualTo("/");
        assertThat(response.children).hasSize(2);
        response.children.forEach(c -> assertThat(c.children).hasSize(5));
    }

    @Test
    public void given_Two_Dirs_And_One_File_In_Root_When_Queried_For_Root_Then_Return_All() throws Exception {
        generateDirectoryWithFiles("/home/", 5);
        generateDirectoryWithFiles("/etc/", 5);
        generateSingleFile("/someFile.png", 350000L);

        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .when().get("/api/v1/metadata/file/tree")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<FileNode>() {});

        assertThat(response.name).isEqualTo("/");
        assertThat(response.children).hasSize(3);
        response.children.stream().limit(2).forEach(c -> assertThat(c.children).hasSize(5));
        assertThat(response.children.getLast().children).isNullOrEmpty();
    }

    @Test
    public void given_Two_Dirs_And_One_With_Nested_Dir_And_One_File_In_Root_When_Queried_For_Root_Then_Return_Nested_Dir_Under_Parent() throws Exception {
        generateDirectoryWithFiles("/home/", 5);
        generateDirectoryWithFiles("/etc/", 5);
        generateDirectoryWithFiles("/home/user/", 10);
        generateSingleFile("/someFile.png", 350000L);

        var response = given()
                .contentType(ContentType.JSON)
                .queryParam("wsID", 1L)
                .when().get("/api/v1/metadata/file/tree")
                .then()
                .statusCode(200)
                .extract().body().as(new TypeRef<FileNode>() {});

        assertThat(response.name).isEqualTo("/");
        assertThat(response.children).hasSize(3);
        var homeDir = response.children.get(0);
        assertThat(homeDir.children).hasSize(6);
        var homeUserDir = homeDir.children.stream().filter(f -> f.fileInfo.isDirectory).findFirst().get();
        assertThat(homeUserDir.children).hasSize(10);
        assertThat(response.children.getLast().children).isNullOrEmpty();
    }

    private void generateDirectoryWithFiles(String directory, int files) throws Exception {
        userTransaction.begin();
        generateFile(directory, 0L);
        IntStream.range(0, files).forEach(e -> {
            generateFile(directory + UUID.randomUUID().toString().substring(0,10), 50000L);
        });
        userTransaction.commit();
    }

    private void generateSingleFile(String filename, Long size) throws Exception {
        userTransaction.begin();
        generateFile(filename, size);
        userTransaction.commit();
    }

    private void generateFile(String filename, Long size) {
        var userUploader = entityManager.find(UserModel.class, defaultUserID);
        var workspace = entityManager.find(WorkspaceModel.class, 1L);
        var journal = new FileJournalModel(workspace, filename, userUploader, FileState.UPLOADED,
                size, UUID.randomUUID().toString(), 0);
        fileJournalRepo.insert(journal);
    }
}

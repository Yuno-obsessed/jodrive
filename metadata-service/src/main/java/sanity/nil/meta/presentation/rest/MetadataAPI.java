package sanity.nil.meta.presentation.rest;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.jboss.resteasy.reactive.RestResponse;
import sanity.nil.meta.consts.FileAction;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.block.BlockMetadata;
import sanity.nil.meta.dto.block.GetBlocksMetadata;
import sanity.nil.meta.dto.file.CreateDirectory;
import sanity.nil.meta.dto.file.FileFilters;
import sanity.nil.meta.dto.file.FileInfo;
import sanity.nil.meta.dto.file.FileNode;
import sanity.nil.meta.service.MetadataService;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/metadata/")
@JBossLog
public class MetadataAPI {

    @Inject
    MetadataService metadataService;

    @DELETE
    @Path("file/{fileID}")
    public Response deleteFile(
            @PathParam("fileID") Long fileID,
            @QueryParam("wsID") Long wsID
    ) {
        metadataService.deleteFile(fileID, wsID);
        return Response.noContent().build();
    }

    @POST
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public BlockMetadata getBlocksMetadata(GetBlocksMetadata request) {
        return metadataService.getBlocksMetadata(request);
    }

    @POST
    @Path("directory")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public RestResponse<FileInfo> createDirectory(CreateDirectory request) {
        return RestResponse.status(
                RestResponse.Status.CREATED,
                metadataService.createDirectory(request)
        );
    }

    @GET
    @Path("directory")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getDirectories(
            @QueryParam("wsID") Long wsID,
            @QueryParam("path") String path
    ) {
        return metadataService.getDirectories(wsID, path);
    }

    @GET
    @Path("file")
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public FileInfo getFileInfo(
            @QueryParam("fileID") Long fileID,
            @QueryParam("wsID") Long wsID,
            @QueryParam("link") String link,
            @QueryParam("path") String path,
            @QueryParam("listVersions") Boolean listVersions,
            @QueryParam("version") Integer version
    ) {
        return metadataService.getFileInfo(fileID, wsID, link, path, listVersions, version);
    }

    @GET
    @Path("file/search")
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<Paged<FileInfo>> getFilesByFilters(
            @QueryParam("wsID") Long wsID,
            @QueryParam("userID") UUID userID,
            @QueryParam("name") String name,
            @QueryParam("deleted") Boolean deleted,
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size
    ) {
        return RestResponse.ok(metadataService.searchFiles(
                new FileFilters(wsID, userID, name, deleted, page, size))
        );
    }

    @GET
    @Path("directory/{directory: .+}/files")
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<Paged<FileInfo>> listDirectory(
            @PathParam("directory") String directory,
            @QueryParam("wsID") Long wsID,
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size
    ) {
        return RestResponse.ok(metadataService.listDirectory(directory, wsID, page, size));
    }

    @GET
    @Path("file/tree")
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<FileNode> getFileTree(
            @QueryParam("wsID") Long wsID,
            @QueryParam("path") String path
    ) {
        return RestResponse.ok(metadataService.listFileTree(wsID, path));
    }

    @POST
    @Path("file/{id}/share")
    @Produces(MediaType.APPLICATION_JSON)
    public String shareFile(
            @PathParam("id") Long fileID,
            @QueryParam("wsID") Long wsID,
            @QueryParam("expiresAt") Long expiresAt
    ) {
        return metadataService.constructLinkForSharing(fileID, wsID, expiresAt);
    }

    @PATCH
    @Path("file/{id}")
    public String updateFile(
            @PathParam("id") Long fileID,
            @QueryParam("wsID") Long wsID,
            @QueryParam("newName") String newName,
            @QueryParam("fileAction") FileAction fileAction
    ) {
        return metadataService.updateFile(fileID, wsID, newName, fileAction);
    }

}

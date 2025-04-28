package sanity.nil.meta.presentation.rest;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import sanity.nil.meta.dto.block.BlockMetadata;
import sanity.nil.meta.dto.block.GetBlockMetadata;
import sanity.nil.meta.dto.file.FileInfo;
import sanity.nil.meta.service.MetadataService;

@Path("/api/v1/metadata/")
@JBossLog
public class MetadataAPI {

    @Inject
    MetadataService metadataService;

    @DELETE
    @Path("file/{fileID}")
    public Response deleteFile(
            @PathParam("fileID") String fileID,
            @QueryParam("wsID") String wsID
    ) {
        metadataService.deleteFile(fileID, wsID);
        return Response.noContent().build();
    }

    @POST
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    @Blocking
    public BlockMetadata getBlocksMetadata(GetBlockMetadata request) {
        log.info("Rest Endpoint thread: " + Thread.currentThread().getName());
        return metadataService.getBlocksMetadata(request);
    }

    @GET
    @Path("file/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public FileInfo getFileInfo(
            @PathParam("id") String fileID,
            @QueryParam("wsID") String wsID,
            @QueryParam("link") String link
    ) {
        return metadataService.getFileInfo(fileID, wsID, link);
    }

    @GET
    @Path("file/{id}/share")
    @Produces(MediaType.APPLICATION_JSON)
    public String shareFile(
            @PathParam("id") String fileID,
            @QueryParam("wsID") String wsID
    ) {
        return metadataService.createLinkForSharing(fileID, wsID);
    }

}

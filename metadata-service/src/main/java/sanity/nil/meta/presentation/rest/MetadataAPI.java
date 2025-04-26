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
    @Path("file/{id}")
    public Response deleteFile(@PathParam("id") String fileID) {
        // TODO: check perms
        metadataService.deleteFile(fileID);
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

//    @GET
//    @Path("/download/{fileId}")
//    @Produces(MediaType.APPLICATION_OCTET_STREAM)
//    @RestStreamElementType("application/octet-stream")
//    @Blocking
//    public Multi<byte[]> download(@PathParam("fileId") String fileId) {
//        return metadataService.getFileBlock(fileId);
//    }

    @GET
    @Path("file/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public FileInfo getFileInfo(@PathParam("id") String fileID) {
        // TODO: check perms
        return metadataService.getFileInfo(fileID);
    }

    @GET
    @Path("file/{id}/share")
    @Produces(MediaType.APPLICATION_JSON)
    public void shareFile(@PathParam("id") String fileID) {
        // TODO: check perms
    }

}

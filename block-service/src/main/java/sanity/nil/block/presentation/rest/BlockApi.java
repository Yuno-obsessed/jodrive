package sanity.nil.block.presentation.rest;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestStreamElementType;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import sanity.nil.block.dto.BlockUpload;
import sanity.nil.block.dto.BlockUploadRequest;
import sanity.nil.block.service.BlockService;

import java.util.List;

@Path("/api/v1")
public class BlockApi {

    @Inject
    BlockService blockService;

    @POST
    @Path("/block")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public BlockUpload uploadBlock(
            @RestForm("blocks") List<FileUpload> blocks,
            @RestForm("body") @PartType(MediaType.APPLICATION_JSON) BlockUploadRequest request
    ) {
        return blockService.saveBlocks(blocks, request);
    }

    @GET
    @Path("/download/{fileId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RestStreamElementType("application/octet-stream")
    public Multi<byte[]> download(
            @PathParam("fileId") String fileId,
            @QueryParam("wsID") String wsID,
            @QueryParam("link") String link
    ) {
        return blockService.getFileBlock(fileId, wsID, link);
    }

}

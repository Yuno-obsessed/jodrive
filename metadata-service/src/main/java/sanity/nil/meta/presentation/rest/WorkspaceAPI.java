package sanity.nil.meta.presentation.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.jboss.resteasy.reactive.RestResponse;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.workspace.CreateWorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceUserDTO;
import sanity.nil.meta.service.WorkspaceService;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/workspace")
public class WorkspaceAPI {

    @Inject
    WorkspaceService workspaceService;

    @GET
    @Path("/{id}")
    public RestResponse<WorkspaceDTO> getWorkspace(@PathParam("id") String id) {
        return RestResponse.ok(
                workspaceService.getWorkspace(id)
        );
    }

    @GET
    @Path("")
    public RestResponse<List<WorkspaceDTO>> getUserWorkspaces() {
        return RestResponse.ok(
                workspaceService.getUserWorkspaces()
        );
    }

    @GET
    @Path("/{id}/users")
    public RestResponse<Paged<WorkspaceUserDTO>> getWorkspaceUsers(
            @PathParam("id") String id,
            @QueryParam("page") int page,
            @QueryParam("size") int size
    ) {
        return RestResponse.ok(
                workspaceService.getWorkspaceUsers(id, page, size)
        );
    }

    @POST
    @Path("")
    public RestResponse<WorkspaceDTO> createWorkspace(CreateWorkspaceDTO dto) {
        return RestResponse.status(
                RestResponse.Status.CREATED,
                workspaceService.createWorkspace(dto)
        );
    }

    @DELETE
    @Path("/{id}/kick")
    public RestResponse<WorkspaceDTO> kickUser(
            @PathParam("id") Long wsID,
            @QueryParam("userID") UUID userID
    ) {
        workspaceService.kickWorkspaceUser(wsID, userID);
        return RestResponse.noContent();
    }

    @GET
    @Path("/{id}/link")
    public RestResponse<String> createJoinWorkspaceLink(
            @PathParam("id") Long wsID
    ) {
        return RestResponse.ok(
                workspaceService.generateJoinWorkspaceLink(wsID)
        );
    }

    @POST
    @Path("/join")
    public RestResponse<WorkspaceDTO> joinWorkspace(
            @QueryParam("link") String link
    ) {
        return RestResponse.status(
                RestResponse.Status.CREATED,
                workspaceService.joinWorkspace(link)
        );
    }

}

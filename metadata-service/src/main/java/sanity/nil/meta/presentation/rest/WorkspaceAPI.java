package sanity.nil.meta.presentation.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.jboss.resteasy.reactive.RestResponse;
import sanity.nil.meta.dto.Paged;
import sanity.nil.meta.dto.workspace.CreateWorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceDTO;
import sanity.nil.meta.dto.workspace.WorkspaceUserDTO;
import sanity.nil.meta.service.WorkspaceService;

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
    public RestResponse<Long> createWorkspace(CreateWorkspaceDTO dto) {
        return RestResponse.ok(
                workspaceService.createWorkspace(dto)
        );
    }
}

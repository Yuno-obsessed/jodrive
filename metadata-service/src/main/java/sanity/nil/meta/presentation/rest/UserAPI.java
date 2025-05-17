package sanity.nil.meta.presentation.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import sanity.nil.meta.dto.user.UserBaseDTO;
import sanity.nil.meta.service.UserService;

import java.util.UUID;

@Path("/api/v1/user/")
public class UserAPI {

    @Inject
    UserService userService;

    @GET
    @Path("{id}")
    public UserBaseDTO getUserInfo(@PathParam("id") String userID) {
        return userService.getUser(UUID.fromString(userID));
    }

    @PATCH
    @Path("{id}")
    public String changeUserAvatar(
            @PathParam("id") String userID,
            @QueryParam("photo") String photo
    ) {
        return userService.changeUserAvatar(userID, photo);
    }

    @POST
    @Path("")
    public String uploadFile(@RestForm("photo") FileUpload photo) {
        return userService.uploadFile(photo);
    }
}

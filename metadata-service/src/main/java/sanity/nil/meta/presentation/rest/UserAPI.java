package sanity.nil.meta.presentation.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import sanity.nil.meta.dto.user.CreateUserDTO;
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

    @POST
    @Path("")
    public UUID createUser(CreateUserDTO createUserDTO) {
        return userService.createUser(createUserDTO);
    }
}

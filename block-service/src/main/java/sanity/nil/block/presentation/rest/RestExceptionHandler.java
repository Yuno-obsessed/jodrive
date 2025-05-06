package sanity.nil.block.presentation.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.jboss.resteasy.reactive.server.UnwrapException;

@ApplicationScoped
@UnwrapException({RuntimeException.class})
public class RestExceptionHandler {

    @ServerExceptionMapper
    public RestResponse<String> mapException(NotFoundException ex) {
        return RestResponse.status(Response.Status.NOT_FOUND, ex.getLocalizedMessage());
    }
}

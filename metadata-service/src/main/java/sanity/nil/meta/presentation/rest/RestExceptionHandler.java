package sanity.nil.meta.presentation.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.jboss.resteasy.reactive.server.UnwrapException;
import sanity.nil.exceptions.WorkspaceIdentityException;
import sanity.nil.meta.exceptions.InsufficientQuotaException;
import sanity.nil.meta.exceptions.InvalidParametersException;

import java.util.NoSuchElementException;

@ApplicationScoped
@UnwrapException({RuntimeException.class})
public class RestExceptionHandler {

    @ServerExceptionMapper
    public RestResponse<String> mapException(InsufficientQuotaException ex) {
        return RestResponse.status(Response.Status.BAD_REQUEST, ex.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<String> mapException(InvalidParametersException ex) {
        return RestResponse.status(Response.Status.BAD_REQUEST, ex.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<String> mapException(NotFoundException ex) {
        return RestResponse.status(Response.Status.NOT_FOUND, ex.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<String> mapException(NoSuchElementException ex) {
        return RestResponse.status(Response.Status.NOT_FOUND, ex.getMessage());
    }

    @ServerExceptionMapper
    public RestResponse<String> mapException(WorkspaceIdentityException ex) {
        return RestResponse.status(Response.Status.FORBIDDEN, ex.getMessage());
    }
}

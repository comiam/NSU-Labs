package org.nsu.fit.tm_backend.exception;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.nsu.fit.tm_backend.exception.data.ServerExceptionResponse;

@Slf4j
public class ServerExceptionMapper implements ExceptionMapper<Throwable> {
    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable exception) {
        log.error("UNHANDLED ERROR: " + exception.getMessage(), exception);

        var status = Response.Status.INTERNAL_SERVER_ERROR;
        var message = "Something went wrong...";
        var details = message;
        var path = uriInfo.getAbsolutePath().getPath();

        if (exception instanceof GeneralException) {
            var generalException = (GeneralException) exception;
            status = generalException.getStatus();
            message = generalException.getMessage();
            details = generalException.getMessage();
        }

        var serverExceptionResponse = ServerExceptionResponse.builder()
            .status(status.getStatusCode())
            .message(message)
            .details(details)
            .path(path)
            .build();

        return Response.status(status)
            .entity(serverExceptionResponse)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .build();
    }
}

package org.nsu.fit.tm_backend.exception;

import javax.ws.rs.core.Response;
import lombok.Getter;

public class GeneralException extends RuntimeException {
    @Getter
    private Response.Status status;

    public GeneralException(String message) {
        this(message, Response.Status.BAD_REQUEST, null);
    }

    public GeneralException(String message, Response.Status status) {
        this(message, status, null);
    }

    public GeneralException(String message, Response.Status status, Throwable cause) {
        super(message, cause);

        this.status = status;
    }
}

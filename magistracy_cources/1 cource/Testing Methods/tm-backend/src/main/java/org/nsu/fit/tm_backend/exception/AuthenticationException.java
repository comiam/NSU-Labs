package org.nsu.fit.tm_backend.exception;

import javax.ws.rs.core.Response;

public class AuthenticationException extends GeneralException {
    public AuthenticationException(String message) {
        super(message, Response.Status.FORBIDDEN);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, Response.Status.FORBIDDEN, cause);
    }
}

package org.nsu.fit.tm_backend.service.impl.auth.exception;


import org.nsu.fit.tm_backend.exception.AuthenticationException;

/**
 * Thrown if an authentication token is invalid.
 */
public class InvalidAuthenticationTokenException extends AuthenticationException {
    public InvalidAuthenticationTokenException(String message) {
        super(message);
    }

    public InvalidAuthenticationTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

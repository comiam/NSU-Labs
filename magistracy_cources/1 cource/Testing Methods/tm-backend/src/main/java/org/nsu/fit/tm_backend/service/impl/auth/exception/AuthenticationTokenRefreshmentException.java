package org.nsu.fit.tm_backend.service.impl.auth.exception;

import org.nsu.fit.tm_backend.exception.AuthenticationException;

public class AuthenticationTokenRefreshmentException extends AuthenticationException {
    public AuthenticationTokenRefreshmentException(String message) {
        super(message);
    }
}

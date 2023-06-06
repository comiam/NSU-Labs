package org.nsu.fit.tm_backend.service.impl.auth.exception;

import org.nsu.fit.tm_backend.exception.AuthenticationException;

public class AccessDeniedException extends AuthenticationException {
    public AccessDeniedException(String message) {
        super(message);
    }
}

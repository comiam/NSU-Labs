package org.nsu.fit.tm_backend.config;

import javax.inject.Inject;
import org.nsu.fit.tm_backend.service.AuthenticationTokenService;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticationTokenDetails;
import org.nsu.fit.tm_backend.service.impl.auth.data.TokenBasedSecurityContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

/**
 * JWT authentication filter.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    @Inject
    private AuthenticationTokenService authenticationTokenService;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String authenticationToken = authorizationHeader.substring(7);
            handleTokenBasedAuthentication(authenticationToken, requestContext);
            return;
        }

        // Other authentication schemes (such as Basic) could be supported
    }

    private void handleTokenBasedAuthentication(String authenticationToken, ContainerRequestContext requestContext) {
        AuthenticationTokenDetails authenticationTokenDetails
                = authenticationTokenService.lookupAuthenticationTokenDetails(authenticationToken);

        AuthenticatedUserDetails authenticatedUserDetails
                = authenticationTokenService.lookupAuthenticatedUserDetails(authenticationTokenDetails);

        boolean isSecure = requestContext.getSecurityContext().isSecure();
        SecurityContext securityContext = new TokenBasedSecurityContext(authenticatedUserDetails, authenticationTokenDetails, isSecure);

        requestContext.setSecurityContext(securityContext);
    }
}

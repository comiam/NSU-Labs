package org.nsu.fit.tm_backend.service;

import java.util.Set;
import org.jvnet.hk2.annotations.Contract;
import org.nsu.fit.tm_backend.service.data.AccountTokenBO;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticationTokenDetails;

@Contract
public interface AuthenticationTokenService {
    AccountTokenBO authenticate(String login, String pass);

    AuthenticationTokenDetails lookupAuthenticationTokenDetails(String authenticationToken);

    AuthenticatedUserDetails lookupAuthenticatedUserDetails(AuthenticationTokenDetails authenticationTokenDetails);
    /**
     * Issue a token for a user with the given authorities.
     */
    String issueToken(String username, Set<String> authorities);

    /**
     * Parse and validate the token.
     */
    AuthenticationTokenDetails parseToken(String token);

    /**
     * Refresh a token.
     */
    String refreshToken(AuthenticationTokenDetails currentTokenDetails);
}

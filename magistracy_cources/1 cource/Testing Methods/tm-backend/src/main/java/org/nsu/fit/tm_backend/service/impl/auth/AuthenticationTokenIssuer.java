package org.nsu.fit.tm_backend.service.impl.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticationTokenDetails;
import org.nsu.fit.tm_backend.shared.Globals;

import java.util.Date;

/**
 * Component which provides operations for issuing JWT tokens.
 */
class AuthenticationTokenIssuer {
    /**
     * Issue a JWT token.
     */
    public String issueToken(AuthenticationTokenDetails authenticationTokenDetails) {
        return Jwts.builder()
                .setId(authenticationTokenDetails.getId())
                .setIssuer(Globals.AUTHENTICATION_JWT_ISSUER)
                .setAudience(Globals.AUTHENTICATION_JWT_AUDIENCE)
                .setSubject(authenticationTokenDetails.getUserName())
                .setIssuedAt(Date.from(authenticationTokenDetails.getIssuedDate().toInstant()))
                .setExpiration(Date.from(authenticationTokenDetails.getExpirationDate().toInstant()))
                .claim(Globals.AUTHENTICATION_JWT_CLAIM_NAMES_AUTHORITIES, authenticationTokenDetails.getAuthorities())
                .claim(Globals.AUTHENTICATION_JWT_CLAIM_NAMES_REFRESH_COUNT, authenticationTokenDetails.getRefreshCount())
                .claim(Globals.AUTHENTICATION_JWT_CLAIM_NAMES_REFRESH_LIMIT, authenticationTokenDetails.getRefreshLimit())
                .signWith(SignatureAlgorithm.HS256, Globals.AUTHENTICATION_JWT_SECRET)
                .compact();
    }
}

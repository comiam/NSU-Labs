package org.nsu.fit.tm_backend.service.impl.auth;

import io.jsonwebtoken.*;
import org.nsu.fit.tm_backend.service.impl.auth.exception.InvalidAuthenticationTokenException;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticationTokenDetails;
import org.nsu.fit.tm_backend.shared.Globals;

import javax.validation.constraints.NotNull;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Component which provides operations for parsing JWT tokens.
 */
class AuthenticationTokenParser {
    /**
     * Parse a JWT token.
     */
    public AuthenticationTokenDetails parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Globals.AUTHENTICATION_JWT_SECRET)
                    .requireAudience(Globals.AUTHENTICATION_JWT_AUDIENCE)
                    .setAllowedClockSkewSeconds(Globals.AUTHENTICATION_JWT_CLOCK_SKEW)
                    .parseClaimsJws(token)
                    .getBody();

            return new AuthenticationTokenDetails(
                    extractTokenIdFromClaims(claims),
                    extractUsernameFromClaims(claims),
                    extractAuthoritiesFromClaims(claims),
                    extractIssuedDateFromClaims(claims),
                    extractExpirationDateFromClaims(claims),
                    extractRefreshCountFromClaims(claims),
                    extractRefreshLimitFromClaims(claims));

        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException | SignatureException e) {
            throw new InvalidAuthenticationTokenException("Invalid token", e);
        } catch (ExpiredJwtException e) {
            throw new InvalidAuthenticationTokenException("Expired token", e);
        } catch (InvalidClaimException e) {
            throw new InvalidAuthenticationTokenException("Invalid value for claim \"" + e.getClaimName() + "\"", e);
        } catch (Exception e) {
            throw new InvalidAuthenticationTokenException("Invalid token", e);
        }
    }

    /**
     * Extract the token identifier from the token claims.
     *
     * @return Identifier of the JWT token
     */
    private String extractTokenIdFromClaims(@NotNull Claims claims) {
        return (String) claims.get(Claims.ID);
    }

    /**
     * Extract the username from the token claims.
     *
     * @return Username from the JWT token
     */
    private String extractUsernameFromClaims(@NotNull Claims claims) {
        return claims.getSubject();
    }

    /**
     * Extract the user authorities from the token claims.
     *
     * @return User authorities from the JWT token
     */
    private Set<String> extractAuthoritiesFromClaims(@NotNull Claims claims) {
        List<String> rolesAsString = (List<String>) claims.getOrDefault(Globals.AUTHENTICATION_JWT_CLAIM_NAMES_AUTHORITIES, new ArrayList<>());
        return rolesAsString.stream().map(String::toUpperCase).collect(Collectors.toSet());
    }

    /**
     * Extract the issued date from the token claims.
     *
     * @return Issued date of the JWT token
     */
    private ZonedDateTime extractIssuedDateFromClaims(@NotNull Claims claims) {
        return ZonedDateTime.ofInstant(claims.getIssuedAt().toInstant(), ZoneId.systemDefault());
    }

    /**
     * Extract the expiration date from the token claims.
     *
     * @return Expiration date of the JWT token
     */
    private ZonedDateTime extractExpirationDateFromClaims(@NotNull Claims claims) {
        return ZonedDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
    }

    /**
     * Extract the refresh count from the token claims.
     *
     * @return Refresh count from the JWT token
     */
    private int extractRefreshCountFromClaims(@NotNull Claims claims) {
        return (int) claims.get(Globals.AUTHENTICATION_JWT_CLAIM_NAMES_REFRESH_COUNT);
    }

    /**
     * Extract the refresh limit from the token claims.
     *
     * @return Refresh limit from the JWT token
     */
    private int extractRefreshLimitFromClaims(@NotNull Claims claims) {
        return (int) claims.get(Globals.AUTHENTICATION_JWT_CLAIM_NAMES_REFRESH_LIMIT);
    }
}

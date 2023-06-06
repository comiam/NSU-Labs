package org.nsu.fit.tm_backend.service.impl.auth;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.jvnet.hk2.annotations.Service;
import org.nsu.fit.tm_backend.exception.AuthenticationException;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.service.AuthenticationTokenService;
import org.nsu.fit.tm_backend.service.CustomerService;
import org.nsu.fit.tm_backend.service.data.AccountTokenBO;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticationTokenDetails;
import org.nsu.fit.tm_backend.service.impl.auth.exception.AuthenticationTokenRefreshmentException;
import org.nsu.fit.tm_backend.service.impl.auth.exception.InvalidAuthenticationTokenException;
import org.nsu.fit.tm_backend.shared.Authority;
import org.nsu.fit.tm_backend.shared.Globals;

/**
 * Лабораторная *: Исследуйте данный класс, подумайте какие потенциальные проблемы он содержит.
 */
@Service
@Slf4j
public class AuthenticationTokenServiceImpl implements AuthenticationTokenService {
    @Inject
    private Repository repository;

    @Inject
    private CustomerService customerService;

    public AccountTokenBO authenticate(String login, String pass) {
        log.info("Credentials: " + login + ":" + pass);

        var accountToken = new AccountTokenBO();
        if (login.equalsIgnoreCase("admin")
                && pass.equalsIgnoreCase("setup")) {
            accountToken.setId(UUID.randomUUID());
            accountToken.setAuthorities(Collections.singleton(Authority.ADMIN_ROLE));
            accountToken.setToken(issueToken(login, accountToken.getAuthorities()));
        } else {
            CustomerPojo customerPojo = customerService.lookupCustomer(login);

            if (customerPojo == null) {
                throw new AuthenticationException(String.format("Customer with login '%s' is not exists.", login));
            }

            accountToken.setId(UUID.randomUUID());
            accountToken.setAuthorities(Collections.singleton(Authority.CUSTOMER_ROLE));;
            accountToken.setToken(issueToken(login, accountToken.getAuthorities()));
        }

        return repository.createAccountToken(accountToken);
    }

    public AuthenticationTokenDetails lookupAuthenticationTokenDetails(String authenticationToken) {
        repository.checkAccountToken(authenticationToken);

        return parseToken(authenticationToken);
    }

    public AuthenticatedUserDetails lookupAuthenticatedUserDetails(AuthenticationTokenDetails authenticationTokenDetails) {
        if (authenticationTokenDetails.getUserName().equalsIgnoreCase("admin")) {
            if (!authenticationTokenDetails.isAdmin()) {
                throw new InvalidAuthenticationTokenException("Invalid token...");
            }
            return new AuthenticatedUserDetails(null, authenticationTokenDetails.getUserName(), authenticationTokenDetails.getAuthorities());
        }

        if (!authenticationTokenDetails.isCustomer()) {
            throw new InvalidAuthenticationTokenException("Invalid token...");
        }

        CustomerPojo customerPojo = repository.getCustomerByLogin(authenticationTokenDetails.getUserName());

        return new AuthenticatedUserDetails(customerPojo.id.toString(), authenticationTokenDetails.getUserName(), authenticationTokenDetails.getAuthorities());
    }

    /**
     * Issue a token for a user with the given authorities.
     */
    public String issueToken(String username, Set<String> authorities) {
        String id = generateTokenIdentifier();
        ZonedDateTime issuedDate = ZonedDateTime.now();
        ZonedDateTime expirationDate = calculateExpirationDate(issuedDate);

        AuthenticationTokenDetails authenticationTokenDetails = new AuthenticationTokenDetails(
                id,
                username,
                authorities,
                issuedDate,
                expirationDate,
                0,
                Globals.AUTHENTICATION_JWT_REFRESH_LIMIT);

        return new AuthenticationTokenIssuer().issueToken(authenticationTokenDetails);
    }

    /**
     * Parse and validate the token.
     */
    public AuthenticationTokenDetails parseToken(String token) {
        return new AuthenticationTokenParser().parseToken(token);
    }

    /**
     * Refresh a token.
     */
    public String refreshToken(AuthenticationTokenDetails currentTokenDetails) {
        if (!currentTokenDetails.isEligibleForRefreshment()) {
            throw new AuthenticationTokenRefreshmentException("This token cannot be refreshed");
        }

        ZonedDateTime issuedDate = ZonedDateTime.now();
        ZonedDateTime expirationDate = calculateExpirationDate(issuedDate);

        // Reuse the same id.
        AuthenticationTokenDetails newTokenDetails = new AuthenticationTokenDetails(
                currentTokenDetails.getId(),
                currentTokenDetails.getUserName(),
                currentTokenDetails.getAuthorities(),
                issuedDate,
                expirationDate,
                currentTokenDetails.getRefreshCount() + 1,
                Globals.AUTHENTICATION_JWT_REFRESH_LIMIT);

        return new AuthenticationTokenIssuer().issueToken(newTokenDetails);
    }

    /**
     * Calculate the expiration date for a token.
     */
    private ZonedDateTime calculateExpirationDate(ZonedDateTime issuedDate) {
        return issuedDate.plusSeconds(Globals.AUTHENTICATION_JWT_VALID_FOR);
    }

    /**
     * Generate a token identifier.
     */
    private String generateTokenIdentifier() {
        return UUID.randomUUID().toString();
    }
}

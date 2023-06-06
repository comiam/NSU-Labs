package org.nsu.fit.tm_backend.service.impl.auth.data;

import org.nsu.fit.tm_backend.shared.Authority;

import java.time.ZonedDateTime;
import java.util.Set;

/**
 * Model that holds details about an authentication token.
 */
public final class AuthenticationTokenDetails {
    private final String id;
    private final String userName;
    private final Set<String> authorities;
    private final ZonedDateTime issuedDate;
    private final ZonedDateTime expirationDate;
    private final int refreshCount;
    private final int refreshLimit;

    public AuthenticationTokenDetails(
            String id,
            String userName,
            Set<String> authorities,
            ZonedDateTime issuedDate,
            ZonedDateTime expirationDate,
            int refreshCount,
            int refreshLimit) {
        this.id = id;
        this.userName = userName;
        this.authorities = authorities;
        this.issuedDate = issuedDate;
        this.expirationDate = expirationDate;
        this.refreshCount = refreshCount;
        this.refreshLimit = refreshLimit;
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public ZonedDateTime getIssuedDate() {
        return issuedDate;
    }

    public ZonedDateTime getExpirationDate() {
        return expirationDate;
    }

    public int getRefreshCount() {
        return refreshCount;
    }

    public int getRefreshLimit() {
        return refreshLimit;
    }

    /**
     * Check if the authentication token is eligible for refreshment.
     */
    public boolean isEligibleForRefreshment() {
        return refreshCount < refreshLimit;
    }

    public boolean isAdmin() {
        return authorities.contains(Authority.ADMIN_ROLE);
    }

    public boolean isCustomer() {
        return authorities.contains(Authority.CUSTOMER_ROLE);
    }
}

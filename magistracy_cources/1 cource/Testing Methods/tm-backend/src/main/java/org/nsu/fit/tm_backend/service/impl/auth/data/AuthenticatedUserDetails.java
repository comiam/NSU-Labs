package org.nsu.fit.tm_backend.service.impl.auth.data;

import org.nsu.fit.tm_backend.shared.Authority;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

public class AuthenticatedUserDetails implements Principal {
    private final String userId;
    private final String userName;
    private final Set<String> authorities;

    public AuthenticatedUserDetails(String userId, String userName, Set<String> authorities) {
        this.userId = userId;
        this.userName = userName;
        this.authorities = Collections.unmodifiableSet(authorities);
    }

    public Set<String> getAuthorities() {
        return authorities;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return userName;
    }

    public boolean isAdmin() {
        return authorities.contains(Authority.ADMIN_ROLE);
    }

    public boolean isCustomer() {
        return authorities.contains(Authority.CUSTOMER_ROLE);
    }
}

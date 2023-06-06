package org.nsu.fit.tm_backend.shared;

public class Globals {
    public static final String ADMIN_LOGIN = "admin";
    public static final String ADMIN_PASS = "setup";

    // The number of seconds to tolerate for clock skew when verifying.
    public static final long AUTHENTICATION_JWT_CLOCK_SKEW = 10;

    // How long the token is valid for (in seconds).
    public static final long AUTHENTICATION_JWT_VALID_FOR = 36000;

    // How many times the token can be refreshed.
    public static final int AUTHENTICATION_JWT_REFRESH_LIMIT = 1;

    public static final String AUTHENTICATION_JWT_ISSUER = "http://example.org";
    public static final String AUTHENTICATION_JWT_AUDIENCE = "http://example.org";

    public static final String AUTHENTICATION_JWT_SECRET = "secret";

    public static final String AUTHENTICATION_JWT_CLAIM_NAMES_AUTHORITIES = "authorities";
    public static final String AUTHENTICATION_JWT_CLAIM_NAMES_REFRESH_COUNT = "refreshCount";
    public static final String AUTHENTICATION_JWT_CLAIM_NAMES_REFRESH_LIMIT = "refreshLimit";
}

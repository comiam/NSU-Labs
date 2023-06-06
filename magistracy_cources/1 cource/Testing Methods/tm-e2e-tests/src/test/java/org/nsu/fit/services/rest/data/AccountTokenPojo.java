package org.nsu.fit.services.rest.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountTokenPojo {
    @JsonProperty("id")
    public UUID id;

    @JsonProperty("authorities")
    public Set<String> authorities;

    @JsonProperty("token")
    public String token;
}

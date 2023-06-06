package org.nsu.fit.tm_backend.service.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountTokenBO {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("authorities")
    private Set<String> authorities;

    @JsonProperty("token")
    private String token;
}

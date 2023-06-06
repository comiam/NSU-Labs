package org.nsu.fit.services.rest.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactPojo {
    @JsonProperty("first_name")
    public String firstName;

    @JsonProperty("last_name")
    public String lastName;

    @JsonProperty("login")
    public String login;

    /**
     * Лабораторная *: здесь следует обратить внимание на хранение и передачу пароля
     * в открытом виде, почему это плохо, как можно исправить.
     */
    @JsonProperty("pass")
    public String pass;

    @JsonProperty("balance")
    public int balance;
}

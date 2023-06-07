package org.nsu.fit.services.rest;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.glassfish.jersey.client.ClientConfig;
import org.nsu.fit.services.log.Logger;
import org.nsu.fit.services.rest.data.*;
import org.nsu.fit.shared.JsonMapper;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RestClient {
    // Note: change url if you want to use the docker compose.
    //private static final String REST_URI = "http://localhost:8080/tm-backend/rest";
    private static final String REST_URI = "http://localhost:8089/tm-backend/rest";

    private final static Client client = ClientBuilder.newClient(new ClientConfig().register(RestClientLogFilter.class));

    public AccountTokenPojo authenticate(String login, String pass) {
        CredentialsPojo credentialsPojo = new CredentialsPojo();

        credentialsPojo.login = login;
        credentialsPojo.pass = pass;

        return post("authenticate", JsonMapper.toJson(credentialsPojo, true), AccountTokenPojo.class, null);
    }

    public CustomerPojo createAutoGeneratedCustomer(AccountTokenPojo accountToken) {
        ContactPojo contactPojo = new ContactPojo();

        // Лабораторная 3: Добавить обработку генерацию фейковых имен, фамилий и логинов.
        // * Исследовать этот вопрос более детально, возможно прикрутить специальную библиотеку для генерации фейковых данных.

        Faker faker = new Faker();
        contactPojo.firstName = faker.name().firstName();
        contactPojo.lastName = faker.name().lastName();
        contactPojo.login = faker.internet().emailAddress();
        contactPojo.pass = faker.internet().password(6, 12);

        return post("customers", JsonMapper.toJson(contactPojo, true), CustomerPojo.class, accountToken);
    }

    public void tryToCreateCustomer(AccountTokenPojo accountToken, CustomerPojo contactPojo) {
        Faker faker = new Faker();
        if (contactPojo.firstName == null) {
            contactPojo.firstName = faker.name().firstName();
        }
        if (contactPojo.lastName == null) {
            contactPojo.lastName = faker.name().lastName();
        }
        if (contactPojo.login == null) {
            contactPojo.login = faker.internet().emailAddress();
        }
        if (contactPojo.pass == null) {
            contactPojo.pass = faker.internet().password(6, 12);
        }
        post("customers", JsonMapper.toJson(contactPojo, true), CustomerPojo.class, accountToken);
    }

    public List<CustomerPojo> getCustomers(AccountTokenPojo accountToken) {
        return get("customers", "", new TypeReference<List<CustomerPojo>>(){}, accountToken);
    }

    public String removeCustomer(UUID customerId, AccountTokenPojo accountToken) {
        return delete("customers/" + customerId.toString(), accountToken);
    }

    public List<PlanPojo> getUserPlans(AccountTokenPojo accountToken, UUID userId) {
        return get("plans/", "customer_id=" + userId.toString(), new TypeReference<ArrayList<PlanPojo>>(){}, accountToken);
    }

    public List<PlanPojo> getAvailablePlans(AccountTokenPojo accountToken) {
        return get("available_plans", "", new TypeReference<ArrayList<PlanPojo>>(){}, accountToken);
    }

    public List<PlanPojo> getAdminPlans(AccountTokenPojo accountToken) {
        return get("plans", "", new TypeReference<ArrayList<PlanPojo>>(){}, accountToken);
    }

    public PlanPojo createPlan(AccountTokenPojo accountToken, String pojo) {
        return post("plans", pojo, PlanPojo.class, accountToken);
    }

    public String deletePlan(AccountTokenPojo accountToken, UUID planId) {
        return delete("plans/" + planId.toString(), accountToken);
    }

    public SubscriptionPojo createSubscription(AccountTokenPojo accountToken, String pojo) {
        return post("subscriptions", pojo, SubscriptionPojo.class, accountToken);
    }

    public String deleteSubscription(AccountTokenPojo accountToken, UUID subscriptionId) {
        return delete("subscriptions/" + subscriptionId.toString(), accountToken);
    }

    public List<SubscriptionPojo> getSubscriptions(AccountTokenPojo accountToken) {
        return get("subscriptions", "", new TypeReference<List<SubscriptionPojo>>(){}, accountToken);
    }

    private static <R> R post(String path, String body, Class<R> responseType, AccountTokenPojo accountToken) {
        // Лабораторная 3: Добавить обработку Responses и Errors. Выводите их в лог.
        // Подумайте почему в filter нет Response чтобы можно было удобно его сохранить.
        Invocation.Builder request = client
                .target(REST_URI)
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        if (accountToken != null) {
            request.header("Authorization", "Bearer " + accountToken.token);
        }
        try {
            String response = request.post(Entity.entity(body, MediaType.APPLICATION_JSON), String.class);
            Logger.info(response);
            return JsonMapper.fromJson(response, responseType);
        } catch (Exception e) {
            Logger.error(e.toString());
            throw e;
        }
    }

    private static <R> R get(String path, String query, TypeReference<R> typeReference, AccountTokenPojo accountToken) {
        Invocation.Builder request = client
                .target(REST_URI)
                .path(path)
                .queryParam("", query)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        if (accountToken != null) {
            request.header("Authorization", "Bearer " + accountToken.token);
        }

        try {
            String response = request.get(String.class);
            Logger.info(response);
            return JsonMapper.fromJson(response, typeReference);
        } catch (Exception e) {
            Logger.error(e.toString());
            throw e;
        }
    }

    private static String delete(String path, AccountTokenPojo accountToken) {
        Invocation.Builder request = client
                .target(REST_URI)
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        if (accountToken != null) {
            request.header("Authorization", "Bearer " + accountToken.token);
        }

        try {
            String response = request.delete(String.class);
            Logger.info(response);
            return response;
        } catch (Exception e) {
            Logger.error(e.toString());
            throw e;
        }
    }

    private static class RestClientLogFilter implements ClientRequestFilter {
        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            if (requestContext.getEntity() != null) {
                Logger.debug(requestContext.getEntity().toString());
            }

            // Лабораторная 3: разобраться как работает данный фильтр
            // и добавить логирование METHOD и HEADERS.
            Logger.debug("method: " + requestContext.getMethod());

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            String json = mapper.writeValueAsString(requestContext.getHeaders());

            Object obj = mapper.readValue(json, Object.class);
            String headers = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            Logger.debug("headers: " + headers);
        }
    }
}
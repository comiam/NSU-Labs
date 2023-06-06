package org.nsu.fit.tm_backend.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.nsu.fit.tm_backend.controller.data.CredentialsRequest;
import org.nsu.fit.tm_backend.controller.data.HealthCheckResponse;
import org.nsu.fit.tm_backend.controller.data.TopUpBalanceRequest;
import org.nsu.fit.tm_backend.mapper.StatisticMapper;
import org.nsu.fit.tm_backend.repository.data.ContactPojo;
import org.nsu.fit.tm_backend.repository.data.CustomerPojo;
import org.nsu.fit.tm_backend.repository.data.PlanPojo;
import org.nsu.fit.tm_backend.repository.data.SubscriptionPojo;
import org.nsu.fit.tm_backend.service.AuthenticationTokenService;
import org.nsu.fit.tm_backend.service.CustomerService;
import org.nsu.fit.tm_backend.service.PlanService;
import org.nsu.fit.tm_backend.service.StatisticService;
import org.nsu.fit.tm_backend.service.SubscriptionService;
import org.nsu.fit.tm_backend.service.impl.auth.data.AuthenticatedUserDetails;
import org.nsu.fit.tm_backend.shared.Authority;
import org.nsu.fit.tm_backend.shared.JsonMapper;

@Path("")
@Slf4j
public class RestController {
    @Inject
    private AuthenticationTokenService authenticationTokenService;

    @Inject
    private CustomerService customerService;

    @Inject
    private SubscriptionService subscriptionService;

    @Inject
    private PlanService planService;

    @Inject
    private StatisticService statisticService;

    @POST
    @Path("/authenticate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response authenticate(CredentialsRequest credentials) {
        var result = authenticationTokenService
            .authenticate(credentials.getLogin(), credentials.getPass());

        return Response.ok().entity(result).build();
    }

    @GET
    @Path("/health_check")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response healthCheck() {
        var result = HealthCheckResponse.builder()
            .dbStatus("OK")
            .status("OK")
            .build();
        return Response.ok().entity(result).build();
    }

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ Authority.ADMIN_ROLE, Authority.CUSTOMER_ROLE })
    public Response me(@Context SecurityContext securityContext) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            ContactPojo contactPojo = customerService.me(authenticatedUserDetails);

            return Response.ok().entity(JsonMapper.toJson(contactPojo, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    // Example request: ../customers?login='john_wick@example.com'
    @GET
    @Path("/customers")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({ Authority.ADMIN_ROLE, Authority.CUSTOMER_ROLE })
    public Response getCustomers(@Context SecurityContext securityContext, @DefaultValue("") @QueryParam("login") String customerLogin) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            if (authenticatedUserDetails.isCustomer()) {
                customerLogin = authenticatedUserDetails.getName();
            }

            String login = customerLogin;

            List<CustomerPojo> customers = customerService
                    .getCustomers().stream()
                    .filter(x -> login.isEmpty() || x.login.equals(login))
                    .collect(Collectors.toList());

            return Response.ok().entity(JsonMapper.toJson(customers, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @POST
    @Path("/customers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response createCustomer(CustomerPojo customerData) {
        try {
            // create new customer
            CustomerPojo customer = customerService.createCustomer(customerData);

            // send the answer
            return Response.ok().entity(JsonMapper.toJson(customer, true)).build();
        } catch (IllegalArgumentException ex) {
            log.error(ex.getMessage(), ex);
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @DELETE
    @Path("/customers/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response deleteCustomer(@PathParam("id") UUID customerId) {
        try {
            customerService.deleteCustomer(customerId);

            // send the answer
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @POST
    @Path("/customers/top_up_balance")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.CUSTOMER_ROLE)
    public Response topUpBalance(@Context SecurityContext securityContext, TopUpBalanceRequest topUpBalanceRequest) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            var customerId = UUID.fromString(authenticatedUserDetails.getUserId());
            customerService.topUpBalance(customerId, topUpBalanceRequest.getMoney());

            // send the answer.
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @GET
    @Path("/plans")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response getPlans(@DefaultValue("") @QueryParam("customer_id") String customerIdStr) {
        try {
            UUID customerId = null;
            if (!StringUtils.isBlank(customerIdStr)) {
                customerId = UUID.fromString(customerIdStr);
            }

            List<PlanPojo> plans = planService.getPlans(customerId);

            return Response.ok().entity(JsonMapper.toJson(plans, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @GET
    @Path("/available_plans")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.CUSTOMER_ROLE)
    public Response getAvailablePlans(@Context SecurityContext securityContext) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            List<PlanPojo> plans = planService
                    .getPlans(UUID.fromString(authenticatedUserDetails.getUserId()));

            return Response.ok().entity(JsonMapper.toJson(plans, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @POST
    @Path("/plans")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response createPlan(String planDataJson) {
        try {
            // convert json to object
            PlanPojo planData = JsonMapper.fromJson(planDataJson, PlanPojo.class);

            // create new customer
            PlanPojo plan = planService.createPlan(planData);

            // send the answer
            return Response.ok().entity(JsonMapper.toJson(plan, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @DELETE
    @Path("/plans/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response deletePlan(@PathParam("id") String planId) {
        try {
            planService.deletePlan(UUID.fromString(planId));

            // send the answer
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @POST
    @Path("/subscriptions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.CUSTOMER_ROLE)
    public Response createSubscription(@Context SecurityContext securityContext, String subscriptionDataJson) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            // convert json to object.
            SubscriptionPojo subscriptionPojo = JsonMapper.fromJson(subscriptionDataJson, SubscriptionPojo.class);

            // create new subscription.
            subscriptionPojo.customerId = UUID.fromString(authenticatedUserDetails.getUserId());
            subscriptionPojo = subscriptionService.createSubscription(subscriptionPojo);

            // send the answer.
            return Response.ok().entity(JsonMapper.toJson(subscriptionPojo, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @DELETE
    @Path("/subscriptions/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.CUSTOMER_ROLE)
    public Response deleteSubscription(@PathParam("id") String subscriptionId) {
        try {
            subscriptionService.deleteSubscription(UUID.fromString(subscriptionId));
            return Response.ok().build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @GET
    @Path("/subscriptions")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.ADMIN_ROLE)
    public Response getSubscriptions(@DefaultValue("") @QueryParam("customer_id") String customerIdStr) {
        try {
            UUID customerId = null;
            if (!StringUtils.isBlank(customerIdStr)) {
                customerId = UUID.fromString(customerIdStr);
            }

            List<SubscriptionPojo> subscriptions = subscriptionService
                    .getSubscriptions(customerId);

            return Response.ok().entity(JsonMapper.toJson(subscriptions, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @GET
    @Path("/available_subscriptions")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.CUSTOMER_ROLE)
    public Response getAvailableSubscriptions(@Context SecurityContext securityContext) {
        try {
            AuthenticatedUserDetails authenticatedUserDetails = (AuthenticatedUserDetails)securityContext.getUserPrincipal();

            List<SubscriptionPojo> subscriptions = subscriptionService
                    .getSubscriptions(UUID.fromString(authenticatedUserDetails.getUserId()));

            return Response.ok().entity(JsonMapper.toJson(subscriptions, true)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getMessage() + "\n" + ExceptionUtils.getFullStackTrace(ex)).build();
        }
    }

    @GET
    @Path("/statistic")
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(Authority.CUSTOMER_ROLE)
    public Response getStatistic(@Context SecurityContext securityContext) {
        var result = statisticService.calculate();
        return Response.ok().entity(StatisticMapper.INSTANCE.toStatisticResponse(result)).build();
    }
}

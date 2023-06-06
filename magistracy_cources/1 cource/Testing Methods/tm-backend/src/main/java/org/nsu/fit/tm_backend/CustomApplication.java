package org.nsu.fit.tm_backend;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Singleton;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.nsu.fit.tm_backend.config.AuthenticationFilter;
import org.nsu.fit.tm_backend.config.AuthorizationFilter;
import org.nsu.fit.tm_backend.config.CORSFilter;
import org.nsu.fit.tm_backend.controller.RestController;
import org.nsu.fit.tm_backend.exception.ServerExceptionMapper;
import org.nsu.fit.tm_backend.repository.Repository;
import org.nsu.fit.tm_backend.repository.impl.MemoryRepository;
import org.nsu.fit.tm_backend.service.AuthenticationTokenService;
import org.nsu.fit.tm_backend.service.CustomerService;
import org.nsu.fit.tm_backend.service.PlanService;
import org.nsu.fit.tm_backend.service.StatisticService;
import org.nsu.fit.tm_backend.service.SubscriptionService;
import org.nsu.fit.tm_backend.service.impl.CustomerServiceImpl;
import org.nsu.fit.tm_backend.service.impl.PlanServiceImpl;
import org.nsu.fit.tm_backend.service.impl.StatisticServiceImpl;
import org.nsu.fit.tm_backend.service.impl.SubscriptionServiceImpl;
import org.nsu.fit.tm_backend.service.impl.auth.AuthenticationTokenServiceImpl;

public class CustomApplication extends ResourceConfig {
    public CustomApplication() {
        register(
            new LoggingFeature(
                Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
                Level.INFO,
                LoggingFeature.Verbosity.PAYLOAD_TEXT,
                10000));

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(MemoryRepository.class).to(Repository.class).in(Singleton.class);

                bind(AuthenticationTokenServiceImpl.class).to(AuthenticationTokenService.class);

                bind(CustomerServiceImpl.class).to(CustomerService.class);
                bind(SubscriptionServiceImpl.class).to(SubscriptionService.class);
                bind(PlanServiceImpl.class).to(PlanService.class);
                bind(StatisticServiceImpl.class).to(StatisticService.class);
            }
        });

        register(AuthenticationFilter.class);
        register(AuthorizationFilter.class);
        register(CORSFilter.class);

        register(ServerExceptionMapper.class);

        register(RestController.class);
    }
}

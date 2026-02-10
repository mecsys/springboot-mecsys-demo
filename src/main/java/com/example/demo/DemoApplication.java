package com.example.demo;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.ObservationFilter;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@Slf4j
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("test")
    public String testEndpoint() {
        return "Hello world";
    }

    @Bean
    public ObservationFilter serverAndClientMetricsTags() {
        return context -> {

            KeyValues keyValues = KeyValues.empty();

            // =========================
            // HTTP SERVER
            // =========================
            if (context instanceof ServerRequestObservationContext serverCtx) {

                var carrier = serverCtx.getCarrier();
                if (carrier != null) {

                    String customerId = carrier.getHeader("customerId");
                    String environmentId = carrier.getHeader("environmentId");

                    if (StringUtils.isNotBlank(customerId)
                            && StringUtils.isNotBlank(environmentId)) {

                        keyValues = keyValues
                                .and(KeyValue.of("customerId", customerId))
                                .and(KeyValue.of("environmentId", environmentId));
                    }
                }
            }

            // =========================
            // HTTP CLIENT
            // =========================
            if (context instanceof ClientRequestObservationContext) {

                ServerRequestObservationContext serverCtx =
                        ObservationThreadLocalAccessor.getCurrentServerContext();
                log.info("Server context presente? {}",
                    ObservationThreadLocalAccessor.getCurrentServerContext() != null);

                if (serverCtx != null && serverCtx.getCarrier() != null) {

                    String customerId =
                            serverCtx.getCarrier().getHeader("customerId");
                    String environmentId =
                            serverCtx.getCarrier().getHeader("environmentId");

                    if (StringUtils.isNotBlank(customerId)
                            && StringUtils.isNotBlank(environmentId)) {

                        keyValues = keyValues
                                .and(KeyValue.of("customerId", customerId))
                                .and(KeyValue.of("environmentId", environmentId));
                    }
                }
            }

            if (!keyValues.isEmpty()) {
                context.addLowCardinalityKeyValues(keyValues);
            }

            return context;
        };
    }

    @Bean
    TaskExecutor taskExecutor(ObservationRegistry registry) {
        return new ObservationAwareExecutor(
                Executors.newFixedThreadPool(10),
                registry
        );
    }

}

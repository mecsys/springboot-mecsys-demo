package com.example.demo;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.ObservationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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
    public ObservationFilter customTagFilter() {
        return context -> {
            log.info("Executing observation filter");

            if (context instanceof ServerRequestObservationContext observationContext) {
                KeyValues keyValues = KeyValues.empty();

                // Optional tag which will be present in metrics only when the condition is evaluated to true
                if (
                        observationContext.getCarrier() != null &
                                observationContext.getCarrier().getHeader("customerId") != null &
                                observationContext.getCarrier().getHeader("environmentId") != null
                ) {
                    var customerId = observationContext.getCarrier().getHeader("customerId");
                    var environmentId = observationContext.getCarrier().getHeader("environmentId");

                    keyValues = keyValues
                            .and(KeyValue.of("customerId", customerId))
                            .and(KeyValue.of("environmentId", environmentId));
                }

                context.addLowCardinalityKeyValues(keyValues);
            }
            return context;
        };
    }
}

package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}

@RestController
class TestController {

    private final WebClient webClient;

    TestController(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://echo.mecsys.com.br").build();
    }

    @GetMapping("/test")
    public Mono<String> test() {
        return webClient.get()
                .uri("/downstream")
                .retrieve()
                .bodyToMono(String.class)
                .thenReturn("ok");
    }

    @GetMapping("/downstream")
    public String downstream() {
        return "downstream-ok";
    }
}

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureObservability
class ObservationMetricsTest {

    @Autowired
    MeterRegistry meterRegistry;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldAddSameLabelsToServerAndClientMetrics() {

        HttpHeaders headers = new HttpHeaders();
        headers.add("customerId", "123");
        headers.add("environmentId", "prod");

        restTemplate.exchange(
                "/test",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // HTTP SERVER metric
        Timer serverTimer = meterRegistry
                .find("http.server.requests")
                .tags(
                        "customerId", "123",
                        "environmentId", "prod"
                )
                .timer();

        // HTTP CLIENT metric
        Timer clientTimer = meterRegistry
                .find("http.client.requests")
                .tags(
                        "customerId", "123",
                        "environmentId", "prod"
                )
                .timer();

        assertThat(serverTimer).isNotNull();
        assertThat(clientTimer).isNotNull();
    }
}

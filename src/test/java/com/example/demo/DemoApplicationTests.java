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


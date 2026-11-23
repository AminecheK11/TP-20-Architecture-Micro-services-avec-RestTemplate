package com.example.eurekaserver;

import com.netflix.discovery.EurekaClientConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EurekaServerApplicationTests {
    @Autowired TestRestTemplate http;
    @Autowired EurekaClientConfig config;
    @Autowired LoggingSystem logging;

    @Test void serverIsStandalone() {
        assertThat(config.shouldRegisterWithEureka()).isFalse();
        assertThat(config.shouldFetchRegistry()).isFalse();
    }

    @Test void dashboardIsAccessible() {
        var response = http.getForEntity("/", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Eureka", "No instances available");
    }

    @Test void registryIsEmpty() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
        var response = http.exchange("/eureka/apps", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"applications\"").doesNotContain("\"instance\"");
    }

    @Test void netflixLogsAreOff() {
        assertThat(logging.getLoggerConfiguration("com.netflix.eureka").getEffectiveLevel()).isEqualTo(LogLevel.OFF);
        assertThat(logging.getLoggerConfiguration("com.netflix.discovery").getEffectiveLevel()).isEqualTo(LogLevel.OFF);
    }
}

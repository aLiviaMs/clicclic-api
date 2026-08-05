package com.clicclic.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "IT" suffix (Integration Test) instead of "Test" is a Maven/Failsafe
 * convention: these run separately from fast unit tests, since spinning up
 * a real Postgres container takes a few seconds.
 *
 * This test proves the whole chain works end to end: Spring Boot starts,
 * connects to a REAL Postgres (not a mock), Flyway runs V1__create_users_table.sql
 * against it, and the app answers HTTP requests. If any layer is
 * misconfigured, this test fails — which is the point.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthCheckIT {

    // Starts an ephemeral Postgres in a Docker container just for this test
    // run, then destroys it. Nothing is shared with the docker-compose
    // Postgres used in manual dev — this is fully isolated and reproducible.
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    // application-dev.yml points at localhost:5432, but Testcontainers picks
    // a random free port for isolation. This overrides Spring's datasource
    // properties at test startup with whatever host/port/credentials the
    // container actually got.
    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void healthEndpointReportsUp() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("http://localhost:" + port + "/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
    }
}

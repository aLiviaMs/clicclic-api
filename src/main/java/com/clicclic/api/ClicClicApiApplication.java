package com.clicclic.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. @SpringBootApplication triggers component scanning
 * (finds our @Service/@RestController/@Repository classes) and
 * auto-configuration (wires the web server, JPA, Flyway, etc. from
 * what's on the classpath and in application.yml).
 */
@SpringBootApplication
public class ClicClicApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClicClicApiApplication.class, args);
    }
}

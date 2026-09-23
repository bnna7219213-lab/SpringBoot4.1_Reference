package com.example.jetty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.jetty.config.ServerConfig;

/**
 * Spring Boot 4.1 embedded server switching demo application entry point.
 *
 * <p>Switch server via Maven Profile:
 * <ul>
 *   <li>{@code mvn spring-boot:run}         -uses Tomcat by default</li>
 *   <li>{@code mvn spring-boot:run -Pjetty} -switches to Jetty</li>
 * </ul>
 *
 * <p>Spring Boot 4.x no longer supports Undertow (Servlet 6.1 compatibility),
 * so only Tomcat and Jetty are demonstrated.
 */
@SpringBootApplication
@EnableConfigurationProperties(ServerConfig.class)
public class TomcatJettySwitchApplication {

    public static void main(String[] args) {
        SpringApplication.run(TomcatJettySwitchApplication.class, args);
    }
}

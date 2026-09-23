package com.example.jetty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds custom application-level server settings from the {@code app.server.*}
 * namespace. These are complementary to Spring Boot's own {@code server.*}
 * properties (which bind directly to the embedded container via
 * {@code ServerProperties}).
 *
 * <p>This class demonstrates a clean pattern: application-specific metadata
 * is kept separate from the server configuration (which Spring handles
 * internally via the respective {@code *ServletWebServerFactory}).
 */
@Data
@ConfigurationProperties(prefix = "app.server")
public class ServerConfig {

    /**
     * Application identifier label, exposed through the /api/server/info endpoint.
     */
    private String label = "springboot4-server";

    /**
     * Whether the background status collector thread is enabled.
     */
    private boolean statusCollectorEnabled = true;

    /**
     * Wraps this config as a {@link ServerProperties} view object.
     *
     * <p>Spring Boot provides its own {@code ServerProperties} (bound to
     * {@code server.*}); this nested class serves as a unified view for the
     * application layer.
     */
    public ServerProperties toServerProperties() {
        ServerProperties props = new ServerProperties();
        props.setLabel(this.label);
        props.setStatusCollectorEnabled(this.statusCollectorEnabled);
        return props;
    }

    /**
     * Unified server properties view object for application layer responses.
     */
    @Data
    public static class ServerProperties {
        private String label;
        private boolean statusCollectorEnabled;
    }
}

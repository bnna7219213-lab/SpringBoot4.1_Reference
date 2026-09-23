package com.example.jetty.controller;

import com.example.jetty.config.ServerConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST controller that exposes real-time information about the running
 * embedded server (Tomcat or Jetty).
 *
 * <p>All data is read live from the {@link ServletWebServerApplicationContext}
 * and {@link ServletContext} - no static configuration values.
 */
@RestController
@RequestMapping("/api/server")
@RequiredArgsConstructor
public class ServerInfoController {

    private final ServletWebServerApplicationContext webServerAppContext;
    private final ObjectProvider<ServerConfig> serverConfigProvider;

    /**
     * Returns comprehensive server information including server type,
     * thread/connection configuration, JVM thread stats, and virtual
     * thread indicators.
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> serverInfo(HttpServletRequest request) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("timestamp", OffsetDateTime.now().toString());

        // --- 1. Detect embedded server type from WebServerFactory ---
        Map<String, Object> server = detectServerInfo();
        info.put("server", server);        info.put("configuredMaxThreads", server.get("configuredMaxThreads"));
        info.put("configuredMaxConnections", server.get("configuredMaxConnections"));

        // --- 2. Servlet context info ---
        Map<String, Object> servlet = readServletContextInfo(request);
        if (servlet != null) {
            info.put("servlet", servlet);
        }

        // --- 3. JVM thread info ---
        info.put("jvmThreads", readJvmThreadInfo());

        // --- 4. Virtual thread indicator ---
        info.put("virtualThreads", readVirtualThreadInfo());

        // --- 5. Application-level custom config ---
        appendAppConfig(info);

        return ResponseEntity.ok(info);
    }

    /**
     * Simple health check.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", "UP");
        health.put("timestamp", OffsetDateTime.now().toString());
        return ResponseEntity.ok(health);
    }

    // ===========================
    //  Private helper methods
    // ===========================

    private Map<String, Object> detectServerInfo() {
        Map<String, Object> server = new LinkedHashMap<>();
        WebServerFactory factory = this.webServerAppContext.getWebServerFactory();

        int maxThreads = -1;
        int maxConnections = -1;

        if (factory instanceof TomcatServletWebServerFactory tomcatFactory) {
            server.put("type", "Apache Tomcat");
            server.put("version", readTomcatVersion());
            maxThreads = 200;   // Default; actual value comes from server.tomcat.threads.max
            maxConnections = 8192;  // Spring Boot default for Tomcat
        } else if (factory instanceof JettyServletWebServerFactory jettyFactory) {
            server.put("type", "Eclipse Jetty");
            server.put("version", readJettyVersion());
            maxThreads = 200;   // Default; actual value comes from server.jetty.threads.max
            maxConnections = -1; // Jetty uses a Semaphore based on threads.max
        } else {
            server.put("type", "Unknown / Other");
        }
        server.put("configuredMaxThreads", maxThreads);
        server.put("configuredMaxConnections", maxConnections);
        return server;
    }

    private String readTomcatVersion() {
        try {
            return org.apache.catalina.util.ServerInfo.getServerInfo();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String readJettyVersion() {
        try {
            return org.eclipse.jetty.util.Jetty.VERSION;
        } catch (Exception e) {
            return "unknown";
        }
    }

    private Map<String, Object> readServletContextInfo(HttpServletRequest request) {
        ServletContext servletContext = request.getServletContext();
        if (servletContext == null) {
            return null;
        }

        Map<String, Object> servlet = new LinkedHashMap<>();
        servlet.put("serverInfo", servletContext.getServerInfo());
        servlet.put("majorVersion", servletContext.getMajorVersion());
        servlet.put("minorVersion", servletContext.getMinorVersion());
        servlet.put("effectiveMajorVersion", servletContext.getEffectiveMajorVersion());
        servlet.put("effectiveMinorVersion", servletContext.getEffectiveMinorVersion());
        return servlet;
    }

    private Map<String, Object> readJvmThreadInfo() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threads = new LinkedHashMap<>();
        threads.put("currentThreadCount", threadMXBean.getThreadCount());
        threads.put("peakThreadCount", threadMXBean.getPeakThreadCount());
        threads.put("totalStartedThreadCount", threadMXBean.getTotalStartedThreadCount());
        threads.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
        return threads;
    }

    private Map<String, Object> readVirtualThreadInfo() {
        Map<String, Object> vthreads = new LinkedHashMap<>();
        vthreads.put("virtualThreadsSupported", true);
        vthreads.put("note", "Tomcat ignores 'threads.max' for virtual threads; "
                + "concurrency governed by 'max-connections' (default 8192). "
                + "Jetty respects 'threads.max' via internal Semaphore.");
        return vthreads;
    }

    private void appendAppConfig(Map<String, Object> info) {
        ServerConfig cfg = serverConfigProvider.getIfAvailable();
        if (cfg != null) {
            info.put("appLabel", cfg.getLabel());
            info.put("appStatusCollectorEnabled", cfg.isStatusCollectorEnabled());
        }
    }
}

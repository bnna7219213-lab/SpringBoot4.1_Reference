# Tomcat / Jetty Embedded Server Switching Demo

This project demonstrates how to **switch between Tomcat and Jetty embedded servers** in **Spring Boot 4.1 + JDK 21**.

---

## Project Purpose

Spring Boot provides out-of-the-box embedded servlet containers. By default, **Tomcat** is used. In Spring Boot 4.x, **Undertow is no longer supported** due to Servlet 6.1 compatibility issues. This project shows how to:

- Use **Tomcat** as the default server (via `spring-boot-starter-web`).
- Switch to **Jetty** by excluding Tomcat at the Maven dependency level and adding `spring-boot-starter-jetty`.
- Understand the key configuration and behavioral differences between the two servers.

---

## How to Run

### 1. Run with Tomcat (default)

```bash
cd tomcat-jetty-switch
mvn spring-boot:run
```

Or explicitly:

```bash
mvn spring-boot:run -P tomcat
```

Tomcat starts on **port 8080** by default (configurable in `application-tomcat.yml`).

### 2. Run with Jetty

```bash
mvn spring-boot:run -Pjetty
```

Jetty starts on **port 8081** (configurable in `application-jetty.yml`).

> **Note:** The Maven `jetty` profile automatically:
> 1. Excludes `spring-boot-starter-tomcat` from the `spring-boot-starter-web` dependency.
> 2. Adds `spring-boot-starter-jetty`.

### 3. Package as JAR

```bash
mvn clean package -Pjetty       # Jetty variant
mvn clean package -P tomcat     # Tomcat variant
```

---

## API Endpoints

### `GET /api/server/info`

Returns real-time information about the embedded server, including:

| Field | Description |
|-------|-------------|
| `server.type` | Embedded server type (`Apache Tomcat` or `Eclipse Jetty`) |
| `server.version` | Server version info |
| `configuredMaxThreads` | Configured maximum worker threads |
| `configuredMaxConnections` | Configured maximum connections |
| `servlet.*` | Servlet context information |
| `jvmThreads.*` | JVM thread statistics |
| `virtualThreads.*` | Virtual thread support indicator and notes |

**Example Response (Tomcat):**

```json
{
  "timestamp": "2025-01-15T10:30:00+08:00",
  "server": {
    "type": "Apache Tomcat",
    "version": "11.0.0",
    "configuredMaxThreads": 200,
    "configuredMaxConnections": 8192
  },
  "configuredMaxThreads": 200,
  "configuredMaxConnections": 8192,
  "jvmThreads": {
    "currentThreadCount": 12,
    "peakThreadCount": 15,
    "daemonThreadCount": 8
  },
  "virtualThreads": {
    "virtualThreadsSupported": true,
    "note": "Tomcat ignores 'threads.max' for virtual threads..."
  }
}
```

### `GET /api/server/health`

Simple health check returning:

```json
{
  "status": "UP",
  "timestamp": "2025-01-15T10:30:00+08:00"
}
```

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Health information |
| `/actuator/info` | Application info |
| `/actuator/metrics` | JVM and application metrics |
| `/actuator/threaddump` | Thread dump |

---

## Key Differences: Tomcat vs Jetty in Spring Boot 4.1

### 1. Virtual Thread Behavior

| Aspect | Tomcat | Jetty |
|--------|--------|-------|
| `threads.max` | **Ignored** when virtual threads enabled | **Respected** - controls concurrency via internal Semaphore |
| Concurrency limit | Governed by `max-connections` (default: **8192**) | Governed by `threads.max` (default: **200**) |
| Recommended config | Set `server.tomcat.max-connections` | Set `server.jetty.threads.max` |

### 2. Configuration Namespace

| Feature | Tomcat | Jetty |
|---------|--------|-------|
| Max threads | `server.tomcat.threads.max` | `server.jetty.threads.max` |
| Max connections | `server.tomcat.max-connections` | - |
| Min spare threads | `server.tomcat.threads.min-spare` | `server.jetty.threads.min` |
| Accept queue size | `server.tomcat.accept-count` | - |
| Connection timeout | `server.tomcat.connection-timeout` | `server.jetty.connection-idle-timeout` |
| Keep-alive timeout | `server.tomcat.keep-alive-timeout` | - |

### 3. Undertow Not Supported

> **Spring Boot 4.x no longer supports Undertow** due to Servlet 6.1 (Jakarta EE 11) compatibility.
> Undertow has not caught up with the Servlet 6.1 specification. Use Tomcat or Jetty instead.

### 4. Default Port Conflict Avoidance

Jetty profile configures a different default port (`8081`) to avoid conflicts if both profiles are run simultaneously. Adjust in `application-jetty.yml` if needed.

---

## Project Structure

```
tomcat-jetty-switch/
├── pom.xml                          # Maven build with tomcat/jetty profiles
├── README.md
└── src/main/
    ├── java/com/example/jetty/
    │   ├── TomcatJettySwitchApplication.java   # Main Spring Boot class
    │   ├── controller/
    │   │   └── ServerInfoController.java        # REST endpoints
    │   └── config/
    │       └── ServerConfig.java                # @ConfigurationProperties
    └── resources/
        ├── application.yml                      # Default config (activates tomcat profile)
        ├── application-tomcat.yml               # Tomcat profile config
        └── application-jetty.yml               # Jetty profile config
```

---

## Requirements

- **JDK 21** (for virtual thread support)
- **Maven 3.9+**
- **Spring Boot 4.1.0**

---

## Reference Links

- [Spring Boot Reference: Embedded Web Servers](https://docs.spring.io/spring-boot/reference/web/servlet.html)
- [Spring Boot: Virtual Threads](https://docs.spring.io/spring-boot/reference/web/servlet.html#web.servlet.embedded-container.virtual-threads)
- [Jakarta Servlet 6.1 Specification](https://jakarta.ee/specifications/servlet/6.1/)

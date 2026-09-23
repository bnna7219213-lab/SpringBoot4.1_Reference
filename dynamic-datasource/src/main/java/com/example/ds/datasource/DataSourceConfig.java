package com.example.ds.datasource;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.ds.GroupDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Dynamic DataSource Configuration
 *
 * <p>This class demonstrates programmatic datasource configuration.
 * The actual datasource definitions are in application.yml, but you can
 * also create datasources dynamically at runtime using this approach.
 *
 * <p>Common uses:
 * <ul>
 *   <li>Loading datasource configs from database / config center</li>
 *   <li>Adding new datasources without restarting application</li>
 *   <li>Constructing datasources with custom properties</li>
 * </ul>
 */
@Slf4j
@Component
public class DataSourceConfig {

    private final DefaultDataSourceCreator dataSourceCreator;

    public DataSourceConfig(DefaultDataSourceCreator dataSourceCreator) {
        this.dataSourceCreator = dataSourceCreator;
    }

    /**
     * Initialize datasource at startup if needed.
     * With YAML configuration, datasources are auto-created.
     * Use this for runtime dynamic datasource creation.
     */
    @PostConstruct
    public void init() {
        log.info("Dynamic DataSource Config initialized");
        log.info("Available datasources: master, slave");

        // Example: Creating a datasource programmatically
        // (not active in this demo, shows the pattern)
        if (shouldCreateExtraDataSource()) {
            createRuntimeDataSource("extra", "jdbc:h2:mem:extra");
        }
    }

    /**
     * Demonstrates how to create a datasource at runtime.
     *
     * @param dsName datasource name
     * @param jdbcUrl JDBC URL
     */
    private void createRuntimeDataSource(String dsName, String jdbcUrl) {
        log.info("Creating runtime datasource: {} -> {}", dsName, jdbcUrl);

        DataSourceProperty property = new DataSourceProperty();
        property.setUrl(jdbcUrl);
        property.setDriverClassName("org.h2.Driver");
        property.setUsername("sa");
        property.setPassword("");

        // Create the datasource
        javax.sql.DataSource ds = dataSourceCreator.createDataSource(property);
        log.info("Datasource {} created: {}", dsName, ds != null ? "success" : "failed");
    }

    /**
     * Placeholder for checking if extra datasource is needed.
     */
    private boolean shouldCreateExtraDataSource() {
        // Could check: environment property, database config, etc.
        return false;
    }
}

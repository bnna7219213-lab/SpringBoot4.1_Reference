package com.example.mpgen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * Externalized generator configuration.
 *
 * <p>Binds properties from generator-config.yml to provide
 * a type-safe configuration object for the code generator.
 *
 * <p>Usage: inject GeneratorConfig into your generator class
 * or access via Spring context.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "mp.generator")
public class GeneratorConfig {

    /**
     * Author name for @author comments in generated files.
     */
    private String author = "code-generator";

    /**
     * Output directory for generated Java files.
     */
    private String outputDir = System.getProperty("user.dir") + "/src/main/java";

    /**
     * Output directory for generated XML mapper files.
     */
    private String xmlOutputDir = System.getProperty("user.dir") + "/src/main/resources";

    /**
     * Parent package for all generated code.
     */
    private String parentPackage = "com.example.mpgen.generated";

    /**
     * Database JDBC URL.
     */
    private String jdbcUrl = "jdbc:h2:mem:mpgen;DB_CLOSE_DELAY=-1;MODE=MySQL";

    /**
     * Database driver class name.
     */
    private String jdbcDriver = "org.h2.Driver";

    /**
     * Database username.
     */
    private String jdbcUsername = "sa";

    /**
     * Database password.
     */
    private String jdbcPassword = "";

    /**
     * Tables to include in generation.
     */
    private List<String> includeTables = List.of("t_product");

    /**
     * Table prefixes to remove from entity class names.
     */
    private List<String> tablePrefix = List.of("t_");

    /**
     * Entity strategy settings.
     */
    private EntityStrategy entity = new EntityStrategy();

    /**
     * Mapper strategy settings.
     */
    private MapperStrategy mapper = new MapperStrategy();

    /**
     * Service strategy settings.
     */
    private ServiceStrategy service = new ServiceStrategy();

    /**
     * Controller strategy settings.
     */
    private ControllerStrategy controller = new ControllerStrategy();

    @Data
    public static class EntityStrategy {
        /** Enable Lombok annotations on entity classes. */
        private boolean lombokModel = true;
        /** Enable @TableField annotations. */
        private boolean tableFieldAnnotationEnable = true;
        /** Enable file override (overwrites existing files). */
        private boolean fileOverride = true;
        /** Logic delete column name. */
        private String logicDeleteColumnName = "deleted";
    }

    @Data
    public static class MapperStrategy {
        /** Enable @Mapper annotation. */
        private boolean mapperAnnotation = true;
        /** Generate base result map in XML. */
        private boolean baseResultMap = true;
        /** Generate base column list in XML. */
        private boolean baseColumnList = true;
        /** Enable file override. */
        private boolean fileOverride = true;
        /** Mapper file name pattern (%s = entity name). */
        private String fileName = "%sMapper";
    }

    @Data
    public static class ServiceStrategy {
        /** Service interface name pattern. */
        private String serviceFileName = "I%sService";
        /** Service impl name pattern. */
        private String serviceImplFileName = "%sServiceImpl";
        /** Enable file override. */
        private boolean fileOverride = true;
    }

    @Data
    public static class ControllerStrategy {
        /** Enable REST style (@RestController). */
        private boolean restStyle = true;
        /** Controller name pattern. */
        private String fileName = "%sController";
        /** Enable file override. */
        private boolean fileOverride = true;
    }
}

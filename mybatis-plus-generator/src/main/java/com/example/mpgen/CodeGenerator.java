package com.example.mpgen;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.baomidou.mybatisplus.generator.fill.Column;

import java.util.Collections;

/**
 * MyBatis-Plus Code Generator.
 *
 * <p>Run this main method to automatically generate code from database tables:
 * <ul>
 *   <li>Entity classes</li>
 *   <li>Mapper interfaces</li>
 *   <li>Mapper XML files</li>
 *   <li>Service interfaces</li>
 *   <li>Service implementations</li>
 *   <li>Controller classes</li>
 * </ul>
 *
 * <p>Features demonstrated:
 * <ul>
 *   <li>FastAutoGenerator fluent API</li>
 *   <li>Externalized configuration</li>
 *   <li>Lombok model support</li>
 *   <li>Custom template injection</li>
 *   <li>Custom mapper XML methods</li>
 *   <li>REST style controllers</li>
 * </ul>
 */
public class CodeGenerator {

    // ===================== CONFIGURATION =====================

    /** JDBC URL - uses H2 in-memory for demo */
    private static final String JDBC_URL = "jdbc:h2:mem:mpgen;DB_CLOSE_DELAY=-1;MODE=MySQL";

    /** JDBC driver class */
    private static final String JDBC_DRIVER = "org.h2.Driver";

    /** Database username */
    private static final String JDBC_USERNAME = "sa";

    /** Database password */
    private static final String JDBC_PASSWORD = "";

    /** Author name for @author comments */
    private static final String AUTHOR = "mybatis-plus-generator";

    /** Output directory (project root) */
    private static final String OUTPUT_DIR = System.getProperty("user.dir") + "/src/main/java";

    /** Parent package name */
    private static final String PARENT_PACKAGE = "com.example.mpgen.generated";

    /** Tables to generate code for */
    private static final String[] TABLES = {"t_product"};

    /** Table prefix to remove from entity names */
    private static final String[] TABLE_PREFIX = {"t_"};

    public static void main(String[] args) {
        // =========================================================
        // Method 1: Using FastAutoGenerator (recommended for MP 3.5+)
        // =========================================================
        FastAutoGenerator
            // Step 1: Data source configuration
            .create(new DataSourceConfig.Builder(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD)
                .driver(JDBC_DRIVER)
            )
            // Step 2: Global configuration
            .globalConfig(builder -> builder
                // Output directory
                .outputDir(OUTPUT_DIR)
                // Author for @author tag
                .author(AUTHOR)
                // Disable opening the output directory
                .disableOpenDir()
                // Use java.time package (not java.util.Date)
                .dateType(DateType.TIME_PACK)
                // Comment date format
                .commentDate("yyyy-MM-dd")
            )
            // Step 3: Package configuration
            .packageConfig(builder -> builder
                // Parent package
                .parent(PARENT_PACKAGE)
                // Entity package
                .entity("entity")
                // Mapper package
                .mapper("mapper")
                // Service package
                .service("service")
                // Service impl package
                .serviceImpl("service.impl")
                // Controller package
                .controller("controller")
                // XML mapper output path
                .xml("mapper.xml")
                // XML output location (in resources)
                .pathInfo(Collections.singletonMap(
                    OutputFile.xml,
                    System.getProperty("user.dir") + "/src/main/resources"
                ))
            )
            // Step 4: Strategy configuration
            .strategyConfig(builder -> builder
                // Tables to generate
                .addInclude(TABLES)
                // Table prefix to remove
                .addTablePrefix(TABLE_PREFIX)

                // ---- Entity strategy ----
                .entityBuilder()
                // Use Lombok
                .enableLombok()
                // Enable table field annotation
                .enableTableFieldAnnotation()
                // Use underline to camel case
                .naming(NamingStrategy.underline_to_camel)
                // Column naming
                .columnNaming(NamingStrategy.underline_to_camel)
                // Enable active record
                .enableActiveRecord()
                // Set logic delete field
                .logicDeleteColumnName("deleted")
                // Auto-fill columns
                .addTableFills(new Column("create_time", FieldFill.INSERT))
                .addTableFills(new Column("update_time", FieldFill.INSERT_UPDATE))
                // Enable file override (be careful in production!)
                .enableFileOverride()

                // ---- Mapper strategy ----
                .mapperBuilder()
                // Generate BaseMapper
                .superClass(com.baomidou.mybatisplus.core.mapper.BaseMapper.class)
                // Format mapper file name (%s will be replaced with entity name)
                .formatMapperFileName("%sMapper")
                // Format XML file name
                .formatXmlFileName("%sMapper")
                // Enable @Mapper annotation
                .mapperAnnotation(org.apache.ibatis.annotations.Mapper.class)
                // Enable file override
                .enableFileOverride()
                // Enable base result map in XML
                .enableBaseResultMap()
                // Enable base column list in XML
                .enableBaseColumnList()

                // ---- Service strategy ----
                .serviceBuilder()
                // Format service interface name
                .formatServiceFileName("I%sService")
                // Format service impl name
                .formatServiceImplFileName("%sServiceImpl")
                // Enable file override
                .enableFileOverride()

                // ---- Controller strategy ----
                .controllerBuilder()
                // Use REST style controllers
                .enableRestStyle()
                // Format controller file name
                .formatFileName("%sController")
                // Enable file override
                .enableFileOverride()
            )
            // Step 5: Template engine
            .templateEngine(new FreemarkerTemplateEngine())
            // Step 6: Inject custom configs (advanced)
            // .injectionConfig(builder -> {
            //     builder.customFile(Collections.singletonMap("DTO.java",
                    PARENT_PACKAGE + ".dto"))
            //         .customFile(Collections.singletonMap("VO.java",
            //     PARENT_PACKAGE + ".vo"));
            // })
            // Execute generation
            .execute();

        System.out.println("========================================");
        System.out.println("Code generation completed successfully!");
        System.out.println("Output directory: " + OUTPUT_DIR);
        System.out.println("Tables generated: " + String.join(", ", TABLES));
        System.out.println("========================================");
    }
}

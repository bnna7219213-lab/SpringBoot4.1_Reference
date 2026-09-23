package com.example.mpgen;

import org.springframework.stereotype.Component;

/**
 * Demonstrates how to use the code generator programmatically.
 *
 * <p>This class shows the recommended pattern for integrating
 * the code generator into your build workflow, typically via
 * a Maven profile or a separate module.
 *
 * <p>Usage: Uncomment the @PostConstruct to auto-generate on startup (dev only).
 */
@Component
public class GeneratedCodeDemo {

    /**
     * Existing approach: Run as a standalone main class.
     *
     * <p>Command line usage:
     * <pre>
     *   java -cp target/classes:target/dependency/* com.example.mpgen.CodeGenerator
     * </pre>
     *
     * Or from IDE: Right-click CodeGenerator.java -> Run 'CodeGenerator.main()'
     */

    /**
     * Maven integration example:
     *
     * <p>To integrate with Maven build, add to pom.xml:
     * <pre>
     *   &lt;plugin&gt;
     *     &lt;groupId&gt;org.codehaus.mojo&lt;/groupId&gt;
     *     &lt;artifactId&gt;exec-maven-plugin&lt;/artifactId&gt;
     *     &lt;version&gt;3.1.0&lt;/version&gt;
     *     &lt;executions&gt;
     *       &lt;execution&gt;
     *         &lt;id&gt;generate-code&lt;/id&gt;
     *         &lt;phase&gt;generate-sources&lt;/phase&gt;
     *         &lt;goals&gt;&lt;goal&gt;java&lt;/goal&gt;&lt;/goals&gt;
     *         &lt;configuration&gt;
     *           &lt;mainClass&gt;com.example.mpgen.CodeGenerator&lt;/mainClass&gt;
     *         &lt;/configuration&gt;
     *       &lt;/execution&gt;
     *     &lt;/executions&gt;
     *   &lt;/plugin&gt;
     * </pre>
     */
}

# template-freemarker

FreeMarker template engine demo with Spring Boot 4.1 + Java 21.

## Technology Stack

- Spring Boot 4.1.0
- Java 21
- FreeMarker (Spring Boot starter)
- Spring Web

## Key FreeMarker Features Demonstrated

### 1. FTL Directives
| Directive | Purpose |
|-----------|---------|
| `<#list items as item>` | Iteration over collections |
| `<#if/elseif/else>` | Conditional blocks |
| `<#include "file">` | Include another template |
| `<#macro name>...</#macro>` | Reusable template functions |
| `<#assign var=value>` | Local variable assignment |

### 2. `.ftlh` Extension (HTML Auto-Escaping)
FreeMarker's `.ftlh` format enables HTML auto-escaping — output like `${userInput}` is automatically escaped to prevent XSS.

### 3. Built-Ins
```ftl
${todo.priority?lower_case}     <!-- string built-in -->
${todos?size}                    <!-- collection built-in -->
${todo.title!"fallback"}        <!-- null fallback -->
${now?datetime?string("yyyy-MM-dd")}  <!-- date formatting -->
${todo.completed?then("Done", "Pending")}  <!-- ternary via built-in -->
${expr?has_content}              <!-- check for null/empty -->
```

### 4. Custom Macros (Reusable Components)
```ftl
<#macro priorityBadge priority>
    <#if priority == "URGENT">...</#if>
</#macro>

<@priorityBadge todo.priority />
```

### 5. Null Safety
```ftl
${todo.description!"No description"}   <!-- default if null -->
${todo.title!"N/A"}                     <!-- default value -->
${!expr}                                 <!-- silent null -->
```

## Project Structure

```
src/main/java/com/example/freemarker/
├── TemplateFreeMarkerApplication.java
├── controller/TodoController.java
├── model/Todo.java
└── service/TodoService.java
src/main/resources/
├── application.yml
└── templates/
    ├── todo-list.ftlh
    └── todo-detail.ftlh
```

## Running

```bash
mvn spring-boot:run
```

## URL

- http://localhost:8083/todos — Todo list
- http://localhost:8083/todos/1 — Todo detail

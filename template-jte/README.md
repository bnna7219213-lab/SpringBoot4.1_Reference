# template-jte

JTE (Java Template Engine) demo with Spring Boot 4.1 + Java 21.

## Technology Stack

- Spring Boot 4.1.0
- Java 21
- jte 3.1.15 (spring-boot-starter)
- Spring Web

## Key JTE Features Demonstrated

### 1. Compile-Time Type Safety
JTE templates declare parameters with `@param` at the top. Using a property
that doesn't exist (e.g., `${todo.title}` instead of `${todo.getTitle()}`)
causes a **COMPILE ERROR** — not a runtime error. This catches bugs early.

### 2. Templates Compiled to Bytecode
JTE pre-compiles templates into Java classes. This means:
- Near-native Java execution speed
- No runtime template text processing
- Automatic hot reload in development mode

### 3. Syntax Overview
```jte
@param Type paramName                     // parameter declaration
@import java.util.List                    // explicit imports
@if (condition) ... @else ... @endif     // conditionals
@for (Type item : collection) ... @endfor // iteration
${expression}                             // HTML-escaped output
!{expression}                             // raw output (use with caution)
```

### 4. Content Interface for Dynamic Content
```java
Content dynamic = output -> output.writeContent("<p>Hello</p>");
model.addAttribute("myContent", dynamic);
```
```jte
${myContent}   // Renders the dynamic Content
```

### 5. Template Composition
```jte
@template.page(title, content)            // define a layout
@template.page("My H1", "Body content")   // use it
```

### 6. Hot Code Reload
Set `spring.jte.development-mode: true` for automatic recompilation
when template files change.

## Project Structure

```
src/main/java/com/example/jte/
├── TemplateJteApplication.java
├── controller/TodoController.java    # @Controller with JTE
├── model/Todo.java                   # Immutable domain model
└── service/TodoService.java
src/main/resources/
├── application.yml
└── jte-classes/
    └── content/
        └── Todo.jte                 # Single template (list+detail)
```

## JTE vs Other Engines

| Feature | Thymeleaf | FreeMarker | Mustache | JTE |
|---------|-----------|------------|----------|-----|
| Natural templates | Yes | No | No | No |
| Logic-less | No | No | Yes | No |
| Compile-time type safe | No | No | No | Yes |
| Performance | Good | Good | Good | Best |
| Performance | — | — | — | Compiled to bytecode |

## Running

```bash
mvn spring-boot:run
```

## URL

- http://localhost:8085/todos — Todo list
- http://localhost:8085/todos/1 — Todo detail

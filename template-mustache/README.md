# template-mustache

Mustache (logic-less) template engine demo with Spring Boot 4.1 + Java 21.

## Technology Stack

- Spring Boot 4.1.0
- Java 21
- Mustache (Spring Boot starter)
- Spring Web

## Key Mustache Features Demonstrated

### 1. Logic-Less Philosophy
Mustache enforces a strict separation between logic and presentation. Unlike FreeMarker/Thymeleaf,
Mustache templates CANNOT:
- Execute expressions like `title.toUpperCase()`
- Compare values like `priority == "HIGH"`
- Format dates like `datetime?string("...")`
- Perform any computation

ALL computation must happen in the Java service/controller layer.

### 2. Core Syntax
```mustache
{{variable}}                  — HTML-escaped variable
{{{rawVar}}}                  — unescaped (raw) variable
{{#section}}...{{/section}}  — block: iterate list OR conditional render
{{^inverted}}...{{/inverted}} — inverted: render if falsy/null/empty
{{> partial}}                 — partial inclusion (like a fragment)
{{! comment}}                 — comment (not rendered)
```

### 3. Sections for Iteration and Conditionals
```mustache
{{#todos}}
    <li>{{title}} - {{statusText}}</li>
{{/todos}}

{{^todos}}
    <p>No todos found</p>
{{/todos}}
```
A section renders once for each item in a list, or conditionally for a boolean/null value.

### 4. Partials (Template Composition)
```mustache
{{> header}}
{{> footer}}
```
Partials are like template fragments — defined in separate files and included inline.

### 5. Lambdas (Advanced)
Mustache lambdas allow custom processing of template sections. The Java layer provides a
functional interface implementation that pre-processes data as needed.

## Project Structure

```
src/main/java/com/example/mustache/
├── TemplateMustacheApplication.java
├── controller/TodoController.java
├── model/Todo.java              # All getters compute display values
├── service/TodoService.java
templates/
├── todo-list.mustache           # List view with section iteration
├── todo-detail.mustache         # Detail view
├── header.mustache              # Partial: navigation bar
├── footer.mustache              # Partial: footer
├── breadcrumb.mustache          # Partial: breadcrumb (placeholder)
```

## Key Design Difference

Because Mustache is logic-less, the Java `Todo` model class MUST include all computed
display properties (like `getCssClassForList()`, `getFormattedCreatedAt()`). The controller
sets up all model values. This is actually a GOOD pattern: presentation logic stays in Java.

## Running

```bash
mvn spring-boot:run
```

## URL

- http://localhost:8084/todos — Todo list
- http://localhost:8084/todos/1 — Todo detail

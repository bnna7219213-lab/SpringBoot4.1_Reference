# template-thymeleaf

Thymeleaf template engine demo with Spring Boot 4.1 + Java 21.

## Technology Stack

- Spring Boot 4.1.0
- Java 21
- Thymeleaf (Spring Boot starter)
- Spring Web

## Key Thymeleaf Features Demonstrated

### 1. Natural Templates
HTML files contain no Thymeleaf-specific tags when attributes are removed — they can be previewed directly in a browser.

### 2. Iteration & Conditional CSS
```html
<li th:each="todo, iterStat : ${todos}"
    th:class="'todo-item ' + ${todo.completed ? 'completed' : ''}">
```

### 3. Fragment Inclusion
```html
<footer th:replace="~{fragments :: footer}">
<header th:replace="~{fragments :: header(${pageTitle})}">
```

### 4. Internationalization
Messages loaded from `messages/messages*.properties`.
- `messages.properties` — English (default)
- `messages_zh_CN.properties` — Simplified Chinese

### 5. Utility Objects
- `#temporals` — date formatting
- `#strings` — string operations (toLowerCase, etc.)
- `#lists` — list utilities (isEmpty)
- `#dates` — current date

### 6. Link & Text Expressions
- `@{...}` — URL (context-path aware)
- `${...}` — variable/field access
- `#{...}` — message source lookup
- `*{...}` — selection expression

### 7. Inline Text
```html
<th th:text="#{detail.title} + ' - ' + ${todo.title}">
```

## Project Structure

```
src/main/java/com/example/thyme/
├── TemplateThymeleafApplication.java
├── controller/TodoController.java    # @Controller with Model
├── model/Todo.java                   # Domain model
└── service/TodoService.java          # In-memory store
src/main/resources/
├── application.yml
├── messages/
│   ├── messages.properties
│   └── messages_zh_CN.properties
└── templates/
    ├── todo-list.html               # List view with iteration
    ├── todo-detail.html             # Object binding + conditional
    └── fragments.html               # Reusable fragments
```

## Running

```bash
mvn spring-boot:run
```

## URL

- http://localhost:8082/todos — Todo list
- http://localhost:8082/todos/1 — Todo detail

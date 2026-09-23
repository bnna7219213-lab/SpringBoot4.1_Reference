package com.example.jte.controller;

import com.example.jte.model.Todo;
import com.example.jte.service.TodoService;
import gg.jte.TemplateOutput;
import gg.jte.Content;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * JTE controller — uses Model to pass strongly-typed data to JTE templates.
 *
 * KEY JTE DIFFERENCE: JTE templates are compiled to Java classes with
 * type-safe `render(templateOutput, param1, param2, ...)` methods.
 * Missing or mistyped parameters cause COMPILE ERRORS.
 */
@Controller
@RequestMapping("/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("todos", todoService.findAll());
        model.addAttribute("pendingCount", todoService.countPending());
        model.addAttribute("completedCount", todoService.countCompleted());
        model.addAttribute("totalCount", todoService.findAll().size());
        model.addAttribute("pageTitle", "Todo List");
        model.addAttribute("engineName", "JTE");
        model.addAttribute("currentTime", java.time.LocalDateTime.now());
        model.addAttribute("todoListContent", (Content) output -> {
            output.writeContent("<p>Generated dynamically via JTE Content interface!</p>");
        });
        return "Todo";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Todo todo = todoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found: " + id));
        model.addAttribute("todo", todo);
        model.addAttribute("pageTitle", "Todo #" + todo.getId());
        return "Todo";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        todoService.toggleCompleted(id);
        return "redirect:/todos";
    }
}

package com.example.mustache.controller;

import com.example.mustache.model.Todo;
import com.example.mustache.service.TodoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Mustache controller — demonstrates how to pre-compute ALL data the template needs.
 * Unlike FreeMarker/Thymeleaf, Mustache templates CANNOT execute expressions or call methods
 * beyond simple property access. Therefore, all formatting and computation MUST happen in Java.
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
        // All computed values are here because the template can't compute anything.
        model.addAttribute("todos", todoService.findAll());

        // Pre-computed derived values — Mustache can only display what it's given
        model.addAttribute("pendingCount", todoService.countPending());
        model.addAttribute("completedCount", todoService.countCompleted());
        model.addAttribute("totalCount", todoService.findAll().size());
        model.addAttribute("hasTodos", !todoService.findAll().isEmpty());
        model.addAttribute("currentTime", java.time.LocalDateTime.now().toString());
        model.addAttribute("appTitle", "Todo List");
        model.addAttribute("engineName", "Mustache");
        return "todo-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Todo todo = todoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found: " + id));
        model.addAttribute("todo", todo);
        // Pre-compute detail display values
        model.addAttribute("pageTitle", "Todo #" + todo.getId() + " - Detail");
        return "todo-detail";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        todoService.toggleCompleted(id);
        return "redirect:/todos";
    }
}

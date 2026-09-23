package com.example.freemarker.controller;

import com.example.freemarker.model.Todo;
import com.example.freemarker.service.TodoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        model.addAttribute("completedCount", todoService.countByStatus(true));
        model.addAttribute("pendingCount", todoService.countByStatus(false));
        model.addAttribute("now", java.time.LocalDateTime.now());
        return "todo-list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Todo todo = todoService.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found: " + id));
        model.addAttribute("todo", todo);
        return "todo-detail";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        todoService.toggleCompleted(id);
        return "redirect:/todos";
    }
}

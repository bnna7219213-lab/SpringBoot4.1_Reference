@import com.example.jte.model.Todo
@import java.util.List
@import gg.jte.Content
@import java.time.LocalDateTime
@import java.time.format.DateTimeFormatter

@param String pageTitle
@param String engineName
@param List<Todo> todos
@param long pendingCount
@param long completedCount
@param long totalCount
@param LocalDateTime currentTime
@param Todo singleTodo
@param Content todoListContent

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${pageTitle} - JTE</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 900px; margin: 0 auto; padding: 20px; background: #f5f7fa; }
        .header { background: linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%); color: white; padding: 24px 32px; border-radius: 12px; margin-bottom: 24px; }
        .header h1 { margin: 0; font-size: 28px; }
        .stats { display: flex; gap: 16px; margin-top: 12px; }
        .stat-card { background: rgba(255,255,255,0.2); padding: 8px 16px; border-radius: 8px; font-size: 14px; }
        .todo-list { list-style: none; padding: 0; margin: 0; }
        .todo-item { background: white; border-radius: 10px; padding: 18px 24px; margin-bottom: 12px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); display: flex; align-items: center; justify-content: space-between; }
        .todo-item.completed { opacity: 0.6; background: #f0fdf4; }
        .todo-item.completed .todo-title { text-decoration: line-through; }
        .urgent { border-left: 4px solid #ef4444; }
        .high { border-left: 4px solid #f59e0b; }
        .medium { border-left: 4px solid #3b82f6; }
        .low { border-left: 4px solid #10b981; }
        .todo-title { font-size: 17px; font-weight: 600; color: #1e293b; }
        .todo-desc { color: #64748b; font-size: 14px; margin-top: 4px; }
        .badge { padding: 3px 10px; border-radius: 12px; font-size: 11px; font-weight: 600; }
        .actions { display: flex; gap: 8px; }
        .btn { padding: 8px 16px; border: none; border-radius: 6px; cursor: pointer; font-size: 13px; font-weight: 500; text-decoration: none; display: inline-block; }
        .btn-primary { background: #a18cd1; color: white; }
        .btn-toggle { background: #e2e8f0; color: #475569; }
        .footer { text-align: center; padding: 24px; color: #94a3b8; font-size: 13px; }
        .notfound { text-align: center; color: #94a3b8; padding: 60px; }
        .badge-pending { background: #fefce8; color: #ca8a04; }
        .badge-completed { background: #f0fdf4; color: #16a34a; }
        .badge-priority-URGENT { background: #fef2f2; color: #ef4444; }
        .badge-priority-HIGH { background: #fffbeb; color: #f59e0b; }
        .badge-priority-MEDIUM { background: #eff6ff; color: #3b82f6; }
        .badge-priority-LOW { background: #ecfdf5; color: #10b981; }
    </style>
</head>
<body>

<!--
JTE KEY FEATURES DEMONSTRATED:
  - @param declarations: compile-time type-safe template parameters
  - @import: explicit imports (like Java)
  - ${var}: output expression (HTML-escaped by default)
  - !{var}: unescaped/raw output
  - @if/@else: conditionals (type-safe)
  - @for/@if: iteration with template-level conditions
  - Template composition via @template calls

  TYPE SAFETY: Using ${todo.title} where `title` doesn't exist causes COMPILE ERROR.
  PERFORMANCE: JTE templates compile to Java bytecode — the fastest template engine.
-->

<div class="header">
    <h1>${pageTitle}</h1>
    <p style="margin: 4px 0 0; opacity: 0.85;">${engineName} Template Engine - Compiled Bytecode Demo</p>
    <div class="stats">
        <div class="stat-card"><strong>${pendingCount}</strong> Pending</div>
        <div class="stat-card"><strong>${completedCount}</strong> Completed</div>
        <div class="stat-card"><strong>${totalCount}</strong> Total Items</div>
    </div>
</div>

@if (totalCount > 0)
    <ul class="todo-list">
        @for (Todo todo : todos)
            <li class="@(todo.isCompleted() ? "todo-item completed " : "todo-item ") + todo.getPriorityCssClass()">
                <div style="flex: 1;">
                    <div class="todo-title">${todo.getTitle()}</div>
                    <div class="todo-desc">@todo.getPageDescription()</div>
                    <div style="margin-top: 6px;">
                        <span class="badge badge-priority-${todo.getPriority()}">${todo.getPriority()}</span>
                        @if (todo.isCompleted())
                            <span class="badge badge-completed">&#10003; Done</span>
                        @else
                            <span class="badge badge-pending">&#9711; Pending</span>
                        @endif
                    </div>
                </div>
                <div class="actions">
                    <form action="/todos/@todo.getId()/toggle" method="post" style="display:inline;">
                        <button type="submit" class="btn btn-toggle">@todo.getToggleAction()</button>
                    </form>
                    <a href="/todos/@todo.getId()" class="btn btn-primary">View</a>
                </div>
            </li>
        @endfor
    </ul>
    <!-- Demonstrate Content interface — dynamic content injection -->
    <div style="background: white; border-radius: 10px; padding: 20px; margin-top: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);">
        ${todoListContent}
    </div>
@else
    <div class="notfound">
        <h3>No todos yet!</h3>
        <p>Add your first todo to get started.</p>
    </div>
@endif

<div class="footer">
    <p>Spring Boot 4.1 + JTE Demo</p>
    <p>Page rendered at: @currentTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))</p>
</div>

</body>
</html>

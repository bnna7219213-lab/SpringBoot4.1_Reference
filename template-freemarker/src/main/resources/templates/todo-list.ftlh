<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Todo List - FreeMarker</title>
    <link rel="stylesheet" href="/css/todo.css">
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 900px; margin: 0 auto; padding: 20px; background: #f5f7fa; }
        .header { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 24px 32px; border-radius: 12px; margin-bottom: 24px; }
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
        .btn-primary { background: #f093fb; color: white; }
        .btn-toggle { background: #e2e8f0; color: #475569; }
        .footer { text-align: center; padding: 24px; color: #94a3b8; font-size: 13px; }
    </style>
</head>
<body>

<!-- FREEMARKER DIRECTIVES REFERENCE:
     <#list>     — iteration
     <#if>       — conditionals
     <#include>  — include another template
     <#macro>    — reusable template functions
     <#assign>   — variable assignment
     ${expr}     — output expression (auto-escaped in .ftlh)
     ftl built-ins: ?string, ?date, ?number, ?has_content, etc.
-->

<#-- Macro for priority badge — reusable component -->
<#macro priorityBadge priority>
    <#if priority == "URGENT">
        <span class="badge" style="background:#fef2f2;color:#ef4444;">${priority}</span>
    <#elseif priority == "HIGH">
        <span class="badge" style="background:#fffbeb;color:#f59e0b;">${priority}</span>
    <#elseif priority == "MEDIUM">
        <span class="badge" style="background:#eff6ff;color:#3b82f6;">${priority}</span>
    <#else>
        <span class="badge" style="background:#ecfdf5;color:#10b981;">${priority}</span>
    </#if>
</#macro>

<div class="header">
    <h1>Todo List</h1>
    <p style="margin: 4px 0 0; opacity: 0.85;">FreeMarker Template Engine Demo</p>
    <div class="stats">
        <div class="stat-card"><strong>${pendingCount}</strong> Pending</div>
        <div class="stat-card"><strong>${completedCount}</strong> Completed</div>
        <#-- Built-in for list size -->
        <div class="stat-card"><strong>${todos?size}</strong> Total Items</div>
    </div>
</div>

<#-- <#list> with item iteration variable -->
<#if todos?has_content>
    <ul class="todo-list">
        <#list todos as todo>
            <#-- Assign local variable -->
            <#assign cssClass = "todo-item " + (todo.completed?then("completed", "")) + " " + todo.priority?lower_case>

            <li class="${cssClass}">
                <div style="flex: 1;">
                    <div class="todo-title">
                        ${todo.title}
                        <#-- String built-in: capitalize, lower_case, upper_case -->
                        <#-- Number formatting: ${todo.id?string["0"]} -->
                    </div>
                    <#-- null fallback operator: ${todo.description!"No description"} -->
                    <div class="todo-desc">${todo.description!"No description"}</div>
                    <div style="margin-top: 6px;">
                        <@priorityBadge todo.priority />
                        <span class="badge" style="background:${todo.completed?then('#f0fdf4', '#fefce8')};
                                              color:${todo.completed?then('#16a34a', '#ca8a04')};">
                            ${todo.completed?then("Done", "Pending")}
                        </span>
                    </div>
                </div>
                <div class="actions">
                    <form action="/todos/${todo.id}/toggle" method="post" style="display:inline;">
                        <button type="submit" class="btn btn-toggle">
                            ${todo.completed?then("Undo", "Complete")}
                        </button>
                    </form>
                    <a href="/todos/${todo.id}" class="btn btn-primary">View</a>
                </div>
            </li>
        </#list>
    </ul>
<#else>
    <div style="text-align:center;color:#94a3b8;padding:60px;">
        <h3>No todos yet!</h3>
    </div>
</#if>

<#-- Date/time formatting with built-ins and FTL string interpolation -->
<div class="footer">
    <p>Spring Boot 4.1 + FreeMarker Demo</p>
    <p>Page rendered at: ${now?datetime}</p>
</div>

</body>
</html>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Todo Detail - FreeMarker</title>
    <style>
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; background: #f5f7fa; }
        .breadcrumb { color: #64748b; font-size: 14px; margin-bottom: 16px; }
        .breadcrumb a { color: #f5576c; text-decoration: none; }
        .card { background: white; border-radius: 12px; padding: 32px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
        .divider { height: 1px; background: #e2e8f0; margin: 24px 0; }
        .label { font-size: 12px; color: #64748b; text-transform: uppercase; font-weight: 600; }
        .value { font-size: 16px; color: #1e293b; }
        .grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
        .status-badge { display: inline-block; padding: 6px 14px; border-radius: 20px; font-size: 13px; font-weight: 600; }
        .back-link { display: inline-block; margin-top: 24px; color: #f5576c; text-decoration: none; font-weight: 500; }
    </style>
</head>
<body>

<div class="breadcrumb">
    <a href="/todos">&#8592; Back to List</a> / <span>#${todo.id}</span>
</div>

<div class="card">
    <#-- Auto-escaping demo: ${todo.title} is HTML-escaped automatically in .ftlh files -->
    <h1 style="margin: 0 0 8px; color: #1e293b;">${todo.title}</h1>

    <div style="margin-bottom: 20px;">
        <#-- Ternary operator in FreeMarker -->
        <#if todo.completed>
            <span class="status-badge" style="background:#f0fdf4;color:#16a34a;">&#10003; Completed</span>
        <#else>
            <span class="status-badge" style="background:#fefce8;color:#ca8a04;">&#9711; Pending</span>
        </#if>
    </div>

    <div class="label">Description</div>
    <p style="margin: 0 0 24px; color: #475569; line-height: 1.6;">${todo.description!"(no description)"}</p>

    <div class="divider"></div>

    <#-- Macro call for a field row — shows macro as template inheritance pattern -->
    <div class="grid">
        <div>
            <div class="label">Priority</div>
            <div class="value">${todo.priority!"N/A"}</div>
        </div>
        <div>
            <div class="label">Status</div>
            <div class="value">${todo.getStatusText()}</div>
        </div>
        <div>
            <div class="label">Created</div>
            <#-- Datetime formatting with ?datetime built-in + string pattern -->
            <div class="value">${todo.createdAt?datetime?string("yyyy-MM-dd HH:mm:ss")!"N/A"}</div>
        </div>
        <div>
            <div class="label">Updated</div>
            <div class="value">${todo.updatedAt?datetime?string("yyyy-MM-dd HH:mm:ss")!"N/A"}</div>
        </div>
    </div>

    <div class="divider"></div>

    <#-- Long-form date display using string built-in patterns -->
    <div class="label">Full Timestamp</div>
    <div style="color:#64748b; font-size:14px; margin-bottom: 16px;">
        Created: ${todo.createdAt?datetime?string("EEEE, MMMM dd, yyyy 'at' HH:mm:ss")}
    </div>

    <a href="/todos" class="back-link">&#8592; Back to List</a>
</div>

</body>
</html>

<%-- ============================================================================
     SEMANA 10 - error.jsp: página de error de MIMARI.
     isErrorPage="true" habilita el objeto implícito EXCEPTION en esta página.
     No arregla el problema: lo muestra de forma controlada y entendible.

     Llega aquí por dos caminos:
       1) el servlet /consulta hace forward con los atributos "mensajeError" y "tipoError"
       2) una excepción no controlada (web.xml <error-page>) -> aquí sí existe "exception"
     ============================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<%
    // El mensaje puede contener texto escrito por el usuario: se escapa antes de imprimirlo.
    String mensaje;
    String tipo = null;
    if (exception != null) {
        mensaje = exception.getMessage() != null ? exception.getMessage() : exception.getClass().getSimpleName();
        tipo = exception.getClass().getSimpleName();
    } else if (request.getAttribute("mensajeError") != null) {
        mensaje = String.valueOf(request.getAttribute("mensajeError"));
        tipo = (String) request.getAttribute("tipoError");
    } else {
        mensaje = "Error no identificado";
    }
    mensaje = mensaje.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - Ocurrió un error</title>
    <style>
        body { font-family: 'Plus Jakarta Sans', 'Segoe UI', sans-serif; background: #f8fafc; color: #334155; padding: 30px; display: flex; justify-content: center; margin: 0; }
        .card { width: 100%; max-width: 700px; background: white; padding: 30px; border-radius: 16px; border: 1px solid #e2e8f0; box-shadow: 0 4px 20px rgba(0,0,0,0.03); }
        h1 { color: #991b1b; font-size: 24px; margin: 0 0 16px 0; }
        .mensaje { background: #fef2f2; border-left: 6px solid #ef4444; color: #991b1b; padding: 14px 16px; border-radius: 8px; font-weight: 500; }
        .nota { font-size: 13px; color: #64748b; margin-top: 14px; }
        a { color: #0284c7; font-weight: 600; margin-right: 16px; }
        code { background: #f1f5f9; padding: 2px 6px; border-radius: 4px; }
    </style>
</head>
<body>
<div class="card">
    <h1>Ocurrió un error</h1>

    <div class="mensaje"><%= mensaje %></div>

    <% if (tipo != null) { %>
    <p class="nota">Tipo de error: <code><%= tipo %></code></p>
    <% } %>

    <p style="margin-top:24px;">
        <a href="consulta.jsp">&larr; Volver a la consulta</a>
        <a href="index.html">Ir al simulador</a>
    </p>
</div>
</body>
</html>

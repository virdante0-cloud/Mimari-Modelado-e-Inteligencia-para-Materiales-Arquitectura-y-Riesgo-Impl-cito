<%-- ============================================================================
     SEMANA 10 - JSP: directiva page + objetos implícitos, aplicados a MIMARI.

     SIN MVC: esta página es solo la VISTA (formulario + datos ya guardados).
     Todo el proceso (leer parámetros, validar, calcular, guardar en application)
     lo hace el servlet  PredimensionamientoServlet  en la URL  /consulta.

       formulario (consulta.jsp)  --GET-->  servlet /consulta  -->  HTML de resultado
                                                    |-- si hay error --> forward a error.jsp

     Adaptación de saludo.jsp -> consulta:   nombre -> categoria (viga | placa | general)
                                             (nuevo) -> luz (metros)
     Pruebas:  consulta?categoria=viga&luz=6.5 | consulta | consulta?categoria= | consulta?luz=abc (error)
     ============================================================================ --%>

<%-- DIRECTIVA PAGE: "ficha técnica" de la página
       contentType -> la salida es HTML en UTF-8 (tildes y eñes bien)
       errorPage   -> si esta JSP lanza una excepción, el control pasa a error.jsp --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="error.jsp" %>
<%
    // RESPONSE: controla la respuesta HTTP. Este encabezado viaja en la cabecera, no se ve en pantalla
    // (se puede revisar en las herramientas de desarrollador -> Red).
    response.setHeader("X-Curso", "Programacion III");

    // APPLICATION: memoria compartida por toda la aplicación. Aquí solo se LEE;
    // quien la escribe es el servlet /consulta.
    Object ultimaConsulta = application.getAttribute("ultimaConsulta");
    Object totalConsultas = application.getAttribute("totalConsultas");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - Consulta de predimensionamiento (JSP)</title>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f8fafc; color: #334155; padding: 30px; display: flex; justify-content: center; margin: 0; }
        .wrapper { width: 100%; max-width: 950px; }
        .card { background: white; padding: 30px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); border: 1px solid #e2e8f0; margin-bottom: 25px; }
        h1 { color: #0f172a; font-size: 24px; margin: 0 0 10px 0; }
        h3 { color: #0f172a; margin: 0 0 15px 0; font-size: 18px; }
        .nota { font-size: 13px; color: #64748b; }
        .row { display: flex; gap: 12px; flex-wrap: wrap; align-items: end; }
        .row label { display: flex; flex-direction: column; font-size: 13px; font-weight: 600; color: #475569; gap: 6px; }
        .row input, .row select { padding: 10px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; }
        .row button { background: #0284c7; color: white; border: none; padding: 11px 20px; font-weight: 600; border-radius: 8px; cursor: pointer; }
        ul.pruebas { line-height: 2; padding-left: 20px; margin: 0; }
        a { color: #0284c7; }
        code { background: #f1f5f9; padding: 2px 6px; border-radius: 4px; font-size: 13px; }
    </style>
</head>
<body>
<div class="wrapper">

    <jsp:include page="header.jsp" />

    <div class="card">
        <h1>Consulta de predimensionamiento</h1>
        <p class="nota" style="margin-bottom:18px;">
            Este formulario envía <code>categoria</code> y <code>luz</code> al servlet <code>/consulta</code>,
            que valida, calcula y responde. Si algo falla, el servlet entrega el control a <code>error.jsp</code>.
        </p>

        <form action="consulta" method="GET" class="row">
            <label>Categoría
                <select name="categoria">
                    <option value="">(vacío = general)</option>
                    <option value="viga">viga</option>
                    <option value="placa">placa</option>
                    <option value="general">general</option>
                </select>
            </label>
            <label>Luz (m)
                <input type="text" name="luz" placeholder="Ej. 6.5 (vacío = 4.0)">
            </label>
            <button type="submit">Consultar</button>
        </form>
    </div>

    <div class="card">
        <h3>Último estado de la aplicación</h3>
        <%-- OUT: escribe en el HTML resultante. Los valores salen de application (los guardó el servlet). --%>
        <%
            if (ultimaConsulta == null) {
                out.println("<p class=\"nota\">Todavía no se ha hecho ninguna consulta desde que arrancó el servidor.</p>");
            } else {
        %>
        <%-- EXPRESIONES <%= %>: imprimen directamente un valor. El texto se escapa por seguridad. --%>
        <p class="nota">
            Última consulta (de cualquier visitante):
            <b><%= String.valueOf(ultimaConsulta).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;") %></b><br>
            Total de consultas desde que arrancó el servidor: <b><%= totalConsultas %></b>
        </p>
        <% } %>
    </div>

    <div class="card">
        <h3>Pruebas de la práctica (Semana 10)</h3>
        <ul class="pruebas">
            <li>Parámetros válidos: <a href="consulta?categoria=viga&amp;luz=6.5">consulta?categoria=viga&amp;luz=6.5</a></li>
            <li>Sin parámetros: <a href="consulta">consulta</a> &rarr; categoría <code>general</code>, luz 4.0</li>
            <li>Parámetro vacío: <a href="consulta?categoria=&amp;luz=">consulta?categoria=&amp;luz=</a> &rarr; también <code>general</code></li>
            <li>Error controlado: <a href="consulta?categoria=placa&amp;luz=abc">consulta?categoria=placa&amp;luz=abc</a> &rarr; <code>error.jsp</code></li>
        </ul>
    </div>

</div>
</body>
</html>

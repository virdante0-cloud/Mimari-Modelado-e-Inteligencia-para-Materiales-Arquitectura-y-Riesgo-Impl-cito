<%-- ============================================================================
     SEMANA 10 - error.jsp (VISTA): página de error de MIMARI.
     isErrorPage="true" habilita el objeto implícito EXCEPTION en esta página.
     No arregla el problema: lo muestra de forma controlada y entendible.

     Llega aquí SIEMPRE a través de ErrorServlet (/error.jsp), que ya preparó y escapó el mensaje:
       1) otro servlet detectó un dato no válido (atributos "mensajeError" y "tipoError")
       2) una JSP con errorPage="/error.jsp" o el contenedor (web.xml <error-page>) trajo una excepción
          -> aquí sí existe "exception"
     Esta página no valida ni procesa nada: solo imprime lo que recibe.
     ============================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - Ocurrió un error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/paginas.css">
</head>
<body>
<div class="wrapper">
    <div class="card">
        <h1 class="error">Ocurrió un error</h1>

        <div class="mensaje">${mensajeSeguro}</div>

        <p class="nota nota-bloque">
            Tipo de error: <code>${tipoSeguro}</code>
            &nbsp;·&nbsp; Excepción capturada (<code>exception</code>):
            <b><%= exception != null ? exception.getClass().getSimpleName() : "ninguna" %></b>
        </p>

        <p class="enlaces separado">
            <a href="${pageContext.request.contextPath}/index.jsp">&larr; Ir al inicio</a>
            <a href="${pageContext.request.contextPath}/saludo.jsp">Saludo</a>
            <a href="${pageContext.request.contextPath}/consulta.jsp">Consulta</a>
        </p>
    </div>
</div>
</body>
</html>

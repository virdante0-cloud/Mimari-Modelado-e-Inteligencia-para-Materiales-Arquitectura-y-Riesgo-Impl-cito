<%-- ============================================================================
     SEMANA 11 - productos.jsp (VISTA, con JSTL de la Semana 14). Solo muestra; NO consulta la base ni valida.
     ProductosServlet (/productos.jsp) revisó la sesión (si no hay usuario, redirigió a login.jsp),
     consultó la tabla productos con JDBC y entregó los datos ya escapados.
     ============================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - Insumos (JSP + JDBC)</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/paginas.css">
</head>
<body>
<div class="wrapper">

    <jsp:include page="/WEB-INF/vistas/header.jsp" />

    <div class="card">
        <h1>Insumos de construcción</h1>
        <div class="dato">Usuario: <b><c:out value="${usuario}" /></b></div>

        <c:choose>
            <c:when test="${empty productos}">
                <p class="nota nota-bloque">La tabla <code>productos</code> no tiene registros. Ejecuta <code>db/schema.sql</code>.</p>
            </c:when>
            <c:otherwise>
                <table class="tabla">
                    <thead><tr><th>ID</th><th>Nombre</th><th>Precio</th></tr></thead>
                    <tbody>
                    <%-- cada fila es un arreglo: [0] id, [1] nombre, [2] precio ya formateado por el servlet --%>
                    <c:forEach var="p" items="${productos}">
                        <tr><td>${p[0]}</td><td><c:out value="${p[1]}" /></td><td>${p[2]}</td></tr>
                    </c:forEach>
                    </tbody>
                </table>
                <p class="nota nota-bloque">${total} registro(s) leídos desde MySQL con JDBC.</p>
            </c:otherwise>
        </c:choose>

        <form action="${pageContext.request.contextPath}/logout" method="POST">
            <button type="submit" class="btn-back" style="border:none;cursor:pointer;">Cerrar sesión</button>
        </form>
    </div>

</div>
</body>
</html>

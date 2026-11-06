<%-- Resultado del predimensionamiento (VISTA). Solo imprime lo que AnalisisEstructuralServlet calculó y validó. --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Resultados del Análisis - MIMARI</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/paginas.css">
</head>
<body>
<div class="wrapper">

    <jsp:include page="/WEB-INF/vistas/header.jsp" />

    <div class="card">
        <h1>Resultados de Predimensionamiento Estructural</h1>

        <%-- alertas: lista de arreglos [tipo, mensaje] preparada por el servlet --%>
        <c:forEach var="alerta" items="${alertas}">
            <div class="${alerta[0] == 'peligro' ? 'alert-danger' : 'alert-success'}"><c:out value="${alerta[1]}" /></div>
        </c:forEach>

        <div class="file-info"><c:out value="${mensajeArchivo}" /></div>

        <div class="results-grid">
            <div class="dato-card"><h3>Altura Total del Edificio</h3><p>${alturaTotal}</p></div>
            <div class="dato-card"><h3>Área Construida Total</h3><p>${areaConstruida}</p></div>
            <div class="dato-card"><h3>Peralte Sugerido para Viga</h3><p>${peralteViga}</p></div>
            <div class="dato-card"><h3>Espesor Mínimo de Placa</h3><p>${espesorPlaca}</p></div>
        </div>

        <a href="${pageContext.request.contextPath}/analisis-estructural" class="btn-back">&larr; Realizar Nuevo Cálculo</a>
    </div>

</div>
</body>
</html>

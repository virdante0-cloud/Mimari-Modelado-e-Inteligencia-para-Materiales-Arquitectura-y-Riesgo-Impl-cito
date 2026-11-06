<%-- ============================================================================
     SEMANA 14 - xml-demo.jsp (VISTA con JSTL XML). XmlDemoServlet entrega el documento en el atributo "xml";
     x:parse lo convierte en un árbol y x:out / x:forEach extraen datos con XPath.
     ============================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="x" uri="jakarta.tags.xml" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - JSTL XML</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/paginas.css">
</head>
<body>
<div class="wrapper">

    <jsp:include page="/WEB-INF/vistas/header.jsp" />

    <div class="card">
        <h1>Demostración JSTL XML</h1>
        <x:parse var="doc" doc="${xml}" />

        <div class="dato">Primer material (XPath <code>$doc/materiales/material[1]/nombre</code>):
            <strong><x:out select="$doc/materiales/material[1]/nombre" /></strong></div>

        <h3 class="separado">Todos los materiales del documento</h3>
        <table class="tabla">
            <thead><tr><th>Material</th><th>Uso</th></tr></thead>
            <tbody>
            <x:forEach select="$doc/materiales/material">
                <tr><td><x:out select="nombre" /></td><td><x:out select="uso" /></td></tr>
            </x:forEach>
            </tbody>
        </table>

        <h3 class="separado">Documento XML original</h3>
        <pre><c:out value="${xml}" /></pre>
    </div>

</div>
</body>
</html>

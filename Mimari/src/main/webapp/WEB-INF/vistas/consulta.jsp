<%-- ============================================================================
     SEMANA 10 - consulta.jsp (VISTA). Solo muestra formulario y resultados; NO valida ni calcula.
     Todo el proceso (leer parámetros, validar, calcular, guardar en application) lo hace
     ConsultaServlet en /consulta.jsp  y luego hace forward a esta página.

       formulario  --GET-->  ConsultaServlet  -->  esta vista (con los datos listos)
                                  |-- si hay error --> /error.jsp

     Adaptación de saludo -> consulta:  nombre -> categoria (viga | placa | general),  (nuevo) luz en metros
     ============================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - Consulta de predimensionamiento (JSP)</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/paginas.css">
</head>
<body>
<div class="wrapper">

    <jsp:include page="/WEB-INF/vistas/header.jsp" />

    <div class="card">
        <h1>Consulta de predimensionamiento</h1>
        <p class="nota nota-bloque">
            Este formulario envía <code>categoria</code> y <code>luz</code> al servlet, que valida, calcula y responde.
            Si algo falla, el servlet entrega el control a <code>error.jsp</code>.
        </p>

        <form action="${pageContext.request.contextPath}/consulta.jsp" method="GET" class="row">
            <label>Categoría
                <select name="categoria">
                    <option value="">(vacío = general)</option>
                    <option value="viga">viga</option>
                    <option value="placa">placa</option>
                    <option value="general">general</option>
                </select>
            </label>
            <label>Luz (m)
                <input type="text" name="luz" inputmode="decimal" placeholder="Ej. 6.5 (vacío = 4.0)">
            </label>
            <button type="submit">Consultar</button>
        </form>
    </div>

    <div class="card">
        <h3>Resultado de la consulta</h3>

        <div class="dato">Categoría consultada: <b>${categoria}</b></div>
        <div class="dato">Luz de cálculo: <b>${luz} m</b>
            <c:if test="${luzPorDefecto}">
                <span class="nota">(valor por defecto: no llegó el parámetro luz)</span>
            </c:if>
        </div>

        <c:if test="${not empty vigaPeralte}">
            <div class="dato">Viga principal: peralte <b>${vigaPeralte} cm</b> x base <b>${vigaBase} cm</b>
                <span class="nota">(h = L / 10, b = h / 2)</span></div>
        </c:if>
        <c:if test="${not empty placaEspesor}">
            <div class="dato">Placa de entrepiso: espesor <b>${placaEspesor} cm</b>
                <span class="nota">(h = L / 25)</span></div>
        </c:if>

        <%-- applicationScope = el objeto application: memoria compartida; el servlet la escribe, aquí solo se lee --%>
        <p class="nota nota-bloque">
            Consulta anterior guardada en <code>application</code> (de cualquier visitante): <b>${consultaAnterior}</b><br>
            Total de consultas desde que arrancó el servidor: <b>${applicationScope.totalConsultas}</b>
        </p>
    </div>

    <div class="card">
        <h3>Pruebas de la práctica (Semana 10)</h3>
        <ul class="pruebas">
            <li>Parámetros válidos: <a href="consulta.jsp?categoria=viga&amp;luz=6.5">consulta.jsp?categoria=viga&amp;luz=6.5</a></li>
            <li>Sin parámetros: <a href="consulta.jsp">consulta.jsp</a> &rarr; categoría <code>general</code>, luz 4.0</li>
            <li>Parámetro vacío: <a href="consulta.jsp?categoria=&amp;luz=">consulta.jsp?categoria=&amp;luz=</a> &rarr; también <code>general</code></li>
            <li>Error controlado: <a href="consulta.jsp?categoria=placa&amp;luz=abc">consulta.jsp?categoria=placa&amp;luz=abc</a> &rarr; <code>error.jsp</code></li>
            <li>Categoría no válida: <a href="consulta.jsp?categoria=muro">consulta.jsp?categoria=muro</a> &rarr; <code>error.jsp</code></li>
            <li>Práctica base de la clase (saludo): <a href="saludo.jsp?nombre=Ana">saludo.jsp?nombre=Ana</a></li>
        </ul>
    </div>

</div>
</body>
</html>

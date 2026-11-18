<%-- Formulario clásico de predimensionamiento (VISTA). Sin validación en HTML:
     todo lo valida AnalisisEstructuralServlet al recibir el POST. --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - Predimensionamiento y Estructuras</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/paginas.css">
</head>
<body>
<div class="wrapper">

    <jsp:include page="/WEB-INF/vistas/header.jsp" />

    <div class="card">
        <h1>Módulo de Registro y Análisis</h1>
        <p class="nota nota-bloque">Ingrese los parámetros geométricos del proyecto. El servidor valida los datos (método POST).</p>

        <form action="${pageContext.request.contextPath}/analisis-estructural" method="POST" enctype="multipart/form-data">
            <div class="form-grid">
                <div class="form-group"><label>Ancho del Edificio (m)</label>
                    <input type="text" inputmode="decimal" name="ancho" placeholder="Ej. 10.5"></div>
                <div class="form-group"><label>Largo del Edificio (m)</label>
                    <input type="text" inputmode="decimal" name="largo" placeholder="Ej. 15.0"></div>
                <div class="form-group"><label>Número de Pisos</label>
                    <input type="text" inputmode="numeric" name="pisos" placeholder="Ej. 4"></div>
                <div class="form-group"><label>Altura Promedio por Piso (m)</label>
                    <input type="text" inputmode="decimal" name="alturaPiso" placeholder="Ej. 2.6"></div>
                <div class="form-group full"><label>Adjuntar Plano Arquitectónico (Imagen opcional)</label>
                    <input type="file" name="planoImagen"></div>
            </div>
            <button type="submit" class="btn-submit">Procesar Cálculo Estructural</button>
        </form>
    </div>

</div>
</body>
</html>

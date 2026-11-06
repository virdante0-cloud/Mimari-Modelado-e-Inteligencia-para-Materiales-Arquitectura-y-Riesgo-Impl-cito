<%-- Registro de cuenta (VISTA, con JSTL). Solo muestra el formulario; RegistroServlet valida y guarda. --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - Crear cuenta</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/acceso.css">
</head>
<body class="acceso">
<jsp:include page="/WEB-INF/vistas/fondo.jsp" />

<main class="panel">
    <div class="marca">
        <span class="logo">M</span>
        <div>
            <strong>MIMARI</strong>
            <small>Modelado e Inteligencia para Materiales, Arquitectura y Riesgo Implícito</small>
        </div>
    </div>

    <nav class="pestanas" aria-label="Acceso">
        <a href="${pageContext.request.contextPath}/login.jsp">Ingresar</a>
        <a class="activa" href="${pageContext.request.contextPath}/registro">Crear cuenta</a>
    </nav>

    <c:if test="${not empty mensajeAcceso}">
        <div class="aviso" role="alert"><c:out value="${mensajeAcceso}" /></div>
    </c:if>

    <form action="${pageContext.request.contextPath}/registro" method="POST" class="formulario">
        <label>Usuario
            <input type="text" name="usuario" value="<c:out value="${usuarioEscrito}" />" autocomplete="username"
                   placeholder="3 a 30 caracteres">
        </label>
        <label>Clave
            <input type="password" name="clave" autocomplete="new-password" placeholder="Mínimo 8 caracteres">
        </label>
        <label>Confirmar clave
            <input type="password" name="confirmar" autocomplete="new-password" placeholder="Repite la clave">
        </label>
        <button type="submit">Crear cuenta y entrar</button>
    </form>

    <p class="pie">Cada cuenta tiene su propio catálogo de insumos, sus fotos y su historial de análisis.</p>
</main>
</body>
</html>

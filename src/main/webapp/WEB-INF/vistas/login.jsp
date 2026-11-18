<%-- ============================================================================
     SEMANA 11 - login.jsp (VISTA, con JSTL). Pantalla de acceso a pantalla completa: mientras no haya sesión, es lo
     único que se ve de MIMARI (AutenticacionFilter bloquea el resto). Solo muestra el formulario; NO valida nada.
       login.jsp --POST--> LoginServlet --> UsuarioService (JPA) --> session + cookie --redirect--> /catalogo
     ============================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - Iniciar sesión</title>
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
        <a class="activa" href="${pageContext.request.contextPath}/login.jsp">Ingresar</a>
        <a href="${pageContext.request.contextPath}/registro">Crear cuenta</a>
    </nav>

    <c:if test="${not empty mensajeAcceso}">
        <div class="aviso" role="alert"><c:out value="${mensajeAcceso}" /></div>
    </c:if>

    <form action="${pageContext.request.contextPath}/login" method="POST" class="formulario">
        <label>Usuario
            <input type="text" name="usuario" value="<c:out value="${ultimoUsuario}" />" autocomplete="username"
                   placeholder="Tu usuario">
        </label>
        <label>Clave
            <input type="password" name="clave" autocomplete="current-password" placeholder="Tu clave">
        </label>
        <button type="submit">Entrar</button>
    </form>

    <p class="pie">
        <c:choose>
            <c:when test="${not empty ultimoUsuario}">La cookie <code>ultimoUsuario</code> recordó tu último usuario.</c:when>
            <c:otherwise>Tu información (insumos, planos y análisis) queda guardada solo en tu cuenta.</c:otherwise>
        </c:choose>
    </p>
</main>
</body>
</html>

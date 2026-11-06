<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Componente reutilizable (Semana 09): barra de navegación MIMARI, animada (header.css).
     Es un FRAGMENTO: no lleva <html>, <head> ni <body> y NO se incluye a sí mismo.
     Otras páginas lo insertan con <jsp:include page="/WEB-INF/vistas/header.jsp" />.
     Semana 14: sin scriptlets, con JSTL. El usuario de la sesión lo guardó Sesion.iniciar (Semana 11).
     Solo muestra enlaces: no valida ni procesa nada. --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<%-- ruta original que pidió el navegador (la página puede haber llegado por forward desde un servlet) --%>
<c:set var="actual" value="${requestScope['jakarta.servlet.forward.servlet_path']}" />
<link rel="stylesheet" href="${ctx}/header.css">
<nav class="mimari-top" aria-label="Principal">
    <span class="orbe orbe-1" aria-hidden="true"></span>
    <span class="orbe orbe-2" aria-hidden="true"></span>

    <a class="marca" href="${ctx}/catalogo">
        <span class="logo" aria-hidden="true">M</span>
        <span class="marca-texto">
            <strong>MIMARI</strong>
            <small>Modelado e Inteligencia para Materiales, Arquitectura y Riesgo Implícito</small>
        </span>
    </a>

    <input type="checkbox" id="nav-toggle" class="nav-toggle" aria-label="Abrir menú">
    <label for="nav-toggle" class="burger" aria-hidden="true"><span></span><span></span><span></span></label>

    <ul class="enlaces">
        <li><a class="${actual == '/catalogo' ? 'activo' : ''}" href="${ctx}/catalogo">Catálogo</a></li>
        <li><a class="${actual == '/simulador' ? 'activo' : ''}" href="${ctx}/simulador">Simulador</a></li>
        <li><a class="${actual == '/proyecto' ? 'activo' : ''}" href="${ctx}/proyecto">Proyecto</a></li>
        <li class="desplegable">
            <a class="${actual == '/practicas' ? 'activo' : ''}" href="${ctx}/practicas">Prácticas &#9662;</a>
            <ul class="submenu">
                <li><a href="${ctx}/saludo.jsp">Saludo (Sem. 10)</a></li>
                <li><a href="${ctx}/consulta.jsp">Consulta (Sem. 10)</a></li>
                <li><a href="${ctx}/productos.jsp">Productos JDBC (Sem. 11)</a></li>
                <li><a href="${ctx}/xml-demo.jsp">JSTL XML (Sem. 14)</a></li>
                <li><a href="${ctx}/analisis-estructural">Formulario clásico</a></li>
            </ul>
        </li>
    </ul>

    <div class="usuario">
        <span class="punto" aria-hidden="true"></span>
        <span class="nombre"><c:out value="${sessionScope.usuario}" /></span>
        <form action="${ctx}/logout" method="POST"><button type="submit">Salir</button></form>
    </div>
</nav>

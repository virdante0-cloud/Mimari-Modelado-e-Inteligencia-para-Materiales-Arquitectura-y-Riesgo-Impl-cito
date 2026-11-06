<%-- ============================================================================
     CATÁLOGO ÚNICO (VISTA con JSTL, Semanas 09, 13 y 14). Sin scriptlets: CatalogoServlet prepara los atributos
       idioma, materiales, elementos, insumos, totalInsumos, carrito, pdfs, fechaActual   y aquí solo se presentan.
     c:*  variables, condiciones, bucles y URLs        fmt:*  idioma, moneda y fecha (mensajes.properties / mensajes_en)
     Las URI de las bibliotecas en Jakarta EE son jakarta.tags.core y jakarta.tags.fmt.
     ============================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="${idioma}" />
<fmt:setBundle basename="mensajes" />
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="${idioma}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - <fmt:message key="cat.titulo" /></title>
    <link rel="stylesheet" href="${ctx}/styles.css">
    <script src="${ctx}/js/materiales.js" defer></script>
</head>
<body>
<header class="hero">
    <jsp:include page="/WEB-INF/vistas/header.jsp" />
    <div class="hero-content">
        <h1><fmt:message key="cat.titulo" /></h1>
        <p><fmt:message key="cat.bienvenida" />, <b><c:out value="${sessionScope.usuario}" /></b>. <fmt:message key="cat.subtitulo" /></p>
        <%-- c:url + c:param arman el enlace de cambio de idioma (añaden el contexto de la aplicación) --%>
        <c:url var="urlEs" value="/catalogo"><c:param name="idioma" value="es" /></c:url>
        <c:url var="urlEn" value="/catalogo"><c:param name="idioma" value="en" /></c:url>
        <p class="subnav">
            <a href="#base"><fmt:message key="cat.base.titulo" /></a>
            <a href="#elementos"><fmt:message key="cat.elem.titulo" /></a>
            <a href="#mis-insumos"><fmt:message key="cat.mis.titulo" /></a>
            <a href="#cotizacion"><fmt:message key="cat.cart.titulo" /></a>
            <a href="#pdf"><fmt:message key="cat.pdf.titulo" /></a>
            <span class="idiomas"><fmt:message key="cat.idioma" />: <a href="${urlEs}">ES</a> · <a href="${urlEn}">EN</a></span>
        </p>
    </div>
</header>

<main class="grid">

    <c:if test="${not empty avisoDatos}">
        <article class="card full-width"><p class="sim-estado error"><c:out value="${avisoDatos}" /></p></article>
    </c:if>

    <%-- ---------------------------------------------------------------- resumen --%>
    <article class="card full-width">
        <div class="kpis">
            <div class="kpi"><span><fmt:message key="cat.resumen.insumos" /></span><strong>${insumos.size()}</strong></div>
            <div class="kpi"><span><fmt:message key="cat.resumen.total" /></span>
                <strong><fmt:formatNumber value="${totalInsumos}" type="currency" currencySymbol="$" maxFractionDigits="0" /></strong></div>
            <div class="kpi"><span><fmt:message key="cat.resumen.pdf" /></span><strong>${pdfs.size()}</strong></div>
        </div>
    </article>

    <%-- ---------------------------------------------------------------- catálogo base --%>
    <article class="card destacado full-width" id="base">
        <h2><fmt:message key="cat.base.titulo" /></h2>
        <p class="sim-subtitle"><fmt:message key="cat.base.desc" /></p>
        <c:choose>
            <%-- c:if / c:choose: condiciones; empty = la colección no existe o no tiene elementos --%>
            <c:when test="${empty materiales}"><p class="sim-estado aviso"><fmt:message key="cat.base.vacio" /></p></c:when>
            <c:otherwise>
                <table class="table-results">
                    <thead><tr><th>#</th><th><fmt:message key="cat.base.material" /></th><th><fmt:message key="cat.base.precio" /></th><th><fmt:message key="cat.base.estado" /></th></tr></thead>
                    <tbody>
                    <%-- c:forEach recorre la lista; varStatus da el número de fila --%>
                    <c:forEach var="item" items="${materiales}" varStatus="fila">
                        <tr>
                            <td><span class="badge">#${fila.count}</span></td>
                            <td><b><c:out value="${item.nombre}" /></b></td>
                            <td><fmt:formatNumber value="${item.precio}" type="currency" currencySymbol="$" maxFractionDigits="0" /> COP</td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.disponible}"><span class="estado ok"><fmt:message key="cat.base.disponible" /></span></c:when>
                                    <c:otherwise><span class="estado no"><fmt:message key="cat.base.nodisponible" /></span></c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </article>

    <%-- ---------------------------------------------------------------- elementos estructurales --%>
    <article class="card destacado full-width" id="elementos">
        <h2><fmt:message key="cat.elem.titulo" /></h2>
        <p class="sim-subtitle"><fmt:message key="cat.elem.desc" /></p>
        <table class="table-results">
            <thead><tr><th>ID</th><th><fmt:message key="cat.elem.nombre" /></th><th><fmt:message key="cat.elem.luz" /></th><th><fmt:message key="cat.elem.espesor" /></th><th><fmt:message key="cat.elem.volumen" /></th></tr></thead>
            <tbody>
            <c:forEach var="e" items="${elementos}">
                <tr>
                    <td><span class="badge">#0${e.numero}</span></td>
                    <td><b><c:out value="${e.nombre}" /></b></td>
                    <td><fmt:formatNumber value="${e.luz}" minFractionDigits="2" maxFractionDigits="2" /> m</td>
                    <td><fmt:formatNumber value="${e.espesor}" minFractionDigits="2" maxFractionDigits="2" /> m</td>
                    <td><b><fmt:formatNumber value="${e.volumen}" minFractionDigits="2" maxFractionDigits="2" /> m³</b></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
        <p class="sim-subtitle margin-top"><b>${elementos.size()}</b> <fmt:message key="cat.elem.total" /></p>
    </article>

    <%-- ---------------------------------------------------------------- mis insumos (por usuario, JPA) --%>
    <article class="card destacado full-width" id="mis-insumos">
        <h2><fmt:message key="cat.mis.titulo" /></h2>
        <p class="sim-subtitle"><fmt:message key="cat.mis.desc" /></p>

        <form action="${ctx}/catalogo" method="POST" class="sim-card">
            <input type="hidden" name="accion" value="guardar">
            <div class="form-row">
                <div class="form-group">
                    <label for="mat-nombre"><fmt:message key="cat.mis.nombre" /></label>
                    <input type="text" id="mat-nombre" name="nombre" placeholder="Ej. Viga de amarre 1/2 pulgada">
                </div>
                <div class="form-group">
                    <label for="mat-tipo"><fmt:message key="cat.mis.tipo" /></label>
                    <input type="text" id="mat-tipo" name="tipo" placeholder="Ej. Estructural / Refuerzo">
                </div>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label for="mat-precio"><fmt:message key="cat.mis.precio" /></label>
                    <input type="text" id="mat-precio" name="precio" inputmode="decimal" placeholder="Ej. 45000">
                </div>
                <div class="form-group">
                    <label for="mat-cantidad"><fmt:message key="cat.mis.cantidad" /></label>
                    <input type="text" id="mat-cantidad" name="cantidad" inputmode="numeric" placeholder="Ej. 12">
                </div>
            </div>
            <button type="submit" class="btn-primary"><fmt:message key="cat.mis.guardar" /></button>
        </form>

        <div class="results-container margin-top">
            <c:choose>
                <c:when test="${empty insumos}">
                    <p class="sim-estado aviso"><fmt:message key="cat.mis.vacio" /></p>
                </c:when>
                <c:otherwise>
                    <%-- Semana 05: filtro dinámico del DOM (materiales.js lee las filas y las filtra en el navegador) --%>
                    <div class="form-group margin-bottom">
                        <label for="input-busqueda">&#128269; <fmt:message key="cat.mis.filtro" /></label>
                        <input type="text" id="input-busqueda" placeholder="...">
                    </div>
                    <table class="table-results" id="tabla-insumos">
                        <thead>
                        <tr><th>ID</th><th><fmt:message key="cat.mis.nombre" /></th><th><fmt:message key="cat.mis.tipo" /></th>
                            <th><fmt:message key="cat.mis.precio" /></th><th><fmt:message key="cat.mis.cantidad" /></th>
                            <th><fmt:message key="cat.mis.subtotal" /></th><th></th></tr>
                        </thead>
                        <tbody>
                        <c:forEach var="i" items="${insumos}">
                            <tr class="fila-insumo" data-nombre="<c:out value="${i.nombre}" />" data-tipo="<c:out value="${i.tipo}" />"
                                data-precio="${i.precio}" data-cantidad="${i.cantidad}">
                                <td>${i.id}</td>
                                <td><c:out value="${i.nombre}" /></td>
                                <td><c:out value="${i.tipo}" /></td>
                                <td><fmt:formatNumber value="${i.precio}" type="currency" currencySymbol="$" /></td>
                                <td>${i.cantidad}</td>
                                <td><b><fmt:formatNumber value="${i.subtotal}" type="currency" currencySymbol="$" /></b></td>
                                <td>
                                    <form action="${ctx}/catalogo" method="POST" class="en-linea">
                                        <input type="hidden" name="accion" value="eliminar">
                                        <input type="hidden" name="id" value="${i.id}">
                                        <button type="submit" class="btn-peligro"><fmt:message key="cat.mis.eliminar" /></button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                        <tr id="fila-sin-resultados" class="hidden"><td colspan="7" class="sin-resultados"><fmt:message key="cat.mis.sinresultados" /></td></tr>
                        </tbody>
                    </table>
                    <div class="total-box margin-top">
                        <strong><fmt:message key="cat.mis.total" />:</strong>
                        <span id="costo-acumulado-total" class="total-price"><fmt:formatNumber value="${totalInsumos}" type="currency" currencySymbol="$" /></span>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </article>

    <%-- ---------------------------------------------------------------- lista de cotización (componente con estado) --%>
    <article class="card destacado full-width" id="cotizacion">
        <h2><fmt:message key="cat.cart.titulo" /></h2>
        <p class="sim-subtitle"><fmt:message key="cat.cart.desc" /> (máx. ${carritoMaximo})</p>
        <form action="${ctx}/catalogo" method="POST" class="sim-card">
            <input type="hidden" name="accion" value="carrito">
            <div class="form-row">
                <div class="form-group">
                    <label for="cart-nombre"><fmt:message key="cat.cart.nombre" /></label>
                    <input type="text" id="cart-nombre" name="nombre" placeholder="Ej. Varilla 3/8">
                </div>
            </div>
            <button type="submit" class="btn-primary"><fmt:message key="cat.cart.agregar" /></button>
        </form>
        <c:if test="${not empty carrito}">
            <ul class="lista-poo margin-top">
                <c:forEach var="n" items="${carrito}"><li class="item-material"><c:out value="${n}" /></li></c:forEach>
            </ul>
            <form action="${ctx}/catalogo" method="POST" class="margin-top">
                <input type="hidden" name="accion" value="vaciar">
                <button type="submit" class="btn-peligro"><fmt:message key="cat.cart.vaciar" /></button>
            </form>
        </c:if>
    </article>

    <%-- ---------------------------------------------------------------- catálogos PDF (por usuario) --%>
    <article class="card destacado full-width" id="pdf">
        <h2><fmt:message key="cat.pdf.titulo" /></h2>
        <p class="sim-subtitle"><fmt:message key="cat.pdf.desc" /></p>
        <form action="${ctx}/catalogo" method="POST" enctype="multipart/form-data" class="sim-card">
            <input type="hidden" name="accion" value="pdf">
            <div class="form-group">
                <label for="pdf-archivo"><fmt:message key="cat.pdf.archivo" /></label>
                <input type="file" id="pdf-archivo" name="archivo">
            </div>
            <button type="submit" class="btn-primary"><fmt:message key="cat.pdf.subir" /></button>
        </form>
        <c:choose>
            <c:when test="${empty pdfs}"><p class="sim-estado aviso margin-top"><fmt:message key="cat.pdf.vacio" /></p></c:when>
            <c:otherwise>
                <table class="table-results margin-top">
                    <tbody>
                    <c:forEach var="p" items="${pdfs}">
                        <c:url var="urlVer" value="/api/archivos"><c:param name="id" value="${p.id}" /></c:url>
                        <c:url var="urlBajar" value="/api/archivos"><c:param name="id" value="${p.id}" /><c:param name="descargar" value="1" /></c:url>
                        <tr>
                            <td>&#128196; <b><c:out value="${p.nombreOriginal}" /></b>
                                <span class="nota-chica">${p.tamanoLegible} · <fmt:formatDate value="${p.creado}" pattern="dd/MM/yyyy HH:mm" timeZone="America/Bogota" /></span></td>
                            <td class="acciones">
                                <a class="btn-enlace" href="${urlVer}" target="_blank" rel="noopener"><fmt:message key="cat.pdf.ver" /></a>
                                <a class="btn-enlace" href="${urlBajar}"><fmt:message key="cat.pdf.descargar" /></a>
                                <form action="${ctx}/catalogo" method="POST" class="en-linea">
                                    <input type="hidden" name="accion" value="eliminarPdf">
                                    <input type="hidden" name="id" value="${p.id}">
                                    <button type="submit" class="btn-peligro"><fmt:message key="cat.pdf.eliminar" /></button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </article>

    <article class="card full-width">
        <%-- fmt:formatDate da formato a la fecha que preparó el servlet --%>
        <p class="nota-chica"><fmt:message key="cat.fecha" />:
            <fmt:formatDate value="${fechaActual}" pattern="dd/MM/yyyy HH:mm" timeZone="America/Bogota" /> · <fmt:message key="cat.nota" /></p>
    </article>
</main>
</body>
</html>

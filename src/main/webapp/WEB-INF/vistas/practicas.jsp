<%-- Prácticas del curso (VISTA con JSTL): acceso a lo visto en cada semana, integrado a MIMARI. Solo enlaces. --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>MIMARI | Prácticas del curso</title>
  <link rel="stylesheet" href="${ctx}/styles.css" />
</head>
<body>
  <header class="hero">
    <jsp:include page="/WEB-INF/vistas/header.jsp" />
    <div class="hero-content">
      <h1>Prácticas del curso</h1>
      <p>Cada semana de Programación III tiene su lugar dentro de MIMARI. Aquí están las páginas de práctica y dónde se usa cada tema en la aplicación.</p>
    </div>
  </header>

  <main class="grid">
    <article class="card destacado">
      <h2>Semanas 4 y 5 - POO y DOM</h2>
      <p>La clase <code>Material</code> y el filtro dinámico en tiempo real están en <code>js/materiales.js</code> y se usan en
        “Mis insumos” del <a href="${ctx}/catalogo#mis-insumos">catálogo</a>.</p>
    </article>
    <article class="card destacado">
      <h2>Semana 9 - Componentes JSP</h2>
      <p>El encabezado (<code>header.jsp</code>) es un fragmento reutilizable que todas las páginas incluyen con
        <code>jsp:include</code>. El catálogo de elementos estructurales vive en el <a href="${ctx}/catalogo#elementos">catálogo</a>.</p>
    </article>
    <article class="card destacado">
      <h2>Semana 10 - Objetos implícitos y errores</h2>
      <p>Práctica base: <a href="${ctx}/saludo.jsp?nombre=Ana">saludo.jsp?nombre=Ana</a> &middot; <a href="${ctx}/saludo.jsp">saludo.jsp</a>.
        Adaptación: <a href="${ctx}/consulta.jsp?categoria=viga&amp;luz=6.5">consulta.jsp?categoria=viga&amp;luz=6.5</a>.
        Error controlado: <a href="${ctx}/saludo.jsp?nombre=Ana&amp;error=1">saludo.jsp?nombre=Ana&amp;error=1</a> (pasa a <code>error.jsp</code>).</p>
    </article>
    <article class="card destacado">
      <h2>Semana 11 - Sesiones, cookies y JDBC</h2>
      <p>La sesión y la cookie <code>ultimoUsuario</code> son el inicio de sesión de toda la aplicación. La lectura directa con JDBC:
        <a href="${ctx}/productos.jsp">productos.jsp</a> (requiere <code>db/schema.sql</code>).</p>
    </article>
    <article class="card destacado">
      <h2>Semana 13 - EJB y JPA</h2>
      <p><code>UsuarioService</code>, <code>InsumoService</code>, <code>ArchivoService</code> y <code>AnalisisService</code> son los servicios sin estado
        (equivalentes a <code>@Stateless</code>); <code>CarritoTemporal</code> es el componente con estado (<code>@Stateful</code>), visible en la
        <a href="${ctx}/catalogo#cotizacion">lista de cotización</a>.</p>
    </article>
    <article class="card destacado">
      <h2>Semana 14 - JSTL</h2>
      <p>Las vistas usan <code>c:*</code>, <code>fmt:*</code> y <code>x:*</code>. Textos en español e inglés en el
        <a href="${ctx}/catalogo?idioma=en">catálogo</a>; XML con XPath en <a href="${ctx}/xml-demo.jsp">xml-demo.jsp</a>.</p>
    </article>
    <article class="card destacado full-width">
      <h2>Formulario clásico de predimensionamiento</h2>
      <p>Envía los datos por POST al servlet, que los valida y muestra el resultado en otra página JSP:
        <a href="${ctx}/analisis-estructural">analisis-estructural</a>.</p>
    </article>
  </main>
</body>
</html>

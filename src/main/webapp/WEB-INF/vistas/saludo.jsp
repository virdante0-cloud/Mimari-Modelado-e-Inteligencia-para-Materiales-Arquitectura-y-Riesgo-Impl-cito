<%-- ============================================================================
     SEMANA 10 - saludo.jsp (VISTA). Solo muestra los datos; NO valida nada.
     El servlet SaludoServlet (/saludo.jsp) lee el parámetro, lo valida, guarda
     "ultimoNombre" en application y hace forward a esta página.

       request     recibe  -> el servlet lo usa para leer "nombre"; aquí se leen los atributos que dejó
       out         escribe -> imprime el saludo en el HTML
       response    controla-> el servlet agrega el encabezado X-Curso (no se ve en pantalla)
       application comparte-> memoria de toda la aplicación (aquí se lee "ultimoNombre")
       exception   explica -> si algo falla, el control pasa a error.jsp (errorPage)
     ============================================================================ --%>

<%-- DIRECTIVA PAGE: "ficha técnica" de la página antes de ejecutarla
       contentType -> salida HTML en UTF-8 (tildes y eñes bien)
       errorPage   -> si esta JSP lanza una excepción, el control pasa a /error.jsp
                      (ruta desde la raíz porque esta vista vive en WEB-INF/vistas) --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - Saludo JSP</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/paginas.css">
</head>
<body>
<div class="wrapper">

    <jsp:include page="/WEB-INF/vistas/header.jsp" />

    <div class="card">
        <h1>Saludo JSP</h1>
        <p class="nota nota-bloque">
            Práctica de la Semana 10: el servlet lee <code>nombre</code> de la URL con <code>request</code>.
            Si no llega (o llega vacío) se usa <code>Estudiante</code>.
        </p>

        <%-- OUT: escribe en el HTML resultante (el nombre ya llega validado desde el servlet) --%>
        <div class="saludo">Hola, <b><% out.print(request.getAttribute("nombre")); %></b></div>

        <%-- APPLICATION: lo guardó el servlet; se lee con una expresión --%>
        <p class="nota">Último nombre en aplicación: <b><%= application.getAttribute("ultimoNombre") %></b></p>

        <form action="${pageContext.request.contextPath}/saludo.jsp" method="GET" class="row separado">
            <label>Nombre
                <input type="text" name="nombre" placeholder="Ej. Ana (vacío = Estudiante)">
            </label>
            <button type="submit">Saludar</button>
        </form>
    </div>

    <div class="card">
        <h3>Pruebas de la práctica (Semana 10)</h3>
        <ul class="pruebas">
            <li>Parámetro válido: <a href="saludo.jsp?nombre=Ana">saludo.jsp?nombre=Ana</a> &rarr; saluda a <code>Ana</code></li>
            <li>Sin parámetro: <a href="saludo.jsp">saludo.jsp</a> &rarr; saluda a <code>Estudiante</code></li>
            <li>Parámetro vacío: <a href="saludo.jsp?nombre=">saludo.jsp?nombre=</a> &rarr; saluda a <code>Estudiante</code></li>
            <li>Nombre no válido: <a href="saludo.jsp?nombre=Ana123">saludo.jsp?nombre=Ana123</a> &rarr; <code>error.jsp</code></li>
            <li>Error controlado: <a href="saludo.jsp?nombre=Ana&amp;error=1">saludo.jsp?nombre=Ana&amp;error=1</a> &rarr; <code>error.jsp</code></li>
        </ul>
    </div>

    <div class="card">
        <h3>Qué hace cada objeto implícito</h3>
        <ul class="pruebas">
            <li><code>request</code> recibe: el servlet lee el parámetro <code>nombre</code>.</li>
            <li><code>out</code> escribe: imprime el saludo en el HTML.</li>
            <li><code>response</code> controla: agrega el encabezado <code>X-Curso</code> a la respuesta HTTP.</li>
            <li><code>application</code> comparte: guarda <code>ultimoNombre</code> para toda la aplicación.</li>
            <li><code>exception</code> explica errores: se usa en <a href="error.jsp">error.jsp</a> (<code>isErrorPage="true"</code>).</li>
        </ul>
    </div>

</div>
</body>
</html>

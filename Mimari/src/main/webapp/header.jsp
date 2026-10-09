<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- Componente reutilizable (Semana 09): barra de navegación MIMARI.
     Es un FRAGMENTO: no lleva <html>, <head> ni <body> y NO se incluye a sí mismo.
     Otras páginas lo insertan con <jsp:include page="header.jsp" />. --%>
<%
    String ctx = request.getContextPath();
%>
<nav style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:12px;
            background:#0f172a;color:white;padding:16px 24px;border-radius:16px;margin-bottom:25px;">
    <div>
        <strong style="font-size:20px;letter-spacing:2px;color:#38bdf8;">MIMARI</strong>
        <div style="font-size:12px;opacity:.85;">Modelado e Inteligencia para Materiales, Arquitectura y Riesgo Implícito</div>
    </div>
    <div style="display:flex;gap:18px;font-size:14px;font-weight:600;">
        <a href="<%= ctx %>/index.html" style="color:white;text-decoration:none;">Simulador</a>
        <a href="<%= ctx %>/index.jsp" style="color:white;text-decoration:none;">Catálogo (JSP)</a>
        <a href="<%= ctx %>/consulta.jsp" style="color:white;text-decoration:none;">Consulta (JSP)</a>
        <a href="<%= ctx %>/analisis-estructural" style="color:white;text-decoration:none;">Formulario (Servlet)</a>
    </div>
</nav>

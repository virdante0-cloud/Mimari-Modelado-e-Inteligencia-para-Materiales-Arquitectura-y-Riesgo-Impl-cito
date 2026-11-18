<%-- Portada: la entrada de MIMARI es el catálogo. AutenticacionFilter ya mandó a /login.jsp a quien no tiene sesión. --%>
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:redirect url="/catalogo" />

package com.mimari.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Punto único de manejo de errores (/error.jsp). A él llegan:
 *   1) los demás servlets, con los atributos "mensajeError" y "tipoError" (errores de validación);
 *   2) el contenedor, cuando una excepción no se controla (web.xml <error-page>) o falta una página (404);
 *   3) las JSP que declaran errorPage="/error.jsp".
 *
 * Aquí se prepara el mensaje (y se ESCAPA, porque puede contener texto externo) y se entrega a la vista
 * WEB-INF/vistas/error.jsp, que tiene isErrorPage="true" y por eso dispone del objeto exception.
 */
@WebServlet({"/error.jsp", "/error"})
public class ErrorServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/error.jsp";

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // La excepción puede venir de una JSP (errorPage) o del contenedor (error-page de web.xml)
        Throwable ex = (Throwable) request.getAttribute("jakarta.servlet.jsp.jspException");
        if (ex == null) {
            ex = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        }
        Object estado = request.getAttribute("jakarta.servlet.error.status_code");

        String mensaje;
        String tipo = null;
        if (ex != null) {
            tipo = ex.getClass().getSimpleName();
            // Solo se muestra el mensaje de las excepciones propias de la validación; el resto podría revelar detalles internos
            boolean mensajeSeguro = (ex instanceof IllegalArgumentException || ex instanceof IllegalStateException)
                    && ex.getMessage() != null;
            mensaje = mensajeSeguro ? ex.getMessage() : "Ocurrió un error interno en el servidor.";
            if (response.getStatus() < 400) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else if (request.getAttribute("mensajeError") != null) {
            mensaje = String.valueOf(request.getAttribute("mensajeError"));
            Object t = request.getAttribute("tipoError");
            tipo = (t == null) ? null : String.valueOf(t);
        } else if (Integer.valueOf(404).equals(estado)) {
            mensaje = "La página que buscas no existe.";
            tipo = "HTTP 404";
        } else {
            mensaje = "Error no identificado";
        }

        request.setAttribute("mensajeSeguro", escaparHtml(mensaje));
        request.setAttribute("tipoSeguro", tipo == null ? null : escaparHtml(tipo));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /** Escapa texto para mostrarlo en HTML (evita XSS). */
    private static String escaparHtml(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}

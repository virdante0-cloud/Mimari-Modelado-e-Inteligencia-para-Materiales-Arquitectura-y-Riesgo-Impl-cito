package com.mimari.servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Semana 10 - práctica del saludo (/saludo y /saludo.jsp).
 *   request     -> lee el parámetro "nombre"
 *   response    -> agrega el encabezado X-Curso
 *   application -> guarda "ultimoNombre" (memoria compartida de la aplicación)
 * La validación se hace aquí; la vista WEB-INF/vistas/saludo.jsp solo muestra los datos.
 *
 * Pruebas: /saludo.jsp?nombre=Ana | /saludo.jsp | /saludo.jsp?nombre= | /saludo.jsp?nombre=Ana&error=1
 */
@WebServlet({"/saludo", "/saludo.jsp"})
public class SaludoServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/saludo.jsp";
    private static final String VISTA_ERROR = "/error.jsp";

    /** Solo letras (con tildes y eñes), espacios, punto, apóstrofo y guion. Máximo 40 caracteres. */
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("[\\p{L} .'-]{1,40}");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        procesar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        procesar(request, response);
    }

    private void procesar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // response: el encabezado viaja en la cabecera HTTP, no se ve en pantalla
        response.setHeader("X-Curso", "Programacion III");

        // request: lee el dato que llega desde el navegador
        String nombre = request.getParameter("nombre");
        if (nombre == null || nombre.isBlank()) {
            nombre = "Estudiante";                               // sin dato -> valor por defecto
        }
        nombre = nombre.trim();

        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("mensajeError",
                    "El nombre solo puede tener letras, espacios, punto, apóstrofo y guion (máximo 40 caracteres).");
            request.setAttribute("tipoError", "NombreNoValido");
            request.getRequestDispatcher(VISTA_ERROR).forward(request, response);
            return;
        }

        // Prueba del manejo de errores: lanza una excepción a propósito y el contenedor la envía a error.jsp
        if ("1".equals(request.getParameter("error"))) {
            throw new IllegalStateException("Error de prueba provocado desde el servlet de saludo (parametro error=1).");
        }

        // application: memoria compartida por toda la aplicación. Solo se guardan datos no privados y ya validados.
        ServletContext application = getServletContext();
        application.setAttribute("ultimoNombre", nombre);

        request.setAttribute("nombre", nombre);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}

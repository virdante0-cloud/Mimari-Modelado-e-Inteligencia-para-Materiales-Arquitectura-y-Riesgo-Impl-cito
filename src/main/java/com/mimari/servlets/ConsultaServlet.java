package com.mimari.servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * Semana 10 - consulta por parámetros (/consulta y /consulta.jsp), adaptación del saludo al proyecto:
 *   nombre -> categoria (viga | placa | general)      (nuevo) -> luz en metros
 * Valida aquí; la vista WEB-INF/vistas/consulta.jsp solo muestra los datos.
 *
 * Pruebas: /consulta?categoria=viga&luz=6.5 | /consulta | /consulta?categoria=&luz= | /consulta?luz=abc (error)
 */
@WebServlet({"/consulta", "/consulta.jsp"})
public class ConsultaServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/consulta.jsp";
    private static final String VISTA_ERROR = "/error.jsp";
    private static final Set<String> CATEGORIAS = Set.of("viga", "placa", "general");
    private static final double LUZ_POR_DEFECTO = 4.0;

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
        response.setHeader("X-Curso", "Programacion III");

        String categoria;
        double luz;
        boolean luzPorDefecto;
        try {
            // request: lee y VALIDA los parámetros
            categoria = request.getParameter("categoria");
            categoria = (categoria == null || categoria.isBlank()) ? "general" : categoria.trim().toLowerCase(Locale.ROOT);
            if (!CATEGORIAS.contains(categoria)) {
                throw new IllegalArgumentException("La categoría debe ser viga, placa o general.");
            }

            String luzTexto = request.getParameter("luz");
            luzPorDefecto = (luzTexto == null || luzTexto.isBlank());
            luz = luzPorDefecto ? LUZ_POR_DEFECTO : decimal(luzTexto);
            if (luz <= 0 || luz > 100) {
                throw new IllegalArgumentException("La luz debe estar entre 0 y 100 m.");
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("mensajeError", e.getMessage());
            request.setAttribute("tipoError", e.getClass().getSimpleName());
            request.getRequestDispatcher(VISTA_ERROR).forward(request, response);
            return;
        }

        // application: memoria compartida por toda la aplicación y todos los visitantes.
        // Solo se guardan datos ya validados; synchronized porque varios visitantes pueden entrar a la vez.
        ServletContext application = getServletContext();
        String consultaAnterior;
        int totalConsultas;
        synchronized (application) {
            consultaAnterior = (String) application.getAttribute("ultimaConsulta");
            Object previo = application.getAttribute("totalConsultas");
            totalConsultas = (previo instanceof Integer) ? ((Integer) previo) + 1 : 1;
            application.setAttribute("ultimaConsulta", categoria + " (L = " + String.format(Locale.ROOT, "%.2f", luz) + " m)");
            application.setAttribute("totalConsultas", totalConsultas);
        }

        // Datos listos para la vista (la JSP no calcula nada)
        boolean general = categoria.equals("general");
        request.setAttribute("categoria", categoria);
        request.setAttribute("luz", String.format(Locale.ROOT, "%.2f", luz));
        request.setAttribute("luzPorDefecto", luzPorDefecto);
        if (categoria.equals("viga") || general) {
            request.setAttribute("vigaPeralte", String.format(Locale.ROOT, "%.0f", luz / 10.0 * 100));
            request.setAttribute("vigaBase", String.format(Locale.ROOT, "%.0f", luz / 10.0 / 2.0 * 100));
        }
        if (categoria.equals("placa") || general) {
            request.setAttribute("placaEspesor", String.format(Locale.ROOT, "%.0f", luz / 25.0 * 100));
        }
        request.setAttribute("consultaAnterior", consultaAnterior == null ? "ninguna" : consultaAnterior);
        request.setAttribute("totalConsultas", totalConsultas);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /** Acepta "6.5" y "6,5". Solo dígitos y un separador decimal. */
    private static double decimal(String valor) {
        String limpio = valor.trim();
        if (limpio.length() > 20 || !limpio.matches("-?[0-9]+([.,][0-9]+)?")) {
            throw new IllegalArgumentException("El campo luz no es un número válido.");
        }
        return Double.parseDouble(limpio.replace(',', '.'));
    }
}

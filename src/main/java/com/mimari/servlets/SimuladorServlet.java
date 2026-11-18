package com.mimari.servlets;

import com.mimari.seguridad.Sesion;
import com.mimari.servicio.AnalisisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Simulador (/simulador): entrega la vista WEB-INF/vistas/simulador.jsp junto con el historial de análisis de IA de la
 * persona (JPA). Si la base de datos falla, la página igual se muestra (el historial queda vacío).
 * El simulador en sí funciona con los servlets /api/archivos, /api/predimensionar, /api/presupuesto e /api/ia.
 */
@WebServlet("/simulador")
public class SimuladorServlet extends HttpServlet {

    private final AnalisisService analisisService = new AnalisisService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("analisisRecientes", analisisService.recientes(Sesion.usuarioId(request), 10));
        } catch (RuntimeException e) {
            log("No se pudo leer el historial de análisis (JPA)", e);
            request.setAttribute("analisisRecientes", List.of());
        }
        request.getRequestDispatcher("/WEB-INF/vistas/simulador.jsp").forward(request, response);
    }
}

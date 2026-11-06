package com.mimari.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Página proyecto (/proyecto): no recibe datos; solo entrega la vista WEB-INF/vistas/proyecto.jsp (el acceso lo protege AutenticacionFilter). */
@WebServlet("/proyecto")
public class ProyectoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/vistas/proyecto.jsp").forward(request, response);
    }
}

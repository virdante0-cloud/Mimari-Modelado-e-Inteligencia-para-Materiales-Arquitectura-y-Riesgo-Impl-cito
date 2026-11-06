package com.mimari.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Página practicas (/practicas): no recibe datos; solo entrega la vista WEB-INF/vistas/practicas.jsp (el acceso lo protege AutenticacionFilter). */
@WebServlet("/practicas")
public class PracticasServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/vistas/practicas.jsp").forward(request, response);
    }
}

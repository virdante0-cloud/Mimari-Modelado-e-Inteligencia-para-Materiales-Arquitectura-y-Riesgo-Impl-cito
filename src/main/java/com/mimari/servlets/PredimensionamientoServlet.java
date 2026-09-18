package com.mimari.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class PredimensionamientoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configurar la respuesta como HTML
        response.setContentType("text/html;charset=UTF-8");

        // Supongamos que recibimos la luz libre por parámetro en la URL
        String luzParam = request.getParameter("luz");
        double luz = (luzParam != null && !luzParam.isEmpty()) ? Double.parseDouble(luzParam) : 4.5;

        // Lógica de cálculo estructural
        double peralteViga = luz / 10;
        double grosorPlaca = luz / 25;

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>MIMARI - Predimensionamiento</title></head>");
            out.println("<body>");
            out.println("<h1>Módulo Estructural MIMARI</h1>");
            out.println("<p>Luz libre evaluada: " + luz + " metros</p>");
            out.println("<ul>");
            out.println("<li><strong>Peralte de Viga proyectado (L/10):</strong> " + String.format("%.2f", peralteViga) + " m</li>");
            out.println("<li><strong>Grosor de Placa proyectado (L/25):</strong> " + String.format("%.2f", grosorPlaca) + " m</li>");
            out.println("</ul>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}
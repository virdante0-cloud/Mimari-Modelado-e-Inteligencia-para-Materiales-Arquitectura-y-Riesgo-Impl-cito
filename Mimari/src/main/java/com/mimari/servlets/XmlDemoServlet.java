package com.mimari.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Semana 14 - demostración de las etiquetas XML de JSTL (/xml-demo y /xml-demo.jsp).
 * El servlet entrega un documento XML fijo (nunca texto escrito por el usuario) y la vista xml-demo.jsp lo lee con
 * x:parse y extrae datos con XPath (x:out, x:forEach).
 */
@WebServlet({"/xml-demo", "/xml-demo.jsp"})
public class XmlDemoServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/xml-demo.jsp";

    private static final String XML = """
            <materiales>
              <material>
                <nombre>Cemento gris 50 kg</nombre>
                <uso>Concreto y morteros</uso>
              </material>
              <material>
                <nombre>Varilla corrugada</nombre>
                <uso>Refuerzo de vigas y columnas</uso>
              </material>
              <material>
                <nombre>Ladrillo estructural</nombre>
                <uso>Muros de carga</uso>
              </material>
            </materiales>
            """;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("xml", XML);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}

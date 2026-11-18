package com.mimari.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Semana 11 - listado de insumos desde la base de datos (/productos y /productos.jsp).
 *   1) Revisa la SESIÓN: si no hay "usuario", redirige a login.jsp (la página queda protegida).
 *   2) Consulta la tabla productos con JDBC (PreparedStatement) y entrega los datos a la vista productos.jsp.
 *
 * Siguiendo la regla del curso (sin MVC: toda la lógica en el servlet), la conexión JDBC y la consulta viven aquí;
 * la JSP solo muestra. La clave y el usuario de la base NO van escritos en el código: se leen de variables de entorno
 * (MIMARI_DB_URL, MIMARI_DB_USER, MIMARI_DB_PASS) y, si no existen, se usan los valores de la clase.
 */
@WebServlet({"/productos", "/productos.jsp"})
public class ProductosServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/productos.jsp";
    private static final String VISTA_ERROR = "/error.jsp";
    private static final String SQL = "SELECT id, nombre, precio FROM productos ORDER BY id";
    private static final Locale ES = Locale.forLanguageTag("es-CO");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1) SESIÓN: página protegida
        HttpSession session = request.getSession(false);
        String usuario = (session == null) ? null : (String) session.getAttribute("usuario");
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        response.setHeader("Cache-Control", "no-store");   // que el botón "atrás" no muestre la página tras cerrar sesión

        // 2) JDBC
        List<String[]> filas = new ArrayList<>();
        NumberFormat moneda = NumberFormat.getCurrencyInstance(ES);
        moneda.setMaximumFractionDigits(0);
        try (Connection cn = obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                BigDecimal precio = rs.getBigDecimal("precio");
                filas.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("nombre"),          // se escapa en la vista con c:out (Semana 14)
                        moneda.format(precio)
                });
            }
        } catch (SQLException | ClassNotFoundException e) {
            log("No se pudo consultar la tabla productos", e);   // el detalle va al log, no a la pantalla
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("mensajeError",
                    "No se pudo consultar la base de datos. Verifica que MySQL esté encendido y que la base programacion3 exista.");
            request.setAttribute("tipoError", "ErrorBaseDeDatos");
            request.getRequestDispatcher(VISTA_ERROR).forward(request, response);
            return;
        }

        request.setAttribute("usuario", usuario);
        request.setAttribute("productos", filas);
        request.setAttribute("total", filas.size());
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private static Connection obtenerConexion() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = env("MIMARI_DB_URL", "jdbc:mysql://localhost:3306/programacion3");
        String user = env("MIMARI_DB_USER", "root");
        String pass = env("MIMARI_DB_PASS", "");
        return DriverManager.getConnection(url, user, pass);
    }

    private static String env(String nombre, String porDefecto) {
        String v = System.getenv(nombre);
        return (v == null || v.isEmpty()) ? porDefecto : v;
    }
}

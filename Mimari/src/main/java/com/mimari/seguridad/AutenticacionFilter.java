package com.mimari.seguridad;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Puerta de entrada de MIMARI: nada de la aplicación se ve sin haber iniciado sesión.
 * Solo son públicas la pantalla de acceso (login/registro), su hoja de estilos y la página de errores.
 *   - páginas sin sesión  -> redirige a /login.jsp
 *   - /api/* sin sesión   -> responde 401 con JSON (el JS lo muestra)
 * Además agrega cabeceras de seguridad a todas las respuestas y evita que el navegador guarde en caché las páginas privadas.
 */
@WebFilter("/*")
public class AutenticacionFilter implements Filter {

    private static final Set<String> PUBLICAS = Set.of(
            "/login", "/login.jsp", "/registro", "/acceso.css", "/error", "/error.jsp", "/favicon.ico");

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");

        String ruta = request.getServletPath();
        if (PUBLICAS.contains(ruta)) {
            chain.doFilter(req, res);
            return;
        }

        if (Sesion.usuarioId(request) == null) {
            if (ruta.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.setHeader("Cache-Control", "no-store");
                response.getOutputStream().write(
                        "{\"ok\":false,\"error\":\"Tu sesión terminó. Vuelve a iniciar sesión.\"}"
                                .getBytes(StandardCharsets.UTF_8));
            } else {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            }
            return;
        }

        if (!esEstatico(ruta)) {
            response.setHeader("Cache-Control", "no-store");
        }
        chain.doFilter(req, res);
    }

    private static boolean esEstatico(String ruta) {
        return ruta.endsWith(".css") || ruta.endsWith(".js") || ruta.endsWith(".png")
                || ruta.endsWith(".jpg") || ruta.endsWith(".svg");
    }
}

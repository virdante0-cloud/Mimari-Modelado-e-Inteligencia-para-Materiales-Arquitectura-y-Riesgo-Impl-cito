package com.mimari.servlets;

import com.mimari.modelo.Usuario;
import com.mimari.seguridad.Sesion;
import com.mimari.servicio.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Semana 11 - inicio de sesión (/login y /login.jsp).
 *   GET  -> muestra la pantalla de acceso (WEB-INF/vistas/login.jsp) con la cookie "ultimoUsuario" precargada
 *   POST -> VALIDA, comprueba usuario y clave (UsuarioService, JPA), abre la SESIÓN, crea la COOKIE y redirige al catálogo
 *
 * Los errores de acceso se muestran en la misma pantalla de acceso (no en error.jsp) y nunca dicen si falló el usuario o la
 * clave. Este servlet no escribe HTML: la vista login.jsp solo muestra lo que recibe.
 */
@WebServlet({"/login", "/login.jsp"})
public class LoginServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/login.jsp";
    private static final int CLAVE_MAXIMA = 128;

    private final UsuarioService usuarioService = new UsuarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (Sesion.usuarioId(request) != null) {
            response.sendRedirect(request.getContextPath() + "/catalogo");
            return;
        }
        request.setAttribute("ultimoUsuario", Sesion.ultimoUsuario(request));
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1) VALIDACIÓN (solo aquí)
        String usuario = request.getParameter("usuario");
        usuario = (usuario == null) ? "" : usuario.trim();
        String claveTexto = request.getParameter("clave");
        if (usuario.isEmpty() || claveTexto == null || claveTexto.isEmpty()) {
            rechazar(request, response, HttpServletResponse.SC_BAD_REQUEST, "Escribe tu usuario y tu clave.", usuario);
            return;
        }
        if (!Sesion.USUARIO_VALIDO.matcher(usuario).matches() || claveTexto.length() > CLAVE_MAXIMA) {
            rechazar(request, response, HttpServletResponse.SC_UNAUTHORIZED, "Usuario o clave incorrectos.", "");
            return;
        }

        // 2) AUTENTICACIÓN
        Usuario autenticado;
        try {
            autenticado = usuarioService.autenticar(usuario, claveTexto.toCharArray());
        } catch (RuntimeException e) {
            log("No se pudo autenticar (JPA)", e);
            rechazar(request, response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "No se pudo conectar con la base de datos. Intenta de nuevo en unos minutos.", usuario);
            return;
        }
        if (autenticado == null) {
            rechazar(request, response, HttpServletResponse.SC_UNAUTHORIZED, "Usuario o clave incorrectos.", usuario);
            return;
        }

        // 3) SESIÓN + COOKIE, y POST -> redirect -> GET
        Sesion.iniciar(request, response, autenticado);
        response.sendRedirect(request.getContextPath() + "/catalogo");
    }

    private void rechazar(HttpServletRequest request, HttpServletResponse response, int estado, String mensaje,
                          String ultimoUsuario) throws ServletException, IOException {
        response.setStatus(estado);
        request.setAttribute("mensajeAcceso", mensaje);
        request.setAttribute("ultimoUsuario", Sesion.USUARIO_VALIDO.matcher(ultimoUsuario).matches() ? ultimoUsuario : "");
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}

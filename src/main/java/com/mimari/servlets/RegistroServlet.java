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
 * Registro de cuentas (/registro). Cada persona tiene su propia cuenta y, con ella, su propia información
 * (insumos, archivos y análisis). VALIDA aquí, guarda el usuario con UsuarioService (la clave se guarda con PBKDF2) y
 * abre la sesión. Los errores se muestran en la misma pantalla de acceso.
 */
@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/registro.jsp";
    private static final int USUARIO_MINIMO = 3;
    private static final int CLAVE_MINIMA = 8;
    private static final int CLAVE_MAXIMA = 128;

    private final UsuarioService usuarioService = new UsuarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (Sesion.usuarioId(request) != null) {
            response.sendRedirect(request.getContextPath() + "/catalogo");
            return;
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String usuario = request.getParameter("usuario");
        usuario = (usuario == null) ? "" : usuario.trim();
        String clave = request.getParameter("clave");
        String confirmar = request.getParameter("confirmar");

        String error = null;
        if (usuario.isEmpty() || clave == null || clave.isEmpty()) {
            error = "Escribe un usuario y una clave.";
        } else if (usuario.length() < USUARIO_MINIMO || !Sesion.USUARIO_VALIDO.matcher(usuario).matches()) {
            error = "El usuario debe tener entre 3 y 30 caracteres: letras, números, punto, guion y guion bajo.";
        } else if (clave.length() < CLAVE_MINIMA || clave.length() > CLAVE_MAXIMA) {
            error = "La clave debe tener entre 8 y 128 caracteres.";
        } else if (!clave.equals(confirmar)) {
            error = "La confirmación no coincide con la clave.";
        }
        if (error != null) {
            rechazar(request, response, HttpServletResponse.SC_BAD_REQUEST, error, usuario);
            return;
        }

        Usuario nuevo;
        try {
            nuevo = usuarioService.registrar(usuario, clave.toCharArray());
        } catch (RuntimeException e) {
            log("No se pudo registrar el usuario (JPA)", e);
            rechazar(request, response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "No se pudo conectar con la base de datos. Intenta de nuevo en unos minutos.", usuario);
            return;
        }
        if (nuevo == null) {
            rechazar(request, response, HttpServletResponse.SC_CONFLICT, "Ese usuario ya existe. Elige otro nombre.", usuario);
            return;
        }

        Sesion.iniciar(request, response, nuevo);
        response.sendRedirect(request.getContextPath() + "/catalogo");
    }

    private void rechazar(HttpServletRequest request, HttpServletResponse response, int estado, String mensaje,
                          String usuario) throws ServletException, IOException {
        response.setStatus(estado);
        request.setAttribute("mensajeAcceso", mensaje);
        request.setAttribute("usuarioEscrito", Sesion.USUARIO_VALIDO.matcher(usuario).matches() ? usuario : "");
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}

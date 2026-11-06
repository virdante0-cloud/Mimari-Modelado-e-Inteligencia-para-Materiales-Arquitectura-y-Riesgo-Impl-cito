package com.mimari.seguridad;

import com.mimari.modelo.Usuario;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.regex.Pattern;

/**
 * Semana 11 - todo lo de la sesión y la cookie en un solo lugar.
 *   sesión (HttpSession)  -> "usuario" (nombre) y "usuarioId": quién está conectado (memoria privada del servidor)
 *   cookie ultimoUsuario  -> solo recuerda el nombre para precargarlo en el formulario; NO da acceso
 */
public final class Sesion {

    /** Letras, números, punto, guion y guion bajo. Sirve también como valor de cookie. */
    public static final Pattern USUARIO_VALIDO = Pattern.compile("[\\p{L}\\p{N}._-]{1,30}");
    public static final String COOKIE_ULTIMO_USUARIO = "ultimoUsuario";
    private static final int UNA_SEMANA = 7 * 24 * 60 * 60;
    private static final int INACTIVIDAD_SEGUNDOS = 30 * 60;

    private Sesion() { }

    /** Abre una sesión NUEVA para el usuario (descarta la anterior: evita la fijación de sesión) y crea la cookie. */
    public static void iniciar(HttpServletRequest request, HttpServletResponse response, Usuario usuario) {
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("usuario", usuario.getNombre());
        session.setAttribute("usuarioId", usuario.getId());
        session.setMaxInactiveInterval(INACTIVIDAD_SEGUNDOS);

        Cookie cookie = new Cookie(COOKIE_ULTIMO_USUARIO, usuario.getNombre());
        cookie.setMaxAge(UNA_SEMANA);
        cookie.setHttpOnly(true);
        cookie.setAttribute("SameSite", "Lax");
        String ctx = request.getContextPath();
        cookie.setPath(ctx.isEmpty() ? "/" : ctx);
        response.addCookie(cookie);
    }

    /** @return el id del usuario conectado, o null si no hay sesión iniciada */
    public static Long usuarioId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object id = session.getAttribute("usuarioId");
        return (id instanceof Long) ? (Long) id : null;
    }

    /** Valor de la cookie ultimoUsuario solo si cumple el formato (el cliente puede alterarla). */
    public static String ultimoUsuario(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "";
        }
        for (Cookie c : cookies) {
            if (COOKIE_ULTIMO_USUARIO.equals(c.getName()) && c.getValue() != null
                    && USUARIO_VALIDO.matcher(c.getValue()).matches()) {
                return c.getValue();
            }
        }
        return "";
    }
}

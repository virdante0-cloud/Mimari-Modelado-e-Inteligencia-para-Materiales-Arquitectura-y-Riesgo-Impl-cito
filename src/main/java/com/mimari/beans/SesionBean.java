package com.mimari.beans;

import com.mimari.modelo.Usuario;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.util.Map;

/**
 * Semana 16 - bean CDI que expone a las páginas JSF QUIÉN está conectado. No inicia sesión: eso lo hace LoginServlet
 * (Semana 11); aquí solo se LEEN los datos que Sesion.iniciar dejó en la HttpSession (usuarioId, usuario, rol).
 * Uso en las vistas:  #{sesion.nombre}   #{sesion.admin}
 */
@Named("sesion")
@RequestScoped
public class SesionBean {

    public Long getUsuarioId() {
        Object id = mapa().get("usuarioId");
        return (id instanceof Long) ? (Long) id : null;
    }

    public String getNombre() {
        Object n = mapa().get("usuario");
        return (n == null) ? "" : n.toString();
    }

    public String getRol() {
        Object r = mapa().get("rol");
        return (r == null) ? Usuario.USUARIO : r.toString();
    }

    public boolean isAdmin() {
        return Usuario.ADMIN.equals(getRol());
    }

    private Map<String, Object> mapa() {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
    }
}

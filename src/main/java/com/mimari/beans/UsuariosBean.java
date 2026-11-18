package com.mimari.beans;

import com.mimari.modelo.Usuario;
import com.mimari.seguridad.Sesion;
import com.mimari.servicio.UsuarioService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Semana 16 - bean CDI de la pantalla del administrador (jsf/usuarios.xhtml): lista las cuentas y permite eliminarlas.
 * Capas de seguridad: (1) AutenticacionFilter solo deja abrir la página con rol ADMIN; (2) la vista solo muestra el botón
 * si corresponde; (3) UsuarioService vuelve a comprobar el rol al eliminar. Nunca se confía solo en la pantalla.
 */
@Named("usuariosBean")
@ViewScoped
public class UsuariosBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(UsuariosBean.class.getName());

    @Inject
    private UsuarioService usuarioService;
    @Inject
    private SesionBean sesion;

    private List<Usuario> usuarios = new ArrayList<>();

    @PostConstruct
    public void cargar() {
        if (!sesion.isAdmin()) {
            return;                                   // un usuario normal nunca carga la lista
        }
        try {
            usuarios = usuarioService.listar();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudo listar los usuarios", e);
            Mensajes.error("No se pudo acceder a la base de datos.");
        }
    }

    public void eliminar(Usuario usuario) {
        try {
            usuarioService.eliminar(sesion.getUsuarioId(), usuario.getId());
            Sesion.revocar(usuario.getId());          // si esa persona tenía sesión abierta, el filtro se la cierra
            Mensajes.info("Usuario \"" + usuario.getNombre() + "\" eliminado junto con sus datos.");
        } catch (SecurityException | IllegalArgumentException e) {
            Mensajes.error(e.getMessage());           // mensajes propios del servicio, seguros para mostrar
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudo eliminar el usuario", e);
            Mensajes.error("No se pudo eliminar el usuario.");
        }
        cargar();
    }

    public List<Usuario> getUsuarios() { return usuarios; }
}

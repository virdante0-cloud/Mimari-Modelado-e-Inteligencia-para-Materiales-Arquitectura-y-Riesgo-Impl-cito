package com.mimari.servicio;

import com.mimari.modelo.Archivo;
import com.mimari.modelo.Usuario;
import com.mimari.seguridad.Claves;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Semana 13 - servicio sin estado: registro y autenticación de usuarios (la clave se guarda con PBKDF2).
 * Semana 16 - roles: el primer usuario que se registra con el nombre "admin" queda como ADMIN; los demás son USUARIO.
 * Solo un ADMIN puede eliminar cuentas (se comprueba AQUÍ, en el servicio, no solo en la pantalla).
 *
 * @ApplicationScoped: una sola instancia para toda la aplicación; los beans CDI la reciben con @Inject.
 * Los servlets de las semanas anteriores siguen creándola con "new" y funciona igual.
 */
@ApplicationScoped
public class UsuarioService {

    public static final String NOMBRE_ADMIN = "admin";

    private final ArchivoService archivoService = new ArchivoService();

    /** @return el usuario creado, o null si ya existe ese nombre */
    public Usuario registrar(String nombre, char[] clave) {
        String hash = Claves.hashear(clave);          // se calcula fuera de la transacción (es lento a propósito)
        return Persistencia.enTransaccion(em -> {
            Long existentes = em.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.nombre = :n", Long.class)
                    .setParameter("n", nombre)
                    .getSingleResult();
            if (existentes > 0) {
                return null;
            }
            // Regla de roles: "admin" será el administrador mientras no exista ninguno; todos los demás son USUARIO
            Long administradores = em.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :r", Long.class)
                    .setParameter("r", Usuario.ADMIN)
                    .getSingleResult();
            boolean seraAdmin = administradores == 0 && NOMBRE_ADMIN.equalsIgnoreCase(nombre);
            Usuario nuevo = new Usuario(nombre, hash, seraAdmin ? Usuario.ADMIN : Usuario.USUARIO);
            em.persist(nuevo);
            return nuevo;
        });
    }

    /** @return el usuario si el nombre y la clave son correctos; null en cualquier otro caso (sin distinguir cuál falló) */
    public Usuario autenticar(String nombre, char[] clave) {
        List<Usuario> encontrados = Persistencia.consultar(em -> em
                .createQuery("SELECT u FROM Usuario u WHERE u.nombre = :n", Usuario.class)
                .setParameter("n", nombre)
                .setMaxResults(1)
                .getResultList());
        if (encontrados.isEmpty()) {
            Claves.gastarTiempo(clave);               // mismo tiempo de respuesta exista o no el usuario
            return null;
        }
        Usuario usuario = encontrados.get(0);
        return Claves.verificar(clave, usuario.getClaveHash()) ? usuario : null;
    }

    /** Todas las cuentas, de la más antigua a la más nueva (la usa la pantalla del administrador). */
    public List<Usuario> listar() {
        return Persistencia.consultar(em -> em
                .createQuery("SELECT u FROM Usuario u ORDER BY u.id", Usuario.class)
                .getResultList());
    }

    /**
     * Elimina una cuenta y TODO lo que le pertenece (insumos, análisis, fichas y archivos en disco).
     * Reglas: solo lo puede hacer un ADMIN; nadie se elimina a sí mismo; un ADMIN no se elimina.
     *
     * @throws SecurityException        si quien pide no es administrador
     * @throws IllegalArgumentException si la cuenta no existe o la regla no lo permite (el mensaje se muestra al usuario)
     */
    public void eliminar(Long solicitanteId, Long objetivoId) {
        Usuario solicitante = buscar(solicitanteId);
        if (solicitante == null || !solicitante.isAdmin()) {
            throw new SecurityException("Solo el administrador puede eliminar usuarios.");
        }
        if (solicitanteId.equals(objetivoId)) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta.");
        }
        Usuario objetivo = buscar(objetivoId);
        if (objetivo == null) {
            throw new IllegalArgumentException("Ese usuario ya no existe.");
        }
        if (objetivo.isAdmin()) {
            throw new IllegalArgumentException("No se puede eliminar a un administrador.");
        }

        List<Archivo> archivos = archivoService.listarTodos(objetivoId);   // se leen antes de borrar sus fichas
        Persistencia.enTransaccion(em -> {
            em.createQuery("DELETE FROM Insumo i WHERE i.usuarioId = :u").setParameter("u", objetivoId).executeUpdate();
            em.createQuery("DELETE FROM Analisis a WHERE a.usuarioId = :u").setParameter("u", objetivoId).executeUpdate();
            em.createQuery("DELETE FROM Archivo a WHERE a.usuarioId = :u").setParameter("u", objetivoId).executeUpdate();
            em.createQuery("DELETE FROM Usuario u WHERE u.id = :u").setParameter("u", objetivoId).executeUpdate();
            return null;
        });
        archivoService.borrarDeDisco(objetivoId, archivos);                // el disco se limpia solo si la BD salió bien
    }

    private Usuario buscar(Long id) {
        return Persistencia.consultar(em -> em.find(Usuario.class, id));
    }
}

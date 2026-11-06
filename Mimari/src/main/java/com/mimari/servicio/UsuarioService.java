package com.mimari.servicio;

import com.mimari.modelo.Usuario;
import com.mimari.seguridad.Claves;

import java.util.List;


/** Semana 13 - servicio sin estado: registro y autenticación de usuarios (la clave se guarda con PBKDF2). */
public class UsuarioService {

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
            Usuario nuevo = new Usuario(nombre, hash);
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
}

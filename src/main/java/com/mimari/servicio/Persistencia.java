package com.mimari.servicio;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

import java.util.function.Function;

/**
 * Semana 13 - dueño del EntityManagerFactory de la unidad "catalogoPU".
 * En un servidor Jakarta EE completo el contenedor inyecta el EntityManager (@PersistenceContext) y esta clase no existe;
 * en Tomcat hay que crearlo a mano. Se crea la PRIMERA vez que se usa (no al arrancar), así, si MySQL está apagado,
 * el resto de MIMARI sigue funcionando y solo falla la página de insumos.
 * PersistenciaListener la cierra al detener la aplicación.
 */
public final class Persistencia {

    public static final String UNIDAD = "catalogoPU";
    private static EntityManagerFactory fabrica;

    private Persistencia() { }

    public static synchronized EntityManagerFactory fabrica() {
        if (fabrica == null || !fabrica.isOpen()) {
            fabrica = Persistence.createEntityManagerFactory(UNIDAD);
        }
        return fabrica;
    }

    /**
     * Ejecuta {@code trabajo} dentro de una transacción (begin/commit; si falla, rollback). Es lo que en un servidor
     * Jakarta EE hace el contenedor solo con los EJB. El EntityManager se abre y se cierra en cada llamada (sin estado).
     */
    public static <T> T enTransaccion(Function<EntityManager, T> trabajo) {
        EntityManager em = fabrica().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T resultado = trabajo.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();                           // si algo falla no queda nada a medias
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** Ejecuta una consulta (solo lectura, sin transacción). */
    public static <T> T consultar(Function<EntityManager, T> trabajo) {
        EntityManager em = fabrica().createEntityManager();
        try {
            return trabajo.apply(em);
        } finally {
            em.close();
        }
    }

    public static synchronized void cerrar() {
        if (fabrica != null && fabrica.isOpen()) {
            fabrica.close();
        }
        fabrica = null;
    }
}

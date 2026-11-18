package com.mimari.servicio;

import com.mimari.modelo.Analisis;

import java.util.List;

/** Semana 13 - servicio sin estado: historial de análisis de IA de cada persona. */
public class AnalisisService {

    public Long guardar(Long usuarioId, Long archivoId, double ancho, double largo, int pisos, double alturaPiso,
                        String motor, String resultado) {
        return Persistencia.enTransaccion(em -> {
            Analisis a = new Analisis(usuarioId, archivoId, ancho, largo, pisos, alturaPiso, motor, resultado);
            em.persist(a);
            return a.getId();
        });
    }

    public List<Analisis> recientes(Long usuarioId, int maximo) {
        return Persistencia.consultar(em -> em
                .createQuery("SELECT a FROM Analisis a WHERE a.usuarioId = :u ORDER BY a.id DESC", Analisis.class)
                .setParameter("u", usuarioId)
                .setMaxResults(maximo)
                .getResultList());
    }
}

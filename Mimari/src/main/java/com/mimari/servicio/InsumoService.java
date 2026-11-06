package com.mimari.servicio;

import com.mimari.modelo.Insumo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Semana 13 - servicio de negocio SIN ESTADO (el equivalente a un EJB @Stateless): los insumos de CADA persona.
 *
 * Sin estado = no guarda nada de un visitante entre una llamada y otra: cada método abre su EntityManager, trabaja y lo
 * cierra (Persistencia). Por eso una sola instancia la pueden usar muchos visitantes a la vez. Todas las operaciones
 * reciben el id del usuario: una persona solo puede ver, crear y borrar SUS insumos.
 *
 * Con un servidor Jakarta EE (WildFly/TomEE) esta clase llevaría @Stateless, el servlet la recibiría con @EJB, el
 * EntityManager llegaría con @PersistenceContext(unitName = "catalogoPU") y el contenedor manejaría la transacción.
 * No valida: los datos le llegan ya aprobados por CatalogoServlet.
 */
public class InsumoService {

    public void crear(Long usuarioId, String nombre, String tipo, BigDecimal precio, int cantidad) {
        Persistencia.enTransaccion(em -> {
            em.persist(new Insumo(usuarioId, nombre, tipo, precio, cantidad));   // INSERT
            return null;
        });
    }

    public List<Insumo> listar(Long usuarioId) {
        return Persistencia.consultar(em -> em
                .createQuery("SELECT i FROM Insumo i WHERE i.usuarioId = :u ORDER BY i.id", Insumo.class)   // JPQL
                .setParameter("u", usuarioId)
                .getResultList());
    }

    /** @return true si existía y era de esa persona (el filtro por usuarioId impide borrar insumos ajenos) */
    public boolean eliminar(Long usuarioId, Long id) {
        return Persistencia.enTransaccion(em -> em
                .createQuery("DELETE FROM Insumo i WHERE i.id = :id AND i.usuarioId = :u")
                .setParameter("id", id)
                .setParameter("u", usuarioId)
                .executeUpdate() > 0);
    }
}

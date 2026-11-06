package com.mimari.servicio;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Semana 13 - componente CON ESTADO (el equivalente a un EJB @Stateful): la lista de insumos "por cotizar" que cada
 * visitante va armando antes de decidir. A diferencia de InsumoService, aquí SÍ se recuerda lo que hizo cada persona:
 * hay una instancia por visitante, guardada en su HttpSession (InsumosServlet la crea la primera vez).
 * Es temporal: no toca la base de datos y se pierde al cerrar la sesión.
 *
 * Con un servidor Jakarta EE llevaría @Stateful y el contenedor se encargaría de crear una instancia por cliente.
 * Los métodos son synchronized porque un mismo visitante puede enviar dos solicitudes a la vez.
 */
public class CarritoTemporal implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int MAXIMO = 20;

    private final List<String> nombres = new ArrayList<>();

    /** @return false si el carrito ya está lleno */
    public synchronized boolean agregar(String nombre) {
        if (nombres.size() >= MAXIMO) {
            return false;
        }
        nombres.add(nombre);
        return true;
    }

    public synchronized void vaciar() {
        nombres.clear();
    }

    /** Copia de solo lectura (así la vista no puede alterar el carrito). */
    public synchronized List<String> getNombres() {
        return Collections.unmodifiableList(new ArrayList<>(nombres));
    }
}

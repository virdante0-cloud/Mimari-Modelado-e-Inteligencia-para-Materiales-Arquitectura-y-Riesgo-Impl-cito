package com.mimari.ia;

/** Error al consultar la IA. El mensaje es seguro para mostrarlo a la persona (no incluye claves ni respuestas internas). */
public class IaException extends Exception {

    private static final long serialVersionUID = 1L;

    public IaException(String mensaje) {
        super(mensaje);
    }

    public IaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

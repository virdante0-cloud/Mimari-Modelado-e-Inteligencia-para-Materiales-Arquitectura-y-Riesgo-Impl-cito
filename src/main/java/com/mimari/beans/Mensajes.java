package com.mimari.beans;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/** Semana 15 - atajo para agregar mensajes que luego muestra <h:messages> en la vista. */
public final class Mensajes {

    private Mensajes() { }

    public static void info(String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, texto, null));
    }

    public static void error(String texto) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, texto, null));
    }
}

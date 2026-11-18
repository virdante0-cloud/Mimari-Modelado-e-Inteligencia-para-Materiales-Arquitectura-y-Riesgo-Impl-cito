package com.mimari.servicio;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Semana 13 - libera el EntityManagerFactory (y sus conexiones) cuando la aplicación se detiene. */
@WebListener
public class PersistenciaListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        Persistencia.cerrar();
    }
}

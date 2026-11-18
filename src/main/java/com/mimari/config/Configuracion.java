package com.mimari.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * ÚNICO lugar donde MIMARI lee su configuración (incluida la clave de Gemini).
 * Prioridad: variable de entorno  >  mimari.local.properties  >  mimari.properties (valores por defecto, sin claves).
 *
 *   gemini.api.key  <->  variable GEMINI_API_KEY     (la clave de la API de Gemini)
 *   gemini.modelo   <->  variable GEMINI_MODELO
 *
 * mimari.local.properties se busca en la carpeta de trabajo (donde está el pom.xml con Main) y en TOMCAT_BASE/conf.
 * La clave nunca se escribe en el código ni se envía al navegador.
 */
public final class Configuracion {

    private static final Properties PROPIEDADES = cargar();

    private Configuracion() { }

    /** @return el valor configurado o {@code porDefecto} si no hay (o está vacío) */
    public static String texto(String clave, String porDefecto) {
        String entorno = System.getenv(clave.toUpperCase().replace('.', '_'));
        if (entorno != null && !entorno.isBlank()) {
            return entorno.trim();
        }
        String valor = PROPIEDADES.getProperty(clave);
        return (valor == null || valor.isBlank()) ? porDefecto : valor.trim();
    }

    public static int entero(String clave, int porDefecto) {
        try {
            return Integer.parseInt(texto(clave, String.valueOf(porDefecto)));
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    // ---------------------------------------------------------------- Gemini

    /** La clave de la API de Gemini, o cadena vacía si todavía no se ha configurado. */
    public static String geminiApiKey() {
        // La variable de entorno oficial de Gemini es GEMINI_API_KEY
        String entorno = System.getenv("GEMINI_API_KEY");
        if (entorno != null && !entorno.isBlank()) {
            return entorno.trim();
        }
        String valor = PROPIEDADES.getProperty("gemini.api.key");
        return (valor == null || valor.isBlank() || valor.startsWith("PEGA_AQUI")) ? "" : valor.trim();
    }

    public static String geminiModelo() {
        String entorno = System.getenv("GEMINI_MODELO");
        return (entorno != null && !entorno.isBlank()) ? entorno.trim() : texto("gemini.modelo", "gemini-2.5-flash");
    }

    public static int geminiTimeoutSegundos() {
        return entero("gemini.timeout.segundos", 60);
    }

    // ---------------------------------------------------------------- almacenamiento

    /** Carpeta donde se guardan las fotos y los PDF (fuera de la carpeta publicada). */
    public static Path carpetaAlmacenamiento() {
        String valor = texto("almacenamiento.carpeta", "");
        if (!valor.isEmpty()) {
            return Paths.get(valor);
        }
        String base = System.getProperty("catalina.base", System.getProperty("java.io.tmpdir"));
        return Paths.get(base, "mimari-uploads");
    }

    // ---------------------------------------------------------------- carga

    private static Properties cargar() {
        Properties p = new Properties();
        try (InputStream in = Configuracion.class.getResourceAsStream("/mimari.properties")) {
            if (in != null) {
                p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            // se usan los valores por defecto del código
        }
        leerArchivo(p, Paths.get("mimari.local.properties"));
        String base = System.getProperty("catalina.base");
        if (base != null) {
            leerArchivo(p, Paths.get(base, "conf", "mimari.local.properties"));
        }
        return p;
    }

    private static void leerArchivo(Properties destino, Path archivo) {
        if (Files.isRegularFile(archivo)) {
            try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
                destino.load(lector);
            } catch (IOException e) {
                // archivo ilegible: se ignora
            }
        }
    }
}

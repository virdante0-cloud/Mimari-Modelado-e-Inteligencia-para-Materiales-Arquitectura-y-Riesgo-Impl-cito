package com.mimari.servlets;

import com.google.gson.JsonObject;
import com.mimari.ia.GeminiClient;
import com.mimari.ia.IaException;
import com.mimari.modelo.Archivo;
import com.mimari.seguridad.Sesion;
import com.mimari.servicio.AnalisisService;
import com.mimari.servicio.ArchivoService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * API del análisis con IA (/api/ia), la usa js/ia.js. Es un PASO APARTE del predimensionamiento:
 *   1) la fotografía ya está subida y guardada (/api/archivos)  ->  llega solo su id
 *   2) las dimensiones salen del formulario del simulador
 *   3) este servlet une foto + dimensiones, consulta a Gemini (GeminiClient) y guarda el resultado en el historial de la persona
 *
 *   POST: archivoId, ancho, largo, pisos, alturaPiso  -> JSON {ok, disponible, modelo, analisis{...}}
 * Si todavía no hay clave de Gemini responde {ok:true, disponible:false, mensaje:...} y el resto de la aplicación sigue igual.
 * Toda la validación vive aquí. No genera HTML.
 */
@WebServlet("/api/ia")
public class IaApiServlet extends HttpServlet {

    private final ArchivoService archivoService = new ArchivoService();
    private final AnalisisService analisisService = new AnalisisService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        Long usuarioId = Sesion.usuarioId(request);

        JsonObject cuerpo = new JsonObject();
        try {
            // ---- validación
            Long archivoId = identificador(request.getParameter("archivoId"));
            double ancho = decimal(request.getParameter("ancho"), "ancho", 500);
            double largo = decimal(request.getParameter("largo"), "largo", 500);
            int pisos = entero(request.getParameter("pisos"), "pisos");
            double alturaPiso = decimal(request.getParameter("alturaPiso"), "alturaPiso", 20);

            if (!GeminiClient.disponible()) {
                cuerpo.addProperty("ok", true);
                cuerpo.addProperty("disponible", false);
                cuerpo.addProperty("mensaje", "La IA todavía no está configurada. Coloca la clave de Gemini en "
                        + "mimari.local.properties (gemini.api.key) o en la variable de entorno GEMINI_API_KEY y reinicia.");
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                Archivo foto = archivoService.buscar(usuarioId, archivoId);
                if (foto == null || !Archivo.PLANO.equals(foto.getTipo())) {
                    throw new IllegalArgumentException("La fotografía no existe. Vuelve a cargarla.");
                }
                byte[] imagen = Files.readAllBytes(archivoService.ruta(foto));

                JsonObject analisis = GeminiClient.analizar(imagen, foto.getContentType(), ancho, largo, pisos, alturaPiso);

                String motor = "gemini:" + GeminiClient.modelo();
                Long id = analisisService.guardar(usuarioId, foto.getId(), ancho, largo, pisos, alturaPiso, motor, analisis.toString());
                cuerpo.addProperty("ok", true);
                cuerpo.addProperty("disponible", true);
                cuerpo.addProperty("modelo", GeminiClient.modelo());
                cuerpo.addProperty("analisisId", id);
                cuerpo.add("analisis", analisis);
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            cuerpo = error(e.getMessage());
        } catch (IaException e) {
            log("Error al consultar Gemini", e);
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            cuerpo = error(e.getMessage());
        } catch (IOException | RuntimeException e) {
            log("No se pudo completar el análisis con IA", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            cuerpo = error("No se pudo completar el análisis. Verifica la base de datos y el archivo.");
        }
        response.getOutputStream().write(cuerpo.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static Long identificador(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Primero carga una fotografía del terreno.");
        }
        if (!valor.trim().matches("[0-9]{1,18}")) {
            throw new IllegalArgumentException("Identificador de fotografía no válido.");
        }
        return Long.valueOf(valor.trim());
    }

    private static double decimal(String valor, String campo, double maximo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el campo obligatorio: " + campo + ".");
        }
        String limpio = valor.trim();
        if (limpio.length() > 20 || !limpio.matches("[0-9]+([.,][0-9]+)?")) {
            throw new IllegalArgumentException("El campo " + campo + " no es un número válido.");
        }
        double numero = Double.parseDouble(limpio.replace(',', '.'));
        if (!Double.isFinite(numero) || numero <= 0 || numero > maximo) {
            throw new IllegalArgumentException("El campo " + campo + " debe estar entre 0 y " + (int) maximo + ".");
        }
        return numero;
    }

    private static int entero(String valor, String campo) {
        if (valor == null || !valor.trim().matches("[0-9]{1,9}")) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un número entero.");
        }
        int numero = Integer.parseInt(valor.trim());
        if (numero < 1 || numero > 100) {
            throw new IllegalArgumentException("El número de pisos debe estar entre 1 y 100.");
        }
        return numero;
    }

    private static JsonObject error(String mensaje) {
        JsonObject o = new JsonObject();
        o.addProperty("ok", false);
        o.addProperty("error", mensaje);
        return o;
    }
}

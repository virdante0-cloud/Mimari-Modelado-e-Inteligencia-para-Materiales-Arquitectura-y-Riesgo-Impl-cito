package com.mimari.ia;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mimari.config.Configuracion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * AQUÍ se conecta MIMARI con la IA de Gemini. Es el único lugar que habla con la API.
 *
 * ¿Dónde se pone la clave? En UN solo sitio, que lee Configuracion:
 *    mimari.local.properties   ->   gemini.api.key=TU_CLAVE        (junto al pom.xml; no se sube a git)
 *    o la variable de entorno  GEMINI_API_KEY
 * Sin clave, la aplicación sigue funcionando: el servlet avisa que la IA no está configurada.
 *
 * Qué hace: envía la fotografía del terreno + las dimensiones de la edificación (ancho, largo, pisos, altura) y pide una
 * evaluación preliminar en JSON. La clave viaja en la cabecera x-goog-api-key (no en la URL) y nunca llega al navegador.
 * El resultado es orientativo: no reemplaza un estudio de suelos ni el criterio de un ingeniero.
 */
public final class GeminiClient {

    private static final String BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final Pattern MODELO_VALIDO = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    private static final String INSTRUCCIONES = """
            Eres un asistente de ingeniería civil que hace una revisión PRELIMINAR a partir de una fotografía de un lote o terreno \
            y de las dimensiones de la edificación que se quiere construir. Responde en español.
            Dimensiones: ancho %s m, largo %s m, %d piso(s), altura por piso %s m.
            Analiza lo que se ve en la fotografía (pendiente o talud, tipo aparente de suelo, vegetación, agua o humedad, \
            muros o construcciones vecinas, accesos) y relaciónalo con las dimensiones dadas.
            Devuelve SOLO un objeto JSON con exactamente estas claves:
            {
              "resumen": "texto de 2 a 4 frases sobre el terreno y la viabilidad preliminar",
              "pendienteEstimada": "texto corto, p. ej. 'moderada, entre 10 y 20 grados'",
              "riesgos": [ { "nivel": "alto | medio | bajo", "descripcion": "texto corto" } ],
              "recomendaciones": [ "texto corto" ]
            }
            Si la imagen no muestra un terreno o una obra, dilo en "resumen" y deja las listas vacías. \
            No inventes datos que no se vean. Indica que se requiere un estudio de suelos y la revisión de un ingeniero.""";

    private GeminiClient() { }

    /** @return true si ya hay una clave de Gemini configurada */
    public static boolean disponible() {
        return !Configuracion.geminiApiKey().isEmpty();
    }

    public static String modelo() {
        return Configuracion.geminiModelo();
    }

    /**
     * @param imagen    bytes de la fotografía (jpeg, png, webp, heic o heif)
     * @param mimeType  tipo de la imagen (ya detectado por el servidor, no el que dijo el navegador)
     * @return el análisis como JSON: resumen, pendienteEstimada, riesgos[], recomendaciones[]
     */
    public static JsonObject analizar(byte[] imagen, String mimeType, double ancho, double largo, int pisos,
                                      double alturaPiso) throws IaException {
        String clave = Configuracion.geminiApiKey();
        if (clave.isEmpty()) {
            throw new IaException("La IA no está configurada.");
        }
        String modelo = Configuracion.geminiModelo();
        if (!MODELO_VALIDO.matcher(modelo).matches()) {
            throw new IaException("El nombre del modelo de Gemini configurado no es válido.");
        }

        String instrucciones = String.format(Locale.ROOT, INSTRUCCIONES,
                numero(ancho), numero(largo), pisos, numero(alturaPiso));

        JsonObject textoParte = new JsonObject();
        textoParte.addProperty("text", instrucciones);
        JsonObject datosImagen = new JsonObject();
        datosImagen.addProperty("mime_type", mimeType);
        datosImagen.addProperty("data", Base64.getEncoder().encodeToString(imagen));
        JsonObject imagenParte = new JsonObject();
        imagenParte.add("inline_data", datosImagen);
        JsonArray partes = new JsonArray();
        partes.add(textoParte);
        partes.add(imagenParte);
        JsonObject contenido = new JsonObject();
        contenido.add("parts", partes);
        JsonArray contenidos = new JsonArray();
        contenidos.add(contenido);
        JsonObject configuracion = new JsonObject();
        configuracion.addProperty("responseMimeType", "application/json");
        configuracion.addProperty("temperature", 0.3);
        JsonObject cuerpo = new JsonObject();
        cuerpo.add("contents", contenidos);
        cuerpo.add("generationConfig", configuracion);

        HttpRequest solicitud = HttpRequest.newBuilder(URI.create(BASE + modelo + ":generateContent"))
                .timeout(Duration.ofSeconds(Configuracion.geminiTimeoutSegundos()))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", clave)
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> respuesta;
        try {
            respuesta = HTTP.send(solicitud, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IaException("La consulta a la IA fue interrumpida.", e);
        } catch (IOException e) {
            throw new IaException("No se pudo contactar a la IA (sin conexión o tiempo agotado).", e);
        }

        int estado = respuesta.statusCode();
        if (estado == 400 || estado == 403) {
            throw new IaException("Gemini rechazó la solicitud (HTTP " + estado + "). Revisa que la clave sea válida y que el modelo exista.");
        }
        if (estado == 429) {
            throw new IaException("Se alcanzó el límite de uso de Gemini. Intenta de nuevo en un momento.");
        }
        if (estado != 200) {
            throw new IaException("Gemini respondió con un error (HTTP " + estado + ").");
        }
        return interpretar(respuesta.body());
    }

    /** Saca el texto de la respuesta de Gemini y lo convierte en el JSON del análisis. */
    static JsonObject interpretar(String cuerpo) throws IaException {
        try {
            JsonObject raiz = JsonParser.parseString(cuerpo).getAsJsonObject();
            JsonArray candidatos = raiz.getAsJsonArray("candidates");
            if (candidatos == null || candidatos.isEmpty()) {
                throw new IaException("Gemini no devolvió contenido (la imagen pudo ser bloqueada por sus filtros).");
            }
            JsonArray partes = candidatos.get(0).getAsJsonObject().getAsJsonObject("content").getAsJsonArray("parts");
            StringBuilder texto = new StringBuilder();
            for (JsonElement parte : partes) {
                JsonElement t = parte.getAsJsonObject().get("text");
                if (t != null) {
                    texto.append(t.getAsString());
                }
            }
            String limpio = texto.toString().strip();
            if (limpio.startsWith("```")) {                      // a veces la respuesta llega dentro de un bloque ```json
                limpio = limpio.replaceFirst("^```[a-zA-Z]*\\s*", "").replaceFirst("\\s*```$", "");
            }
            JsonElement analisis = JsonParser.parseString(limpio);
            if (analisis.isJsonObject()) {
                return analisis.getAsJsonObject();
            }
            JsonObject envuelto = new JsonObject();
            envuelto.addProperty("resumen", limpio);
            return envuelto;
        } catch (IaException e) {
            throw e;
        } catch (RuntimeException e) {                           // JSON inválido o estructura inesperada
            throw new IaException("No se pudo interpretar la respuesta de Gemini.", e);
        }
    }

    private static String numero(double valor) {
        return String.format(Locale.ROOT, "%.2f", valor);
    }
}

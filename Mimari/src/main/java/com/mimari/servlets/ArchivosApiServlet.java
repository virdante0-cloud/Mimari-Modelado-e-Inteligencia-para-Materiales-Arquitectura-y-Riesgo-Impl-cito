package com.mimari.servlets;

import com.google.gson.JsonObject;
import com.mimari.modelo.Archivo;
import com.mimari.seguridad.Sesion;
import com.mimari.servicio.ArchivoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

/**
 * API de archivos de cada persona (/api/archivos). Aquí se SUBE la fotografía del terreno, de forma independiente de
 * cualquier cálculo: primero se carga y se guarda; después, otros pasos (predimensionamiento, IA) la usan por su id.
 *   POST multipart  archivo=<foto>  [tipo=plano]        -> JSON {ok, id, nombre, tamano, url}
 *   POST            accion=eliminar & id=N               -> JSON {ok}
 *   GET  ?id=N [&descargar=1]                            -> devuelve el archivo (solo si es de quien lo pide)
 * Valida la extensión Y el contenido real (cabecera del archivo). Los bytes se guardan fuera de la carpeta publicada.
 */
@WebServlet("/api/archivos")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 15L * 1024 * 1024, maxRequestSize = 16L * 1024 * 1024)
public class ArchivosApiServlet extends HttpServlet {

    private static final long FOTO_MAXIMA = 8L * 1024 * 1024;
    private static final Set<String> EXTENSIONES_FOTO = Set.of("jpg", "jpeg", "png", "webp", "heic", "heif");

    private final ArchivoService archivoService = new ArchivoService();

    // ---------------------------------------------------------------- subir / eliminar

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        Long usuarioId = Sesion.usuarioId(request);

        JsonObject cuerpo = new JsonObject();
        try {
            if ("eliminar".equals(request.getParameter("accion"))) {
                boolean borrado = archivoService.eliminar(usuarioId, identificador(request.getParameter("id")), Archivo.PLANO);
                if (!borrado) {
                    throw new IllegalArgumentException("El archivo no existe.");
                }
                cuerpo.addProperty("ok", true);
            } else {
                Archivo guardado = subirFoto(request, usuarioId);
                cuerpo.addProperty("ok", true);
                cuerpo.addProperty("id", guardado.getId());
                cuerpo.addProperty("nombre", guardado.getNombreOriginal());
                cuerpo.addProperty("tamano", guardado.getTamano());
                cuerpo.addProperty("tipo", guardado.getContentType());
                cuerpo.addProperty("url", request.getContextPath() + "/api/archivos?id=" + guardado.getId());
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            cuerpo = error(e.getMessage());
        } catch (IllegalStateException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            cuerpo = error("El archivo supera el tamaño permitido (8 MB).");
        } catch (RuntimeException e) {
            log("No se pudo guardar el archivo (JPA/disco)", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            cuerpo = error("No se pudo guardar el archivo. Verifica la base de datos.");
        }
        response.getOutputStream().write(cuerpo.toString().getBytes(StandardCharsets.UTF_8));
    }

    private Archivo subirFoto(HttpServletRequest request, Long usuarioId) throws IOException, ServletException {
        String tipo = request.getParameter("tipo");
        if (tipo != null && !tipo.isBlank() && !Archivo.PLANO.equals(tipo)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido en esta ruta.");
        }
        Part parte = request.getPart("archivo");
        if (parte == null || parte.getSize() <= 0) {
            throw new IllegalArgumentException("Selecciona una fotografía.");
        }
        if (parte.getSize() > FOTO_MAXIMA) {
            throw new IllegalArgumentException("La fotografía supera el tamaño permitido (8 MB).");
        }
        String enviado = parte.getSubmittedFileName();
        String nombre = (enviado == null || enviado.isBlank()) ? "foto" : Paths.get(enviado).getFileName().toString();
        int punto = nombre.lastIndexOf('.');
        String extension = (punto < 0) ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);
        if (!EXTENSIONES_FOTO.contains(extension)) {
            throw new IllegalArgumentException("Solo se permiten imágenes (jpg, png, webp, heic).");
        }
        byte[] datos;
        try (InputStream in = parte.getInputStream()) {
            datos = in.readNBytes((int) FOTO_MAXIMA + 1);
        }
        String mime = detectarImagen(datos);               // el tipo real sale del contenido, no de lo que diga el navegador
        if (mime == null) {
            throw new IllegalArgumentException("El archivo no es una imagen válida.");
        }
        String extensionReal = switch (mime) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "heic";
        };
        String limpio = nombre.replaceAll("[^\\p{L}\\p{N} ._()-]", "_");
        if (limpio.length() > 150) {
            limpio = limpio.substring(limpio.length() - 150);
        }
        return archivoService.guardar(usuarioId, Archivo.PLANO, limpio, mime, extensionReal, datos);
    }

    /** @return el tipo MIME según los primeros bytes, o null si no es una imagen permitida */
    static String detectarImagen(byte[] d) {
        if (d.length >= 3 && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (d.length >= 8 && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') {
            return "image/png";
        }
        if (d.length >= 12 && d[0] == 'R' && d[1] == 'I' && d[2] == 'F' && d[3] == 'F'
                && d[8] == 'W' && d[9] == 'E' && d[10] == 'B' && d[11] == 'P') {
            return "image/webp";
        }
        if (d.length >= 12 && d[4] == 'f' && d[5] == 't' && d[6] == 'y' && d[7] == 'p') {   // contenedor ISO-BMFF (heic/heif)
            String marca = new String(d, 8, 4, StandardCharsets.ISO_8859_1);
            if (marca.startsWith("hei") || marca.startsWith("mif") || marca.equals("msf1")) {
                return "image/heic";
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- descargar / ver

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long usuarioId = Sesion.usuarioId(request);
        Archivo archivo;
        try {
            archivo = archivoService.buscar(usuarioId, identificador(request.getParameter("id")));
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (RuntimeException e) {
            log("No se pudo leer el archivo (JPA)", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        if (archivo == null || !Files.isRegularFile(archivoService.ruta(archivo))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);         // también si es de otra persona: no se revela que existe
            return;
        }
        boolean descargar = "1".equals(request.getParameter("descargar"));
        String nombreSeguro = archivo.getNombreOriginal().replaceAll("[^\\p{L}\\p{N} ._()-]", "_");
        response.setContentType(archivo.getContentType());
        response.setContentLengthLong(archivo.getTamano());
        response.setHeader("Cache-Control", "private, no-store");
        response.setHeader("Content-Security-Policy", "default-src 'none'; img-src 'self' data:; style-src 'unsafe-inline'; sandbox");
        response.setHeader("Content-Disposition", (descargar ? "attachment" : "inline")
                + "; filename=\"" + nombreSeguro + "\"");
        try (OutputStream out = response.getOutputStream()) {
            Files.copy(archivoService.ruta(archivo), out);
        }
    }

    private static Long identificador(String valor) {
        if (valor == null || !valor.trim().matches("[0-9]{1,18}")) {
            throw new IllegalArgumentException("Identificador no válido.");
        }
        return Long.valueOf(valor.trim());
    }

    private static JsonObject error(String mensaje) {
        JsonObject o = new JsonObject();
        o.addProperty("ok", false);
        o.addProperty("error", mensaje);
        return o;
    }
}

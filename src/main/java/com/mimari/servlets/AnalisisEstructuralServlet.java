package com.mimari.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Formulario clásico de predimensionamiento (/analisis-estructural).
 *   GET  -> muestra el formulario (vista formulario.jsp)
 *   POST -> VALIDA, calcula, guarda el plano y entrega los datos a resultado.jsp
 *
 * Este servlet NO escribe HTML: solo valida/calcula y hace forward a una JSP.
 * Es independiente de los demás servlets: si otro falla, este sigue funcionando.
 */
@WebServlet("/analisis-estructural")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 10
)
public class AnalisisEstructuralServlet extends HttpServlet {

    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/formulario.jsp";
    private static final String VISTA_RESULTADO = "/WEB-INF/vistas/resultado.jsp";
    private static final String VISTA_ERROR = "/error.jsp";

    private static final double ALTURA_MINIMA_PISO = 2.20;
    private static final double ESBELTEZ_MAXIMA = 4.0;
    private static final Set<String> EXTENSIONES_PERMITIDAS =
            Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif");
    private static final Locale ES = Locale.forLanguageTag("es-CO");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            // 1) VALIDACIÓN (todo aquí, nunca en HTML/JSP)
            double ancho = decimal(request.getParameter("ancho"), "ancho");
            double largo = decimal(request.getParameter("largo"), "largo");
            int pisos = entero(request.getParameter("pisos"), "pisos");
            double alturaPiso = decimal(request.getParameter("alturaPiso"), "alturaPiso");

            // 2) CÁLCULO
            validarRangos(ancho, largo, pisos, alturaPiso);
            List<String[]> alertas = new ArrayList<>();
            double alturaTotal = pisos * alturaPiso;
            double areaConstruida = ancho * largo * pisos;
            double luz = Math.max(ancho, largo);
            double peralteViga = luz / 10.0;
            double espesorPlaca = luz / 25.0;
            double esbeltez = alturaTotal / Math.min(ancho, largo);

            if (alturaPiso < ALTURA_MINIMA_PISO) {
                alertas.add(new String[]{"peligro", String.format(Locale.ROOT,
                        "ALERTA CRÍTICA: La altura por piso (%.2f m) viola el mínimo normativo de habitabilidad (%.2f m).",
                        alturaPiso, ALTURA_MINIMA_PISO)});
            } else {
                alertas.add(new String[]{"exito", "Altura por piso conforme a la normativa de confort."});
            }
            if (esbeltez > ESBELTEZ_MAXIMA) {
                alertas.add(new String[]{"peligro", String.format(Locale.ROOT,
                        "ALERTA SÍSMICA: Relación de esbeltez elevada (%.2f). Alto riesgo de volcamiento.", esbeltez)});
            }

            // 3) PLANO ADJUNTO (opcional)
            String mensajeArchivo = guardarPlano(request);

            // 4) Se entregan los datos ya listos a la vista (la JSP solo los muestra)
            request.setAttribute("alertas", alertas);
            request.setAttribute("mensajeArchivo", mensajeArchivo);
            request.setAttribute("alturaTotal", String.format(ES, "%.2f", alturaTotal) + " m");
            request.setAttribute("areaConstruida", String.format(ES, "%.2f", areaConstruida) + " m²");
            request.setAttribute("peralteViga", String.format(ES, "%.2f", peralteViga * 100) + " cm");
            request.setAttribute("espesorPlaca", String.format(ES, "%.2f", espesorPlaca * 100) + " cm");
            request.getRequestDispatcher(VISTA_RESULTADO).forward(request, response);

        } catch (IllegalArgumentException e) {
            enviarError(request, response, e.getMessage(), e.getClass().getSimpleName());
        } catch (IllegalStateException e) {
            // Tomcat lo lanza si el archivo/solicitud supera los límites de @MultipartConfig
            enviarError(request, response, "El archivo adjunto supera el tamaño permitido (5 MB).", "ArchivoDemasiadoGrande");
        }
    }

    private static void enviarError(HttpServletRequest request, HttpServletResponse response,
                                    String mensaje, String tipo) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute("mensajeError", mensaje);
        request.setAttribute("tipoError", tipo);
        request.getRequestDispatcher(VISTA_ERROR).forward(request, response);
    }

    // ---------------------------------------------------------------- validación

    private static void validarRangos(double ancho, double largo, int pisos, double alturaPiso) {
        if (!Double.isFinite(ancho) || ancho <= 0 || ancho > 500) {
            throw new IllegalArgumentException("El ancho debe estar entre 0 y 500 m.");
        }
        if (!Double.isFinite(largo) || largo <= 0 || largo > 500) {
            throw new IllegalArgumentException("El largo debe estar entre 0 y 500 m.");
        }
        if (pisos < 1 || pisos > 100) {
            throw new IllegalArgumentException("El número de pisos debe estar entre 1 y 100.");
        }
        if (!Double.isFinite(alturaPiso) || alturaPiso <= 0 || alturaPiso > 20) {
            throw new IllegalArgumentException("La altura por piso debe estar entre 0 y 20 m.");
        }
    }

    /** Acepta "10.5" y "10,5". Solo dígitos, un separador decimal y signo opcional. */
    private static double decimal(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el campo obligatorio: " + campo + ".");
        }
        String limpio = valor.trim();
        if (limpio.length() > 20 || !limpio.matches("-?[0-9]+([.,][0-9]+)?")) {
            throw new IllegalArgumentException("El campo " + campo + " no es un número válido.");
        }
        return Double.parseDouble(limpio.replace(',', '.'));
    }

    private static int entero(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el campo obligatorio: " + campo + ".");
        }
        String limpio = valor.trim();
        if (!limpio.matches("-?[0-9]{1,9}")) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un número entero.");
        }
        return Integer.parseInt(limpio);
    }

    // ---------------------------------------------------------------- plano adjunto
    // Se guarda FUERA de la carpeta publicada, con nombre aleatorio y solo si la extensión es de imagen.

    private static String guardarPlano(HttpServletRequest request) {
        try {
            Part parte = request.getPart("planoImagen");
            if (parte == null || parte.getSize() <= 0) {
                return "Ningún archivo adjuntado.";
            }
            String enviado = parte.getSubmittedFileName();
            String nombre = (enviado == null) ? "plano" : Paths.get(enviado).getFileName().toString();
            int punto = nombre.lastIndexOf('.');
            String extension = (punto < 0) ? "" : nombre.substring(punto + 1).toLowerCase(Locale.ROOT);

            if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
                return "Archivo rechazado: solo se permiten imágenes (jpg, png, gif, webp, bmp, heic).";
            }
            String base = System.getProperty("catalina.base", System.getProperty("java.io.tmpdir"));
            Path carpeta = Paths.get(base, "mimari-uploads");
            Files.createDirectories(carpeta);
            Path destino = carpeta.resolve(UUID.randomUUID() + "." + extension);
            try (InputStream in = parte.getInputStream()) {
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            return "Archivo guardado exitosamente.";
        } catch (Exception e) {
            return "Error al procesar el archivo adjunto.";
        }
    }
}

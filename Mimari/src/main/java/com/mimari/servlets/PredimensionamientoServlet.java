package com.mimari.servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Servlet ÚNICO de MIMARI: toda la lógica vive aquí (sin capas modelo/util ni patrón MVC).
 * Las páginas JSP solo muestran contenido; los cálculos y validaciones los hace este servlet.
 *
 *   /analisis-estructural   GET  -> formulario HTML
 *                           POST -> resultados HTML (predimensionamiento + plano)
 *   /api/predimensionar     GET|POST -> mismo cálculo, respuesta JSON (lo consume index.html)
 *   /consulta               GET|POST -> consulta por parámetros (categoria, luz) - Semana 10
 *
 * Fórmulas:
 *   luz (L) = max(ancho, largo)      peralte de viga = L / 10      base de viga = peralte / 2
 *   espesor de placa = L / 25        volumen de concreto = área construida total x espesor
 *   esbeltez = altura total / lado menor   (alerta si > 4)
 */
@WebServlet({"/analisis-estructural", "/api/predimensionar", "/consulta"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class PredimensionamientoServlet extends HttpServlet {

    private static final double ALTURA_MINIMA_PISO = 2.20;
    private static final double ESBELTEZ_MAXIMA = 4.0;
    private static final Set<String> EXTENSIONES_PERMITIDAS =
            Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif");
    private static final Locale ES = Locale.forLanguageTag("es-CO");

    private static final String ESTILOS_BASE = """
            * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Inter', sans-serif; }
            body { background: #f8fafc; color: #0f172a; padding: 40px 20px; display: flex; justify-content: center; }
            .container { width: 100%; max-width: 900px; background: #ffffff; padding: 40px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; }
            h1 { font-size: 26px; font-weight: 700; color: #0f172a; margin-bottom: 24px; }
            """;

    private static final String ESTILOS_FORM = """
            p.subtitle { color: #64748b; font-size: 15px; margin-bottom: 32px; }
            .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 24px; }
            .form-group { display: flex; flex-direction: column; }
            .form-group label { font-weight: 600; font-size: 14px; color: #334155; margin-bottom: 8px; }
            .form-group input { padding: 12px 16px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 15px; outline: none; transition: border-color 0.2s; }
            .form-group input:focus { border-color: #0284c7; }
            .full-width { grid-column: span 2; }
            button { background: #0284c7; color: white; border: none; padding: 14px 24px; font-size: 16px; font-weight: 600; border-radius: 8px; cursor: pointer; width: 100%; transition: background 0.2s; }
            button:hover { background: #0369a1; }
            """;

    private static final String ESTILOS_RESULTADO = """
            .results-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; }
            .card { background: #f8fafc; padding: 20px; border-radius: 12px; border: 1px solid #e2e8f0; }
            .card h3 { font-size: 14px; color: #64748b; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; }
            .card p { font-size: 22px; font-weight: 700; color: #0f172a; }
            .alert-danger { background: #fef2f2; border-left: 6px solid #ef4444; padding: 16px; border-radius: 8px; color: #991b1b; font-weight: 500; margin-bottom: 16px; }
            .alert-success { background: #f0fdf4; border-left: 6px solid #22c55e; padding: 16px; border-radius: 8px; color: #166534; font-weight: 500; margin-bottom: 16px; }
            .file-info { font-size: 14px; color: #475569; background: #f1f5f9; padding: 12px; border-radius: 8px; margin-bottom: 24px; }
            .btn-back { display: inline-block; background: #0f172a; color: white; text-decoration: none; padding: 12px 20px; border-radius: 8px; font-weight: 600; font-size: 14px; }
            .dato { background: #eff6ff; border-left: 4px solid #2563eb; padding: 12px 16px; border-radius: 8px; margin: 10px 0; font-size: 15px; }
            .nota { font-size: 13px; color: #64748b; margin: 14px 0; }
            code { background: #f1f5f9; padding: 2px 6px; border-radius: 4px; font-size: 13px; }
            nav.menu { display: flex; gap: 18px; flex-wrap: wrap; font-size: 14px; font-weight: 600; margin-bottom: 24px; }
            nav.menu a { color: #0284c7; text-decoration: none; }
            """;

    // ======================================================================
    //  ENRUTADO: cada URL del @WebServlet llama a su método
    // ======================================================================
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/api/predimensionar" -> responderJson(request, response, false);
            case "/consulta" -> consulta(request, response);
            default -> mostrarFormulario(response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/api/predimensionar" -> responderJson(request, response, true);
            case "/consulta" -> consulta(request, response);
            default -> mostrarResultadoHtml(request, response);
        }
    }

    // ======================================================================
    //  /analisis-estructural  (páginas HTML generadas por el servlet)
    // ======================================================================
    private void mostrarFormulario(HttpServletResponse response) throws IOException {
        String cuerpo = """
                <div class="container">
                <h1 style="font-size:28px;margin-bottom:8px;">MIMARI - Módulo de Registro y Análisis</h1>
                <p class="subtitle">Ingrese los parámetros geométricos del proyecto. Envío seguro por método POST.</p>
                <form action="analisis-estructural" method="POST" enctype="multipart/form-data">
                <div class="form-grid">
                <div class="form-group"><label>Ancho del Edificio (m)</label>
                <input type="number" step="0.1" name="ancho" required placeholder="Ej. 10.5"></div>
                <div class="form-group"><label>Largo del Edificio (m)</label>
                <input type="number" step="0.1" name="largo" required placeholder="Ej. 15.0"></div>
                <div class="form-group"><label>Número de Pisos</label>
                <input type="number" name="pisos" required placeholder="Ej. 4"></div>
                <div class="form-group"><label>Altura Promedio por Piso (m)</label>
                <input type="number" step="0.1" name="alturaPiso" required placeholder="Ej. 2.6"></div>
                <div class="form-group full-width"><label>Adjuntar Plano Arquitectónico (Imagen)</label>
                <input type="file" name="planoImagen" accept="image/*"></div>
                </div>
                <button type="submit"><i class="fa-solid fa-calculator"></i> Procesar Cálculo Estructural</button>
                </form>
                <p style="margin-top:20px;font-size:14px;"><a href="index.html">&larr; Volver al simulador</a></p>
                </div>
                """;
        escribirPagina(response, "MIMARI - Predimensionamiento y Estructuras", ESTILOS_BASE + ESTILOS_FORM, cuerpo);
    }

    private void mostrarResultadoHtml(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        List<String[]> alertas = new ArrayList<>();
        Map<String, Double> r;
        try {
            double ancho = decimal(request.getParameter("ancho"), "ancho");
            double largo = decimal(request.getParameter("largo"), "largo");
            int pisos = entero(request.getParameter("pisos"), "pisos");
            double alturaPiso = decimal(request.getParameter("alturaPiso"), "alturaPiso");
            r = calcular(ancho, largo, pisos, alturaPiso, alertas);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String cuerpo = "<div class=\"container\"><h1>Datos no válidos</h1>"
                    + "<div class=\"alert-danger\">" + escaparHtml(e.getMessage()) + "</div>"
                    + "<a href=\"analisis-estructural\" class=\"btn-back\">Volver al formulario</a></div>";
            escribirPagina(response, "Datos no válidos - MIMARI", ESTILOS_BASE + ESTILOS_RESULTADO, cuerpo);
            return;
        }

        String mensajeArchivo = leerPlano(request);

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"container\">");
        sb.append("<h1><i class=\"fa-solid fa-square-poll-vertical\"></i> Resultados de Predimensionamiento Estructural</h1>");

        for (String[] a : alertas) {
            boolean peligro = "peligro".equals(a[0]);
            sb.append(peligro
                    ? "<div class=\"alert-danger\"><i class=\"fa-solid fa-triangle-exclamation\"></i> "
                    : "<div class=\"alert-success\"><i class=\"fa-solid fa-circle-check\"></i> ")
              .append(escaparHtml(a[1])).append("</div>");
        }

        sb.append("<div class=\"file-info\"><i class=\"fa-solid fa-file-image\"></i> ")
          .append(escaparHtml(mensajeArchivo)).append("</div>");

        sb.append("<div class=\"results-grid\">");
        tarjeta(sb, "Altura Total del Edificio", String.format(ES, "%.2f", r.get("alturaTotal")) + " m");
        tarjeta(sb, "Área Construida Total", String.format(ES, "%.2f", r.get("areaTotalConstruida")) + " m²");
        tarjeta(sb, "Peralte Sugerido para Viga", String.format(ES, "%.2f", r.get("peralteViga") * 100) + " cm");
        tarjeta(sb, "Espesor Mínimo de Placa", String.format(ES, "%.2f", r.get("espesorPlaca") * 100) + " cm");
        sb.append("</div>");

        sb.append("<a href=\"analisis-estructural\" class=\"btn-back\"><i class=\"fa-solid fa-arrow-left\"></i> Realizar Nuevo Cálculo</a>");
        sb.append("</div>");

        escribirPagina(response, "Resultados del Análisis - MIMARI", ESTILOS_BASE + ESTILOS_RESULTADO, sb.toString());
    }

    // ======================================================================
    //  /api/predimensionar  (JSON para el simulador de index.html)
    //  POST (FormData): ancho, largo, pisos, alturaPiso, planoImagen[opcional]
    //  GET  ?ancho=6&largo=8&pisos=2&alturaPiso=2.6   (para probar en el navegador)
    // ======================================================================
    private void responderJson(HttpServletRequest request, HttpServletResponse response, boolean conArchivo)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        String cuerpo;
        try {
            double ancho = decimal(request.getParameter("ancho"), "ancho");
            double largo = decimal(request.getParameter("largo"), "largo");
            int pisos = entero(request.getParameter("pisos"), "pisos");
            double alturaPiso = decimal(request.getParameter("alturaPiso"), "alturaPiso");

            List<String[]> alertas = new ArrayList<>();
            Map<String, Double> r = calcular(ancho, largo, pisos, alturaPiso, alertas);
            String mensajeArchivo = conArchivo ? leerPlano(request) : "Ningún archivo adjuntado.";

            StringBuilder sb = new StringBuilder(512).append("{\"ok\":true");
            for (Map.Entry<String, Double> e : r.entrySet()) {
                sb.append(",\"").append(e.getKey()).append("\":");
                if (e.getKey().equals("pisos")) {
                    sb.append(e.getValue().longValue());
                } else {
                    sb.append(String.format(Locale.ROOT, "%.4f", e.getValue()));   // siempre con punto decimal
                }
            }
            sb.append(",\"archivo\":\"").append(escaparJson(mensajeArchivo)).append('"');
            sb.append(",\"alertas\":[");
            for (int i = 0; i < alertas.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append("{\"nivel\":\"").append(escaparJson(alertas.get(i)[0]))
                  .append("\",\"mensaje\":\"").append(escaparJson(alertas.get(i)[1])).append("\"}");
            }
            sb.append("]}");
            response.setStatus(HttpServletResponse.SC_OK);
            cuerpo = sb.toString();
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            cuerpo = "{\"ok\":false,\"error\":\"" + escaparJson(e.getMessage()) + "\"}";
        }

        try (PrintWriter out = response.getWriter()) {
            out.print(cuerpo);
        }
    }

    // ======================================================================
    //  /consulta  (Semana 10: request, response, out, application y manejo de errores)
    //  Parámetros: categoria (viga | placa | general), luz (metros)
    //  La página consulta.jsp solo trae el formulario; este servlet hace todo el proceso.
    // ======================================================================
    private void consulta(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // request: lee los datos que llegan por la URL o el formulario
        String categoria = request.getParameter("categoria");
        if (categoria == null || categoria.isBlank()) {
            categoria = "general";                       // sin dato -> valor por defecto
        }
        categoria = categoria.trim().toLowerCase(Locale.ROOT);
        if (categoria.length() > 40) {
            categoria = categoria.substring(0, 40);      // evita guardar textos enormes en memoria compartida
        }

        double luz = 4.0;
        String luzTexto = request.getParameter("luz");
        boolean luzPorDefecto = (luzTexto == null || luzTexto.isBlank());
        try {
            if (!luzPorDefecto) {
                luz = decimal(luzTexto, "luz");
                if (luz <= 0 || luz > 100) {
                    throw new IllegalArgumentException("La luz debe estar entre 0 y 100 m.");
                }
            }
        } catch (IllegalArgumentException e) {
            // Error controlado: el servidor entrega el control a error.jsp
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("mensajeError", e.getMessage());
            request.setAttribute("tipoError", e.getClass().getSimpleName());
            request.getRequestDispatcher("/error.jsp").forward(request, response);
            return;
        }

        // application (ServletContext): memoria compartida por toda la aplicación y todos los visitantes.
        // Solo se guardan datos no privados; synchronized porque varios visitantes pueden entrar a la vez.
        ServletContext application = getServletContext();
        String consultaAnterior;
        int totalConsultas;
        synchronized (application) {
            consultaAnterior = (String) application.getAttribute("ultimaConsulta");
            Object previo = application.getAttribute("totalConsultas");
            totalConsultas = (previo instanceof Integer) ? ((Integer) previo) + 1 : 1;
            application.setAttribute("ultimaConsulta", categoria + " (L = " + luz + " m)");
            application.setAttribute("totalConsultas", totalConsultas);
        }

        // response: encabezado HTTP (viaja en la cabecera, no se ve en pantalla)
        response.setHeader("X-Curso", "Programacion III");

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"container\">");
        sb.append("<nav class=\"menu\"><a href=\"index.html\">Simulador</a><a href=\"index.jsp\">Catálogo (JSP)</a>"
                + "<a href=\"consulta.jsp\">Consulta (JSP)</a><a href=\"analisis-estructural\">Formulario (Servlet)</a></nav>");
        sb.append("<h1>Consulta de predimensionamiento</h1>");
        sb.append("<div class=\"dato\">Categoría consultada: <b>").append(escaparHtml(categoria)).append("</b></div>");
        sb.append("<div class=\"dato\">Luz de cálculo: <b>").append(String.format(Locale.ROOT, "%.2f", luz)).append(" m</b>");
        if (luzPorDefecto) {
            sb.append(" <span class=\"nota\">(valor por defecto: no llegó el parámetro luz)</span>");
        }
        sb.append("</div>");

        boolean general = categoria.equals("general");
        if (categoria.equals("viga") || general) {
            sb.append("<div class=\"dato\">Viga principal: peralte <b>")
              .append(String.format(Locale.ROOT, "%.0f", luz / 10.0 * 100)).append(" cm</b> x base <b>")
              .append(String.format(Locale.ROOT, "%.0f", luz / 10.0 / 2.0 * 100))
              .append(" cm</b> <span class=\"nota\">(h = L / 10, b = h / 2)</span></div>");
        }
        if (categoria.equals("placa") || general) {
            sb.append("<div class=\"dato\">Placa de entrepiso: espesor <b>")
              .append(String.format(Locale.ROOT, "%.0f", luz / 25.0 * 100))
              .append(" cm</b> <span class=\"nota\">(h = L / 25)</span></div>");
        }
        if (!categoria.equals("viga") && !categoria.equals("placa") && !general) {
            sb.append("<div class=\"dato\">La categoría <b>").append(escaparHtml(categoria))
              .append("</b> no tiene fórmula registrada; use <code>viga</code>, <code>placa</code> o <code>general</code>.</div>");
        }

        sb.append("<p class=\"nota\">Consulta anterior guardada en <code>application</code> (de cualquier visitante): <b>")
          .append(consultaAnterior == null ? "ninguna" : escaparHtml(consultaAnterior))
          .append("</b><br>Total de consultas desde que arrancó el servidor: <b>").append(totalConsultas).append("</b></p>");
        sb.append("<a href=\"consulta.jsp\" class=\"btn-back\">&larr; Volver a la página de consulta</a>");
        sb.append("</div>");

        // out: el PrintWriter de la respuesta escribe el HTML final
        escribirPagina(response, "MIMARI - Consulta de predimensionamiento", ESTILOS_BASE + ESTILOS_RESULTADO, sb.toString());
    }

    // ======================================================================
    //  LÓGICA DE CÁLCULO (compartida por /analisis-estructural y /api/predimensionar)
    // ======================================================================
    /** Devuelve los resultados en orden y agrega las alertas ({nivel, mensaje}) a la lista recibida. */
    private static Map<String, Double> calcular(double ancho, double largo, int pisos, double alturaPiso,
                                                List<String[]> alertas) {
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

        double alturaTotal = pisos * alturaPiso;
        double areaBase = ancho * largo;
        double areaTotalConstruida = areaBase * pisos;
        double perimetro = 2 * (ancho + largo);
        double areaMuros = perimetro * alturaTotal;
        double luz = Math.max(ancho, largo);
        double peralteViga = luz / 10.0;
        double baseViga = peralteViga / 2.0;
        double espesorPlaca = luz / 25.0;
        double volumenConcreto = areaTotalConstruida * espesorPlaca;
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

        Map<String, Double> r = new LinkedHashMap<>();
        r.put("ancho", ancho);
        r.put("largo", largo);
        r.put("pisos", (double) pisos);
        r.put("alturaPiso", alturaPiso);
        r.put("alturaTotal", alturaTotal);
        r.put("areaBase", areaBase);
        r.put("areaTotalConstruida", areaTotalConstruida);
        r.put("perimetro", perimetro);
        r.put("areaMuros", areaMuros);
        r.put("luz", luz);
        r.put("peralteViga", peralteViga);
        r.put("baseViga", baseViga);
        r.put("espesorPlaca", espesorPlaca);
        r.put("volumenConcreto", volumenConcreto);
        r.put("relacionEsbeltez", esbeltez);
        return r;
    }

    // ======================================================================
    //  LECTURA DE PARÁMETROS
    // ======================================================================
    /** Convierte a double. Acepta "10.5" y "10,5". Lanza IllegalArgumentException con mensaje legible. */
    private static double decimal(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el campo obligatorio: " + campo + ".");
        }
        try {
            return Double.parseDouble(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " no es un número válido.");
        }
    }

    private static int entero(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el campo obligatorio: " + campo + ".");
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un número entero.");
        }
    }

    // ======================================================================
    //  PLANO ADJUNTO
    //  Seguridad: se guarda FUERA de la carpeta publicada (nadie puede ejecutarlo ni
    //  descargarlo por URL), con nombre aleatorio y solo si la extensión es de imagen.
    // ======================================================================
    private static String leerPlano(HttpServletRequest request) {
        try {
            return guardarPlano(request.getPart("planoImagen"));
        } catch (ServletException | IOException | IllegalStateException e) {
            return "Error al procesar el archivo adjunto.";
        }
    }

    private static String guardarPlano(Part parte) {
        try {
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
            return "Archivo guardado exitosamente: " + nombre;
        } catch (Exception e) {
            return "Error al procesar el archivo adjunto.";
        }
    }

    // ======================================================================
    //  SALIDA HTML / ESCAPES
    // ======================================================================
    private static void tarjeta(StringBuilder sb, String titulo, String valor) {
        sb.append("<div class=\"card\"><h3>").append(escaparHtml(titulo)).append("</h3><p>")
          .append(escaparHtml(valor)).append("</p></div>");
    }

    private static void escribirPagina(HttpServletResponse response, String titulo, String estilos, String cuerpo)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"es\">");
            out.println("<head>");
            out.println("<meta charset=\"UTF-8\">");
            out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            out.println("<title>" + escaparHtml(titulo) + "</title>");
            out.println("<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap\" rel=\"stylesheet\">");
            out.println("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css\">");
            out.println("<style>");
            out.println(estilos);
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            out.println(cuerpo);
            out.println("</body>");
            out.println("</html>");
        }
    }

    /** Escapa texto para insertarlo en HTML (evita XSS). */
    private static String escaparHtml(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Escapa texto para meterlo dentro de un string JSON. */
    private static String escaparJson(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}

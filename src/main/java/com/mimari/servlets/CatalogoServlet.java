package com.mimari.servlets;

import com.mimari.modelo.Archivo;
import com.mimari.modelo.Insumo;
import com.mimari.seguridad.Sesion;
import com.mimari.servicio.ArchivoService;
import com.mimari.servicio.CarritoTemporal;
import com.mimari.servicio.InsumoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Catálogo único de MIMARI (/catalogo): es la página de entrada después de iniciar sesión.
 *
 *   Semana 09  catálogo de elementos estructurales (cálculo del volumen aquí, la vista solo lo muestra)
 *   Semana 11  usa la sesión: cada persona ve y guarda SU información (Sesion.usuarioId)
 *   Semana 13  insumos por usuario con JPA a través de InsumoService (sin estado) + CarritoTemporal (con estado)
 *   Semana 14  la vista catalogo.jsp usa JSTL (c, fmt) con textos en español e inglés
 *
 *   GET  -> prepara los datos y hace forward a WEB-INF/vistas/catalogo.jsp
 *   POST accion = guardar | eliminar | carrito | vaciar | pdf | eliminarPdf   (VALIDA aquí y redirige: POST -> redirect -> GET)
 * Este servlet no escribe HTML. Si la base de datos falla, la página igual se muestra con la parte fija del catálogo.
 */
@WebServlet("/catalogo")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 15L * 1024 * 1024, maxRequestSize = 16L * 1024 * 1024)
public class CatalogoServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/vistas/catalogo.jsp";
    private static final String VISTA_ERROR = "/error.jsp";
    private static final String ATRIBUTO_CARRITO = "carritoTemporal";
    private static final Set<String> IDIOMAS = Set.of("es", "en");
    private static final Pattern TEXTO_VALIDO = Pattern.compile("[\\p{L}\\p{N} .,()/+#%\"'-]{1,100}");
    private static final Pattern TIPO_VALIDO = Pattern.compile("[\\p{L}\\p{N} .,()/+#%-]{1,60}");
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("999999999999.99");   // cabe en DECIMAL(14,2)
    private static final long PDF_MAXIMO = 15L * 1024 * 1024;

    // Equivalen al @EJB de la clase: no tienen estado, una sola instancia sirve a todos los visitantes
    private final InsumoService insumoService = new InsumoService();
    private final ArchivoService archivoService = new ArchivoService();

    // ---------------------------------------------------------------- GET

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long usuarioId = Sesion.usuarioId(request);

        request.setAttribute("idioma", idioma(request));
        request.setAttribute("fechaActual", new Date());
        request.setAttribute("materiales", materialesBase());
        request.setAttribute("elementos", elementosEstructurales());
        request.setAttribute("carrito", carrito(request).getNombres());
        request.setAttribute("carritoMaximo", CarritoTemporal.MAXIMO);

        // Datos de la persona (JPA). Si la base falla, se avisa pero el resto del catálogo se sigue mostrando.
        List<Insumo> insumos = List.of();
        List<Archivo> pdfs = List.of();
        BigDecimal total = BigDecimal.ZERO;
        try {
            insumos = insumoService.listar(usuarioId);
            pdfs = archivoService.listar(usuarioId, Archivo.CATALOGO);
            for (Insumo i : insumos) {
                total = total.add(i.getSubtotal());
            }
        } catch (RuntimeException e) {
            log("No se pudieron leer los datos del usuario (JPA)", e);
            request.setAttribute("avisoDatos",
                    "No se pudo acceder a la base de datos: tus insumos y catálogos PDF no se pueden mostrar por ahora.");
        }
        request.setAttribute("insumos", insumos);
        request.setAttribute("totalInsumos", total);
        request.setAttribute("pdfs", pdfs);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    // ---------------------------------------------------------------- POST

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Long usuarioId = Sesion.usuarioId(request);
        String ancla = "catalogo";
        try {
            String accion = request.getParameter("accion");
            if (accion == null) {
                throw new IllegalArgumentException("Acción no reconocida.");
            }
            switch (accion) {
                case "guardar" -> {
                    insumoService.crear(usuarioId, nombre(request.getParameter("nombre")),
                            tipo(request.getParameter("tipo")), precio(request.getParameter("precio")),
                            cantidad(request.getParameter("cantidad")));
                    ancla = "mis-insumos";
                }
                case "eliminar" -> {
                    insumoService.eliminar(usuarioId, identificador(request.getParameter("id")));
                    ancla = "mis-insumos";
                }
                case "carrito" -> {
                    if (!carrito(request).agregar(nombre(request.getParameter("nombre")))) {
                        throw new IllegalArgumentException(
                                "La lista de cotización está llena (máximo " + CarritoTemporal.MAXIMO + ").");
                    }
                    ancla = "cotizacion";
                }
                case "vaciar" -> {
                    carrito(request).vaciar();
                    ancla = "cotizacion";
                }
                case "pdf" -> {
                    subirPdf(request, usuarioId);
                    ancla = "pdf";
                }
                case "eliminarPdf" -> {
                    archivoService.eliminar(usuarioId, identificador(request.getParameter("id")), Archivo.CATALOGO);
                    ancla = "pdf";
                }
                default -> throw new IllegalArgumentException("Acción no reconocida.");
            }
        } catch (IllegalArgumentException e) {
            enviarError(request, response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage(), e.getClass().getSimpleName());
            return;
        } catch (IllegalStateException e) {
            // Tomcat lo lanza si el archivo/solicitud supera los límites de @MultipartConfig
            enviarError(request, response, HttpServletResponse.SC_BAD_REQUEST,
                    "El archivo supera el tamaño permitido (15 MB).", "ArchivoDemasiadoGrande");
            return;
        } catch (RuntimeException e) {
            log("Error de persistencia (JPA)", e);              // el detalle va al log, no a la pantalla
            enviarError(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "No se pudo guardar en la base de datos. Verifica que MySQL esté encendido y que el DataSource "
                            + "jdbc/programacion3 esté configurado.", "ErrorPersistencia");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/catalogo#" + ancla);
    }

    private void subirPdf(HttpServletRequest request, Long usuarioId) throws IOException, ServletException {
        Part parte = request.getPart("archivo");
        if (parte == null || parte.getSize() <= 0) {
            throw new IllegalArgumentException("Selecciona un archivo PDF.");
        }
        if (parte.getSize() > PDF_MAXIMO) {
            throw new IllegalArgumentException("El PDF supera el tamaño permitido (15 MB).");
        }
        String enviado = parte.getSubmittedFileName();
        String nombre = (enviado == null) ? "catalogo.pdf" : Paths.get(enviado).getFileName().toString();
        if (!nombre.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Solo se permiten archivos PDF.");
        }
        byte[] datos;
        try (InputStream in = parte.getInputStream()) {
            datos = in.readNBytes((int) PDF_MAXIMO + 1);
        }
        // Se comprueba el contenido real (los PDF empiezan con "%PDF-"), no solo el nombre
        if (datos.length < 5 || datos[0] != '%' || datos[1] != 'P' || datos[2] != 'D' || datos[3] != 'F' || datos[4] != '-') {
            throw new IllegalArgumentException("El archivo no es un PDF válido.");
        }
        String limpio = nombre.replaceAll("[^\\p{L}\\p{N} ._()-]", "_");
        if (limpio.length() > 150) {
            limpio = limpio.substring(limpio.length() - 150);
        }
        archivoService.guardar(usuarioId, Archivo.CATALOGO, limpio, "application/pdf", "pdf", datos);
    }

    private static void enviarError(HttpServletRequest request, HttpServletResponse response, int estado,
                                    String mensaje, String tipo) throws ServletException, IOException {
        response.setStatus(estado);
        request.setAttribute("mensajeError", mensaje);
        request.setAttribute("tipoError", tipo);
        request.getRequestDispatcher(VISTA_ERROR).forward(request, response);
    }

    // ---------------------------------------------------------------- datos del catálogo

    /** El idioma (es | en) se valida aquí y se recuerda en la sesión. */
    private static String idioma(HttpServletRequest request) {
        String idioma = "es";
        HttpSession session = request.getSession(true);
        Object guardado = session.getAttribute("idioma");
        if (guardado instanceof String g && IDIOMAS.contains(g)) {
            idioma = g;
        }
        String pedido = request.getParameter("idioma");
        if (pedido != null && IDIOMAS.contains(pedido.trim().toLowerCase(Locale.ROOT))) {
            idioma = pedido.trim().toLowerCase(Locale.ROOT);
            session.setAttribute("idioma", idioma);
        }
        return idioma;
    }

    private static List<Map<String, Object>> materialesBase() {
        return List.of(
                Map.<String, Object>of("nombre", "Cemento gris 50 kg", "precio", 32000, "disponible", true),
                Map.<String, Object>of("nombre", "Varilla corrugada 1/2\" x 6 m", "precio", 38500, "disponible", true),
                Map.<String, Object>of("nombre", "Ladrillo estructural", "precio", 1800, "disponible", false),
                Map.<String, Object>of("nombre", "Arena de río (m³)", "precio", 95000, "disponible", true));
    }

    /** Semana 09: el método auxiliar que antes se declaraba en la JSP ahora vive aquí; la vista solo imprime. */
    private static double calcularVolumenAprox(double ancho, double largo, double espesor) {
        return ancho * largo * espesor;
    }

    private static List<Map<String, Object>> elementosEstructurales() {
        String[] nombres = {"Viga principal eje A", "Placa de entrepiso nivel 1", "Zapatas de cimentación"};
        double[] luz = {6.50, 5.00, 2.80};
        double[] factor = {0.65, 0.20, 0.45};               // peraltes o espesores en metros
        List<Map<String, Object>> lista = new ArrayList<>();
        for (int i = 0; i < nombres.length; i++) {
            lista.add(Map.<String, Object>of("numero", i + 1, "nombre", nombres[i], "luz", luz[i],
                    "espesor", factor[i], "volumen", calcularVolumenAprox(luz[i], 4.0, factor[i])));
        }
        return lista;
    }

    /** Un CarritoTemporal por visitante, guardado en su sesión (componente con estado). */
    private static CarritoTemporal carrito(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        synchronized (session) {
            CarritoTemporal carrito = (CarritoTemporal) session.getAttribute(ATRIBUTO_CARRITO);
            if (carrito == null) {
                carrito = new CarritoTemporal();
                session.setAttribute(ATRIBUTO_CARRITO, carrito);
            }
            return carrito;
        }
    }

    // ---------------------------------------------------------------- validación (solo aquí)

    private static String nombre(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el campo obligatorio: nombre.");
        }
        String limpio = valor.trim();
        if (!TEXTO_VALIDO.matcher(limpio).matches()) {
            throw new IllegalArgumentException(
                    "El nombre solo puede tener letras, números y . , ( ) / + # % \" ' - (máximo 100 caracteres).");
        }
        return limpio;
    }

    private static String tipo(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el campo obligatorio: categoría.");
        }
        String limpio = valor.trim();
        if (!TIPO_VALIDO.matcher(limpio).matches()) {
            throw new IllegalArgumentException(
                    "La categoría solo puede tener letras, números y . , ( ) / + # % - (máximo 60 caracteres).");
        }
        return limpio;
    }

    /** Acepta "59.90" y "59,90". */
    private static BigDecimal precio(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el campo obligatorio: precio.");
        }
        String limpio = valor.trim();
        if (limpio.length() > 17 || !limpio.matches("[0-9]+([.,][0-9]{1,2})?")) {
            throw new IllegalArgumentException("El precio no es válido (use números, con máximo dos decimales).");
        }
        BigDecimal precio = new BigDecimal(limpio.replace(',', '.')).setScale(2, RoundingMode.HALF_UP);
        if (precio.signum() <= 0 || precio.compareTo(PRECIO_MAXIMO) > 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que 0 y no superar 999.999.999.999,99.");
        }
        return precio;
    }

    private static int cantidad(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el campo obligatorio: cantidad.");
        }
        String limpio = valor.trim();
        if (!limpio.matches("[0-9]{1,6}")) {
            throw new IllegalArgumentException("La cantidad debe ser un número entero.");
        }
        int cantidad = Integer.parseInt(limpio);
        if (cantidad < 1 || cantidad > 100000) {
            throw new IllegalArgumentException("La cantidad debe estar entre 1 y 100000.");
        }
        return cantidad;
    }

    private static Long identificador(String valor) {
        if (valor == null || !valor.trim().matches("[0-9]{1,18}")) {
            throw new IllegalArgumentException("Identificador no válido.");
        }
        return Long.valueOf(valor.trim());
    }
}

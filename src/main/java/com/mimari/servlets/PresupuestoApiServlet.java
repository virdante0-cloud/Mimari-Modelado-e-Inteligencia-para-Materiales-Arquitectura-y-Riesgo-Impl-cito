package com.mimari.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * API JSON del presupuesto base (/api/presupuesto), la usa js/presupuesto.js.
 *   ancho, largo, pisos, alturaPiso            -> geometría (se recalcula el área de muros)
 *   tipoLadrillo, precioLadrillo, precioCristal, numVentanas, numPuertas
 *
 * Toda la validación vive aquí. Responde JSON (no HTML). Independiente de los demás servlets.
 */
@WebServlet("/api/presupuesto")
public class PresupuestoApiServlet extends HttpServlet {

    private static final Pattern TEXTO_VALIDO = Pattern.compile("[\\p{L}\\p{N} .,()/+#%-]{1,60}");
    private static final double PRECIO_LADRILLO_DEFECTO = 1200;
    private static final double PRECIO_CRISTAL_DEFECTO = 85000;
    private static final double PRECIO_MAXIMO = 1_000_000_000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        responder(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        responder(request, response);
    }

    private void responder(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        String cuerpo;
        try {
            // ---- validación
            double ancho = decimal(request.getParameter("ancho"), "ancho");
            double largo = decimal(request.getParameter("largo"), "largo");
            int pisos = entero(request.getParameter("pisos"), "pisos");
            double alturaPiso = decimal(request.getParameter("alturaPiso"), "alturaPiso");
            if (ancho <= 0 || ancho > 500 || largo <= 0 || largo > 500) {
                throw new IllegalArgumentException("El ancho y el largo deben estar entre 0 y 500 m.");
            }
            if (pisos < 1 || pisos > 100) {
                throw new IllegalArgumentException("El número de pisos debe estar entre 1 y 100.");
            }
            if (alturaPiso <= 0 || alturaPiso > 20) {
                throw new IllegalArgumentException("La altura por piso debe estar entre 0 y 20 m.");
            }

            String tipoLadrillo = request.getParameter("tipoLadrillo");
            tipoLadrillo = (tipoLadrillo == null || tipoLadrillo.isBlank()) ? "Ladrillo Estándar" : tipoLadrillo.trim();
            if (!TEXTO_VALIDO.matcher(tipoLadrillo).matches()) {
                throw new IllegalArgumentException(
                        "El tipo de mampostería admite letras, números y . , ( ) / + # % - (máximo 60 caracteres).");
            }
            double precioLadrillo = precio(request.getParameter("precioLadrillo"), "precioLadrillo", PRECIO_LADRILLO_DEFECTO);
            double precioCristal = precio(request.getParameter("precioCristal"), "precioCristal", PRECIO_CRISTAL_DEFECTO);
            int numVentanas = cantidad(request.getParameter("numVentanas"), "numVentanas");
            int numPuertas = cantidad(request.getParameter("numPuertas"), "numPuertas");

            // ---- cálculo (mismas reglas del simulador)
            double areaMuros = 2 * (ancho + largo) * (pisos * alturaPiso);
            double areaVentanas = numVentanas * 1.5;          // supuesto: 1.5 m² por ventana
            double areaPuertas = numPuertas * 2.0;            // supuesto: 2.0 m² por puerta
            double areaNeta = Math.max(0, areaMuros - (areaVentanas + areaPuertas));
            long cantLadrillos = (long) Math.ceil(areaNeta * 55 * 1.05);   // 55 und/m² + 5 % desperdicio
            double costoLadrillos = cantLadrillos * precioLadrillo;
            double costoCristal = areaVentanas * precioCristal;
            double costoPuertas = numPuertas * 150000.0;      // costo promedio marco/puerta
            double costoTotal = costoLadrillos + costoCristal + costoPuertas;

            cuerpo = "{\"ok\":true"
                    + ",\"tipoLadrillo\":\"" + escaparJson(tipoLadrillo) + "\""
                    + ",\"cantLadrillos\":" + cantLadrillos
                    + ",\"costoLadrillos\":" + num(costoLadrillos)
                    + ",\"areaCristal\":" + num(areaVentanas)
                    + ",\"costoCristal\":" + num(costoCristal)
                    + ",\"cantPuertas\":" + numPuertas
                    + ",\"costoPuertas\":" + num(costoPuertas)
                    + ",\"costoTotal\":" + num(costoTotal) + "}";
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            cuerpo = "{\"ok\":false,\"error\":\"" + escaparJson(e.getMessage()) + "\"}";
        }

        try (PrintWriter out = response.getWriter()) {
            out.print(cuerpo);
        }
    }

    // ---------------------------------------------------------------- lectura / validación

    private static double precio(String valor, String campo, double porDefecto) {
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        double p = decimal(valor, campo);
        if (p < 0 || p > PRECIO_MAXIMO) {
            throw new IllegalArgumentException("El campo " + campo + " debe estar entre 0 y 1.000.000.000.");
        }
        return p;
    }

    private static int cantidad(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return 0;
        }
        int n = entero(valor, campo);
        if (n < 0 || n > 1000) {
            throw new IllegalArgumentException("El campo " + campo + " debe estar entre 0 y 1000.");
        }
        return n;
    }

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

    // ---------------------------------------------------------------- JSON

    private static String num(double v) {
        return String.format(Locale.ROOT, "%.2f", v);
    }

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

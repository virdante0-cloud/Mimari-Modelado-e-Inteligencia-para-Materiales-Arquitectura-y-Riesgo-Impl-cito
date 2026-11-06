package com.mimari.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * API JSON de predimensionamiento (/api/predimensionar), la usa js/simulador.js.
 *   POST o GET: ancho, largo, pisos, alturaPiso   (GET sirve para probar en el navegador:
 *                                                  /api/predimensionar?ancho=6&largo=8&pisos=2&alturaPiso=2.6)
 *
 * Solo calcula (fórmulas de predimensionamiento). La fotografía NO viaja aquí: se sube aparte en /api/archivos y el análisis
 * con IA es otro paso (/api/ia). Valida todo aquí. No genera HTML: responde JSON. Independiente de los demás servlets.
 */
@WebServlet("/api/predimensionar")
public class PredimensionarApiServlet extends HttpServlet {

    private static final double ALTURA_MINIMA_PISO = 2.20;
    private static final double ESBELTEZ_MAXIMA = 4.0;

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
            double ancho = decimal(request.getParameter("ancho"), "ancho");
            double largo = decimal(request.getParameter("largo"), "largo");
            int pisos = entero(request.getParameter("pisos"), "pisos");
            double alturaPiso = decimal(request.getParameter("alturaPiso"), "alturaPiso");

            List<String[]> alertas = new ArrayList<>();
            Map<String, Double> r = calcular(ancho, largo, pisos, alturaPiso, alertas);

            StringBuilder sb = new StringBuilder(512).append("{\"ok\":true");
            for (Map.Entry<String, Double> e : r.entrySet()) {
                sb.append(",\"").append(e.getKey()).append("\":");
                if (e.getKey().equals("pisos")) {
                    sb.append(e.getValue().longValue());
                } else {
                    sb.append(String.format(Locale.ROOT, "%.4f", e.getValue()));
                }
            }
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

    // ---------------------------------------------------------------- validación + cálculo

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

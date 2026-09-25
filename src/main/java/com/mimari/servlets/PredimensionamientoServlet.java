package com.mimari.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.nio.file.Paths;

@WebServlet("/analisis-estructural")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class PredimensionamientoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"es\">");
            out.println("<head>");
            out.println("<meta charset=\"UTF-8\">");
            out.println("<title>MIMARI - Predimensionamiento y Estructuras</title>");
            out.println("<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap\" rel=\"stylesheet\">");
            out.println("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css\">");
            out.println("<style>");
            out.println("* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Inter', sans-serif; }");
            out.println("body { background: #f8fafc; color: #0f172a; padding: 40px 20px; display: flex; justify-content: center; }");
            out.println(".container { width: 100%; max-width: 900px; background: #ffffff; padding: 40px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; }");
            out.println("h1 { font-size: 28px; font-weight: 700; color: #0f172a; margin-bottom: 8px; }");
            out.println("p.subtitle { color: #64748b; font-size: 15px; margin-bottom: 32px; }");
            out.println(".form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; margin-bottom: 24px; }");
            out.println(".form-group { display: flex; flex-direction: column; }");
            out.println(".form-group label { font-weight: 600; font-size: 14px; color: #334155; margin-bottom: 8px; }");
            out.println(".form-group input { padding: 12px 16px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 15px; outline: none; transition: border-color 0.2s; }");
            out.println(".form-group input:focus { border-color: #0284c7; }");
            out.println(".full-width { grid-column: span 2; }");
            out.println("button { background: #0284c7; color: white; border: none; padding: 14px 24px; font-size: 16px; font-weight: 600; border-radius: 8px; cursor: pointer; width: 100%; transition: background 0.2s; }");
            out.println("button:hover { background: #0369a1; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");

            out.println("<div class=\"container\">");
            out.println("<h1>MIMARI - Módulo de Registro y Análisis</h1>");
            out.println("<p class=\"subtitle\">Ingrese los parámetros geométricos del proyecto. Envío seguro por método POST.</p>");

            out.println("<form action=\"analisis-estructural\" method=\"POST\" enctype=\"multipart/form-data\">");
            out.println("<div class=\"form-grid\">");

            out.println("<div class=\"form-group\">");
            out.println("<label>Ancho del Edificio (m)</label>");
            out.println("<input type=\"number\" step=\"0.1\" name=\"ancho\" required placeholder=\"Ej. 10.5\">");
            out.println("</div>");

            out.println("<div class=\"form-group\">");
            out.println("<label>Largo del Edificio (m)</label>");
            out.println("<input type=\"number\" step=\"0.1\" name=\"largo\" required placeholder=\"Ej. 15.0\">");
            out.println("</div>");

            out.println("<div class=\"form-group\">");
            out.println("<label>Número de Pisos</label>");
            out.println("<input type=\"number\" name=\"pisos\" required placeholder=\"Ej. 4\">");
            out.println("</div>");

            out.println("<div class=\"form-group\">");
            out.println("<label>Altura Promedio por Piso (m)</label>");
            out.println("<input type=\"number\" step=\"0.1\" name=\"alturaPiso\" required placeholder=\"Ej. 2.6\">");
            out.println("</div>");

            out.println("<div class=\"form-group full-width\">");
            out.println("<label>Adjuntar Plano Arquitectónico (Imagen)</label>");
            out.println("<input type=\"file\" name=\"planoImagen\" accept=\"image/*\">");
            out.println("</div>");

            out.println("</div>");
            out.println("<button type=\"submit\"><i class=\"fa-solid fa-calculator\"></i> Procesar Cálculo Estructural</button>");
            out.println("</form>");

            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        double ancho = Double.parseDouble(request.getParameter("ancho"));
        double largo = Double.parseDouble(request.getParameter("largo"));
        int pisos = Integer.parseInt(request.getParameter("pisos"));
        double alturaPiso = Double.parseDouble(request.getParameter("alturaPiso"));

        String mensajeArchivo = "Ningún archivo adjuntado.";
        try {
            Part filePart = request.getPart("planoImagen");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdir();
                filePart.write(uploadPath + File.separator + fileName);
                mensajeArchivo = "Archivo guardado exitosamente: " + fileName;
            }
        } catch (Exception e) {
            mensajeArchivo = "Error al procesar el archivo adjunto.";
        }

        double alturaTotal = pisos * alturaPiso;
        double areaBase = ancho * largo;
        double areaTotalConstruida = areaBase * pisos;
        double luzReferencia = Math.max(ancho, largo);
        double peralteViga = luzReferencia / 10.0;
        double espesorPlaca = luzReferencia / 25.0;

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html lang=\"es\">");
            out.println("<head>");
            out.println("<meta charset=\"UTF-8\">");
            out.println("<title>Resultados del Análisis - MIMARI</title>");
            out.println("<link href=\"https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap\" rel=\"stylesheet\">");
            out.println("<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css\">");
            out.println("<style>");
            out.println("* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Inter', sans-serif; }");
            out.println("body { background: #f8fafc; color: #0f172a; padding: 40px 20px; display: flex; justify-content: center; }");
            out.println(".container { width: 100%; max-width: 900px; background: #ffffff; padding: 40px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); border: 1px solid #e2e8f0; }");
            out.println("h1 { font-size: 26px; font-weight: 700; color: #0f172a; margin-bottom: 24px; }");
            out.println(".results-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; }");
            out.println(".card { background: #f8fafc; padding: 20px; border-radius: 12px; border: 1px solid #e2e8f0; }");
            out.println(".card h3 { font-size: 14px; color: #64748b; margin-bottom: 6px; text-transform: uppercase; letter-spacing: 0.5px; }");
            out.println(".card p { font-size: 22px; font-weight: 700; color: #0f172a; }");
            out.println(".alert-danger { background: #fef2f2; border-left: 6px solid #ef4444; padding: 16px; border-radius: 8px; color: #991b1b; font-weight: 500; margin-bottom: 16px; }");
            out.println(".alert-success { background: #f0fdf4; border-left: 6px solid #22c55e; padding: 16px; border-radius: 8px; color: #166534; font-weight: 500; margin-bottom: 16px; }");
            out.println(".file-info { font-size: 14px; color: #475569; background: #f1f5f9; padding: 12px; border-radius: 8px; margin-bottom: 24px; }");
            out.println(".btn-back { display: inline-block; background: #0f172a; color: white; text-decoration: none; padding: 12px 20px; border-radius: 8px; font-weight: 600; font-size: 14px; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");

            out.println("<div class=\"container\">");
            out.println("<h1><i class=\"fa-solid fa-square-poll-vertical\"></i> Resultados de Predimensionamiento Estructural</h1>");

            if (alturaPiso < 2.20) {
                out.println("<div class=\"alert-danger\"><i class=\"fa-solid fa-triangle-exclamation\"></i> ALERTA CRÍTICA: La altura por piso (" + alturaPiso + "m) viola el mínimo normativo de habitabilidad (2.20m).</div>");
            } else {
                out.println("<div class=\"alert-success\"><i class=\"fa-solid fa-circle-check\"></i> Altura por piso conforme a la normativa de confort.</div>");
            }

            double relacionEsbeltez = alturaTotal / Math.min(ancho, largo);
            if (relacionEsbeltez > 4.0) {
                out.println("<div class=\"alert-danger\"><i class=\"fa-solid fa-triangle-exclamation\"></i> ALERTA SÍSMICA: Relación de esbeltez elevada (" + String.format("%.2f", relacionEsbeltez) + "). Alto riesgo de volcamiento.</div>");
            }

            out.println("<div class=\"file-info\"><i class=\"fa-solid fa-file-image\"></i> " + mensajeArchivo + "</div>");

            out.println("<div class=\"results-grid\">");
            out.println("<div class=\"card\"><h3>Altura Total del Edificio</h3><p>" + String.format("%.2f", alturaTotal) + " m</p></div>");
            out.println("<div class=\"card\"><h3>Área Construida Total</h3><p>" + String.format("%.2f", areaTotalConstruida) + " m²</p></div>");
            out.println("<div class=\"card\"><h3>Peralte Sugerido para Viga</h3><p>" + String.format("%.2f", peralteViga * 100) + " cm</p></div>");
            out.println("<div class=\"card\"><h3>Espesor Mínimo de Placa</h3><p>" + String.format("%.2f", espesorPlaca * 100) + " cm</p></div>");
            out.println("</div>");

            out.println("<a href=\"analisis-estructural\" class=\"btn-back\"><i class=\"fa-solid fa-arrow-left\"></i> Realizar Nuevo Cálculo</a>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}
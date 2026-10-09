<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%-- Comentario JSP: Página de gestión de materiales y predimensionamiento MIMARI --%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MIMARI - Catálogo y Predimensionamiento Dinámico</title>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body { font-family: 'Plus Jakarta Sans', sans-serif; background: #f8fafc; color: #334155; padding: 30px; display: flex; justify-content: center; margin: 0; }
        .wrapper { width: 100%; max-width: 950px; }
        .card { background: white; padding: 30px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.03); border: 1px solid #e2e8f0; margin-bottom: 25px; }
        h3 { color: #0f172a; margin: 0 0 15px 0; font-size: 18px; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { padding: 12px 15px; border: 1px solid #e2e8f0; text-align: left; font-size: 14px; }
        th { background: #0f172a; color: white; }
        tr:nth-child(even) { background: #f8fafc; }
        .badge { background: #e0f2fe; color: #0284c7; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 12px; }
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
        .form-group { display: flex; flex-direction: column; }
        .form-group label { font-size: 13px; font-weight: 600; margin-bottom: 6px; color: #475569; }
        .form-group input { padding: 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; outline: none; transition: border-color 0.2s; }
        .form-group input:focus { border-color: #0284c7; }
        .full { grid-column: span 2; }
        .btn-submit { background: #0284c7; color: white; border: none; padding: 14px; font-weight: 600; border-radius: 8px; cursor: pointer; width: 100%; margin-top: 15px; transition: background 0.2s; }
        .btn-submit:hover { background: #0369a1; }
    </style>
</head>
<body>

<div class="wrapper">

    <!-- 4. REUTILIZAR COMPONENTES: Inclusión obligatoria del header -->
    <jsp:include page="header.jsp" />

    <div class="card">
        <h3>Parámetros de Entrada para Ingeniería</h3>
        <!-- El formulario apunta por POST al servlet PredimensionamientoServlet (@WebServlet("/analisis-estructural")) -->
        <form action="analisis-estructural" method="POST" enctype="multipart/form-data">
            <div class="form-grid">
                <div class="form-group">
                    <label>Ancho del Edificio (m)</label>
                    <input type="number" step="0.1" name="ancho" required placeholder="Ej. 10.5">
                </div>
                <div class="form-group">
                    <label>Largo del Edificio (m)</label>
                    <input type="number" step="0.1" name="largo" required placeholder="Ej. 15.0">
                </div>
                <div class="form-group">
                    <label>Número de Pisos</label>
                    <input type="number" name="pisos" required placeholder="Ej. 4">
                </div>
                <div class="form-group">
                    <label>Altura Promedio por Piso (m) [Mínimo: 2.20m]</label>
                    <input type="number" step="0.1" name="alturaPiso" required placeholder="Ej. 2.6">
                </div>
                <div class="form-group full">
                    <label>Adjuntar Plano Arquitectónico (Imagen opcional)</label>
                    <input type="file" name="planoImagen" accept="image/*">
                </div>
            </div>
            <button type="submit" class="btn-submit">Procesar Datos en el Servidor (POST)</button>
        </form>
    </div>

    <div class="card">
        <h3>Consulta rápida por parámetros (Semana 10)</h3>
        <p style="font-size: 14px; color: #64748b; margin-bottom: 15px;">
            El servlet <b>/consulta</b> lee <code>categoria</code> y <code>luz</code> desde la URL (<code>request</code>),
            responde con <code>out</code> y <code>response</code>, comparte datos con <code>application</code>
            y, si algo falla, entrega el control a <b>error.jsp</b>.
            Ejemplo: <a href="consulta?categoria=viga&amp;luz=6.5">consulta?categoria=viga&amp;luz=6.5</a>
            &nbsp;·&nbsp; <a href="consulta.jsp">Abrir página de consulta</a>
        </p>
    </div>

    <div class="card">
        <h3>📊 Listado Dinámico de Elementos Estructurales Precalificados</h3>
        <p style="font-size: 14px; color: #64748b; margin-bottom: 15px;">
            A continuación se cargan de forma dinámica los componentes analizados en el sistema para el proyecto de ingeniería:
        </p>

        <!-- 2. DECLARACIÓN JSP: Método o variable global de clase en el servidor -->
        <%!
            // Declaramos un método auxiliar para calcular el volumen de concreto preliminar
            public double calcularVolumenAprox(double ancho, double largo, double espesor) {
                return ancho * largo * espesor;
            }

            // Declaramos una variable de recuento del sistema
            private int totalElementosEvaluados = 3;
        %>

        <!-- 3. SCRIPTLET JSP Y DATOS DINÁMICOS: Arreglo de elementos estructurales del proyecto -->
        <%
            String[] tipoElementos = {"Viga Principal Eje A", "Placa de Entrepiso Nivel 1", "Zapatas de Cimentación"};
            double[] luzAsociada = {6.50, 5.00, 2.80};
            double[] factorCalculado = {0.65, 0.20, 0.45}; // Peraltes o espesores en metros
        %>

        <table>
            <tr>
                <th>ID</th>
                <th>Elemento Estructural</th>
                <th>Luz de Cálculo (m)</th>
                <th>Peralte / Espesor (m)</th>
                <th>Volumen Estándar Aprox. (m³)</th>
            </tr>

            <!-- Bucle Scriptlet para recorrer los datos dinámicos -->
            <%
                for (int i = 0; i < tipoElementos.length; i++) {
                    double vol = calcularVolumenAprox(luzAsociada[i], 4.0, factorCalculado[i]);
            %>
            <tr>
                <td><span class="badge">#0<%= (i + 1) %></span></td>
                <td><b><%= tipoElementos[i] %></b></td>
                <td><%= luzAsociada[i] %> m</td>
                <td><%= factorCalculado[i] %> m</td>
                <td><b><%= String.format("%.2f", vol) %> m³</b></td>
            </tr>
            <%
                }
            %>
        </table>

        <!-- 2. EXPRESIÓN JSP: Imprimir directamente el valor de la variable declarada -->
        <p style="margin-top: 20px; font-size: 13px; color: #64748b;">
            <b>Total de componentes registrados en memoria del servidor:</b> <%= totalElementosEvaluados %> elementos analizados bajo normativa técnica.
        </p>
    </div>

</div>

</body>
</html>

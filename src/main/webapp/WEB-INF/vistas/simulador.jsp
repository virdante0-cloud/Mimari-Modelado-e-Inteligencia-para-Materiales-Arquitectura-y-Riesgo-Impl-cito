<%-- Simulador (VISTA con JSTL). Tres pasos independientes, cada uno con su módulo JS y su servlet:
       1) js/carga-imagen.js -> /api/archivos   guarda la fotografía en la cuenta de la persona
       2) js/simulador.js    -> /api/predimensionar (+ presupuesto.js, plano-canvas.js)   calcula con las dimensiones
       3) js/ia.js           -> /api/ia          une foto + dimensiones y consulta a Gemini
     Los módulos se avisan con eventos (mimari:imagen, mimari:calculo). SimuladorServlet solo entrega el historial. --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>MIMARI | Simulador con IA</title>
  <link rel="stylesheet" href="${ctx}/styles.css" />
  <!-- Conversión de fotos HEIC (iPhone) a JPG en el navegador -->
  <script src="https://cdn.jsdelivr.net/npm/heic2any@0.0.4/dist/heic2any.min.js"></script>
  <script src="${ctx}/js/carga-imagen.js" defer></script>
  <script src="${ctx}/js/simulador.js" defer></script>
  <script src="${ctx}/js/plano-canvas.js" defer></script>
  <script src="${ctx}/js/presupuesto.js" defer></script>
  <script src="${ctx}/js/ia.js" defer></script>
</head>
<body>
  <header class="hero">
    <jsp:include page="/WEB-INF/vistas/header.jsp" />
    <div class="hero-content">
      <h1>Simulador de predimensionamiento con IA</h1>
      <p>Carga la fotografía de tu terreno, indica las dimensiones de la edificación y obtén el predimensionamiento, el presupuesto base y el análisis de la IA.</p>
    </div>
  </header>

  <main class="grid">
    <!-- SIMULADOR INTERACTIVO (SIMULACIÓN DE LA METODOLOGÍA) -->
    <article class="card destacado full-width" id="simulador">
      <h2>Simulador Metodológico & Procesamiento IA</h2>
      <p class="sim-subtitle">Carga la fotografía del terreno (se guarda en tu cuenta), indica las dimensiones y calcula. El análisis de la IA usa tu fotografía y esas dimensiones.</p>

      <div class="simulador-container">
        <!-- Paso 1: Carga de Imagen y EXIF -->
        <div class="sim-card">
          <h3>1. Carga de Fotografía del Terreno</h3>
          <p class="nota-chica">Se guarda en tu cuenta apenas la eliges. Es un paso independiente del cálculo.</p>
          <div class="upload-box" id="drop-zone">
            <input type="file" id="file-input" hidden />
            <div id="upload-content">
              <svg viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2" width="48" height="48">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <polyline points="21 15 16 10 5 21"/>
              </svg>
              <p>Haz clic para subir la imagen del lote/terreno</p>
            </div>
            <img id="preview-img" src="" alt="Previsualización terreno" class="hidden" />
          </div>

          <p id="upload-estado" class="sim-estado hidden" role="status"></p>
          <!-- Información real del archivo cargado (la arma carga-imagen.js) -->
          <div id="exif-results" class="results-box hidden"></div>
        </div>

        <!-- Paso 2: Especificación de Materiales y Costos -->
        <div class="sim-card">
          <h3>2. Parámetros de Estructura y Costos Base</h3>
          <form id="calc-form">
            <div class="form-row">
              <div class="form-group">
                <label for="ancho">Ancho del Edificio (m):</label>
                <input type="text" inputmode="decimal" id="ancho" value="6" />
              </div>
              <div class="form-group">
                <label for="largo">Largo del Edificio (m):</label>
                <input type="text" inputmode="decimal" id="largo" value="8" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="pisos">Número de Pisos:</label>
                <input type="text" inputmode="decimal" id="pisos" value="2" />
              </div>
              <div class="form-group">
                <label for="altura-piso">Altura por Piso (m) [mín. 2.20]:</label>
                <input type="text" inputmode="decimal" id="altura-piso" value="2.6" />
              </div>
            </div>

            <div class="form-group">
              <label for="tipo-ladrillo">Tipo de Mampostería Principal:</label>
              <input type="text" id="tipo-ladrillo" value="Ladrillo común rojo (Tolete)" placeholder="Ej. Ladrillo tolete rojo" />
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="precio-ladrillo">Precio por Ladrillo ($):</label>
                <input type="text" inputmode="decimal" id="precio-ladrillo" value="1200" />
              </div>
              <div class="form-group">
                <label for="precio-cristal">Precio m² Cristal ($):</label>
                <input type="text" inputmode="decimal" id="precio-cristal" value="85000" />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label for="num-ventanas">Número de Ventanas:</label>
                <input type="text" inputmode="decimal" id="num-ventanas" value="4" />
              </div>
              <div class="form-group">
                <label for="num-puertas">Número de Puertas:</label>
                <input type="text" inputmode="decimal" id="num-puertas" value="2" />
              </div>
            </div>

            <button type="button" id="btn-procesar" class="btn-primary">Calcular y analizar</button>
            <p id="sim-estado" class="sim-estado hidden" role="status"></p>
          </form>
        </div>
      </div>

      <!-- Paso 3: Resultados, Fórmulas, Verificación Estructural y Plano 2D -->
      <div id="output-section" class="results-container hidden">
        <hr class="divider" />
        <h3>3. Verificación Estructural, Dictamen & Proyección Plani-volumétrica</h3>

        <div class="output-grid">
          <!-- Render IA & Plano Técnico Generado -->
          <div class="render-box">
            <h4>Fotografía analizada</h4>
            <img id="foto-resultado" class="foto-terreno hidden" alt="Fotografía del terreno cargada" />
            <p class="img-caption" id="foto-pie">No hay fotografía cargada.</p>

            <h4 class="margin-top">🤖 Análisis de IA (Gemini)</h4>
            <div class="ia-panel" id="ia-panel">
              <p id="ia-estado" class="sim-estado aviso">Calcula para solicitar el análisis.</p>
              <div id="ia-resultado" class="hidden">
                <p id="ia-resumen"></p>
                <p><strong>Pendiente estimada:</strong> <span id="ia-pendiente"></span></p>
                <h5>Riesgos detectados</h5>
                <ul id="ia-riesgos"></ul>
                <h5>Recomendaciones</h5>
                <ul id="ia-recomendaciones"></ul>
                <p class="img-caption" id="ia-modelo"></p>
              </div>
            </div>

            <!-- Generador de Plano Técnico 2D / Corte Estructural en Canvas -->
            <h4 class="margin-top">📐 Plano de Corte y Predimensionamiento (IA Canvas)</h4>
            <canvas id="canvas-plano" width="400" height="250" style="border: 1px solid #cbd5e1; background: #f8fafc; border-radius: 8px; width: 100%;"></canvas>
            <p class="img-caption">Esquema estructural generado dinámicamente con medidas de vigas, placa y columnas.</p>
          </div>

          <!-- Cuadro de Fórmulas, Predimensionamiento y Presupuesto -->
          <div class="data-box">
            <h4>📊 Desglose Técnico, Cargas y Predimensionamiento</h4>
            
            <!-- Fórmulas Estructurales Aplicadas -->
            <div class="formula-card">
              <h5>Fórmulas Estructurales (NSR-10 / Predimensionamiento):</h5>
              <p><strong>Espesor Placa/Techo:</strong> h = L / 25 (Donde L es la luz libre)</p>
              <p><strong>Peralte Viga Principal:</strong> h_v = L / 10 | <strong>Ancho Viga:</strong> b = h_v / 2</p>
              <p><strong>Volumen Concreto Estructural:</strong> V = Largo × Ancho × Espesor (m³)</p>
            </div>

            <!-- Métricas del Predimensionamiento -->
            <div class="predim-results" style="background: #eff6ff; padding: 12px; border-radius: 6px; margin-bottom: 15px;">
              <p><span id="out-origen" class="origen-badge">—</span></p>
              <p><strong>Altura Total:</strong> <span id="out-altura-total">0.00</span> m &nbsp;|&nbsp; <strong>Área Construida:</strong> <span id="out-area-construida">0.00</span> m²</p>
              <p><strong>Relación de Esbeltez:</strong> <span id="out-esbeltez">0.00</span></p>
              <p><strong>Luz Máxima (L):</strong> <span id="out-luz-libre">4.00</span> m</p>
              <p><strong>Grosor Estimado de Placa (h):</strong> <span id="out-grosor-placa">0.16</span> m (<span id="out-grosor-cm">16</span> cm)</p>
              <p><strong>Sección de Viga Sugerida:</strong> <span id="out-seccion-viga">40 cm x 20 cm</span></p>
              <p><strong>Volumen Concreto Requerido:</strong> <span id="out-volumen-concreto">0.00</span> m³</p>
            </div>

            <table class="table-results">
              <thead>
                <tr>
                  <th>Insumo / Elemento</th>
                  <th>Cantidad Calculada</th>
                  <th>Costo Estimado</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td><span id="out-tipo-ladrillo">Ladrillo</span></td>
                  <td><span id="out-cant-ladrillos">0</span> unidades</td>
                  <td>$<span id="out-costo-ladrillos">0</span></td>
                </tr>
                <tr>
                  <td>Cristal para Ventanas</td>
                  <td><span id="out-cant-cristal">0</span> m²</td>
                  <td>$<span id="out-costo-cristal">0</span></td>
                </tr>
                <tr>
                  <td>Marcos y Puertas Base</td>
                  <td><span id="out-cant-puertas">0</span> unidades</td>
                  <td>$<span id="out-costo-puertas">0</span></td>
                </tr>
              </tbody>
            </table>

            <p id="presupuesto-estado" class="sim-estado hidden" role="status"></p>

            <div class="total-box">
              <strong>Costo Base Estimado:</strong>
              <span id="out-costo-total" class="total-price">$0</span>
            </div>

            <!-- Dictamen y Factor de Seguridad Estructural -->
            <div class="dictamen-box alert-success" id="box-dictamen">
              <strong id="titulo-dictamen">✅ Dictamen de Predimensionamiento:</strong>
              <ul id="texto-dictamen" class="lista-dictamen"></ul>
            </div>
          </div>
        </div>
      </div>
    </article>


    <article class="card destacado full-width" id="historial">
      <h2>Mis análisis recientes</h2>
      <p class="sim-subtitle">Cada análisis de IA queda guardado en tu cuenta.</p>
      <c:choose>
        <c:when test="${empty analisisRecientes}">
          <p class="sim-estado aviso">Aún no tienes análisis de IA guardados.</p>
        </c:when>
        <c:otherwise>
          <table class="table-results">
            <thead><tr><th>Fecha</th><th>Dimensiones</th><th>Motor</th></tr></thead>
            <tbody>
              <c:forEach var="a" items="${analisisRecientes}">
                <tr>
                  <td><fmt:formatDate value="${a.creado}" pattern="dd/MM/yyyy HH:mm" timeZone="America/Bogota" /></td>
                  <td>${a.ancho} x ${a.largo} m · ${a.pisos} piso(s) · ${a.alturaPiso} m</td>
                  <td><c:out value="${a.motor}" /></td>
                </tr>
              </c:forEach>
            </tbody>
          </table>
        </c:otherwise>
      </c:choose>
    </article>
  </main>
</body>
</html>

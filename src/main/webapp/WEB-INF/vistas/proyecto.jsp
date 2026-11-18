<%-- Proyecto (VISTA): planteamiento, objetivo y flujo operativo de MIMARI. Contenido fijo; ProyectoServlet solo hace forward. --%>
<%@ page contentType="text/html;charset=UTF-8" errorPage="/error.jsp" %>
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>MIMARI | El proyecto</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/styles.css" />
</head>
<body>
  <header class="hero">
    <jsp:include page="/WEB-INF/vistas/header.jsp" />
    <div class="hero-content">
      <h1>Dimensionamiento espacial y evaluación de riesgo geotécnico con IA</h1>
      <p>Integración de análisis de imágenes, visión por computador y cálculo estructural normativo para la viabilidad de
        construcción en tiempo real.</p>
    </div>
  </header>

  <main class="grid">
    <!-- Problemática Científica -->
    <article class="card destacado" id="problematica">
      <div class="card-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
          <line x1="12" y1="9" x2="12" y2="13" />
          <line x1="12" y1="17" x2="12.01" y2="17" />
        </svg>
      </div>
      <h2>Planteamiento de la Problemática</h2>
      <p>
        Las herramientas tradicionales de estimación presupuestal y CAD ejecutan cálculos puramente matemáticos sobre
        entornos asumiendo terrenos ideales e isótropos. En la construcción informal y proyectos de pequeña/mediana
        escala, se omite el análisis geotécnico preliminar, lo que genera riesgos severos por vulnerabilidad a deslaves,
        fallas por esfuerzo cortante en muros y sobrecostos por desperdicio de insumos.
      </p>
      <span class="badge">Estatus: Brecha Tecnológica Identificada</span>
    </article>

    <!-- Objetivo General -->
    <article class="card destacado" id="objetivo">
      <div class="card-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="12" cy="12" r="10" />
          <circle cx="12" cy="12" r="6" />
          <circle cx="12" cy="12" r="2" />
        </svg>
      </div>
      <h2>Objetivo General</h2>
      <p>
        Desarrollar una solución web interactiva que sintetice metadatos geográficos (EXIF), segmentación de imágenes
        mediante visión por computador y modelos predictivos de IA, para determinar la viabilidad geotécnica de un lote,
        dimensionar volumétricamente la estructura y emitir un presupuesto optimizado.
      </p>
      <span class="badge">Enfoque: Ingeniería + IA Multimodal</span>
    </article>

    <!-- Justificación -->
    <article class="card destacado" id="justificacion">
      <div class="card-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="#2563eb" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        </svg>
      </div>
      <h2>Justificación y Relevancia</h2>
      <p>
        El proyecto democratiza la evaluación técnica estructural. Al procesar imágenes fotográficas del sitio, el
        sistema extrae las coordenadas GPS para consultar históricos pluviométricos y topográficos, correlacionándolos
        con la inclinación del talud estimada por visión artificial para prevenir desastres.
      </p>
      <span class="badge">Impacto: Mitigación de Riesgo</span>
    </article>

    <!-- Arquitectura de Procesamiento de IA -->
    <article class="card destacado full-width" id="procesamiento">
      <h2>Flujo Operativo de Procesamiento por IA</h2>
      <div class="pipeline-steps">
        <div class="step">
          <div class="step-num">01</div>
          <h3>Extracción EXIF & Geo-localización</h3>
          <p>Lectura de metadatos de la imagen subida (coordenadas GPS, altitud y hora) para consultar APIS meteorológicas.</p>
        </div>
        <div class="step">
          <div class="step-num">02</div>
          <h3>Análisis Visión por Computador</h3>
          <p>Segmentación semántica del terreno para identificar pendientes y cohesión aparente del suelo.</p>
        </div>
        <div class="step">
          <div class="step-num">03</div>
          <h3>Cálculo de Cargas y Vanos</h3>
          <p>Aplicación de fórmulas mecánicas para evaluar el peso muerto de mampostería vs. áreas de apertura.</p>
        </div>
        <div class="step">
          <div class="step-num">04</div>
          <h3>Dictamen de Viabilidad & Render</h3>
          <p>Generación de reporte de estabilidad y sintetización visual 3D descriptiva de la estructura final.</p>
        </div>
      </div>
    </article>

  </main>
</body>
</html>

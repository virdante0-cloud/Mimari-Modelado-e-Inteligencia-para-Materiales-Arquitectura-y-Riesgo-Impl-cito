/**
 * PROGRAMACIÓN III - SEMANA 05
 * Proyecto: MIMARI
 * Integración de Manipulación del DOM, Funciones de Acceso, Filtro Dinámico y Predimensionamiento Estructural
 * Estudiante: Diego Alejandro Vargas Torres
 */

// ==========================================
// A. VARIABLES GLOBALES DE ALMACENAMIENTO
// ==========================================
let imagenSeleccionada = null;
let metadatosImagen = null;

const LIMITE_MB = 5;
const LIMITE_BYTES = LIMITE_MB * 1024 * 1024;

// ==========================================
// B. CLASE Y POO (GESTIÓN DE INSUMOS)
// ==========================================
class Material {
  constructor(nombre, tipo, precioUnitario, cantidad) {
    this.nombre = nombre;
    this.tipo = tipo;
    this.precioUnitario = Number(precioUnitario);
    this.cantidad = Number(cantidad);
  }

  calcularSubtotal() {
    return this.precioUnitario * this.cantidad;
  }

  resumen() {
    const subtotalFormat = this.calcularSubtotal().toLocaleString("es-CO");
    const precioFormat = this.precioUnitario.toLocaleString("es-CO");
    return `${this.nombre} (${this.tipo}) — ${this.cantidad} unds × $${precioFormat} = Subtotal: $${subtotalFormat}`;
  }
}

const listaMateriales = [];

// ==========================================
// C. FUNCIONES DE ACCESO AL DOM (SEMANA 5)
// ==========================================
// Uso de getElementById()
const dropZone = document.getElementById("drop-zone");
const fileInput = document.getElementById("file-input");
const formMaterial = document.getElementById("form-material");
const inputNombre = document.getElementById("mat-nombre");
const inputTipo = document.getElementById("mat-tipo");
const inputPrecio = document.getElementById("mat-precio");
const inputCantidad = document.getElementById("mat-cantidad");
const ulLista = document.getElementById("lista-materiales");
const btnProcesar = document.getElementById("btn-procesar");

// Uso de querySelector()
const spanTotal = document.querySelector("#costo-acumulado-total");
const inputBusqueda = document.querySelector("#input-busqueda");

// ==========================================
// D. PROCESAMIENTO DE IMÁGENES Y METADATOS
// ==========================================
if (dropZone && fileInput) {
  dropZone.addEventListener("click", () => fileInput.click());

  fileInput.addEventListener("change", async (e) => {
    const fileOriginal = e.target.files[0];
    if (!fileOriginal) return;

    if (fileOriginal.size > LIMITE_BYTES) {
      alert(`La imagen supera el límite permitido de ${LIMITE_MB} MB. Sube una foto más liviana.`);
      fileInput.value = "";
      return;
    }

    procesarMetadatos(fileOriginal);

    let fileParaRender = fileOriginal;
    const esHeic = fileOriginal.name.toLowerCase().endsWith(".heic") || 
                   fileOriginal.name.toLowerCase().endsWith(".heif") || 
                   fileOriginal.type === "image/heic";

    if (esHeic && typeof heic2any === "function") {
      try {
        const convertedBlob = await heic2any({
          blob: fileOriginal,
          toType: "image/jpeg",
          quality: 0.8
        });
        fileParaRender = new File([convertedBlob], fileOriginal.name.replace(/\.(heic|heif)$/i, ".jpg"), { type: "image/jpeg" });
      } catch (err) {
        console.warn("No se pudo convertir HEIC en el cliente:", err);
      }
    }

    const reader = new FileReader();
    reader.onload = (event) => {
      imagenSeleccionada = event.target.result;
      const previewImg = document.querySelector("#preview-img");
      if (previewImg) {
        previewImg.src = imagenSeleccionada;
        previewImg.classList.remove("hidden");
      }
    };
    reader.readAsDataURL(fileParaRender);
  });
}

function procesarMetadatos(file) {
  metadatosImagen = {
    nombreArchivo: file.name,
    tamanoMB: (file.size / (1024 * 1024)).toFixed(2) + " MB",
    tipo: file.type || "image/jpeg"
  };

  const exifResults = document.querySelector("#exif-results");
  if (exifResults) {
    exifResults.innerHTML = `
      <h4>📍 Información del Archivo Cargado</h4>
      <ul>
        <li><strong>Nombre:</strong> ${metadatosImagen.nombreArchivo}</li>
        <li><strong>Tamaño:</strong> ${metadatosImagen.tamanoMB}</li>
        <li><strong>Formato:</strong> ${metadatosImagen.tipo}</li>
      </ul>
    `;
    exifResults.classList.remove("hidden");
  }
}

// ==========================================
// E. REGISTRO Y EVENTOS DEL FORMULARIO
// ==========================================
if (formMaterial) {
  formMaterial.addEventListener("submit", (event) => {
    event.preventDefault();

    const nuevoMaterial = new Material(
      inputNombre.value,
      inputTipo.value,
      inputPrecio.value,
      inputCantidad.value
    );

    listaMateriales.push(nuevoMaterial);
    
    // Actualizar lista y resetear filtro
    if (inputBusqueda) inputBusqueda.value = "";
    actualizarListadoMateriales(listaMateriales);

    formMaterial.reset();
    inputNombre.focus();
  });
}

// ==========================================
// F. LÓGICA DE FILTRADO Y MANIPULACIÓN DEL DOM (SEMANA 5)
// ==========================================

/**
 * Evento 'input' para el filtro dinámico en tiempo real
 */
if (inputBusqueda) {
  inputBusqueda.addEventListener("input", (e) => {
    const textoConsulta = e.target.value.toLowerCase().trim();
    filtrarInsumosMIMARI(textoConsulta);
  });
}

/**
 * Función encargada de filtrar el arreglo según el texto digitado
 * @param {string} criterio 
 */
function filtrarInsumosMIMARI(criterio) {
  const materialesFiltrados = listaMateriales.filter((mat) => {
    const nombreCoincide = mat.nombre.toLowerCase().includes(criterio);
    const tipoCoincide = mat.tipo.toLowerCase().includes(criterio);
    return nombreCoincide || tipoCoincide;
  });

  actualizarListadoMateriales(materialesFiltrados);
}

/**
 * Función para renderizar dinámicamente los elementos en la interfaz
 * @param {Array} arregloAMostrar 
 */
function actualizarListadoMateriales(arregloAMostrar) {
  if (!ulLista) return;
  ulLista.innerHTML = "";
  let totalAcumulado = 0;

  if (arregloAMostrar.length === 0) {
    ulLista.innerHTML = `<li class="sin-resultados">Sin insumos registrados que coincidan.</li>`;
  } else {
    arregloAMostrar.forEach((mat) => {
      const li = document.createElement("li");
      li.className = "item-material";
      li.textContent = mat.resumen();
      ulLista.appendChild(li);

      totalAcumulado += mat.calcularSubtotal();
    });
  }

  if (spanTotal) {
    spanTotal.textContent = `$${totalAcumulado.toLocaleString("es-CO")}`;
  }

  destacarElementosEnDOM();
}

/**
 * Función que demuestra la selección múltiple con querySelectorAll()
 */
function destacarElementosEnDOM() {
  const elementosLi = document.querySelectorAll(".item-material");
  elementosLi.forEach((li, index) => {
    li.style.borderLeft = index % 2 === 0 ? "4px solid #2563eb" : "4px solid #059669";
  });
}

// ==========================================
// G. MÓDULO DE PREDISEÑO ESTRUCTURAL Y PLANOS (INGENIERÍA & ARQUITECTURA)
// ==========================================

/**
 * Realiza los cálculos de predimensionamiento de vigas, losas y volumen de concreto
 * @param {number} luzLibreM - Longitud en metros del espacio entre apoyos (L)
 * @param {number} areaTotal - Área total proyectada en m²
 */
function calcularPredimensionamientoEstructural(luzLibreM = 4.0, areaTotal = 36.0) {
  // 1. Predimensionamiento de Placa / Techo: h = L / 25
  const grosorPlaca = luzLibreM / 25; // en metros

  // 2. Predimensionamiento de Vigas Principales: h_v = L / 10, b = h_v / 2
  const peralteViga = luzLibreM / 10; // en metros
  const baseViga = peralteViga / 2;    // en metros

  // 3. Cálculo de Volumen de Concreto en m³: V = Largo x Ancho x Espesor
  const volumenConcreto = (areaTotal * grosorPlaca).toFixed(2);

  // 4. Actualización de elementos en el DOM
  const elemLuz = document.getElementById("out-luz-libre");
  const elemGrosor = document.getElementById("out-grosor-placa");
  const elemViga = document.getElementById("out-seccion-viga");
  const elemVolumen = document.getElementById("out-volumen-concreto");

  if (elemLuz) elemLuz.textContent = luzLibreM.toFixed(2);
  if (elemGrosor) elemGrosor.textContent = grosorPlaca.toFixed(2);
  if (elemViga) elemViga.textContent = `${(peralteViga * 100).toFixed(0)} cm x ${(baseViga * 100).toFixed(0)} cm`;
  if (elemVolumen) elemVolumen.textContent = volumenConcreto;

  // Renderizar plano 2D en el Canvas
  dibujarPlanoEstructural2D(luzLibreM, grosorPlaca, peralteViga);
}

/**
 * Genera el dibujo vectorial del plano de corte estructural con cotas usando HTML5 Canvas
 */
function dibujarPlanoEstructural2D(luz, hPlaca, hViga) {
  const canvas = document.getElementById("canvas-plano");
  if (!canvas) return;
  const ctx = canvas.getContext("2d");

  // Limpiar lienzo
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // Fondo técnico
  ctx.fillStyle = "#f1f5f9";
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  // Dibujar terreno / cimentación
  ctx.fillStyle = "#cbd5e1";
  ctx.fillRect(20, 200, 360, 30);

  // Columnas
  ctx.fillStyle = "#475569";
  ctx.fillRect(50, 80, 20, 120);  // Columna Izq
  ctx.fillRect(330, 80, 20, 120); // Columna Der

  // Viga Principal (L / 10)
  ctx.fillStyle = "#2563eb";
  const altoVigaPx = Math.max(15, hViga * 40);
  ctx.fillRect(50, 80, 300, altoVigaPx);

  // Placa de Techo (L / 25)
  ctx.fillStyle = "#1e40af";
  const altoPlacaPx = Math.max(8, hPlaca * 40);
  ctx.fillRect(40, 80 - altoPlacaPx, 320, altoPlacaPx);

  // Cotas y Textos de medidas
  ctx.fillStyle = "#0f172a";
  ctx.font = "12px sans-serif";
  ctx.fillText(`Luz (L) = ${luz.toFixed(1)}m`, 160, 130);
  ctx.fillText(`Placa (h) = ${(hPlaca * 100).toFixed(0)}cm`, 160, 65);
  ctx.fillText(`Viga = ${(hViga * 100).toFixed(0)}cm`, 160, 100);

  // Línea de Cota de Luz
  ctx.beginPath();
  ctx.moveTo(70, 140);
  ctx.lineTo(330, 140);
  ctx.strokeStyle = "#0f172a";
  ctx.lineWidth = 1;
  ctx.stroke();
}

// ==========================================
// H. PROCESAMIENTO GENERAL DEL SIMULADOR
// ==========================================
if (btnProcesar) {
  btnProcesar.addEventListener("click", () => {
    // 1. Mostrar la sección de resultados
    const outputSection = document.getElementById("output-section");
    if (outputSection) {
      outputSection.classList.remove("hidden");
    }

    // 2. Obtener valores introducidos por el usuario
    const tipoLadrillo = document.getElementById("tipo-ladrillo")?.value || "Ladrillo Estándar";
    const precioLadrillo = Number(document.getElementById("precio-ladrillo")?.value) || 1200;
    const precioCristal = Number(document.getElementById("precio-cristal")?.value) || 85000;
    const numVentanas = Number(document.getElementById("num-ventanas")?.value) || 0;
    const numPuertas = Number(document.getElementById("num-puertas")?.value) || 0;

    // 3. Cálculos de mampostería y vanos
    const areaVentanas = numVentanas * 1.5; // Supuesto de 1.5 m² por ventana
    const areaPuertas = numPuertas * 2.0;   // Supuesto de 2.0 m² por puerta
    const areaTotalMuros = 40.0;            // Muro estándar de prueba
    const areaNeta = Math.max(0, areaTotalMuros - (areaVentanas + areaPuertas));

    const cantLadrillos = Math.ceil(areaNeta * 55 * 1.05); // 55 ladrillos/m² + 5% desperdicio
    const costoLadrillos = cantLadrillos * precioLadrillo;
    const costoCristal = areaVentanas * precioCristal;
    const costoPuertas = numPuertas * 150000; // Costo promedio estimado marco/puerta
    const costoTotal = costoLadrillos + costoCristal + costoPuertas;

    // 4. Actualización del DOM en el Cuadro Presupuestal
    const outTipoLadrillo = document.getElementById("out-tipo-ladrillo");
    const outCantLadrillos = document.getElementById("out-cant-ladrillos");
    const outCostoLadrillos = document.getElementById("out-costo-ladrillos");
    const outCantCristal = document.getElementById("out-cant-cristal");
    const outCostoCristal = document.getElementById("out-costo-cristal");
    const outCantPuertas = document.getElementById("out-cant-puertas");
    const outCostoPuertas = document.getElementById("out-costo-puertas");
    const outCostoTotal = document.getElementById("out-costo-total");

    if (outTipoLadrillo) outTipoLadrillo.textContent = tipoLadrillo;
    if (outCantLadrillos) outCantLadrillos.textContent = cantLadrillos.toLocaleString("es-CO");
    if (outCostoLadrillos) outCostoLadrillos.textContent = costoLadrillos.toLocaleString("es-CO");
    if (outCantCristal) outCantCristal.textContent = areaVentanas.toFixed(1);
    if (outCostoCristal) outCostoCristal.textContent = costoCristal.toLocaleString("es-CO");
    if (outCantPuertas) outCantPuertas.textContent = numPuertas;
    if (outCostoPuertas) outCostoPuertas.textContent = costoPuertas.toLocaleString("es-CO");
    if (outCostoTotal) outCostoTotal.textContent = `$${costoTotal.toLocaleString("es-CO")}`;

    // 5. Ejecutar módulo de predimensionamiento e ilustración en Canvas
    calcularPredimensionamientoEstructural(4.5, 36.0);

    // Desplazar vista suavemente hacia los resultados
    outputSection?.scrollIntoView({ behavior: "smooth" });
  });
}
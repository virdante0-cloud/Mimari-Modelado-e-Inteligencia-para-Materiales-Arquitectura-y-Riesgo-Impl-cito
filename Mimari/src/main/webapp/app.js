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
let archivoParaEnviar = null; // archivo (ya convertido si era HEIC) que se envía al servidor Java

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
      archivoParaEnviar = null;
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

    archivoParaEnviar = fileParaRender;

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

function escaparHTML(texto) {
  const div = document.createElement("div");
  div.textContent = texto;
  return div.innerHTML;
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
        <li><strong>Nombre:</strong> ${escaparHTML(metadatosImagen.nombreArchivo)}</li>
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
// G. CÁLCULO ESTRUCTURAL: SERVIDOR JAVA (TOMCAT) + RESPALDO LOCAL
// ==========================================
// El cálculo "oficial" lo hace el servlet Java (PredimensionamientoServlet.java) en /api/predimensionar.
// La URL es RELATIVA para que funcione tanto en /mimari/ como en /mimari/index.html.
// Si el servidor no responde (p. ej. abres index.html con doble clic), se usa calcularLocal(),
// que aplica exactamente las mismas fórmulas, y la pantalla lo avisa.
const API_URL = "api/predimensionar";

class ErrorValidacion extends Error {}

function leerEntrada() {
  return {
    ancho: Number(document.getElementById("ancho")?.value),
    largo: Number(document.getElementById("largo")?.value),
    pisos: Number(document.getElementById("pisos")?.value),
    alturaPiso: Number(document.getElementById("altura-piso")?.value)
  };
}

// Mismas reglas y mensajes que calcular() del servlet
function validarEntrada({ ancho, largo, pisos, alturaPiso }) {
  if (!Number.isFinite(ancho) || ancho <= 0 || ancho > 500) throw new ErrorValidacion("El ancho debe estar entre 0 y 500 m.");
  if (!Number.isFinite(largo) || largo <= 0 || largo > 500) throw new ErrorValidacion("El largo debe estar entre 0 y 500 m.");
  if (!Number.isInteger(pisos) || pisos < 1 || pisos > 100) throw new ErrorValidacion("El número de pisos debe estar entre 1 y 100.");
  if (!Number.isFinite(alturaPiso) || alturaPiso <= 0 || alturaPiso > 20) throw new ErrorValidacion("La altura por piso debe estar entre 0 y 20 m.");
}

async function calcularEnServidor(entrada, archivo) {
  const datos = new FormData();
  datos.append("ancho", entrada.ancho);
  datos.append("largo", entrada.largo);
  datos.append("pisos", entrada.pisos);
  datos.append("alturaPiso", entrada.alturaPiso);
  if (archivo) datos.append("planoImagen", archivo, archivo.name);

  const respuesta = await fetch(API_URL, { method: "POST", body: datos });

  let cuerpo = null;
  try { cuerpo = await respuesta.json(); } catch (_) { /* la respuesta no era JSON */ }

  if (!respuesta.ok) {
    if (respuesta.status === 400 && cuerpo && cuerpo.error) throw new ErrorValidacion(cuerpo.error);
    throw new Error(`El servidor respondió HTTP ${respuesta.status}`);
  }
  if (!cuerpo || cuerpo.ok !== true) throw new Error("Respuesta inesperada del servidor");
  return cuerpo;
}

// Réplica de calcular() del servlet para trabajar sin servidor
function calcularLocal({ ancho, largo, pisos, alturaPiso }) {
  const alturaTotal = pisos * alturaPiso;
  const areaBase = ancho * largo;
  const areaTotalConstruida = areaBase * pisos;
  const perimetro = 2 * (ancho + largo);
  const luz = Math.max(ancho, largo);
  const peralteViga = luz / 10;
  const espesorPlaca = luz / 25;
  const relacionEsbeltez = alturaTotal / Math.min(ancho, largo);

  const alertas = [];
  if (alturaPiso < 2.20) {
    alertas.push({ nivel: "peligro", mensaje: `ALERTA CRÍTICA: La altura por piso (${alturaPiso.toFixed(2)} m) viola el mínimo normativo de habitabilidad (2.20 m).` });
  } else {
    alertas.push({ nivel: "exito", mensaje: "Altura por piso conforme a la normativa de confort." });
  }
  if (relacionEsbeltez > 4.0) {
    alertas.push({ nivel: "peligro", mensaje: `ALERTA SÍSMICA: Relación de esbeltez elevada (${relacionEsbeltez.toFixed(2)}). Alto riesgo de volcamiento.` });
  }

  return {
    ancho, largo, pisos, alturaPiso, alturaTotal, areaBase, areaTotalConstruida, perimetro,
    areaMuros: perimetro * alturaTotal,
    luz, peralteViga, baseViga: peralteViga / 2, espesorPlaca,
    volumenConcreto: areaTotalConstruida * espesorPlaca,
    relacionEsbeltez, alertas, archivo: ""
  };
}

function mostrarEstado(texto, tipo) {
  const estado = document.getElementById("sim-estado");
  if (!estado) return;
  if (!texto) {
    estado.classList.add("hidden");
    estado.textContent = "";
    return;
  }
  estado.textContent = texto;
  estado.className = `sim-estado ${tipo || "aviso"}`;
}

function asignarTexto(id, valor) {
  const el = document.getElementById(id);
  if (el) el.textContent = valor;
}

/**
 * Pinta predimensionamiento, esbeltez, dictamen y plano 2D a partir del objeto de cálculo
 * (el mismo formato lo entrega el servidor Java o calcularLocal()).
 */
function mostrarPredimensionamiento(c, origen) {
  asignarTexto("out-luz-libre", c.luz.toFixed(2));
  asignarTexto("out-grosor-placa", c.espesorPlaca.toFixed(2));
  asignarTexto("out-grosor-cm", (c.espesorPlaca * 100).toFixed(0));
  asignarTexto("out-seccion-viga", `${(c.peralteViga * 100).toFixed(0)} cm x ${(c.baseViga * 100).toFixed(0)} cm`);
  asignarTexto("out-volumen-concreto", c.volumenConcreto.toFixed(2));
  asignarTexto("out-altura-total", c.alturaTotal.toFixed(2));
  asignarTexto("out-area-construida", c.areaTotalConstruida.toFixed(2));
  asignarTexto("out-esbeltez", c.relacionEsbeltez.toFixed(2));

  const badge = document.getElementById("out-origen");
  if (badge) {
    badge.textContent = origen === "servidor" ? "Calculado en el servidor Java (Tomcat)" : "Calculado localmente (sin servidor)";
    badge.className = `origen-badge ${origen}`;
  }

  mostrarDictamen(c);
  dibujarPlanoEstructural2D(c.luz, c.espesorPlaca, c.peralteViga);
}

function mostrarDictamen(c) {
  const caja = document.getElementById("box-dictamen");
  const titulo = document.getElementById("titulo-dictamen");
  const lista = document.getElementById("texto-dictamen");
  if (!caja || !lista) return;

  const hayPeligro = c.alertas.some((a) => a.nivel === "peligro");
  caja.classList.toggle("alert-danger", hayPeligro);
  caja.classList.toggle("alert-success", !hayPeligro);
  if (titulo) {
    titulo.textContent = hayPeligro
      ? "⚠️ Dictamen de Predimensionamiento: se detectaron alertas"
      : "✅ Dictamen de Predimensionamiento: sin alertas";
  }

  lista.innerHTML = "";
  c.alertas.forEach((a) => {
    const li = document.createElement("li");
    li.textContent = a.mensaje;
    lista.appendChild(li);
  });
  if (!hayPeligro) {
    const li = document.createElement("li");
    li.textContent = `Relación de esbeltez ${c.relacionEsbeltez.toFixed(2)} (límite 4.00).`;
    lista.appendChild(li);
  }
}

/**
 * Dibuja el plano de corte estructural con cotas usando HTML5 Canvas
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

  // Viga Principal (L / 10) — altura limitada para que quepa en el dibujo
  ctx.fillStyle = "#2563eb";
  const altoVigaPx = Math.min(45, Math.max(15, hViga * 40));
  ctx.fillRect(50, 80, 300, altoVigaPx);

  // Placa de Techo (L / 25)
  ctx.fillStyle = "#1e40af";
  const altoPlacaPx = Math.min(20, Math.max(8, hPlaca * 40));
  ctx.fillRect(40, 80 - altoPlacaPx, 320, altoPlacaPx);

  // Cotas y Textos de medidas
  ctx.fillStyle = "#0f172a";
  ctx.font = "12px sans-serif";
  ctx.fillText(`Luz (L) = ${luz.toFixed(1)}m`, 160, 160);
  ctx.fillText(`Placa (h) = ${(hPlaca * 100).toFixed(0)}cm`, 160, 65);
  ctx.fillStyle = "#ffffff";
  ctx.fillText(`Viga = ${(hViga * 100).toFixed(0)}cm`, 160, 80 + altoVigaPx / 2 + 4);

  // Línea de Cota de Luz
  ctx.beginPath();
  ctx.moveTo(70, 170);
  ctx.lineTo(330, 170);
  ctx.strokeStyle = "#0f172a";
  ctx.lineWidth = 1;
  ctx.stroke();
}

// ==========================================
// H. PROCESAMIENTO GENERAL DEL SIMULADOR
// ==========================================
function mostrarPresupuesto(c) {
  const tipoLadrillo = document.getElementById("tipo-ladrillo")?.value || "Ladrillo Estándar";
  const precioLadrillo = Number(document.getElementById("precio-ladrillo")?.value) || 1200;
  const precioCristal = Number(document.getElementById("precio-cristal")?.value) || 85000;
  const numVentanas = Number(document.getElementById("num-ventanas")?.value) || 0;
  const numPuertas = Number(document.getElementById("num-puertas")?.value) || 0;

  // Mampostería y vanos: el área de muros sale de la geometría real (perímetro × altura total)
  const areaVentanas = numVentanas * 1.5; // Supuesto de 1.5 m² por ventana
  const areaPuertas = numPuertas * 2.0;   // Supuesto de 2.0 m² por puerta
  const areaNeta = Math.max(0, c.areaMuros - (areaVentanas + areaPuertas));

  const cantLadrillos = Math.ceil(areaNeta * 55 * 1.05); // 55 ladrillos/m² + 5% desperdicio
  const costoLadrillos = cantLadrillos * precioLadrillo;
  const costoCristal = areaVentanas * precioCristal;
  const costoPuertas = numPuertas * 150000; // Costo promedio estimado marco/puerta
  const costoTotal = costoLadrillos + costoCristal + costoPuertas;

  asignarTexto("out-tipo-ladrillo", tipoLadrillo);
  asignarTexto("out-cant-ladrillos", cantLadrillos.toLocaleString("es-CO"));
  asignarTexto("out-costo-ladrillos", costoLadrillos.toLocaleString("es-CO"));
  asignarTexto("out-cant-cristal", areaVentanas.toFixed(1));
  asignarTexto("out-costo-cristal", costoCristal.toLocaleString("es-CO"));
  asignarTexto("out-cant-puertas", numPuertas);
  asignarTexto("out-costo-puertas", costoPuertas.toLocaleString("es-CO"));
  asignarTexto("out-costo-total", `$${costoTotal.toLocaleString("es-CO")}`);
}

if (btnProcesar) {
  btnProcesar.addEventListener("click", async () => {
    const entrada = leerEntrada();

    try {
      validarEntrada(entrada);
    } catch (err) {
      mostrarEstado(err.message, "error");
      return;
    }

    const textoBoton = btnProcesar.textContent;
    btnProcesar.disabled = true;
    btnProcesar.textContent = "Calculando…";

    try {
      let calculo;
      let origen;
      try {
        calculo = await calcularEnServidor(entrada, archivoParaEnviar);
        origen = "servidor";
        mostrarEstado(archivoParaEnviar && calculo.archivo ? calculo.archivo : "", "ok");
      } catch (err) {
        if (err instanceof ErrorValidacion) throw err;
        console.warn("Servidor Java no disponible, se calcula en el navegador:", err);
        calculo = calcularLocal(entrada);
        origen = "local";
        mostrarEstado("No se pudo contactar al servidor Java: el cálculo se hizo en el navegador con las mismas fórmulas.", "aviso");
      }

      const outputSection = document.getElementById("output-section");
      outputSection?.classList.remove("hidden");

      mostrarPredimensionamiento(calculo, origen);
      mostrarPresupuesto(calculo);

      outputSection?.scrollIntoView({ behavior: "smooth" });
    } catch (err) {
      mostrarEstado(err.message, "error");
    } finally {
      btnProcesar.disabled = false;
      btnProcesar.textContent = textoBoton;
    }
  });
}

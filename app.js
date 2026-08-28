/**
 * PROGRAMACIÓN III - SEMANA 04
 * Proyecto: MIMARI - Lector Avanzado de Metadatos EXIF y Gestión POO
 * Estudiante: Diego Alejandro Vargas Torres
 */

// ==========================================
// A. VARIABLES GLOBALES DE ALMACENAMIENTO
// ==========================================
let imagenSeleccionada = null; // String Base64 listo para la API de IA
let metadatosImagen = null;    // Objeto con la información técnica y geográfica real

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
// C. ELEMENTOS DEL DOM
// ==========================================
// Elementos de Carga y Previsualización
const dropZone = document.getElementById("drop-zone");
const fileInput = document.getElementById("file-input");
const uploadContent = document.getElementById("upload-content");
const previewImg = document.getElementById("preview-img");
const exifResults = document.getElementById("exif-results");

// Elementos del Formulario POO
const formMaterial = document.getElementById("form-material");
const inputNombre = document.getElementById("mat-nombre");
const inputTipo = document.getElementById("mat-tipo");
const inputPrecio = document.getElementById("mat-precio");
const inputCantidad = document.getElementById("mat-cantidad");
const ulLista = document.getElementById("lista-materiales");
const spanTotal = document.getElementById("costo-acumulado-total");

// ==========================================
// D. EVENTOS DE CARGA Y PROCESAMIENTO DE IMAGEN
// ==========================================
dropZone.addEventListener("click", () => fileInput.click());

fileInput.addEventListener("change", async (e) => {
  const fileOriginal = e.target.files[0];
  if (!fileOriginal) return;

  // 1. Validar límite de peso (5 MB)
  if (fileOriginal.size > LIMITE_BYTES) {
    alert(`La imagen supera el límite permitido de ${LIMITE_MB} MB. Sube una foto más liviana.`);
    fileInput.value = "";
    return;
  }

  // Restablecer vista previa de carga
  uploadContent.innerHTML = `<p>🔄 Procesando archivo...</p>`;
  uploadContent.classList.remove("hidden");
  previewImg.classList.add("hidden");
  exifResults.classList.add("hidden");

  // 2. Extraer EXIF binario directamente del archivo original
  await procesarExifSeguro(fileOriginal);

  // 3. Gestionar previsualización (Convertir si es HEIC)
  let fileParaRender = fileOriginal;
  const esHeic = fileOriginal.name.toLowerCase().endsWith(".heic") || 
                 fileOriginal.name.toLowerCase().endsWith(".heif") || 
                 fileOriginal.type === "image/heic";

  if (esHeic) {
    try {
      uploadContent.innerHTML = `<p>🔄 Convirtiendo HEIC a JPG para vista previa...</p>`;
      const convertedBlob = await heic2any({
        blob: fileOriginal,
        toType: "image/jpeg",
        quality: 0.8
      });

      fileParaRender = new File(
        [convertedBlob], 
        fileOriginal.name.replace(/\.(heic|HEIC|heif|HEIF)$/, ".jpg"), 
        { type: "image/jpeg" }
      );
    } catch (error) {
      console.warn("No se pudo renderizar la vista previa HEIC:", error);
    }
  }

  // 4. Mostrar vista previa e instanciar Base64
  const reader = new FileReader();
  reader.onload = (event) => {
    imagenSeleccionada = event.target.result;
    previewImg.src = imagenSeleccionada;
    previewImg.classList.remove("hidden");
    uploadContent.classList.add("hidden");
  };

  reader.readAsDataURL(fileParaRender);
});

// ==========================================
// FUNCIÓN ROBUSTA DE LECTURA EXIF
// ==========================================
function procesarExifSeguro(file) {
  return new Promise((resolve) => {
    try {
      EXIF.getData(file, function () {
        const camara = EXIF.getTag(this, "Model") || EXIF.getTag(this, "Make");
        const fecha = EXIF.getTag(this, "DateTimeOriginal") || EXIF.getTag(this, "DateTime");
        const lat = EXIF.getTag(this, "GPSLatitude");
        const lon = EXIF.getTag(this, "GPSLongitude");
        const latRef = EXIF.getTag(this, "GPSLatitudeRef") || "N";
        const lonRef = EXIF.getTag(this, "GPSLongitudeRef") || "W";

        let gpsTexto = null;
        if (lat && lon && Array.isArray(lat) && Array.isArray(lon)) {
          const latDec = (lat[0] + lat[1] / 60 + lat[2] / 3600).toFixed(4);
          const lonDec = (lon[0] + lon[1] / 60 + lon[2] / 3600).toFixed(4);
          gpsTexto = `${latDec}° ${latRef}, ${lonDec}° ${lonRef}`;
        }

        const tieneMetadata = camara || fecha || gpsTexto;

        if (tieneMetadata) {
          metadatosImagen = {
            estado: "Con Metadatos EXIF",
            nombreArchivo: file.name,
            tamanoMB: (file.size / (1024 * 1024)).toFixed(2) + " MB",
            camara: camara || "Dispositivo Móvil",
            fechaCaptura: fecha || "No especificada",
            coordenadas: gpsTexto || "Sin coordenadas GPS registradas"
          };

          exifResults.innerHTML = `
            <h4>📍 Metadatos EXIF Extraídos de la Fotografía</h4>
            <ul>
              <li><strong>Dispositivo:</strong> ${metadatosImagen.camara}</li>
              <li><strong>Fecha Captura:</strong> ${metadatosImagen.fechaCaptura}</li>
              <li><strong>Ubicación GPS:</strong> ${metadatosImagen.coordenadas}</li>
              <li><strong>Peso Archivo:</strong> ${metadatosImagen.tamanoMB}</li>
            </ul>
          `;
        } else {
          establecerSinMetadatos(file);
        }

        exifResults.classList.remove("hidden");
        resolve();
      });
    } catch (err) {
      console.warn("Excepción capturada en EXIF:", err);
      establecerSinMetadatos(file);
      exifResults.classList.remove("hidden");
      resolve();
    }
  });
}

function establecerSinMetadatos(file) {
  metadatosImagen = {
    estado: "Sin Metadatos EXIF",
    nombreArchivo: file.name,
    tamanoMB: (file.size / (1024 * 1024)).toFixed(2) + " MB",
    coordenadas: null
  };

  exifResults.innerHTML = `
    <h4>⚠️ Sin Metadatos EXIF Detectados</h4>
    <p>La imagen cargada (<strong>${file.name}</strong> - ${metadatosImagen.tamanoMB}) no contiene metadatos de cámara o GPS en sus cabeceras. La IA usará evaluación geovisual.</p>
  `;
}

// ==========================================
// F. LÓGICA DE REGISTRO POO (FORMULARIO)
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
    renderizarLista();

    formMaterial.reset();
    inputNombre.focus();
  });
}

function renderizarLista() {
  if (!ulLista) return;
  ulLista.innerHTML = "";
  let totalAcumulado = 0;

  listaMateriales.forEach((mat) => {
    const li = document.createElement("li");
    li.className = "item-material";
    li.textContent = mat.resumen();
    ulLista.appendChild(li);

    totalAcumulado += mat.calcularSubtotal();
  });

  if (spanTotal) {
    spanTotal.textContent = `$${totalAcumulado.toLocaleString("es-CO")}`;
  }
}
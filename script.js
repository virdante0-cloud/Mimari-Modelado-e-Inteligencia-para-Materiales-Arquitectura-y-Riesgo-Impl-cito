document.addEventListener("DOMContentLoaded", () => {
  // Elementos DOM Carga de Imagen
  const dropZone = document.getElementById("drop-zone");
  const fileInput = document.getElementById("file-input");
  const uploadContent = document.getElementById("upload-content");
  const previewImg = document.getElementById("preview-img");
  const exifResults = document.getElementById("exif-results");

  // Elementos DOM Formulario y Resultados
  const btnProcesar = document.getElementById("btn-procesar");
  const outputSection = document.getElementById("output-section");

  // Evento para abrir selector de archivos
  dropZone.addEventListener("click", () => fileInput.click());

  // Evento al seleccionar una imagen
  fileInput.addEventListener("change", (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        previewImg.src = event.target.result;
        previewImg.classList.remove("hidden");
        uploadContent.classList.add("hidden");
        // Simular lectura EXIF
        exifResults.classList.remove("hidden");
      };
      reader.readAsDataURL(file);
    }
  });

  // Procesamiento y Cálculo
  btnProcesar.addEventListener("click", () => {
    // Captura de valores de entrada
    const tipoLadrillo = document.getElementById("tipo-ladrillo").value;
    const precioLadrillo = parseFloat(document.getElementById("precio-ladrillo").value) || 0;
    const precioCristal = parseFloat(document.getElementById("precio-cristal").value) || 0;
    const numVentanas = parseInt(document.getElementById("num-ventanas").value) || 0;
    const numPuertas = parseInt(document.getElementById("num-puertas").value) || 0;

    // Supuestos de dimensiones estándar para la simulación
    const areaParedesBase = 60; // 60 m^2 de muro estándar
    const areaPorVentana = 1.5; // 1.5 m^2 por ventana
    const areaPorPuerta = 2.0;   // 2.0 m^2 por puerta

    // Cálculos mecánicos y volumétricos
    const areaVanos = (numVentanas * areaPorVentana) + (numPuertas * areaPorPuerta);
    const areaNetaMuro = Math.max(0, areaParedesBase - areaVanos);

    // Cantidad de ladrillos (55 por m^2 + 5% desperdicio)
    const cantLadrillos = Math.ceil(areaNetaMuro * 55 * 1.05);
    const costoLadrillos = cantLadrillos * precioLadrillo;

    // Área de cristal requerida
    const cantCristal = numVentanas * areaPorVentana;
    const costoCristal = cantCristal * precioCristal;

    // Costo base estimado de marcos/puertas ($150,000 COP por unidad base)
    const costoPuertas = numPuertas * 150000;

    // Costo total
    const costoTotal = costoLadrillos + costoCristal + costoPuertas;

    // Actualización de la interfaz
    document.getElementById("out-tipo-ladrillo").textContent = tipoLadrillo;
    document.getElementById("out-cant-ladrillos").textContent = cantLadrillos.toLocaleString();
    document.getElementById("out-costo-ladrillos").textContent = costoLadrillos.toLocaleString();

    document.getElementById("out-cant-cristal").textContent = cantCristal.toFixed(1);
    document.getElementById("out-costo-cristal").textContent = costoCristal.toLocaleString();

    document.getElementById("out-cant-puertas").textContent = numPuertas;
    document.getElementById("out-costo-puertas").textContent = costoPuertas.toLocaleString();

    document.getElementById("out-costo-total").textContent = "$" + costoTotal.toLocaleString();

    // Mostrar sección de resultados con desplazamiento suave
    outputSection.classList.remove("hidden");
    outputSection.scrollIntoView({ behavior: "smooth" });
  });
});
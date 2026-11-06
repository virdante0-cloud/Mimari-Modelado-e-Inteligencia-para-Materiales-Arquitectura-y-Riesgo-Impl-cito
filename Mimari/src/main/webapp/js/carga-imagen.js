/**
 * MÓDULO 1 - Carga de la fotografía del terreno. Es un paso INDEPENDIENTE del cálculo:
 * al elegir la foto se convierte (HEIC -> JPG), se muestra la vista previa y se SUBE y se guarda en la cuenta de la
 * persona (/api/archivos). Cuando el servidor la aprueba avisa con el evento "mimari:imagen" ({id, nombre, url});
 * si no, avisa con null. No valida nada: el servidor decide si el archivo es aceptable.
 */
(function () {
  "use strict";

  const API_URL = "api/archivos";                // relativa: funciona en / y en /mimari/
  const dropZone = document.getElementById("drop-zone");
  const fileInput = document.getElementById("file-input");
  if (!dropZone || !fileInput) return;

  dropZone.addEventListener("click", () => fileInput.click());

  fileInput.addEventListener("change", async (e) => {
    const original = e.target.files[0];
    avisarImagen(null);                           // hasta que el servidor confirme, no hay fotografía válida
    if (!original) {
      return;
    }

    mostrarInfoArchivo(original);

    let paraSubir = original;
    const nombre = original.name.toLowerCase();
    const esHeic = nombre.endsWith(".heic") || nombre.endsWith(".heif") || original.type === "image/heic";

    if (esHeic && typeof heic2any === "function") {
      try {
        const convertido = await heic2any({ blob: original, toType: "image/jpeg", quality: 0.8 });
        const blob = Array.isArray(convertido) ? convertido[0] : convertido;
        paraSubir = new File([blob], original.name.replace(/\.(heic|heif)$/i, ".jpg"), { type: "image/jpeg" });
      } catch (err) {
        console.warn("No se pudo convertir HEIC en el navegador:", err);
      }
    }

    mostrarVistaPrevia(paraSubir);
    await subir(paraSubir);
  });

  async function subir(archivo) {
    mostrarEstado("Guardando la fotografía en tu cuenta…", "aviso");
    const datos = new FormData();
    datos.append("tipo", "plano");
    datos.append("archivo", archivo, archivo.name);

    try {
      const respuesta = await fetch(API_URL, { method: "POST", body: datos });

      let cuerpo = null;
      try { cuerpo = await respuesta.json(); } catch (_) { /* la respuesta no era JSON */ }

      if (!respuesta.ok || !cuerpo || cuerpo.ok !== true) {
        mostrarEstado(cuerpo && cuerpo.error ? cuerpo.error : `El servidor respondió HTTP ${respuesta.status}`, "error");
        return;
      }
      mostrarEstado("Fotografía guardada en tu cuenta.", "ok");
      avisarImagen({ id: cuerpo.id, nombre: cuerpo.nombre, url: cuerpo.url });
    } catch (err) {
      console.warn("No se pudo subir la fotografía:", err);
      mostrarEstado("No se pudo contactar al servidor. Verifica que MIMARI esté en ejecución.", "error");
    }
  }

  function avisarImagen(imagen) {
    document.dispatchEvent(new CustomEvent("mimari:imagen", { detail: { imagen } }));
  }

  function mostrarEstado(texto, tipo) {
    const estado = document.getElementById("upload-estado");
    if (!estado) return;
    estado.textContent = texto;
    estado.className = `sim-estado ${tipo || "aviso"}`;
  }

  function mostrarVistaPrevia(archivo) {
    const lector = new FileReader();
    lector.onload = (evento) => {
      const img = document.getElementById("preview-img");
      if (img) {
        img.src = evento.target.result;
        img.classList.remove("hidden");
      }
    };
    lector.readAsDataURL(archivo);
  }

  /** Muestra nombre, tamaño y formato construyendo el DOM con textContent (sin innerHTML). */
  function mostrarInfoArchivo(archivo) {
    const caja = document.getElementById("exif-results");
    if (!caja) return;

    const titulo = document.createElement("h4");
    titulo.textContent = "📍 Información del archivo cargado";

    const lista = document.createElement("ul");
    const datos = [
      ["Nombre", archivo.name],
      ["Tamaño", (archivo.size / (1024 * 1024)).toFixed(2) + " MB"],
      ["Formato", archivo.type || "image/jpeg"]
    ];
    datos.forEach(([etiqueta, valor]) => {
      const li = document.createElement("li");
      const fuerte = document.createElement("strong");
      fuerte.textContent = etiqueta + ": ";
      li.appendChild(fuerte);
      li.appendChild(document.createTextNode(valor));
      lista.appendChild(li);
    });

    caja.replaceChildren(titulo, lista);
    caja.classList.remove("hidden");
  }
})();

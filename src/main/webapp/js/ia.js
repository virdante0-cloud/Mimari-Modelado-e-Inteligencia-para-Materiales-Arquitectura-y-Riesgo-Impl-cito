/**
 * MÓDULO 6 - Análisis con IA (Gemini). Es un paso APARTE del cálculo:
 *   - guarda la fotografía que anunció carga-imagen.js (evento "mimari:imagen")
 *   - cuando simulador.js termina el cálculo (evento "mimari:calculo") envía al servidor SOLO el id de la foto y las
 *     dimensiones a /api/ia; el servidor consulta a Gemini (la clave nunca llega al navegador) y devuelve el análisis.
 * No valida ni interpreta nada: muestra con textContent lo que responde el servidor. Si este módulo falla, el cálculo,
 * el plano y el presupuesto siguen funcionando.
 */
(function () {
  "use strict";

  const API_URL = "api/ia";
  let imagen = null;

  document.addEventListener("mimari:imagen", (e) => {
    imagen = e.detail.imagen;
    mostrarFoto(imagen);
  });

  document.addEventListener("mimari:calculo", async (e) => {
    const c = e.detail;
    if (!imagen) {
      mostrarEstado("Carga una fotografía del terreno para obtener el análisis de la IA.", "aviso");
      return;
    }

    mostrarEstado("Consultando a la IA con tu fotografía y las dimensiones…", "aviso");
    ocultarResultado();

    const datos = new URLSearchParams();
    datos.append("archivoId", imagen.id);
    datos.append("ancho", c.ancho);
    datos.append("largo", c.largo);
    datos.append("pisos", c.pisos);
    datos.append("alturaPiso", c.alturaPiso);

    try {
      const respuesta = await fetch(API_URL, { method: "POST", body: datos });

      let cuerpo = null;
      try { cuerpo = await respuesta.json(); } catch (_) { /* la respuesta no era JSON */ }

      if (!respuesta.ok || !cuerpo || cuerpo.ok !== true) {
        mostrarEstado(cuerpo && cuerpo.error ? cuerpo.error : `El servidor respondió HTTP ${respuesta.status}`, "error");
        return;
      }
      if (cuerpo.disponible === false) {
        mostrarEstado(cuerpo.mensaje || "La IA todavía no está configurada.", "aviso");
        return;
      }
      mostrarEstado("", "ok");
      mostrarAnalisis(cuerpo);
    } catch (err) {
      console.warn("No se pudo consultar a la IA:", err);
      mostrarEstado("No se pudo contactar al servidor para el análisis de IA.", "error");
    }
  });

  // ---- presentación (solo textContent; nada de innerHTML)

  function mostrarFoto(img) {
    const foto = document.getElementById("foto-resultado");
    const pie = document.getElementById("foto-pie");
    if (!foto || !pie) return;
    if (img) {
      foto.src = img.url;
      foto.classList.remove("hidden");
      pie.textContent = img.nombre;
    } else {
      foto.classList.add("hidden");
      foto.removeAttribute("src");
      pie.textContent = "No hay fotografía cargada.";
    }
  }

  function mostrarEstado(texto, tipo) {
    const estado = document.getElementById("ia-estado");
    if (!estado) return;
    if (!texto) {
      estado.classList.add("hidden");
      estado.textContent = "";
      return;
    }
    estado.textContent = texto;
    estado.className = `sim-estado ${tipo || "aviso"}`;
  }

  function ocultarResultado() {
    document.getElementById("ia-resultado")?.classList.add("hidden");
  }

  function asignarTexto(id, valor) {
    const el = document.getElementById(id);
    if (el) el.textContent = valor;
  }

  function mostrarAnalisis(r) {
    const a = r.analisis || {};
    asignarTexto("ia-resumen", texto(a.resumen) || "La IA no devolvió un resumen.");
    asignarTexto("ia-pendiente", texto(a.pendienteEstimada) || "no determinada");

    const riesgos = document.getElementById("ia-riesgos");
    if (riesgos) {
      riesgos.replaceChildren();
      lista(a.riesgos).forEach((item) => {
        const li = document.createElement("li");
        const nivel = String(item && item.nivel ? item.nivel : "").toLowerCase();
        if (["alto", "medio", "bajo"].includes(nivel)) {
          const etiqueta = document.createElement("span");
          etiqueta.className = `riesgo-${nivel}`;
          etiqueta.textContent = nivel.toUpperCase() + ": ";
          li.appendChild(etiqueta);
        }
        li.appendChild(document.createTextNode(texto(item && item.descripcion !== undefined ? item.descripcion : item)));
        riesgos.appendChild(li);
      });
      if (!riesgos.children.length) riesgos.appendChild(itemVacio("Sin riesgos reportados."));
    }

    const recs = document.getElementById("ia-recomendaciones");
    if (recs) {
      recs.replaceChildren();
      lista(a.recomendaciones).forEach((item) => {
        const li = document.createElement("li");
        li.textContent = texto(item);
        recs.appendChild(li);
      });
      if (!recs.children.length) recs.appendChild(itemVacio("Sin recomendaciones."));
    }

    asignarTexto("ia-modelo", `Análisis orientativo generado por ${texto(r.modelo)}. No reemplaza un estudio de suelos ni la revisión de un ingeniero.`);
    document.getElementById("ia-resultado")?.classList.remove("hidden");
  }

  function lista(valor) { return Array.isArray(valor) ? valor : []; }
  function texto(valor) { return valor === undefined || valor === null ? "" : String(valor); }
  function itemVacio(mensaje) {
    const li = document.createElement("li");
    li.textContent = mensaje;
    return li;
  }
})();

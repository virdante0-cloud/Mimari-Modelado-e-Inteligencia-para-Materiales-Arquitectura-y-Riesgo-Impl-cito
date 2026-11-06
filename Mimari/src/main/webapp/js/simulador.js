/**
 * MÓDULO 2 - Simulador: envía las dimensiones al servlet /api/predimensionar y muestra la respuesta.
 * NO valida ni calcula: lo hace el servidor. Si el servidor responde con un error, se muestra tal cual.
 * La fotografía NO se envía aquí: ya la subió y guardó carga-imagen.js, de forma independiente.
 * Al terminar avisa con el evento "mimari:calculo" (lo escuchan plano-canvas.js, presupuesto.js e ia.js).
 */
(function () {
  "use strict";

  const API_URL = "api/predimensionar";          // relativa: funciona en / y en /mimari/
  const boton = document.getElementById("btn-procesar");
  if (!boton) return;

  boton.addEventListener("click", async () => {
    const datos = new FormData();
    datos.append("ancho", valorDe("ancho"));
    datos.append("largo", valorDe("largo"));
    datos.append("pisos", valorDe("pisos"));
    datos.append("alturaPiso", valorDe("altura-piso"));

    const textoBoton = boton.textContent;
    boton.disabled = true;
    boton.textContent = "Calculando…";

    try {
      const respuesta = await fetch(API_URL, { method: "POST", body: new URLSearchParams(datos) });

      let cuerpo = null;
      try { cuerpo = await respuesta.json(); } catch (_) { /* la respuesta no era JSON */ }

      if (!respuesta.ok) {
        // 400: el servidor explica qué dato no es válido
        mostrarEstado(cuerpo && cuerpo.error ? cuerpo.error : `El servidor respondió HTTP ${respuesta.status}`, "error");
        return;
      }
      if (!cuerpo || cuerpo.ok !== true) {
        mostrarEstado("Respuesta inesperada del servidor.", "error");
        return;
      }

      mostrarEstado("", "ok");
      const seccion = document.getElementById("output-section");
      seccion?.classList.remove("hidden");
      mostrarPredimensionamiento(cuerpo);
      document.dispatchEvent(new CustomEvent("mimari:calculo", { detail: cuerpo }));
      seccion?.scrollIntoView({ behavior: "smooth" });
    } catch (err) {
      console.warn("No se pudo contactar al servidor:", err);
      mostrarEstado("No se pudo contactar al servidor. Verifica que MIMARI esté en ejecución.", "error");
    } finally {
      boton.disabled = false;
      boton.textContent = textoBoton;
    }
  });

  function valorDe(id) {
    const campo = document.getElementById(id);
    return campo ? campo.value : "";
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

  function mostrarPredimensionamiento(c) {
    asignarTexto("out-luz-libre", c.luz.toFixed(2));
    asignarTexto("out-grosor-placa", c.espesorPlaca.toFixed(2));
    asignarTexto("out-grosor-cm", (c.espesorPlaca * 100).toFixed(0));
    asignarTexto("out-seccion-viga", `${(c.peralteViga * 100).toFixed(0)} cm x ${(c.baseViga * 100).toFixed(0)} cm`);
    asignarTexto("out-volumen-concreto", c.volumenConcreto.toFixed(2));
    asignarTexto("out-altura-total", c.alturaTotal.toFixed(2));
    asignarTexto("out-area-construida", c.areaTotalConstruida.toFixed(2));
    asignarTexto("out-esbeltez", c.relacionEsbeltez.toFixed(2));

    const insignia = document.getElementById("out-origen");
    if (insignia) {
      insignia.textContent = "Calculado y validado en el servidor Java";
      insignia.className = "origen-badge servidor";
    }
    mostrarDictamen(c);
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

    lista.replaceChildren();
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
})();

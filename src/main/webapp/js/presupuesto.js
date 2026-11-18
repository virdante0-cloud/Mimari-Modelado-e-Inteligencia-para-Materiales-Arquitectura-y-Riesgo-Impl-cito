/**
 * MÓDULO 4 - Presupuesto base: pide el cálculo al servlet /api/presupuesto y llena la tabla.
 * NO valida ni calcula. Se activa con el evento "mimari:calculo" (que usa los datos ya validados por el servidor).
 * Si este módulo falla, el predimensionamiento y el plano siguen funcionando.
 */
(function () {
  "use strict";

  const API_URL = "api/presupuesto";

  document.addEventListener("mimari:calculo", async (e) => {
    const c = e.detail;

    const datos = new URLSearchParams();
    datos.append("ancho", c.ancho);
    datos.append("largo", c.largo);
    datos.append("pisos", c.pisos);
    datos.append("alturaPiso", c.alturaPiso);
    datos.append("tipoLadrillo", valorDe("tipo-ladrillo"));
    datos.append("precioLadrillo", valorDe("precio-ladrillo"));
    datos.append("precioCristal", valorDe("precio-cristal"));
    datos.append("numVentanas", valorDe("num-ventanas"));
    datos.append("numPuertas", valorDe("num-puertas"));

    try {
      const respuesta = await fetch(API_URL, { method: "POST", body: datos });

      let cuerpo = null;
      try { cuerpo = await respuesta.json(); } catch (_) { /* la respuesta no era JSON */ }

      if (!respuesta.ok || !cuerpo || cuerpo.ok !== true) {
        mostrarEstado(cuerpo && cuerpo.error ? cuerpo.error : `El servidor respondió HTTP ${respuesta.status}`);
        return;
      }
      mostrarEstado("");
      mostrarPresupuesto(cuerpo);
    } catch (err) {
      console.warn("No se pudo calcular el presupuesto:", err);
      mostrarEstado("No se pudo contactar al servidor para calcular el presupuesto.");
    }
  });

  function valorDe(id) {
    const campo = document.getElementById(id);
    return campo ? campo.value : "";
  }

  function asignarTexto(id, valor) {
    const el = document.getElementById(id);
    if (el) el.textContent = valor;
  }

  function mostrarEstado(texto) {
    const estado = document.getElementById("presupuesto-estado");
    if (!estado) return;
    if (!texto) {
      estado.classList.add("hidden");
      estado.textContent = "";
      return;
    }
    estado.textContent = texto;
    estado.className = "sim-estado error";
  }

  function pesos(valor) {
    return Number(valor).toLocaleString("es-CO", { maximumFractionDigits: 0 });
  }

  function mostrarPresupuesto(p) {
    asignarTexto("out-tipo-ladrillo", p.tipoLadrillo);
    asignarTexto("out-cant-ladrillos", Number(p.cantLadrillos).toLocaleString("es-CO"));
    asignarTexto("out-costo-ladrillos", pesos(p.costoLadrillos));
    asignarTexto("out-cant-cristal", Number(p.areaCristal).toFixed(1));
    asignarTexto("out-costo-cristal", pesos(p.costoCristal));
    asignarTexto("out-cant-puertas", p.cantPuertas);
    asignarTexto("out-costo-puertas", pesos(p.costoPuertas));
    asignarTexto("out-costo-total", `$${pesos(p.costoTotal)}`);
  }
})();

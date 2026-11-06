/**
 * MÓDULO 5 - Insumos de la persona: POO + eventos del DOM + filtro dinámico (Semanas 4 y 5).
 * Los insumos ya vienen guardados en la base de datos (JPA) y dibujados por el servidor con JSTL; este módulo NO valida
 * ni guarda nada: lee las filas de la tabla, crea objetos Material y filtra en tiempo real en el navegador.
 * Si este módulo falla, la tabla se sigue viendo completa y los formularios siguen funcionando.
 */
(function () {
  "use strict";

  // ---- Clase (POO)
  class Material {
    constructor(nombre, tipo, precioUnitario, cantidad, fila) {
      this.nombre = nombre;
      this.tipo = tipo;
      this.precioUnitario = Number(precioUnitario);
      this.cantidad = Number(cantidad);
      this.fila = fila;                         // <tr> que lo representa en la página
    }

    calcularSubtotal() {
      return this.precioUnitario * this.cantidad;
    }

    coincideCon(criterio) {
      return this.nombre.toLowerCase().includes(criterio) || this.tipo.toLowerCase().includes(criterio);
    }
  }

  // ---- Acceso al DOM (getElementById / querySelector / querySelectorAll)
  const inputBusqueda = document.getElementById("input-busqueda");
  const spanTotal = document.querySelector("#costo-acumulado-total");
  const filaSinResultados = document.getElementById("fila-sin-resultados");
  const filas = document.querySelectorAll("#tabla-insumos tr.fila-insumo");
  if (!inputBusqueda || filas.length === 0) return;

  const listaMateriales = Array.from(filas).map((fila) =>
    new Material(fila.dataset.nombre, fila.dataset.tipo, fila.dataset.precio, fila.dataset.cantidad, fila)
  );

  // ---- Filtro dinámico en tiempo real (evento 'input')
  inputBusqueda.addEventListener("input", (e) => {
    filtrar(e.target.value.toLowerCase().trim());
  });

  function filtrar(criterio) {
    let total = 0;
    let visibles = 0;
    listaMateriales.forEach((mat) => {
      const coincide = mat.coincideCon(criterio);
      mat.fila.classList.toggle("hidden", !coincide);
      if (coincide) {
        total += mat.calcularSubtotal();
        visibles += 1;
      }
    });
    if (filaSinResultados) filaSinResultados.classList.toggle("hidden", visibles !== 0);
    if (spanTotal) spanTotal.textContent = "$" + total.toLocaleString("es-CO", { maximumFractionDigits: 2 });
  }
})();

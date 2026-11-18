/**
 * MÓDULO 3 - Plano de corte estructural con cotas (HTML5 Canvas).
 * Solo dibuja los datos que ya validó y calculó el servidor; los recibe con el evento "mimari:calculo".
 */
(function () {
  "use strict";

  const canvas = document.getElementById("canvas-plano");
  if (!canvas) return;

  document.addEventListener("mimari:calculo", (e) => {
    const c = e.detail;
    dibujarPlano(c.luz, c.espesorPlaca, c.peralteViga);
  });

  function dibujarPlano(luz, hPlaca, hViga) {
    const ctx = canvas.getContext("2d");

    // Fondo técnico
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = "#f1f5f9";
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // Terreno / cimentación
    ctx.fillStyle = "#cbd5e1";
    ctx.fillRect(20, 200, 360, 30);

    // Columnas
    ctx.fillStyle = "#475569";
    ctx.fillRect(50, 80, 20, 120);
    ctx.fillRect(330, 80, 20, 120);

    // Viga principal (L / 10), con altura limitada para que quepa en el dibujo
    ctx.fillStyle = "#2563eb";
    const altoVigaPx = Math.min(45, Math.max(15, hViga * 40));
    ctx.fillRect(50, 80, 300, altoVigaPx);

    // Placa de techo (L / 25)
    ctx.fillStyle = "#1e40af";
    const altoPlacaPx = Math.min(20, Math.max(8, hPlaca * 40));
    ctx.fillRect(40, 80 - altoPlacaPx, 320, altoPlacaPx);

    // Cotas y textos
    ctx.fillStyle = "#0f172a";
    ctx.font = "12px sans-serif";
    ctx.fillText(`Luz (L) = ${luz.toFixed(1)}m`, 160, 160);
    ctx.fillText(`Placa (h) = ${(hPlaca * 100).toFixed(0)}cm`, 160, 65);
    ctx.fillStyle = "#ffffff";
    ctx.fillText(`Viga = ${(hViga * 100).toFixed(0)}cm`, 160, 80 + altoVigaPx / 2 + 4);

    // Línea de cota de la luz
    ctx.beginPath();
    ctx.moveTo(70, 170);
    ctx.lineTo(330, 170);
    ctx.strokeStyle = "#0f172a";
    ctx.lineWidth = 1;
    ctx.stroke();
  }
})();

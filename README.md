*semana9: actualización de todo y ajuste de JSP y servlets*

Reorganización general del proyecto MIMARI. Estaba muy desordenado: el
HTML/JS y el backend Java no se comunicaban, el despliegue en Tomcat no
mostraba nada y la lógica estaba repartida en varios lugares.

*POR QUÉ SE AJUSTÓ*
- El simulador (index.html) calculaba todo en el navegador con valores
  fijos y nunca llamaba al servidor Java.
- header.jsp se incluía a sí mismo, lo que causaba un bucle y error 500
  en las páginas JSP.
- El WAR se llamaba Mimari-1.0-SNAPSHOT, por lo que la URL no era la
  esperada, y el proyecto estaba compilado para una versión de Java más
  nueva que la de Tomcat.
- Había capas separadas (modelo/util) que el profesor recomendó no usar:
  la lógica debe ir solo en servlets.

*CAMBIOS*
- Servlets: toda la lógica queda en un único PredimensionamientoServlet
  (sin MVC), con tres rutas:
  /analisis-estructural (formulario y resultados HTML),
  /api/predimensionar (JSON para el simulador) y
  /consulta (proceso de la Semana 10).
- Se eliminan los paquetes modelo y util y el servlet de API duplicado.
- JSP: ahora solo son vista. header.jsp ya no se incluye a sí mismo;
  se agregan consulta.jsp (formulario) y error.jsp (isErrorPage y
  exception); index.jsp conserva scriptlets, declaración e include.
- index.html y app.js: nuevos campos (ancho, largo, pisos, altura por
  piso), envío al servlet con fetch, y cálculo local de respaldo si el
  servidor no responde. El dictamen ya no es texto fijo.
- Subida de planos: solo imágenes, guardadas fuera de la carpeta
  publicada y con nombre aleatorio.
- pom.xml: packaging war, finalName mimari, Java 17, Jakarta Servlet 6
  (Tomcat 10.1).
- web.xml: página de bienvenida y error-page hacia error.jsp.
- README: pasos de compilación, despliegue y solución de problemas.

*PRUEBAS*
- Probado el servlet con Chromium: cálculo, validaciones, subida de
  archivos, /consulta y errores con forward a error.jsp.
- Pendiente de verificar en Tomcat 10.1 real: generación del WAR con
  Maven y el funcionamiento de los JSP.

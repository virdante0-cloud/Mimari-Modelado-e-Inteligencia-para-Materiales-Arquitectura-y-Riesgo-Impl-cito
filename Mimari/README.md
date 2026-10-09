# MIMARI — Modelado e Inteligencia para Materiales, Arquitectura y Riesgo Implícito

Aplicación web en Java (Servlets + JSP) con un simulador HTML/JS **conectado** al servidor:
el navegador envía los datos al servlet, el servlet calcula el predimensionamiento y devuelve JSON.

## Tecnologías
Java 17+ (probado con el JDK 24 de Corretto) · Maven · Jakarta Servlet 6.0 · **Apache Tomcat 10.1** · HTML/CSS/JS

> Tomcat 9 o anterior **no sirve**: usa `javax.servlet` y este proyecto usa `jakarta.servlet`.

## Estructura (sin MVC: toda la lógica en un solo servlet)
```
src/main/java/com/mimari/servlets/PredimensionamientoServlet.java   <- TODA la lógica
    /analisis-estructural   GET: formulario | POST: resultados en HTML
    /api/predimensionar     GET|POST -> JSON (lo usa index.html)
    /consulta               GET|POST -> consulta por parámetros (Semana 10)
src/main/webapp/
  index.html + app.js + styles.css    simulador (página de inicio)
  index.jsp + header.jsp              catálogo JSP (scriptlets, declaración, include del header)
  consulta.jsp                        formulario de la Semana 10 (solo vista; envía al servlet /consulta)
  error.jsp                           página de error (isErrorPage + exception)
  WEB-INF/web.xml
```
No hay carpetas `modelo` ni `util`: las fórmulas, validaciones y el guardado del plano están dentro del servlet.
Los JSP solo muestran contenido; no calculan nada.

## Compilar (genera `target/mimari.war`)
- **IntelliJ:** pestaña *Maven* → *Lifecycle* → doble clic en `clean` y luego en `package`.
- **Terminal (con Maven instalado):** `mvn clean package`

## Desplegar en Tomcat 10.1
1. Copia `target/mimari.war` a `TOMCAT/webapps/`.
2. Inicia Tomcat: `TOMCAT\bin\startup.bat` (Windows) o `TOMCAT/bin/catalina.sh run` (Linux/Mac).
3. Abre **http://localhost:8080/mimari/**

| URL | Qué es |
|---|---|
| `/mimari/` | Simulador (index.html) |
| `/mimari/index.jsp` | Catálogo JSP + formulario clásico |
| `/mimari/consulta.jsp` | Formulario de la Semana 10 (envía al servlet `/mimari/consulta`) |
| `/mimari/consulta?categoria=viga&luz=6.5` | Consulta procesada por el servlet |
| `/mimari/analisis-estructural` | Formulario/resultados generados por el servlet |
| `/mimari/api/predimensionar?ancho=6&largo=8&pisos=2&alturaPiso=2.6` | API JSON (prueba rápida en el navegador) |

Si cambias el código: vuelve a hacer `package`, borra `webapps/mimari.war` **y** la carpeta `webapps/mimari/`, copia el WAR nuevo.

## Si "no aparece nada"
- **404 en todo:** la URL lleva el nombre del WAR (`/mimari/`), no `/`.
- **404 solo en los servlets / `ClassNotFoundException: javax...`:** estás en Tomcat 9; usa 10.1.
- **`UnsupportedClassVersionError`:** el Java con el que corre Tomcat es más viejo que 17 (revisa `JAVA_HOME`).
- **Error 500 / pantalla en blanco:** mira `TOMCAT/logs/catalina.<fecha>.log` y `localhost.<fecha>.log`.
- **No abre ni `localhost:8080`:** otro programa usa el puerto 8080, o Tomcat no arrancó (revisa los logs).

## Planos subidos
Se guardan fuera de la carpeta publicada, en `TOMCAT/mimari-uploads/`, con nombre aleatorio.
Solo se aceptan imágenes (jpg, png, gif, webp, bmp, heic).

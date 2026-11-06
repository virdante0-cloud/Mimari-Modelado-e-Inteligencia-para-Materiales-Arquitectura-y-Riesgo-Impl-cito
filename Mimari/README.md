# MIMARI — Modelado e Inteligencia para Materiales, Arquitectura y Riesgo Implícito

Aplicación web en Java (Servlets + JSP/JSTL + JPA + JS) para predimensionar una edificación a partir de una fotografía del terreno
y de sus dimensiones, con un análisis opcional por IA (Gemini). Sin frameworks MVC: toda la lógica y la validación viven en los
servlets; las JSP solo muestran datos.

## Entrega: Semana 13 y Semana 14 (rama `semana-13-14`)
Esta rama reúne el trabajo de las dos semanas en una sola entrega, con dos commits (uno por semana) para que se vea qué aportó cada una.

### Semana 13 · EJB y JPA
- **Entidades JPA** (`modelo/`): `Usuario`, `Insumo`, `Archivo`, `Analisis`, con `META-INF/persistence.xml` (unidad `catalogoPU`, Hibernate 6, `RESOURCE_LOCAL`).
- **Servicios sin estado** (`servicio/`), equivalentes a EJB `@Stateless`: `UsuarioService`, `InsumoService`, `ArchivoService`, `AnalisisService`;
  `CarritoTemporal` (con estado, equivalente a `@Stateful`).
- **`Persistencia`**: dueño del `EntityManagerFactory` y de las transacciones (begin/commit/rollback); `PersistenciaListener` lo cierra al detener la aplicación.
- **DataSource por JNDI** (`jdbc/programacion3`), publicado desde `Main.java` (Tomcat embebido) o `db/context.xml.ejemplo` (Tomcat externo).
- **Registro y acceso de usuarios** con clave en PBKDF2 (`seguridad/Claves`).

### Semana 14 · JSTL e internacionalización
- **Vistas sin scriptlets** en `WEB-INF/vistas/` con JSTL (`jakarta.tags.core`, `fmt`, `xml`); las excepciones (`saludo.jsp`, `error.jsp`) son a propósito.
- **i18n español/inglés** con `mensajes.properties` y `mensajes_en.properties` (`/catalogo?idioma=en`).
- **`xml-demo.jsp`** con XPath y el catálogo de materiales y elementos estructurales renderizado con JSTL.

### Corrección incluida en esta rama
Al registrar un usuario aparecía `object of type Usuario is not an instance of Usuario`: la clase estaba cargada dos veces por classloaders distintos
(classpath de IntelliJ y `WEB-INF/classes` del Tomcat embebido). Se soluciona en `Main.java` haciendo que el contexto web delegue primero al classloader padre
(`WebappLoader` con `setDelegate(true)`).

## Cómo está organizada la aplicación (el recorrido de una persona)
1. **Acceso (`/login.jsp`, `/registro`)**: pantalla completa con fondo animado. Mientras no haya sesión, es lo único que se ve:
   `AutenticacionFilter` bloquea todo lo demás (las páginas redirigen a login; `/api/*` responde 401). Cada persona tiene su cuenta
   (clave guardada con PBKDF2) y **todo lo que guarda queda solo en su cuenta**.
2. **Catálogo (`/catalogo`, la entrada)**: un único catálogo con JSTL en español/inglés: materiales base, elementos estructurales,
   **Mis insumos** (se agregan, se filtran en vivo y se eliminan), lista de cotización (temporal) y **catálogos en PDF** que cada persona sube y consulta.
3. **Simulador (`/simulador`)**: tres pasos separados:
    1) **Cargar la fotografía** (se sube y se guarda al elegirla; no depende de ningún cálculo),
    2) **Calcular** el predimensionamiento con las dimensiones (lo hace el servidor),
    3) **Analizar con IA** (une la foto ya guardada + las dimensiones y consulta a Gemini). Más plano y presupuesto.
4. **Proyecto (`/proyecto`)** y **Prácticas (`/practicas`)**: la descripción del proyecto y el mapa de las semanas del curso.

## Tecnologías
Java 17+ · Maven · Jakarta Servlet 6.0 · JSP + JSTL (`jakarta.tags.*`) · JPA con Hibernate 6 · MySQL/MariaDB · Gson · Tomcat 10.1 embebido
(se ejecuta con `Main` desde IntelliJ) o Tomcat 10.1 externo con el `.war`.

## Cómo ejecutar (recomendado): desde IntelliJ con `Main`
1. Instala MySQL o MariaDB y déjalo encendido. Abre la carpeta del proyecto (la del `pom.xml`) y deja que Maven descargue las dependencias.
2. Si tu MySQL tiene clave, define las variables de entorno `MIMARI_DB_USER` y `MIMARI_DB_PASS` (Run > Edit Configurations > Environment variables).
   Por defecto usa `root` sin clave y la base `programacion3` (se crea sola). `MIMARI_DB_URL` cambia la URL completa.
3. Ejecuta `src/main/java/com/mimari/Main.java` y abre **http://localhost:8080/** (otro puerto: variable `PUERTO`).
4. Crea tu cuenta en **Crear cuenta** (usuario de 3 a 30 caracteres; clave de 8 o más).
   Las tablas las crea Hibernate solas; `db/schema.sql` muestra su estructura y crea la tabla de la práctica JDBC (`productos`).

## Dónde se pone la clave de Gemini (IA)
Un solo sitio, que lee la clase `config/Configuracion.java` (prioridad: variable de entorno > archivo local > valores por defecto):
- **Opción A (recomendada):** copia `mimari.local.properties.ejemplo` como **`mimari.local.properties`** (junto al `pom.xml`) y escribe
  `gemini.api.key=TU_CLAVE`. Ese archivo está en `.gitignore`: nunca se sube a git. Reinicia `Main`.
- **Opción B:** variable de entorno `GEMINI_API_KEY` (y `GEMINI_MODELO` para cambiar de modelo; por defecto `gemini-2.5-flash`, que acepta imagen).
- La llamada a la API está en **`ia/GeminiClient.java`** (ahí se edita el *prompt* `INSTRUCCIONES` y el formato de la respuesta). La clave viaja en
  una cabecera HTTP desde el servidor; **nunca llega al navegador**.
- Sin clave todo sigue funcionando: el paso 3 solo avisa que la IA no está configurada. Cada análisis se guarda en el historial de la persona.
- El resultado es orientativo y no reemplaza un estudio de suelos ni la revisión de un ingeniero.

## Estructura
```
src/main/java/com/mimari/
  Main.java                 arranca Tomcat embebido y publica el DataSource jdbc/programacion3 (JNDI)
  config/Configuracion      lee claves y ajustes (Gemini, carpeta de archivos)
  seguridad/                AutenticacionFilter (puerta de entrada), Sesion (sesión y cookie), Claves (PBKDF2)
  modelo/                   entidades JPA: Usuario, Insumo, Archivo, Analisis
  servicio/                 InsumoService, ArchivoService, AnalisisService, UsuarioService (sin estado, como EJB @Stateless);
                            CarritoTemporal (con estado, como @Stateful); Persistencia (EntityManagerFactory y transacciones)
  ia/                       GeminiClient, IaException
  servlets/                 un servlet por función, independientes entre sí:
    LoginServlet · RegistroServlet · LogoutServlet                 acceso y sesión
    CatalogoServlet  · SimuladorServlet · ProyectoServlet · PracticasServlet   páginas principales
    ArchivosApiServlet (/api/archivos)   sube y entrega fotos de cada persona
    PredimensionarApiServlet · PresupuestoApiServlet · IaApiServlet   JSON del simulador
    SaludoServlet · ConsultaServlet · ProductosServlet · XmlDemoServlet · AnalisisEstructuralServlet   prácticas del curso
    ErrorServlet (/error.jsp)            único punto de manejo de errores
src/main/resources/         META-INF/persistence.xml · mensajes.properties (es) · mensajes_en.properties · mimari.properties
src/main/webapp/
  index.jsp                 lleva al catálogo
  acceso.css · header.css · paginas.css · styles.css    estilos (acceso, encabezado animado, páginas internas, catálogo y simulador)
  js/                       un módulo por función: carga-imagen · simulador · plano-canvas · presupuesto · ia · materiales
  WEB-INF/vistas/           JSP que solo MUESTRAN datos (no se abren directo por URL)
db/schema.sql · db/context.xml.ejemplo · mimari.local.properties.ejemplo
```

## Reglas de diseño
- **Validación solo en los servlets.** HTML, JSP y JS no validan; muestran lo que responde el servidor.
- **Sin HTML en Java.** Los servlets preparan atributos y hacen `forward` a una JSP; las APIs devuelven JSON.
- **Servlets y JS separados por función**: si uno falla, los demás siguen. Si la base de datos o la IA fallan, el resto de la página se muestra.
- **Cada persona ve solo lo suyo:** todas las consultas filtran por `usuario_id`; los archivos van a una carpeta por usuario fuera de la carpeta publicada.
- **Un solo punto de errores:** `ErrorServlet` escapa el mensaje y lo muestra en `error.jsp`.

## Dónde vive cada semana del curso (nada se borró: se reorganizó)
| Semana | Dónde |
|---|---|
| 4 y 5 · POO y DOM | `js/materiales.js` (clase `Material`, filtro en vivo) en “Mis insumos” |
| 9 · Componentes JSP | `header.jsp` con `jsp:include`; elementos estructurales en el catálogo (el cálculo ahora lo hace `CatalogoServlet`) |
| 10 · Objetos implícitos y errores | `saludo.jsp`, `consulta.jsp`, `error.jsp` (`isErrorPage`) |
| 11 · Sesiones, cookies y JDBC | `Sesion` (sesión + cookie `ultimoUsuario`), login/registro, `ProductosServlet` (JDBC directo a `productos`) |
| 13 · EJB y JPA | entidades, servicios sin estado, `CarritoTemporal` (con estado), `persistence.xml`, DataSource JNDI |
| 14 · JSTL | todas las vistas sin scriptlets (salvo `saludo.jsp` y `error.jsp`, a propósito), i18n es/en, `xml-demo.jsp` con XPath |

## URLs (con `Main`: `http://localhost:8080`; con `.war`: `http://localhost:8080/mimari`)
`/login.jsp` · `/registro` · `/catalogo` · `/catalogo?idioma=en` · `/simulador` · `/proyecto` · `/practicas` · `/saludo.jsp?nombre=Ana` ·
`/consulta.jsp?categoria=viga&luz=6.5` · `/productos.jsp` · `/xml-demo.jsp` · `/analisis-estructural`

## Archivos subidos
Fotos y PDF se guardan fuera de la carpeta publicada (`target/tomcat/mimari-uploads/` con `Main`; cambia el sitio con `almacenamiento.carpeta`),
en una carpeta por usuario y con nombre aleatorio. Se comprueba el contenido real (cabecera del archivo), no solo la extensión.
Fotos: jpg, png, webp, heic hasta 8 MB. Catálogos: PDF hasta 15 MB.

## Alternativa: Tomcat externo con `.war`
1. `mvn clean package` genera `target/mimari.war`; cópialo a `TOMCAT/webapps/` (Tomcat **10.1**).
2. Copia `db/context.xml.ejemplo` a `TOMCAT/conf/Catalina/localhost/mimari.xml` (DataSource) y, para la clave de Gemini, `mimari.local.properties` a `TOMCAT/conf/`.
3. Abre `http://localhost:8080/mimari/`. Si cambias el código: borra `webapps/mimari.war` y `webapps/mimari/` y copia el WAR nuevo.

## Si algo no abre
- **No carga ninguna página / error de base de datos:** MySQL apagado o clave distinta (`MIMARI_DB_USER` / `MIMARI_DB_PASS`).
- **Tabla `insumo` con error de columnas:** viene de una versión anterior; ejecuta `DROP TABLE insumo;` y reinicia (Hibernate la crea de nuevo).
- **`Address already in use`:** usa `PUERTO=9090`. **`No se encontró src/main/webapp`:** *Working directory* = carpeta del proyecto.
- **La IA dice que no está configurada:** falta la clave (ver arriba). **Error 502 de la IA:** clave o modelo inválidos, o sin internet.
- **Error al compilar un JSP con JDK nuevo:** sube la versión de `ecj` en `pom.xml`.
- **`object of type Usuario is not an instance of Usuario` al registrarse:** la clase está cargada dos veces; revisa que `Main.java` use `WebappLoader` con `setDelegate(true)` y haz *Rebuild* (o borra `target/`).
- **Aviso de Hibernate "5.5.0 version for MySQLDialect is no longer supported" con XAMPP:** XAMPP trae MariaDB, que se presenta como 5.5.x. Es solo un aviso; para quitarlo, en `persistence.xml` agrega la propiedad `hibernate.dialect` = `org.hibernate.dialect.MariaDBDialect`.
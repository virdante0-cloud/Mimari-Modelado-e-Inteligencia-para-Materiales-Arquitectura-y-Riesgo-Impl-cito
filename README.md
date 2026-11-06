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

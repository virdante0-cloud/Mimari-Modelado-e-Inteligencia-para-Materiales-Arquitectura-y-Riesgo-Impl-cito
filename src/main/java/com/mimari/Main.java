package com.mimari;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.loader.WebappLoader;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/**
 * Arranca MIMARI con un Tomcat embebido: basta con darle "Run" a esta clase en IntelliJ.
 * No hay que compilar el .war ni copiarlo a ningún Tomcat.
 *
 *   Abre:  http://localhost:8080/
 *   Otro puerto:  variable de entorno PUERTO  (p. ej. PUERTO=9090)
 *
 * IMPORTANTE: ejecútalo con el directorio de trabajo en la carpeta del proyecto (la que tiene pom.xml);
 * IntelliJ lo hace así por defecto ($MODULE_WORKING_DIR$).
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int puerto = leerPuerto();

        File webapp = new File("src/main/webapp");
        if (!webapp.isDirectory()) {
            throw new IllegalStateException("No se encontró src/main/webapp. En IntelliJ: Run > Edit Configurations > "
                    + "Working directory = la carpeta del proyecto (donde está pom.xml).");
        }
        File clases = new File("target/classes");

        // Tomcat guarda sus archivos temporales (JSP compilados, subidas) dentro de target/tomcat
        File base = new File("target/tomcat");
        base.mkdirs();

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(base.getAbsolutePath());
        tomcat.setPort(puerto);
        tomcat.getConnector();          // crea el conector HTTP
        tomcat.enableNaming();          // Semana 13: habilita JNDI (java:comp/env) para publicar el DataSource

        // Contexto raíz "" => http://localhost:8080/  (lee WEB-INF/web.xml y los @WebServlet)
        Context contexto = tomcat.addWebapp("", webapp.getAbsolutePath());

        // Las clases que compila IntelliJ/Maven (target/classes) se publican como WEB-INF/classes
        WebResourceRoot recursos = new StandardRoot(contexto);
        recursos.addPreResources(new DirResourceSet(recursos, "/WEB-INF/classes", clases.getAbsolutePath(), "/"));
        contexto.setResources(recursos);

        // Una sola copia de las clases: target/classes ya está en el classpath de IntelliJ, así que el cargador de la
        // aplicación debe preguntar primero al "padre". Con JSF/CDI/Hibernate juntos, si no, una clase (p. ej. Usuario)
        // se carga dos veces y Hibernate falla con "Can not get java.lang.Long field ... on ...".
        WebappLoader cargador = new WebappLoader();
        cargador.setDelegate(true);
        contexto.setLoader(cargador);

        // Semana 13: DataSource administrado por el SERVIDOR, publicado por JNDI como java:comp/env/jdbc/programacion3.
        // JPA (persistence.xml) lo busca por ese nombre; la clave nunca queda en el código (variables de entorno).
        ContextResource ds = new ContextResource();
        ds.setName("jdbc/programacion3");
        ds.setType("javax.sql.DataSource");
        ds.setAuth("Container");
        ds.setProperty("factory", "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory");
        ds.setProperty("driverClassName", "com.mysql.cj.jdbc.Driver");
        ds.setProperty("url", entorno("MIMARI_DB_URL", "jdbc:mysql://localhost:3306/programacion3?createDatabaseIfNotExist=true"));
        ds.setProperty("username", entorno("MIMARI_DB_USER", "root"));
        ds.setProperty("password", entorno("MIMARI_DB_PASS", ""));
        ds.setProperty("maxTotal", "10");
        contexto.getNamingResources().addResource(ds);

        // Semanas 15-16: BeanManager de CDI (Weld) publicado por JNDI para que JSF encuentre los beans @Named
        ContextResource beanManager = new ContextResource();
        beanManager.setName("BeanManager");
        beanManager.setType("jakarta.enterprise.inject.spi.BeanManager");
        beanManager.setProperty("factory", "org.jboss.weld.resources.ManagerObjectFactory");
        contexto.getNamingResources().addResource(beanManager);

        tomcat.start();
        System.out.println();
        System.out.println(">>> MIMARI listo en  http://localhost:" + puerto + "/   (detén con el botón Stop)");
        System.out.println();
        tomcat.getServer().await();
    }

    private static String entorno(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return (valor == null || valor.isEmpty()) ? porDefecto : valor;
    }

    private static int leerPuerto() {
        String valor = System.getenv("PUERTO");
        if (valor != null && valor.matches("[0-9]{2,5}")) {
            return Integer.parseInt(valor);
        }
        return 8080;
    }
}

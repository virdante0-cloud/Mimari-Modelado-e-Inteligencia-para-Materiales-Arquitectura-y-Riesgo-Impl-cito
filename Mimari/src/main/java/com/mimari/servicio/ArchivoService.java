package com.mimari.servicio;

import com.mimari.config.Configuracion;
import com.mimari.modelo.Archivo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Semana 13 - servicio sin estado: guarda los archivos de cada persona (fotos del terreno y PDF de catálogos).
 * Los bytes van a disco, en una carpeta por usuario FUERA de la carpeta publicada, con nombre aleatorio; la ficha va a la BD.
 * Todas las operaciones filtran por usuario: nadie puede leer ni borrar archivos de otro.
 */
public class ArchivoService {

    public Archivo guardar(Long usuarioId, String tipo, String nombreOriginal, String contentType,
                           String extension, byte[] datos) {
        String almacenado = UUID.randomUUID() + "." + extension;
        Path destino = carpetaDe(usuarioId).resolve(almacenado);
        try {
            Files.createDirectories(destino.getParent());
            Files.write(destino, datos);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo en disco.", e);
        }
        try {
            return Persistencia.enTransaccion(em -> {
                Archivo a = new Archivo(usuarioId, tipo, nombreOriginal, almacenado, contentType, datos.length);
                em.persist(a);
                return a;
            });
        } catch (RuntimeException e) {
            borrarDisco(destino);                       // si la BD falla no queda un archivo huérfano
            throw e;
        }
    }

    /** @return la ficha si el archivo existe y es de esa persona; null en otro caso */
    public Archivo buscar(Long usuarioId, Long id) {
        List<Archivo> lista = Persistencia.consultar(em -> em
                .createQuery("SELECT a FROM Archivo a WHERE a.id = :id AND a.usuarioId = :u", Archivo.class)
                .setParameter("id", id)
                .setParameter("u", usuarioId)
                .getResultList());
        return lista.isEmpty() ? null : lista.get(0);
    }

    public List<Archivo> listar(Long usuarioId, String tipo) {
        return Persistencia.consultar(em -> em
                .createQuery("SELECT a FROM Archivo a WHERE a.usuarioId = :u AND a.tipo = :t ORDER BY a.id DESC", Archivo.class)
                .setParameter("u", usuarioId)
                .setParameter("t", tipo)
                .getResultList());
    }

    public boolean eliminar(Long usuarioId, Long id, String tipo) {
        Archivo archivo = buscar(usuarioId, id);
        if (archivo == null || !archivo.getTipo().equals(tipo)) {
            return false;
        }
        Persistencia.enTransaccion(em -> em
                .createQuery("DELETE FROM Archivo a WHERE a.id = :id AND a.usuarioId = :u")
                .setParameter("id", id)
                .setParameter("u", usuarioId)
                .executeUpdate());
        borrarDisco(ruta(archivo));
        return true;
    }

    public Path ruta(Archivo archivo) {
        return carpetaDe(archivo.getUsuarioId()).resolve(archivo.getNombreAlmacenado());
    }

    private static Path carpetaDe(Long usuarioId) {
        return Configuracion.carpetaAlmacenamiento().resolve("u" + usuarioId);
    }

    private static void borrarDisco(Path ruta) {
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException e) {
            // no es crítico: queda un archivo suelto en disco
        }
    }
}

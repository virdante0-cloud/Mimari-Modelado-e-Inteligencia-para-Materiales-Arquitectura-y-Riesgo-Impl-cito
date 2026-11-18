package com.mimari.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

/**
 * Semana 13 - entidad JPA: un archivo que subió una persona. Los bytes están en disco (fuera de la carpeta publicada);
 * aquí solo queda la ficha. tipo = "plano" (fotografía del terreno) o "catalogo" (PDF).
 */
@Entity
@Table(name = "archivo")
public class Archivo {

    public static final String PLANO = "plano";
    public static final String CATALOGO = "catalogo";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "nombre_original", nullable = false, length = 150)
    private String nombreOriginal;

    @Column(name = "nombre_almacenado", nullable = false, length = 80)
    private String nombreAlmacenado;

    @Column(name = "content_type", nullable = false, length = 60)
    private String contentType;

    @Column(nullable = false)
    private long tamano;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creado = new Date();

    public Archivo() { }

    public Archivo(Long usuarioId, String tipo, String nombreOriginal, String nombreAlmacenado,
                   String contentType, long tamano) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.nombreOriginal = nombreOriginal;
        this.nombreAlmacenado = nombreAlmacenado;
        this.contentType = contentType;
        this.tamano = tamano;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getTipo() { return tipo; }
    public String getNombreOriginal() { return nombreOriginal; }
    public String getNombreAlmacenado() { return nombreAlmacenado; }
    public String getContentType() { return contentType; }
    public long getTamano() { return tamano; }
    public Date getCreado() { return creado; }

    /** Tamaño legible para la vista, p. ej. "2,35 MB". */
    public String getTamanoLegible() {
        if (tamano >= 1024 * 1024) {
            return String.format(java.util.Locale.forLanguageTag("es-CO"), "%.2f MB", tamano / (1024.0 * 1024.0));
        }
        return String.format(java.util.Locale.forLanguageTag("es-CO"), "%.0f KB", Math.max(1, tamano / 1024.0));
    }
}

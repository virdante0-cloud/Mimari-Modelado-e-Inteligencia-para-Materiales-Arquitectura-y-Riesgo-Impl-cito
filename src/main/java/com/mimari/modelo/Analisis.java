package com.mimari.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

/**
 * Semana 13 - entidad JPA: el historial de análisis de IA de cada persona (qué foto, qué dimensiones, qué respondió la IA).
 */
@Entity
@Table(name = "analisis")
public class Analisis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "archivo_id")
    private Long archivoId;

    @Column(nullable = false)
    private double ancho;

    @Column(nullable = false)
    private double largo;

    @Column(nullable = false)
    private int pisos;

    @Column(name = "altura_piso", nullable = false)
    private double alturaPiso;

    @Column(nullable = false, length = 60)
    private String motor;

    @Lob
    @Column(nullable = false)
    private String resultado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creado = new Date();

    public Analisis() { }

    public Analisis(Long usuarioId, Long archivoId, double ancho, double largo, int pisos, double alturaPiso,
                    String motor, String resultado) {
        this.usuarioId = usuarioId;
        this.archivoId = archivoId;
        this.ancho = ancho;
        this.largo = largo;
        this.pisos = pisos;
        this.alturaPiso = alturaPiso;
        this.motor = motor;
        this.resultado = resultado;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public Long getArchivoId() { return archivoId; }
    public double getAncho() { return ancho; }
    public double getLargo() { return largo; }
    public int getPisos() { return pisos; }
    public double getAlturaPiso() { return alturaPiso; }
    public String getMotor() { return motor; }
    public String getResultado() { return resultado; }
    public Date getCreado() { return creado; }
}

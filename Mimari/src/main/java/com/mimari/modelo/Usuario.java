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

/** Semana 13 - entidad JPA: una persona que usa MIMARI. Guarda el hash de la clave, nunca la clave. */
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(name = "clave_hash", nullable = false, length = 200)
    private String claveHash;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creado = new Date();

    public Usuario() { }

    public Usuario(String nombre, String claveHash) {
        this.nombre = nombre;
        this.claveHash = claveHash;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getClaveHash() { return claveHash; }
    public Date getCreado() { return creado; }
}

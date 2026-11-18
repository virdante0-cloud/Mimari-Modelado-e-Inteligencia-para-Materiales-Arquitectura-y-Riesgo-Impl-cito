package com.mimari.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.io.Serializable;
import java.util.Date;

/**
 * Semana 13 - entidad JPA: una persona que usa MIMARI. Guarda el hash de la clave, nunca la clave.
 * Semana 16 - lleva un ROL sencillo: ADMIN (puede eliminar usuarios) o USUARIO (solo gestiona lo suyo).
 * Es Serializable porque los beans CDI de vista (@ViewScoped) guardan listas de estas entidades.
 */
@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String ADMIN = "ADMIN";
    public static final String USUARIO = "USUARIO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(name = "clave_hash", nullable = false, length = 200)
    private String claveHash;

    /** ADMIN | USUARIO. El valor por defecto en la BD hace que las cuentas anteriores a esta versión queden como USUARIO. */
    @Column(nullable = false, length = 10, columnDefinition = "varchar(10) default 'USUARIO'")
    private String rol = USUARIO;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creado = new Date();

    public Usuario() { }

    public Usuario(String nombre, String claveHash, String rol) {
        this.nombre = nombre;
        this.claveHash = claveHash;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getClaveHash() { return claveHash; }
    public String getRol() { return rol; }
    public Date getCreado() { return creado; }

    public boolean isAdmin() {
        return ADMIN.equals(rol);
    }
}

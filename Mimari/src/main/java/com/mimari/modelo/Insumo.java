package com.mimari.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Semana 13 - entidad JPA: un insumo o componente que una persona agregó a SU catálogo (cemento, varilla, ladrillo...).
 * Cada insumo pertenece a un usuario (usuarioId): nadie ve ni toca los de otro.
 * Solo guarda datos: no valida (la validación está en CatalogoServlet).
 */
@Entity
@Table(name = "insumo")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // AUTO_INCREMENT de MySQL
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private int cantidad;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date creado = new Date();

    public Insumo() { }                                   // JPA exige un constructor vacío

    public Insumo(Long usuarioId, String nombre, String tipo, BigDecimal precio, int cantidad) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.tipo = tipo;
        this.precio = precio;
        this.cantidad = cantidad;
    }

    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public BigDecimal getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public Date getCreado() { return creado; }

    /** Precio x cantidad. Se calcula, no se guarda (JPA usa los campos, no los getters). */
    public BigDecimal getSubtotal() {
        return precio.multiply(BigDecimal.valueOf(cantidad));
    }
}

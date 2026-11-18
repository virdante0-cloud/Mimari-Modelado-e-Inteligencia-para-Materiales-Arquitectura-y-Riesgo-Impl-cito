package com.mimari.beans;

import com.mimari.modelo.Insumo;
import com.mimari.servicio.InsumoService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Semanas 15 y 16 - bean CDI de "Mis insumos" (jsf/insumos.xhtml).
 *   @Named      el nombre con el que la vista lo llama:  #{insumosBean.nombre}, #{insumosBean.guardar}
 *   @ViewScoped vive mientras el usuario esté en esta página (se descarta al salir de ella)
 * La vista (XHTML) pone los componentes y los validadores; aquí solo está la lógica. Cero HTML en Java.
 * Ciclo de vida JSF: restaurar vista -> aplicar valores -> VALIDAR -> actualizar modelo (setters) -> invocar acción (guardar).
 */
@Named("insumosBean")
@ViewScoped
public class InsumosBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(InsumosBean.class.getName());

    @Inject
    private InsumoService insumoService;
    @Inject
    private SesionBean sesion;

    // Campos del formulario (los llena JSF en la fase "actualizar modelo", solo si pasaron la validación)
    private String nombre;
    private String tipo;
    private BigDecimal precio;
    private Integer cantidad;

    // Datos que muestra <h:dataTable>
    private List<Insumo> insumos = new ArrayList<>();

    @PostConstruct
    public void cargar() {
        try {
            insumos = insumoService.listar(sesion.getUsuarioId());
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudieron leer los insumos", e);
            Mensajes.error("No se pudo acceder a la base de datos. Intenta de nuevo en unos minutos.");
        }
    }

    /** Acción del botón "Guardar". Si algún validador falla, JSF NO llega hasta aquí. */
    public void guardar() {
        try {
            insumoService.crear(sesion.getUsuarioId(), nombre.trim(), tipo.trim(),
                    precio.setScale(2, RoundingMode.HALF_UP), cantidad);
            nombre = null;
            tipo = null;
            precio = null;
            cantidad = null;
            cargar();
            Mensajes.info("Insumo guardado.");
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudo guardar el insumo", e);
            Mensajes.error("No se pudo guardar. Verifica que MySQL esté encendido.");
        }
    }

    /** Acción del botón "Eliminar" de cada fila. El servicio solo borra si el insumo es de esta persona. */
    public void eliminar(Insumo insumo) {
        try {
            if (insumoService.eliminar(sesion.getUsuarioId(), insumo.getId())) {
                Mensajes.info("Insumo eliminado.");
            }
            cargar();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudo eliminar el insumo", e);
            Mensajes.error("No se pudo eliminar el insumo.");
        }
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Insumo i : insumos) {
            total = total.add(i.getSubtotal());
        }
        return total;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public List<Insumo> getInsumos() { return insumos; }
}

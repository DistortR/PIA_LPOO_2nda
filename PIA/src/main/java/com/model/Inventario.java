package com.model;

import java.io.Serializable;

public class Inventario implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String nombre;
    private long cantidad;

    public Inventario(String id, String nombre, long cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getCantidad() {
        return cantidad;
    }

    public void setCantidad(long cantidad) {
        this.cantidad = cantidad;
    }

    public void addCantidad(long cantidad) {
        if (cantidad > 0)
            this.cantidad += cantidad;
    }

    public void removeCantidad(long cantidad) {
        if (cantidad > 0)
            this.cantidad -= cantidad;
    }

}

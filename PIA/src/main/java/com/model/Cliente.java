package com.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String nombre;
    private String apellido;
    private String email;
    private LocalDate fechaRegistro;
    private Integer puntosFidelidad;

    private Membresia membresiaActual;

    public Cliente(String id, String nombre, String apellido, String email) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.fechaRegistro = LocalDate.now();
        this.puntosFidelidad = 0;
        this.membresiaActual = null;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public void agregarPuntos(int puntos) {
        if (puntos > 0) {
            this.puntosFidelidad += puntos;
        }
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getPuntosFidelidad() {
        return puntosFidelidad;
    }

    public void setPuntosFidelidad(int puntosFidelidad) {
        this.puntosFidelidad = puntosFidelidad;
    }

    public Membresia getMembresiaActual() {
        return membresiaActual;
    }

    public void setMembresiaActual(Membresia membresiaActual) {
        this.membresiaActual = membresiaActual;
    }

    @Override
    public String toString() {
        String estadoMembresia = (membresiaActual != null && membresiaActual.esValida()) ? "Activa" : "Sin Membresía/Vencida";

        return "Cliente{" +
                "ID='" + id + '\'' +
                ", Nombre='" + getNombreCompleto() + '\'' +
                ", Email='" + email + '\'' +
                ", Puntos=" + puntosFidelidad +
                ", Estado=" + estadoMembresia +
                '}';
    }
}
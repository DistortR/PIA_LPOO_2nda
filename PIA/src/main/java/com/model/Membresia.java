package com.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Membresia implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TipoMembresia {
        BASICA, PREMIUM, ESTUDIANTE, ANUAL
    }

    private TipoMembresia tipo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private double costo;
    private boolean activa;

    public Membresia(TipoMembresia tipo, int duracionMeses, double costoBase) {
        this.tipo = tipo;
        this.fechaInicio = LocalDate.now();
        this.fechaFin = fechaInicio.plusMonths(duracionMeses);
        this.costo = costoBase;
        this.activa = true;
    }

    // Método para verificar si sigue vigente hoy
    public boolean esValida() {
        return activa && LocalDate.now().isBefore(fechaFin);
    }

    public void cancelar() {
        this.activa = false;
    }

    public long diasRestantes() {
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaFin);
    }

    // Getters
    public TipoMembresia getTipo() { return tipo; }
    public double getCosto() { return costo; }
    public LocalDate getFechaFin() { return fechaFin; }

    public void setActiva(boolean activa) { this.activa = activa; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public void setFechaInicio(LocalDate fechaInicio) {this.fechaInicio = fechaInicio; }
}
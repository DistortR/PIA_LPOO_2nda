package com.model;

import java.io.Serializable;

public class UsuarioEmpleado implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password; // En un caso real, esto debería estar hasheado
    private String nombreCompleto;
    private String rol; // "ADMIN", "RECEPCION"

    public UsuarioEmpleado(String username, String password, String nombre, String rol) {
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombre;
        this.rol = rol;
    }
    public boolean autenticar(String user, String pass) {
        return this.username.equals(user) && this.password.equals(pass);
    }

    public String getNombreCompleto() { return nombreCompleto; }
    public String getRol() { return rol; }

    @Override
    public String toString() {
        return nombreCompleto + " [" + rol + "]";
    }
}
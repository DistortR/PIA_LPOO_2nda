package com.controller;

import com.main.MainApp;
import com.model.Cliente;
import com.model.Membresia;
import com.util.GymException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ControlAccesoIbarra {
    private List<String> registroAccesos;

    public ControlAccesoIbarra() {
        this.registroAccesos = new ArrayList<>();
    }

    public boolean validarEntrada(Cliente cliente) throws GymException {
        Membresia mem = cliente.getMembresiaActual();
        if (mem == null) {
            throw new GymException("El cliente no tiene membresía activa.");
        }

        if (!mem.esValida()) {
            throw new GymException("Membresía vencida. Por favor renovar.");
        }

        cliente.agregarPuntos(10);
        registrarEvento(cliente, "ENTRADA");
        MainApp.gestorClientes.actualizar(cliente);
        MainApp.actualizarVistaClientes(MainApp.tableView);
        return true;
    }

    public void registrarSalida(Cliente cliente) {
        registrarEvento(cliente, "SALIDA");
    }

    private void registrarEvento(Cliente c, String tipo) {
        String log = String.format("[%s] %s - Cliente: %s (%s). 10 puntos añadidos, %d puntos totales.",
                LocalDateTime.now(), tipo, c.getNombreCompleto(), c.getId(), c.getPuntosFidelidad());
        registroAccesos.add(log);
        System.out.println(log);
    }

    public List<String> getHistorialAccesos() {
        return registroAccesos;
    }
}
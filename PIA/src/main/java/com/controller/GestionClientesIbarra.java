package com.controller;

import com.model.Cliente;
import com.model.UsuarioEmpleado;
import com.util.Serializador;
import com.util.GymException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestionClientesIbarra {

    private List<Cliente> clientes;
    private List<UsuarioEmpleado> empleados; // Lista de empleados
    private Serializador<Cliente> dbCliente;
    private Serializador<UsuarioEmpleado> dbEmpleado; // Serializador para empleados

    String proyectoDir = System.getProperty("user.dir");
    private final String DB_FILE_CLIENTES = proyectoDir + "\\data\\clientes.ser";
    private final String DB_FILE_EMPLEADOS = proyectoDir + "\\data\\empleados.ser";

    public GestionClientesIbarra() {
        this.dbCliente = new Serializador<>();
        this.dbEmpleado = new Serializador<>();

        this.clientes = dbCliente.cargar(DB_FILE_CLIENTES);
        this.empleados = dbEmpleado.cargar(DB_FILE_EMPLEADOS);

        // Carga de administrador inicial (Requisito: Si no hay empleados, crea el admin)
        if (this.empleados.isEmpty()) {
            this.empleados.add(new UsuarioEmpleado("jibarra", "admin123", "Juan Ibarra (ADMIN)", "ADMIN"));
            try { guardarEmpleados(); } catch (GymException e) { /* Ignorar en inicialización */ }
        }
    }

    public void registrarCliente(Cliente c) throws GymException {
        if (buscarCliente(c.getId()).isPresent()) {
            throw new GymException("El cliente con ID " + c.getId() + " ya existe.");
        }
        clientes.add(c);
        guardarClientes();
    }

    public void actualizarCliente(Cliente clienteModificado) throws GymException {
        boolean encontrado = false;

        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getId().equals(clienteModificado.getId())) {
                clientes.set(i, clienteModificado);
                encontrado = true;
                break;
            }
        }

        if (encontrado) {
            guardarClientes();
        } else {
            throw new GymException("No se puede actualizar: El cliente con ID " + clienteModificado.getId() + " no existe.");
        }
    }

    public Optional<Cliente> buscarCliente(String id) {
        return clientes.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public void eliminarCliente(String id) throws GymException {
        if (clientes.removeIf(c -> c.getId().equals(id))) {
            guardarClientes();
        } else {
            throw new GymException("Cliente con ID " + id + " no encontrado para eliminar.");
        }
    }

    private void guardarClientes() throws GymException {
        dbCliente.guardar(DB_FILE_CLIENTES, clientes);
    }

    private void guardarEmpleados() throws GymException {
        dbEmpleado.guardar(DB_FILE_EMPLEADOS, empleados);
    }

    public List<Cliente> getListaClientes() { return clientes; }

    public UsuarioEmpleado autenticar(String user, String pass) throws GymException {
        Optional<UsuarioEmpleado> userOpt = empleados.stream()
                .filter(e -> e.autenticar(user, pass))
                .findFirst();

        if (userOpt.isPresent()) {
            return userOpt.get();
        } else {
            throw new GymException("Credenciales inválidas. Acceso denegado.");
        }
    }
}
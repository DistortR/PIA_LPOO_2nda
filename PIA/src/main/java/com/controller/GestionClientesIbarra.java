package com.controller;

import com.model.Cliente;
import com.model.UsuarioEmpleado;
import com.util.Gestionador;
import com.util.Serializador;
import com.util.GymException;
import com.util.Validador;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class GestionClientesIbarra implements Gestionador<Cliente>{
    private List<Cliente> clientes;
    private List<UsuarioEmpleado> empleados;
    private Serializador<Cliente> dbCliente;
    private Serializador<UsuarioEmpleado> dbEmpleado;

    String proyectoDir = System.getProperty("user.dir"); //private final String DB_FILE_CLIENTES =
    //proyectoDir + File.separator + "data" + File.separator + "clientes.ser";
    private final String DB_FILE_CLIENTES = proyectoDir + File.separator + "data" + File.separator + "clientes.ser";
    private final String DB_FILE_EMPLEADOS = proyectoDir + File.separator + "data" + File.separator + "empleados.ser";

    public GestionClientesIbarra() {
        this.dbCliente = new Serializador<>();
        this.dbEmpleado = new Serializador<>();

        this.clientes = dbCliente.cargar(DB_FILE_CLIENTES);
        this.empleados = dbEmpleado.cargar(DB_FILE_EMPLEADOS);

        if (this.empleados.isEmpty()) {
            this.empleados.add(new UsuarioEmpleado("Aibarra", "admin123", "Alexis Ibarra (ADMIN)", "ADMIN"));
            try { guardarEmpleados(); } catch (GymException e) {}
        }
    }

    public void registrar(Cliente c) throws GymException {
        if (!Validador.validarEntrada(c.getNombre())) {
            throw new GymException("El nombre ingresado es invalido");
        }

        if (!Validador.validarEntrada(c.getApellido())) {
            throw new GymException("El apellido ingresado es invalido");
        }

        if (!Validador.validarCorreo(c.getEmail())) {
            throw new GymException("El correo ingresado es invalido");
        }

        if (buscar(c.getId()).isPresent()) {
            throw new GymException("El cliente con ID " + c.getId() + " ya existe.");
        }

        clientes.add(c);
        guardarClientes();
    }

    public void actualizar(Cliente clienteModificado) throws GymException {
        if (!Validador.validarEntrada(clienteModificado.getNombre())) {
            throw new GymException("El nombre ingresado es invalido");
        }

        if (!Validador.validarEntrada(clienteModificado.getApellido())) {
            throw new GymException("El apellido ingresado es invalido");
        }

        if (!Validador.validarCorreo(clienteModificado.getEmail())) {
            throw new GymException("El correo ingresado es invalido");
        }

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

    public Optional<Cliente> buscar(String id) {
        return clientes.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public void eliminar(String id) throws GymException {
        if (clientes.removeIf(c -> c.getId().equals(id))) {
            guardarClientes();
        } else {
            throw new GymException("Cliente con ID " + id + " no encontrado para eliminar.");
        }
    }

    private synchronized void guardarClientes() throws GymException { //la hice synchronized
        dbCliente.guardar(DB_FILE_CLIENTES, clientes);
    }

    private void guardarEmpleados() throws GymException {
        dbEmpleado.guardar(DB_FILE_EMPLEADOS, empleados);
    }

    public List<Cliente> getLista() { return clientes; }

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
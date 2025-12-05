package com.controller;

import com.model.Cliente;
import com.model.UsuarioEmpleado;
import com.util.Gestionador;
import com.util.Serializador;
import com.util.GymException;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class GestionClientesIbarra implements Gestionador<Cliente>{
    private ObservableList<Cliente> listaClientes;
    private List<Cliente> clientes;
    private List<UsuarioEmpleado> empleados;
    private Serializador<Cliente> dbCliente;
    private Serializador<UsuarioEmpleado> dbEmpleado;

    String proyectoDir = System.getProperty("user.dir");
    private final String DB_FILE_CLIENTES = proyectoDir + "\\data\\clientes.ser";
    private final String DB_FILE_EMPLEADOS = proyectoDir + "\\data\\empleados.ser";

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

    private boolean validarNombre(String nombre) {
        if (nombre == null || nombre.isEmpty()) {
            return false;
        }

        for (int i = 0; i < nombre.length(); i++) {
            char c = nombre.charAt(i);
            if (!Character.isLetter(c) && c != ' ') {
                return false;
            }
        }
        return true;
    }

    private boolean validarCorreo(String correo) {
        if (correo == null || correo.isEmpty()) {
            return false;
        }
        int arroba = correo.indexOf('@');
        if (arroba <= 0 || arroba >= correo.length() - 1) {
            return false;
        }

        int punto = correo.indexOf('.', arroba);
        if (punto <= arroba + 1 || punto >= correo.length() - 1) {
            return false;
        }
        return true;
    }

    public void registrar(Cliente c) throws GymException {
        if (!validarNombre(c.getNombre())) {
            throw new GymException("El nombre ingresado es invalido");
        }

        if (!validarNombre(c.getApellido())) {
            throw new GymException("El apellido ingresado es invalido");
        }

        if (!validarCorreo(c.getEmail())) {
            throw new GymException("El correo ingresado es invalido");
        }

        if (buscar(c.getId()).isPresent()) {
            throw new GymException("El cliente con ID " + c.getId() + " ya existe.");
        }

        clientes.add(c);
        guardarClientes();
    }

    public void actualizar(Cliente clienteModificado) throws GymException {
        boolean encontrado = false;

        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getId().equals(clienteModificado.getId())) {
                clientes.set(i, clienteModificado);
                encontrado = true;
                break;
            }
        }

        if (!validarNombre(clienteModificado.getNombre())) {
            throw new GymException("El nombre ingresado es invalido");
        }

        if (!validarNombre(clienteModificado.getApellido())) {
            throw new GymException("El apellido ingresado es invalido");
        }

        if (!validarCorreo(clienteModificado.getEmail())) {
            throw new GymException("El correo ingresado es invalido");
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

    private void guardarClientes() throws GymException {
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
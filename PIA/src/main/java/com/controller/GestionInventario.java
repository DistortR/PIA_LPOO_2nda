package com.controller;

import com.model.Inventario;
import com.util.Gestionador;
import com.util.GymException;
import com.util.Serializador;
import com.util.Validador;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class GestionInventario implements Gestionador<Inventario> {
    private ObservableList<Inventario> listaInventarios; //quitar
    private List<Inventario> inventario;    
    private Serializador<Inventario> serializadorInventario;

    private final String proyectoDir = System.getProperty("user.dir");
    private final String DB_FILE_INVENTARIO = proyectoDir + File.separator +"data" + File.separator + "inventarios.ser";

    public GestionInventario() {
        this.serializadorInventario = new Serializador<>();
        this.inventario = serializadorInventario.cargar(DB_FILE_INVENTARIO);

    }

    public void registrar(Inventario inv) throws GymException {
        if (!Validador.validarEntrada(inv.getNombre())) {
            throw new GymException("El nombre de la clase debe contener solo letras y espacios.");
        }

        if (buscar(inv.getId()).isPresent()) {
            throw new GymException("El cliente con ID " + inv.getId() + " ya existe.");
        }
        inventario.add(inv);
        guardarInventarios();
    }

    public void eliminar(String id) throws GymException {
        if (inventario.removeIf(inv -> inv.getId().equals(id))) {
            guardarInventarios();
        } else {
            throw new GymException("Cliente con ID " + id + " no encontrado para eliminar.");
        }
    }

    public void actualizar(Inventario objModificado) throws GymException {
        if (!Validador.validarEntrada(objModificado.getNombre())) {
            throw new GymException("El nombre de la clase debe contener solo letras y espacios.");
        }

        boolean encontrado = false;
        for (int i = 0; i < inventario.size(); i++) {
            if (inventario.get(i).getId().equals(objModificado.getId())) {
                inventario.set(i, objModificado);
                encontrado = true;
                break;
            }
        }
        if (encontrado) {
            guardarInventarios();
        }
        else throw new GymException("No se puede actualizar el objeto " + objModificado.getId() + ", no existe.");
    }

    public Optional<Inventario> buscar(String id) throws GymException {

        return inventario.stream().filter(inv -> inv.getId().equals(id)).findFirst();
    }

    public List<Inventario> getLista() {
        return inventario;
    }

    private void guardarInventarios() throws GymException {
        serializadorInventario.guardar(DB_FILE_INVENTARIO, inventario);
    }
}

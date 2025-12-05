package com.controller;

import com.model.Inventario;
import com.util.Gestionador;
import com.util.GymException;
import com.util.Serializador;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class GestionInventario implements Gestionador<Inventario> {
    private ObservableList<Inventario> listaInventarios;
    private List<Inventario> inventario;    
    private Serializador<Inventario> serializadorInventario;

    private final String proyectoDir = System.getProperty("user.dir");
    private final String DB_FILE_INVENTARIO = proyectoDir + "\\data\\inventarios.ser";

    public GestionInventario() {
        this.serializadorInventario = new Serializador<>();
        this.inventario = serializadorInventario.cargar(DB_FILE_INVENTARIO);

    }

    public void registrar(Inventario inv) throws GymException {
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

    public List<Inventario> getLista() throws GymException {
        return inventario;
    }

    private void guardarInventarios() throws GymException {
        serializadorInventario.guardar(DB_FILE_INVENTARIO, inventario);
    }
}

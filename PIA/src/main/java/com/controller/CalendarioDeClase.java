package com.controller;

import com.model.ClaseGrupal;
import com.util.Gestionador;
import com.util.GymException;
import com.util.Serializador;
import com.util.Validador;

import java.util.List;
import java.util.Optional;

public class CalendarioDeClase implements Gestionador<ClaseGrupal> {
    private List<ClaseGrupal> clases;
    private Serializador<ClaseGrupal> dbClase;

    String proyectoDir = System.getProperty("user.dir");
    private final String DB_FILE_CLASES = proyectoDir + "\\data\\clases.ser";

    public CalendarioDeClase() {
        this.dbClase = new Serializador<>();
        this.clases = dbClase.cargar(DB_FILE_CLASES);
    }

    public void registrar(ClaseGrupal clase) throws GymException {
        if (!Validador.validarEntrada(clase.getDescription())) {
            throw new GymException("El nombre de la clase debe contener solo letras y espacios.");
        }
        if (buscar(clase.getId()).isPresent()) {
            throw new GymException("La clase con ID " + clase.getId() + " ya existe.");
        }
        clases.add(clase);
        guardarClases();
    }

    public void eliminar(String id) throws GymException {
        if (clases.removeIf(c -> c.getId().equals(id))) {
            guardarClases();
        } else {
            throw new GymException("Clase con ID " + id + " no encontrado para eliminar.");
        }
    }

    public void actualizar(ClaseGrupal claseModificada) throws GymException {
        if (!Validador.validarEntrada(claseModificada.getDescription())) {
            throw new GymException("El nombre de la clase debe contener solo letras y espacios.");
        }

        boolean encontrado = false;

        for (int i = 0; i < clases.size(); i++) {
            if (clases.get(i).getId().equals(claseModificada.getId())) {
                clases.set(i, claseModificada);
                encontrado = true;
                break;
            }
        }

        if (encontrado) {
            guardarClases();
        } else {
            throw new GymException("No se puede actualizar: La clase con ID " + claseModificada.getId() + " no existe.");
        }
    }

    public Optional<ClaseGrupal> buscar(String id) throws GymException {
        return clases.stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public List<ClaseGrupal> getLista() throws GymException { return clases; }

    public void guardarClases() throws GymException {
        dbClase.guardar(DB_FILE_CLASES, clases);
    }
}
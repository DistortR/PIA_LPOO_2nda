package com.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Serializador<T> {

    public void guardar(String archivo, List<T> lista) throws GymException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(archivo))) {
            oos.writeObject(lista);
        } catch (IOException e) {
            throw new GymException("Error al guardar datos en " + archivo);
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> cargar(String archivo) {
        File file = new File(archivo);
        if (!file.exists()) return new ArrayList<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return (ArrayList<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }
}
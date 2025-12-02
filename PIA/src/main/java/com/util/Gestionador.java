package com.util;


import java.util.List;
import java.util.Optional;

public interface Gestionador <T>{
    void registrar(T obj) throws GymException;
    void eliminar(String id) throws GymException;
    void actualizar(T obj) throws GymException;
    Optional<T> buscar(String id) throws GymException;
    List<T> getLista() throws GymException;
}
package com.util;

public class GymException extends Exception {
    public GymException(String mensaje) {
        super("Error GymPOS: " + mensaje);
    }
}
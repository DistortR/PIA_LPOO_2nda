package com.util;

public class Validador {
    public static boolean validarEntrada(String nombre) {
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

    public static boolean validarCorreo(String correo) {
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
}

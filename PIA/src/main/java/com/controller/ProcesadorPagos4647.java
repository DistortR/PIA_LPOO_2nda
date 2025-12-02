package com.controller;

import com.model.Membresia;
import com.util.GymException;
import java.util.Random;

public class ProcesadorPagos4647 {

    private final int DIGITOS_MATRICULA = 123;

    public double calcularTotalConCentavos(double precioBase, double descuento) {
        double centavos = precioBase * descuento;
        return precioBase - centavos;
    }

    public boolean procesarPagoTarjeta(String numeroTarjeta, double monto) throws GymException {
        if (numeroTarjeta.length() < 16) {
            throw new GymException("Número de tarjeta inválido.");
        }

        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean aprobado = new Random().nextInt(10) > 0;

        if (!aprobado) {
            throw new GymException("Fondos insuficientes o tarjeta rechazada.");
        }

        System.out.println("Pago procesado por: $" + String.format("%.2f", monto));
        return true;
    }

    public String generarRecibo(String nombreCliente, Membresia mem, double total) {
        return "--- RECIBO DE PAGO ---\n" +
                "Cliente: " + nombreCliente + "\n" +
                "Concepto: " + mem.getTipo() + "\n" +
                "Total Pagado: $" + String.format("%.2f", total) + "\n" +
                "Autorización: " + System.currentTimeMillis();
    }
}
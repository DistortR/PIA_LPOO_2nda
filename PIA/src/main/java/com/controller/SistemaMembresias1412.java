package com.controller;

import com.model.Cliente;
import com.model.Membresia;
import com.model.Membresia.TipoMembresia;
import com.util.GymException;
import java.time.LocalDate;

// IMPORTA TU CLASE REAL DE PROCESADOR DE PAGOS
// Asumiendo que usaste los dígitos 123
import com.controller.ProcesadorPagos4647;

public class SistemaMembresias1412 {

    // Precios base
    private static final double PRECIO_BASICO = 500.0;
    private static final double PRECIO_PREMIUM = 900.0;

    // Dependencia: Instancia del procesador de pagos (AJUSTA EL NOMBRE SI ES NECESARIO)
    private ProcesadorPagos4647 procesadorPagos = new ProcesadorPagos4647();

    private Membresia calcularCostoMembresia(TipoMembresia tipo, int meses) throws GymException {
        if (meses <= 0) throw new GymException("La duración debe ser al menos un mes.");

        double costoBase = 0;

        switch (tipo) {
            case BASICA:
                costoBase = PRECIO_BASICO * meses;
                break;
            case PREMIUM:
                costoBase = PRECIO_PREMIUM * meses;
                if (meses >= 12) costoBase *= 0.90; // 10% descuento por volumen
                break;
            case ESTUDIANTE:
                costoBase = (PRECIO_BASICO * 0.70) * meses; // 30% descuento fijo
                break;
            case ANUAL:
                costoBase = (PRECIO_PREMIUM * 12) * 0.85; // Ejemplo: 15% descuento anual
                meses = 12; // Asegurar la duración
                break;
        }

        // Llama al procesador para aplicar el requisito de los centavos de matrícula
        double costoFinalConCentavos = procesadorPagos.calcularTotalConCentavos(costoBase);

        // Crea el objeto Membresia con el costo final
        return new Membresia(tipo, meses, costoFinalConCentavos);
    }

    public void inscribirCliente(Cliente cliente, TipoMembresia tipo, int meses, String tarjetaSimulada) throws GymException {
        if (cliente.getMembresiaActual() != null && cliente.getMembresiaActual().esValida()) {
            throw new GymException("El cliente ya tiene una membresía activa. Use el método renovarMembresia.");
        }

        Membresia nuevaMembresia = calcularCostoMembresia(tipo, meses);
        double costoFinal = nuevaMembresia.getCosto();

        if (procesadorPagos.procesarPagoTarjeta(tarjetaSimulada, costoFinal)) {
            cliente.setMembresiaActual(nuevaMembresia);
            cliente.agregarPuntos(50);

            // Opcional: Generar recibo
            String recibo = procesadorPagos.generarRecibo(cliente.getNombreCompleto(), nuevaMembresia, costoFinal);
            System.out.println(recibo);

            System.out.println("Cliente " + cliente.getId() + " inscrito con éxito. Total pagado: $" + String.format("%.2f", costoFinal));
        } else {
            throw new GymException("Error desconocido al procesar el pago para la nueva suscripción.");
        }
    }

    public void renovarMembresia(Cliente cliente, int mesesExtras, String tarjetaSimulada) throws GymException {
        if (mesesExtras <= 0) {
            throw new GymException("La renovación debe ser por al menos un mes.");
        }

        Membresia membresiaActual = cliente.getMembresiaActual();

        if (membresiaActual == null) {
            throw new GymException("El cliente " + cliente.getNombreCompleto() + " no tiene una membresía registrada.");
        }

        double precioBasePorMes = membresiaActual.getTipo() == TipoMembresia.BASICA || membresiaActual.getTipo() == TipoMembresia.ESTUDIANTE
                ? PRECIO_BASICO : PRECIO_PREMIUM;

        double costoBaseRenovacion = precioBasePorMes * mesesExtras;


        double costoFinalRenovacion = procesadorPagos.calcularTotalConCentavos(costoBaseRenovacion);


        if (!procesadorPagos.procesarPagoTarjeta(tarjetaSimulada, costoFinalRenovacion)) {
            throw new GymException("Pago rechazado. No se pudo completar la renovación.");
        }


        LocalDate fechaInicioRenovacion = membresiaActual.esValida()
                ? membresiaActual.getFechaFin().plusDays(1) // Si es válida, empieza el día después de vencer
                : LocalDate.now(); // Si está vencida, empieza hoy


        LocalDate nuevaFechaFin = fechaInicioRenovacion.plusMonths(mesesExtras);
        membresiaActual.setFechaFin(nuevaFechaFin);
        membresiaActual.setActiva(true);

        cliente.agregarPuntos(mesesExtras * 10);
        String recibo = procesadorPagos.generarRecibo(cliente.getNombreCompleto(), membresiaActual, costoFinalRenovacion);
        System.out.println(recibo);

        System.out.println("Membresía renovada para " + cliente.getNombreCompleto() + " hasta el " + nuevaFechaFin + ". Puntos añadidos.");
    }

}
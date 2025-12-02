package com.controller;

import com.model.Cliente;
import com.model.Inventario;
import com.model.Membresia;
import javafx.concurrent.Task;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GeneradorReportesA extends Task<Boolean> {

    private List<Cliente> listaClientes;
    private List<Inventario> listaInventario;

    public GeneradorReportesA(List<Cliente> listaClientes, List<Inventario> listaInventarios) {
        this.listaClientes = listaClientes;
        this.listaInventario = listaInventarios;
    }

    @Override
    protected Boolean call() throws Exception {
        updateMessage("Iniciando Reporte de Inventarios...");
        updateProgress(0, 50);
        Thread.sleep(500);

        String nombreArchivo = "ReporteA.txt";

        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            updateMessage("Iniciando Reporte de Inventarios...");
            String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            writer.write("=================================================================================\n");
            writer.write("                             REPORTE MENSUAL GIMNASIO                            \n");
            writer.write("=================================================================================\n");
            writer.write("Fecha de generación: " + fechaHora + "\n");
            writer.write("---------------------------------------------------------------------------------\n\n");

            updateProgress(20, 100);
            updateMessage("Procesando datos de clientes y membresías...");
            writer.write("~~~ CLIENTES Y ESTADO DE MEMBRESÍAS ~~~\n");

            String formatoClientes = "%-10s | %-30s | %-15s | %-15s | %-10s\n";
            writer.write(String.format(formatoClientes, "ID", "Nombre", "Membresía", "Vencimiento", "Estado"));
            writer.write("---------------------------------------------------------------------------------\n");

            if (listaClientes != null && !listaClientes.isEmpty()) {
                for (Cliente c : listaClientes) {

                    String nombre = c.getNombreCompleto();
                    String id = c.getId();
                    String tipoMembresia = "Sin membresía";
                    String fechaVence = "N/A";
                    String estado = "Inactivo";

                    Membresia m = c.getMembresiaActual();
                    if (m != null) {
                        tipoMembresia = m.getTipo().toString();
                        fechaVence = m.getFechaFin().toString();
                        estado = m.esValida() ? "Activo" : "Vencido";
                    }

                    writer.write(String.format(formatoClientes, id, nombre, tipoMembresia, fechaVence, estado));
                }
            } else {
                writer.write("No hay clientes registrados en el sistema.\n");
            }
            writer.write("\n");

            updateProgress(60, 100);
            Thread.sleep(300);

            updateMessage("Procesando inventario del gimnasio...");
            writer.write("~~~ SECCIÓN: INVENTARIO Y EQUIPO ~~~\n");

            String formatoInventario = "%-10s | %-30s | %-10s\n";
            writer.write(String.format(formatoInventario, "ID", "Producto / Equipo", "Cantidad"));
            writer.write("---------------------------------------------------------------------------------\n");

            if (listaInventario != null && !listaInventario.isEmpty()) {
                for (Inventario inv : listaInventario) {
                    String idInv = inv.getId();
                    String nombreInv = inv.getNombre();
                    long cantidad = inv.getCantidad();

                    writer.write(String.format(formatoInventario, idInv, nombreInv, String.valueOf(cantidad)));
                }
            } else {
                writer.write("   El inventario está vacío.\n");
            }

            updateMessage("Reporte generado exitosamente.");
            updateProgress(100, 100);

        } catch (IOException e) {
            updateMessage("Error al escribir el archivo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        return null;
    }
}

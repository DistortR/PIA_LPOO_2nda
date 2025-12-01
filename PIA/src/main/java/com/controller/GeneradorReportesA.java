package com.controller;

import com.model.Cliente;
import com.model.Inventario;
import com.model.Membresia;
import com.model.UsuarioEmpleado;
import javafx.concurrent.Task;
import java.io.FileWriter;
import java.util.List;

import static com.main.MainApp.*;

public class GeneradorReportesA extends Task<Boolean> {

    @Override
    protected Boolean call() throws Exception {
        updateMessage("Iniciando generación de reporte...");
        Thread.sleep(1000);

        updateMessage("Recopilando estadísticas...");

        updateMessage("Escribiendo archivo PDF/TXT...");
        try (FileWriter writer = new FileWriter("Reporte_Gym.txt")) {
            writer.write("REPORTE MENSUAL GIMNASIO\n");
            writer.write("Generado: " + java.time.LocalDateTime.now() + "\n");
            writer.write("----------------------------\n");

            List<Cliente> clientes = gestorClientes.getLista();
            List<Inventario> inventario = gestorInventario.getLista();
            //List<Membresia> membresias = gestorMembresias
        }

        updateMessage("Reporte generado exitosamente.");
        return true;
    }
}
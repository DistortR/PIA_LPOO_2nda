package com.controller;

import javafx.concurrent.Task;
import java.io.FileWriter;

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

        }

        updateMessage("Reporte generado exitosamente.");
        return true;
    }
}
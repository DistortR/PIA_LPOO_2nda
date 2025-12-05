package com.view;

import com.model.Cliente;
import com.util.GymException;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import static com.main.MainApp.*;

public class VistaControlAcceso {
    public static VBox crearVistaControlAcceso() {
        TextField txtIdCliente = new TextField();
        txtIdCliente.setPromptText("ID del Cliente");
        Button btnAcceso = new Button("Validar Entrada");
        Button btnSalida = new Button("Registrar Salida");

        ListView<String> logListView = new ListView<>();
        logListView.setItems(javafx.collections.FXCollections.observableList(controlAcceso.getHistorialAccesos()));

        Label lblResultado = new Label("Estado: Esperando escaneo...");
        lblResultado.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        btnAcceso.setOnAction(e -> {
            try {
                Cliente cliente = gestorClientes.buscar(txtIdCliente.getText()).orElseThrow(() -> new GymException("Cliente con ID no encontrado."));

                if (controlAcceso.validarEntrada(cliente)) {
                    lblResultado.setText("ACCESO PERMITIDO: Bienvenido(a) " + cliente.getNombreCompleto());
                    lblResultado.setStyle("-fx-font-weight: bold; -fx-text-fill: green; -fx-font-size: 16px;");
                }
            } catch (GymException ex) {
                lblResultado.setText("ACCESO DENEGADO: " + ex.getMessage());
                lblResultado.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-font-size: 16px;");
            } finally {
                logListView.setItems(javafx.collections.FXCollections.observableList(controlAcceso.getHistorialAccesos()));
            }
        });

        btnSalida.setOnAction(e -> {
            try {
                Cliente cliente = gestorClientes.buscar(txtIdCliente.getText()).orElseThrow(() -> new GymException("Cliente con ID no encontrado."));
                controlAcceso.registrarSalida(cliente);
                lblResultado.setText("Salida registrada para: " + cliente.getNombreCompleto());
                lblResultado.setStyle("-fx-font-weight: bold; -fx-text-fill: blue; -fx-font-size: 16px;");
            } catch (GymException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
            } finally {
                logListView.setItems(javafx.collections.FXCollections.observableList(controlAcceso.getHistorialAccesos()));
            }
        });

        HBox inputArea = new HBox(10, txtIdCliente, btnAcceso, btnSalida);
        VBox.setVgrow(logListView, Priority.ALWAYS);

        return new VBox(15, lblResultado, inputArea, new Label("--- Historial de Accesos ---"), logListView);
    }
}

package com.view;

import com.model.Cliente;
import com.model.Membresia;
import com.util.GymException;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import static com.main.MainApp.*;

public class MembresiasView {
    public static VBox crearVistaMembresias() {
        TextField txtClienteId = new TextField();
        txtClienteId.setPromptText("ID Cliente a Cobrar");
        ComboBox<Membresia.TipoMembresia> cmbTipo = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(Membresia.TipoMembresia.values()));
        cmbTipo.getSelectionModel().selectFirst();
        Spinner<Integer> spnMeses = new Spinner<>(1, 12, 1);


        Button btnInscribir = new Button("Inscribir Cliente (Pagar)");
        Button btnRenovar = new Button("Renovar Membresía");
        TextArea logArea = new TextArea();
        logArea.setEditable(false);

        btnInscribir.setOnAction(e -> {
            try {
                Cliente cliente = gestorClientes.buscar(txtClienteId.getText()).orElseThrow(() -> new GymException("Cliente no encontrado."));
                Membresia.TipoMembresia tipo = cmbTipo.getValue();
                int meses = spnMeses.getValue();

                gestorMembresias.inscribirCliente(cliente, tipo, meses, "1234567890123456");
                logArea.appendText("Inscripción exitosa para " + cliente.getNombreCompleto() + ". Verifique en Clientes.\n");
                gestorClientes.actualizar(cliente);
                actualizarVistaClientes(tableView);

                txtClienteId.clear();
                spnMeses.getValueFactory().setValue(1);
                txtClienteId.requestFocus();

            } catch (GymException ex) {
                logArea.appendText("ERROR: " + ex.getMessage() + "\n");
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Pago/Membresía", ex.getMessage());
            }
        });

        btnRenovar.setOnAction(e -> {
            try {
                Cliente cliente = gestorClientes.buscar(txtClienteId.getText()).orElseThrow(() -> new GymException("Cliente no encontrado."));
                int meses = spnMeses.getValue();

                if (cliente.getPuntosFidelidad() >= 100)
                {
                    showDiscountPopup(cliente, logArea);
                    gestorMembresias.renovarMembresia(cliente, meses, "1234567890123456", 0.3);
                }
                else
                {
                    gestorMembresias.renovarMembresia(cliente, meses, "1234567890123456", 0.0);
                }
                logArea.appendText("Renovación exitosa para " + cliente.getNombreCompleto() + ".\n");

                txtClienteId.clear();
                spnMeses.getValueFactory().setValue(1);
                txtClienteId.requestFocus();

            } catch (GymException ex) {
                logArea.appendText("ERROR: " + ex.getMessage() + "\n");
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Renovación", ex.getMessage());
            }
        });

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.addRow(0, new Label("ID Cliente:"), txtClienteId);
        grid.addRow(1, new Label("Tipo:"), cmbTipo);
        grid.addRow(2, new Label("Meses:"), spnMeses);

        HBox cajaBotones = new HBox(10, btnInscribir, btnRenovar);
        grid.add(cajaBotones, 1, 3);

        VBox.setVgrow(logArea, Priority.ALWAYS);
        VBox layout = new VBox(15, grid, logArea);
        layout.setPadding(new javafx.geometry.Insets(20));
        return layout;    }

}

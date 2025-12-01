package com.view;

import com.model.Cliente;
import com.model.Membresia;
import com.util.GymException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import static com.main.MainApp.*;

public class VistaClientes {
    public static TableView<Cliente> tableView = new TableView<>();
    public static BorderPane CRUDVistaClientes() {
        tableView.setItems(javafx.collections.FXCollections.observableList(gestorClientes.getLista()));

        TableColumn<Cliente, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));

        TableColumn<Cliente, String> nombreCol = new TableColumn<>("Nombre Completo");
        nombreCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombreCompleto()));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Cliente, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEmail()));

        TableColumn<Cliente, String> fechaRegistroCol = new TableColumn<>("Fecha Registro");
        fechaRegistroCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFechaRegistro().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

        TableColumn<Cliente, String> membresiaCol = new TableColumn<>("Estado Membresía");
        membresiaCol.setCellValueFactory(cellData -> {
            Membresia mem = cellData.getValue().getMembresiaActual();
            if (mem != null && (mem.esValida() || Objects.requireNonNull(mem).esActualizable()))
            {
                if (Objects.requireNonNull(mem).esActualizable() && mem.diasRestantes() == 0) // actualizar membresia automaticamente
                {
                    try {
                        gestorMembresias.inscribirCliente(cellData.getValue(),
                                mem.getTipo(),
                                mem.getMeses(),
                                "1234567890123456");
                    } catch (GymException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("Inscripción exitosa para " + cellData.getValue().getNombreCompleto() + ". Verifique en Clientes.\n");
                }
                return new javafx.beans.property.SimpleStringProperty(mem.getTipo().name() + " (Vence: " + mem.getFechaFin() + ")");
            }
            else {
                return new javafx.beans.property.SimpleStringProperty("INACTIVO");
            }
        });

        tableView.getColumns().addAll(idCol, nombreCol, emailCol, fechaRegistroCol, membresiaCol);

        Button btnAgregar = new Button("Registrar Cliente");
        btnAgregar.setOnAction(e -> {
            CreateClient(tableView);
        });

        Button btnActualizar = new Button("Actualizar Cliente");
        btnActualizar.setOnAction(e -> UpdateClient(tableView));

        Button btnEliminar = new Button("Eliminar Cliente");
        btnEliminar.setOnAction(e -> {
            deleteClient(tableView);
        });

        BorderPane panel = new BorderPane();
        panel.setCenter(tableView);

        HBox botones = new HBox(10, btnAgregar, btnActualizar, btnEliminar);
        botones.setPadding(new Insets(10));
        panel.setBottom(botones);
        return panel;
    }

    private static void CreateClient(TableView<Cliente> tableView) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Agregar Cliente");
        dialog.setHeaderText("Ingrese los datos");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        TextField lastNameField = new TextField();
        TextField emailField = new TextField();
        nameField.setPromptText("Ej. Arturo");
        lastNameField.setPromptText("Ej. Garcia");
        emailField.setPromptText("Ej. juan@gmail.com");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Apellido:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> nameField.requestFocus());

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if(!nameField.getText().isEmpty() && !lastNameField.getText().isEmpty() && !emailField.getText().isEmpty()) {
                    Cliente c = new Cliente("C" + (gestorClientes.getLista().size() + 1), nameField.getText(), lastNameField.getText(), emailField.getText());
                    gestorClientes.registrar(c);
                    tableView.setItems(javafx.collections.FXCollections.observableList(gestorClientes.getLista()));
                }
                else {
                    mostrarAlerta(Alert.AlertType.WARNING, "Datos Incompletos", "Por favor complete todos los campos.");
                }
            } catch (GymException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error Registro", ex.getMessage());
            }
        }
    }

    private static void UpdateClient(TableView<Cliente> tableView) {
        Cliente selectedClient = tableView.getSelectionModel().getSelectedItem();

        if (selectedClient == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "ALERTA", "Por favor, selecciona un cliente de la lista para editar.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Actualizar Cliente");
        dialog.setHeaderText("Modifique los datos del cliente");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selectedClient.getNombre());
        TextField lastNameField = new TextField(selectedClient.getApellido());
        TextField emailField = new TextField(selectedClient.getEmail());

        nameField.setPromptText("Nombre");
        lastNameField.setPromptText("Apellido");
        emailField.setPromptText("Email");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Apellido:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (!nameField.getText().isEmpty() || !lastNameField.getText().isEmpty() || !emailField.getText().isEmpty()) {
                try {
                    selectedClient.setNombre(nameField.getText());
                    selectedClient.setApellido(lastNameField.getText());
                    selectedClient.setEmail(emailField.getText());

                    gestorClientes.actualizar(selectedClient);
                    tableView.refresh();
                } catch (Exception ex) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al Actualizar", ex.getMessage());
                }
            }
            else {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos Incompletos", "Por favor complete al menos un campo para actualizar.");
            }
        }
    }

    private static void deleteClient(TableView<Cliente> tableView) {
        Cliente selectedClient = tableView.getSelectionModel().getSelectedItem();

        if (selectedClient == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "ALERTA", "Por favor, selecciona un cliente de la lista para eliminar.");
            return;
        }

        Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION);
        alertConfirm.setTitle("Confirmar Eliminación");
        alertConfirm.setHeaderText("¿Está seguro de eliminar al cliente?");
        alertConfirm.setContentText("Cliente: " + selectedClient.getNombreCompleto());

        Optional<ButtonType> result = alertConfirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gestorClientes.eliminar(selectedClient.getId());
                tableView.setItems(javafx.collections.FXCollections.observableList(gestorClientes.getLista()));
            } catch (GymException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al Eliminar", ex.getMessage());
            }
        }
    }

    private void actualizarVistaClientes(TableView<Cliente> tableView) {
        if (tableView != null)
        {
            tableView.setItems(javafx.collections.FXCollections.observableList(gestorClientes.getLista()));
            tableView.refresh();
        }
    }

}

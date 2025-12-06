package com.view;

import com.main.MainApp;
import com.model.Cliente;
import com.model.Membresia;
import com.main.MainApp;
import com.util.GymException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.main.MainApp.*;

public class ClienteView {
    public static BorderPane CRUDVistaClientes() {
        tableView = new TableView<>();
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
            if (cellData.getValue().isMembresiaRenovable() && !mem.esValida())
            {
                System.out.println("Membresia del usuario "+ cellData.getValue().getNombreCompleto() +" renovada");
                return new javafx.beans.property.SimpleStringProperty(mem.getTipo().name() + " (Vence: " + mem.getFechaFin() + ")");
            }
            return new javafx.beans.property.SimpleStringProperty(mem != null && mem.esValida() ? mem.getTipo().name() + " (Vence: " + mem.getFechaFin() + ")" : "INACTIVA");
        });

        TableColumn<Cliente, String> puntosCol = new TableColumn<>("Puntos de fidelidad");
        puntosCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPuntosFidelidad().toString()));

        tableView.getColumns().addAll(idCol, nombreCol, emailCol,  fechaRegistroCol, membresiaCol, puntosCol);

        Button btnAgregar = new Button("Registrar Cliente");
        btnAgregar.setOnAction(e -> {
            CreateClient(tableView);
        });

        Button btnActualizar = new Button("Actualizar Cliente");
        btnActualizar.setOnAction(e -> {
            UpdateClient(tableView);
        });

        Button btnEliminar = new Button("Eliminar Cliente");
        btnEliminar.setOnAction(e -> {
            deleteClient(tableView);
        });

        BorderPane panel = new BorderPane();
        panel.getStyleClass().add("content-pane");
        tableView.getStyleClass().add("custom-table");
        panel.setCenter(tableView);

        HBox botones = new HBox(10, btnAgregar, btnActualizar, btnEliminar);
        botones.getStyleClass().add("button-row");
        botones.setPadding(new Insets(12));
        panel.setBottom(botones);
        return panel;
    }

    private static void CreateClient(TableView<Cliente> tableView) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Agregar Cliente");
        dialog.setHeaderText("Ingrese los datos");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image(ClienteView.class.getResourceAsStream("/create.png")));

        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }

        try {
            if (ClienteView.class.getResourceAsStream("/create.png") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(ClienteView.class.getResourceAsStream("/create.png"));
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(48);
                iv.setFitHeight(48);
                iv.setPreserveRatio(true);
                dialog.getDialogPane().setGraphic(iv);
            }
        } catch (Exception ex) {}

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

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image(ClienteView.class.getResourceAsStream("/edit.jpg")));

        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }

        try {
            if (ClienteView.class.getResourceAsStream("/edit.jpg") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(ClienteView.class.getResourceAsStream("/edit.jpg"));
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(48);
                iv.setFitHeight(48);
                iv.setPreserveRatio(true);
                dialog.getDialogPane().setGraphic(iv);
            }
        } catch (Exception ex) {}

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
        Platform.runLater(() -> nameField.requestFocus());

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

        Stage stage = (Stage) alertConfirm.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(ClienteView.class.getResourceAsStream("/delete.png")));

        try {
            if (ClienteView.class.getResourceAsStream("/delete.png") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(ClienteView.class.getResourceAsStream("/delete.png"));
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(48);
                iv.setFitHeight(48);
                iv.setPreserveRatio(true);
                alertConfirm.getDialogPane().setGraphic(iv);
            }
        } catch (Exception ex) {}

        if (stylesheet != null) {
            alertConfirm.getDialogPane().getStylesheets().add(stylesheet);
            alertConfirm.getDialogPane().getStyleClass().add("dialog-pane");
        }

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

}

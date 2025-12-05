package com.view;

import com.model.Inventario;
import com.util.GymException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.converter.LongStringConverter;

import java.util.Optional;
import java.util.function.UnaryOperator;

import static com.main.MainApp.gestorInventario;
import static com.main.MainApp.mostrarAlerta;

public class VistaInventario {
    private static final UnaryOperator<TextFormatter.Change> integerFilter = change -> {
        String text = change.getControlNewText();
        if (text.matches("-?([1-9][0-9]*)?")) {return change;}
        return null;
    };

    public static BorderPane crearVistaInventario() {
        TableView<Inventario> tablaInventario = new TableView<>();
        tablaInventario.setItems(javafx.collections.FXCollections.observableList(gestorInventario.getLista()));

        TableColumn<Inventario, String> idObjCol = new TableColumn<>("ID");
        idObjCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));

        TableColumn<Inventario, String> nombreObjCol = new TableColumn<>("Nombre");
        nombreObjCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombre()));

        TableColumn<Inventario, String> cantidadCol = new TableColumn<>("Cantidad");
        cantidadCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCantidad().toString()));
        tablaInventario.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tablaInventario.getColumns().addAll(idObjCol, nombreObjCol, cantidadCol);

        Button btnAgregar = new Button("Registrar Inventario");
        btnAgregar.setOnAction(e -> {
            crearInventario(tablaInventario);
        });

        Button btnActualizar = new Button("Actualizar Inventario");
        btnActualizar.setOnAction(e -> {
            actualizarInventario(tablaInventario);
        });

        Button btnEliminar = new Button("Eliminar Inventario");
        btnEliminar.setOnAction(e -> {
            eliminarInventario(tablaInventario);
        });

        BorderPane panel = new BorderPane();
        panel.setCenter(tablaInventario);

        HBox botones = new HBox(10, btnAgregar, btnActualizar, btnEliminar);
        botones.setPadding(new Insets(10));
        panel.setBottom(botones);
        return panel;
    }

    public static void crearInventario(TableView<Inventario> tablaInventario) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Agregar Inventario");
        dialog.setHeaderText("Ingrese los datos del inventario");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image(VistaInventario.class.getResourceAsStream("/create.png")));

        try {
            if (VistaInventario.class.getResourceAsStream("/create.png") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(VistaInventario.class.getResourceAsStream("/create.png"));
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
        TextField amountField = new TextField();
        nameField.setPromptText("Ej. Arturo");
        amountField.setPromptText("(sólo caracteres numéricos)");
        amountField.setTextFormatter(new TextFormatter<>(new LongStringConverter(), 0L, integerFilter));

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Cantidad: "), 0, 1);
        grid.add(amountField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if(!nameField.getText().isEmpty() && !amountField.getText().isEmpty()) {
                    Inventario inv = new Inventario("C" + (gestorInventario.getLista().size() + 1), nameField.getText(), Long.parseLong(amountField.getText()));
                    gestorInventario.registrar(inv);
                    tablaInventario.setItems(javafx.collections.FXCollections.observableList(gestorInventario.getLista()));
                }
                else {
                    mostrarAlerta(Alert.AlertType.WARNING, "Datos Incompletos", "Por favor complete todos los campos.");
                }
            } catch (GymException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error Registro", ex.getMessage());
            }
        }
    }

    private static void actualizarInventario(TableView<Inventario> tablaInventario) {
        Inventario selectedInv = tablaInventario.getSelectionModel().getSelectedItem();
        if (selectedInv == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "ALERTA", "Por favor, selecciona un objeto de la lista para editar.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Actualizar Inventario");
        dialog.setHeaderText("Modifique los datos del objeto");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image(VistaInventario.class.getResourceAsStream("/edit.jpg")));

        try {
            if (VistaInventario.class.getResourceAsStream("/edit.jpg") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(VistaInventario.class.getResourceAsStream("/edit.jpg"));
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

        TextField nameField = new TextField(selectedInv.getNombre());
        TextField amountField = new TextField(selectedInv.getCantidad().toString());

        nameField.setPromptText("Nombre");
        amountField.setPromptText("Cantidad");

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Cantidad:"), 0, 1);
        grid.add(amountField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(nameField::requestFocus);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (!nameField.getText().isEmpty() || !amountField.getText().isEmpty()) {
                try {
                    selectedInv.setNombre(nameField.getText());
                    selectedInv.setCantidad(Long.parseLong(amountField.getText()));

                    gestorInventario.actualizar(selectedInv);
                    tablaInventario.refresh();
                } catch (Exception ex) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al Actualizar", ex.getMessage());
                }
            }
            else {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos Incompletos", "Por favor complete al menos un campo para actualizar.");
            }
        }

    }

    private static void eliminarInventario(TableView<Inventario> tablaInventario) {
        Inventario selectedInv = tablaInventario.getSelectionModel().getSelectedItem();

        if (selectedInv == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "ALERTA", "Por favor, selecciona un objeto de la lista para eliminar.");
            return;
        }

        Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION);
        alertConfirm.setTitle("Confirmar Eliminación");
        alertConfirm.setHeaderText("¿Está seguro de eliminar el objeto?");
        alertConfirm.setContentText("Objeto: " + selectedInv.getNombre());

        Stage stage = (Stage) alertConfirm.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(VistaInventario.class.getResourceAsStream("/delete.png")));

        try {
            if (VistaInventario.class.getResourceAsStream("/delete.png") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(VistaInventario.class.getResourceAsStream("/delete.png"));
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(48);
                iv.setFitHeight(48);
                iv.setPreserveRatio(true);
                alertConfirm.getDialogPane().setGraphic(iv);
            }
        } catch (Exception ex) {}

        Optional<ButtonType> result = alertConfirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gestorInventario.eliminar(selectedInv.getId());
                tablaInventario.setItems(javafx.collections.FXCollections.observableList(gestorInventario.getLista()));
            } catch (GymException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al Eliminar", ex.getMessage());
            }
        }
    }
}
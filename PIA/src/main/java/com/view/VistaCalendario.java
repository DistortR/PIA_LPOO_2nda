package com.view;

import com.model.ClaseGrupal;
import com.util.GymException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.main.MainApp.*;

public class VistaCalendario {
    private static TableView<ClaseGrupal> claseGrupalTableView;
    private static GridPane gridVisualCalendario;
    private static Label lblTituloMes;
    private static LocalDate selectedDate = LocalDate.now();
    private static YearMonth mesActualCalendario = YearMonth.now();


    public static BorderPane crearVistaCalendarioDeClases() {
        claseGrupalTableView = new TableView<>();
        BorderPane panel = new BorderPane();
        panel.setPadding(new Insets(10));

        VBox contenedorCalendario = new VBox(10);
        contenedorCalendario.setAlignment(Pos.TOP_CENTER);

        HBox navegacion = new HBox(15);
        navegacion.setAlignment(Pos.CENTER);
        navegacion.setPadding(new Insets(10));

        Button btnAnterior = new Button("<");
        Button btnSiguiente = new Button(">");
        lblTituloMes = new Label();
        lblTituloMes.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        btnAnterior.setOnAction(e -> {
            mesActualCalendario = mesActualCalendario.minusMonths(1);
            showCalendario();
        });

        btnSiguiente.setOnAction(e -> {
            mesActualCalendario = mesActualCalendario.plusMonths(1);
            showCalendario();
        });

        navegacion.getChildren().addAll(btnAnterior, lblTituloMes, btnSiguiente);

        gridVisualCalendario = new GridPane();
        gridVisualCalendario.setHgap(10);
        gridVisualCalendario.setVgap(10);
        gridVisualCalendario.setAlignment(Pos.CENTER);

        contenedorCalendario.getChildren().addAll(navegacion, gridVisualCalendario);

        TableColumn<ClaseGrupal, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDate().toString()));

        TableColumn<ClaseGrupal, String> colHora = new TableColumn<>("Hora");
        colHora.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getHour().toString()));

        TableColumn<ClaseGrupal, String> colDesc = new TableColumn<>("Actividad");
        colDesc.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDescription()));

        claseGrupalTableView.getColumns().addAll(colFecha, colHora, colDesc);
        claseGrupalTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button btnAgregar = new Button("Agregar Clase");
        btnAgregar.setOnAction(e -> {
            createClaseGrupal(claseGrupalTableView);
        });

        Button btnActualizar = new Button("Actualizar Clase");
        btnActualizar.setOnAction(e -> {
            updateClase(claseGrupalTableView);
        });

        Button btnEliminar = new Button("Eliminar Clase");
        btnEliminar.setOnAction(e -> {
            deleteClase(claseGrupalTableView);
        });

        HBox barraHerramientas = new HBox(10, btnAgregar, btnActualizar, btnEliminar);
        barraHerramientas.setAlignment(Pos.CENTER_RIGHT);
        barraHerramientas.setPadding(new Insets(10, 0, 0, 0));

        VBox contenedorDerecho = new VBox(10, new Label("Detalle de Clases"), claseGrupalTableView, barraHerramientas);
        VBox.setVgrow(claseGrupalTableView, Priority.ALWAYS);

        SplitPane divisor = new SplitPane(contenedorCalendario, contenedorDerecho);
        divisor.setDividerPositions(0.65);
        panel.setCenter(divisor);

        showCalendario();
        return panel;
    }

    private static void showCalendario() {
        lblTituloMes.setText(mesActualCalendario.getMonth().name() + " " + mesActualCalendario.getYear());
        gridVisualCalendario.getChildren().clear();

        String[] diasSemana = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        for (int i = 0; i < diasSemana.length; i++) {
            Label lbl = new Label(diasSemana[i]);
            lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
            lbl.setMaxWidth(Double.MAX_VALUE);
            gridVisualCalendario.add(lbl, i, 0);
        }

        LocalDate primerDiaDelMes = mesActualCalendario.atDay(1);
        int diaSemanaInicio = primerDiaDelMes.getDayOfWeek().getValue();
        if (diaSemanaInicio == 7) diaSemanaInicio = 0;

        int diasEnMes = mesActualCalendario.lengthOfMonth();

        try {
            List<ClaseGrupal> todasLasClases = controlCalendario.getLista();

            for (int i = 1; i <= diasEnMes; i++) {
                LocalDate fechaDia = mesActualCalendario.atDay(i);

                List<ClaseGrupal> clasesDelDia = todasLasClases.stream().filter(c -> c.getDate() != null).filter(c -> c.getDate().isEqual(fechaDia)).collect(Collectors.toList());

                Button btnDia = new Button();
                btnDia.setPrefSize(200, 200);
                btnDia.setAlignment(Pos.TOP_LEFT);

                VBox contenidoCelda = new VBox(2);
                Label lblNumero = new Label(String.valueOf(i));
                lblNumero.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: black;");
                contenidoCelda.getChildren().add(lblNumero);

                for (ClaseGrupal c : clasesDelDia) {
                    Label lblClase = new Label("• " + c.getHour() + " " + c.getDescription());
                    lblClase.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
                    lblClase.setMaxWidth(180);
                    lblClase.setWrapText(false);
                    contenidoCelda.getChildren().add(lblClase);
                }

                if (clasesDelDia.size() > 3) {
                    Label lblExtra = new Label("... (+" + (clasesDelDia.size()-3) + ")");
                    lblExtra.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
                    contenidoCelda.getChildren().add(lblExtra);
                }

                btnDia.setGraphic(contenidoCelda);

                if (fechaDia.equals(LocalDate.now())) {
                    btnDia.setStyle("-fx-border-color: #3cec3c; -fx-border-width: 2;");
                }

                btnDia.setOnAction(e -> mostrarClasesEnTabla(fechaDia));

                int col = (diaSemanaInicio + i - 1) % 7;
                int row = (diaSemanaInicio + i - 1) / 7 + 1;
                gridVisualCalendario.add(btnDia, col, row);
            }

        } catch (GymException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No cargaron las clases.");
        }
    }
    private static void createClaseGrupal(TableView<ClaseGrupal> claseGrupalTableView) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Crear Clase");
        dialog.setHeaderText("Ingrese los datos de la clase grupal");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image(VistaCalendario.class.getResourceAsStream("/create.png")));

        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }

        try {
            if (VistaCalendario.class.getResourceAsStream("/create.png") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(VistaCalendario.class.getResourceAsStream("/create.png"));
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(48);
                iv.setFitHeight(48);
                iv.setPreserveRatio(true);
                dialog.getDialogPane().setGraphic(iv);
            }
        } catch (Exception ex) {}

        ButtonType btnGuardarType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtDesc = new TextField();
        txtDesc.setPromptText("Ej. Yoga, CrossFit");
        DatePicker datePicker = new DatePicker(selectedDate);
        TextField txtHora = new TextField();
        txtHora.setPromptText("HH:mm (Ej. 17:30)");

        grid.add(new Label("Actividad:"), 0, 0);
        grid.add(txtDesc, 1, 0);
        grid.add(new Label("Fecha:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Hora (24h):"), 0, 2);
        grid.add(txtHora, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == btnGuardarType) {
            if(!txtDesc.getText().isEmpty() && !txtHora.getText().isEmpty() && datePicker.getValue() != null) {
                try {
                    ClaseGrupal clase = new ClaseGrupal("CL" + (controlCalendario.getLista().size() + 1), txtDesc.getText(), datePicker.getValue(), LocalTime.parse(txtHora.getText()));
                    controlCalendario.registrar(clase);
                    showCalendario();
                    mostrarClasesEnTabla(clase.getDate());
                } catch (GymException ex) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", ex.getMessage());
                } catch (DateTimeParseException ex) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error de Formato", "Formato de hora inválido. Use HH:mm (Ej. 14:30).");
                }
            }
            else {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos Incompletos", "Por favor complete todos los campos.");
            }
        }
    }

    private static void updateClase(TableView<ClaseGrupal> claseGrupalTableView) {
        ClaseGrupal selectedClaseGrupal = claseGrupalTableView.getSelectionModel().getSelectedItem();

        if (selectedClaseGrupal == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "ALERTA", "Por favor, selecciona una clase de la lista para editar.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Clase");
        dialog.setHeaderText("Ingrese los datos para editar la clase grupal");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image(VistaCalendario.class.getResourceAsStream("/edit.jpg")));

        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }

        try {
            if (VistaCalendario.class.getResourceAsStream("/edit.jpg") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(VistaCalendario.class.getResourceAsStream("/edit.jpg"));
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(48);
                iv.setFitHeight(48);
                iv.setPreserveRatio(true);
                dialog.getDialogPane().setGraphic(iv);
            }
        } catch (Exception ex) {}

        ButtonType btnGuardarType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardarType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField txtDesc = new TextField(selectedClaseGrupal.getDescription());
        DatePicker datePicker = new DatePicker(selectedClaseGrupal.getDate());
        TextField txtHora = new TextField(selectedClaseGrupal.getHour().toString());

        grid.add(new Label("Actividad:"), 0, 0);
        grid.add(txtDesc, 1, 0);
        grid.add(new Label("Fecha:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Hora (24h):"), 0, 2);
        grid.add(txtHora, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == btnGuardarType) {
            if(!txtDesc.getText().isEmpty() && !txtHora.getText().isEmpty() && datePicker.getValue() != null) {
                try {
                    ClaseGrupal claseModificada = new ClaseGrupal(selectedClaseGrupal.getId(), txtDesc.getText(), datePicker.getValue(), LocalTime.parse(txtHora.getText()));
                    controlCalendario.actualizar(claseModificada);
                    showCalendario();
                    mostrarClasesEnTabla(claseModificada.getDate());
                } catch (GymException ex) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error al Actualizar", ex.getMessage());
                } catch (DateTimeParseException ex) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error de Formato", "Formato de hora inválido. Use HH:mm (Ej. 14:30).");
                }
            }
            else {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos Incompletos", "Por favor complete todos los campos.");
            }
        }
    }

    private static void deleteClase(TableView<ClaseGrupal> claseGrupalTableView) {
        ClaseGrupal selectedClaseGrupal = claseGrupalTableView.getSelectionModel().getSelectedItem();

        if (selectedClaseGrupal == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "ALERTA", "Por favor, selecciona una clase de la lista para eliminar.");
            return;
        }

        Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION);
        alertConfirm.setTitle("Confirmar Eliminación");
        alertConfirm.setHeaderText("¿Está seguro de eliminar la clase?");
        alertConfirm.setContentText("Clase: " + selectedClaseGrupal.getDescription());

        Stage stage = (Stage) alertConfirm.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(VistaCalendario.class.getResourceAsStream("/delete.png")));

        try {
            if (VistaCalendario.class.getResourceAsStream("/delete.png") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(VistaCalendario.class.getResourceAsStream("/delete.png"));
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
                controlCalendario.eliminar(selectedClaseGrupal.getId());
                showCalendario();
                mostrarClasesEnTabla(selectedClaseGrupal.getDate());
            } catch (GymException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al Eliminar", ex.getMessage());
            }
        }
    }


    private static void mostrarClasesEnTabla(LocalDate fecha) {
        selectedDate = fecha;

        try {
            List<ClaseGrupal> todas = controlCalendario.getLista();
            List<ClaseGrupal> delDia = todas.stream().filter(c -> c.getDate() != null).filter(c -> c.getDate().isEqual(fecha)).collect(Collectors.toList());

            claseGrupalTableView.setItems(javafx.collections.FXCollections.observableList(delDia));
        } catch (GymException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Datos", "No se pudieron cargar las clases para la fecha: " + fecha);
        }
    }

}

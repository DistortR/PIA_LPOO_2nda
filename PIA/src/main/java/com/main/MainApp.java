package com.main;

import com.controller.*;
import com.model.ClaseGrupal;
import com.model.Cliente;
import com.model.Membresia;
import com.model.Membresia.TipoMembresia;
import com.model.UsuarioEmpleado;
import com.util.GymException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Optional;

public class MainApp extends Application {

    private TableView<ClaseGrupal> claseGrupalTableView = new TableView<>();
    private TableView<Cliente> tableView = new TableView<>();
    private GestionClientesIbarra gestorClientes;
    private ControlAccesoIbarra controlAcceso;
    private SistemaMembresias1412 gestorMembresias;
    private ProcesadorPagos4647 procesadorPagos;
    private CalendarioDeClase controlCalendario = new CalendarioDeClase();

    private LocalDate selectedDate = LocalDate.now();
    private YearMonth mesActualCalendario = YearMonth.now();
    private GridPane gridVisualCalendario;
    private Label lblTituloMes;
    private UsuarioEmpleado usuarioLogeado = null;
    private String stylesheet;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            gestorClientes = new GestionClientesIbarra();
            controlAcceso = new ControlAccesoIbarra();
            gestorMembresias = new SistemaMembresias1412();
            procesadorPagos = new ProcesadorPagos4647();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error Fatal", e.getMessage());
            return;
        }

        cargarVistaLogin();
    }

    private void cargarVistaLogin() {
        try {
            if (getClass().getResource("/Estilos.css") != null && stylesheet == null) {
                stylesheet = getClass().getResource("/Estilos.css").toExternalForm();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof com.controller.LoginController) {
                ((com.controller.LoginController) ctrl).init(this, gestorClientes);
            }

            Scene scene = new Scene(root);
            if (stylesheet != null) scene.getStylesheets().add(stylesheet);

            primaryStage.setTitle("GymPOSAI4647");
            if (getClass().getResourceAsStream("/gym.jpg") != null) {
                primaryStage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/gym.jpg")));
            }
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar Login", e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUsuarioLogeado(UsuarioEmpleado usuario) {
        this.usuarioLogeado = usuario;
    }

    public void mostrarVistaPrincipal() {
        if (primaryStage != null) {
            primaryStage.setScene(crearVistaPrincipal(primaryStage));
            primaryStage.centerOnScreen();
        }
    }

    private Scene crearVistaPrincipal(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-root");
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("main-tabs");

        Tab tabClientes = new Tab("Clientes", CRUDVistaClientes());
        Tab tabMembresias = new Tab("Membresías & Pagos", crearVistaMembresias());
        Tab tabAcceso = new Tab("Control de Acceso", crearVistaControlAcceso());
        Tab tabReportes = new Tab("Reportes", crearVistaReportes());
        Tab tabCalendario = new Tab("Calendario de Clases", crearVistaCalendarioDeClases());

        tabPane.getTabs().addAll(tabClientes, tabMembresias, tabAcceso, tabReportes, tabCalendario);
        tabPane.getTabs().forEach(t -> t.setClosable(false));

        Label lblUser = new Label("SESIÓN: " + usuarioLogeado.getNombreCompleto() + " | ROL: " + usuarioLogeado.getRol());
        lblUser.getStyleClass().add("session-label");
        lblUser.setPadding(new Insets(8, 12, 8, 12));

        javafx.scene.image.Image icon = new javafx.scene.image.Image(getClass().getResourceAsStream("/person.png"));
        javafx.scene.image.ImageView iconView = new javafx.scene.image.ImageView(icon);
        iconView.setFitHeight(20);
        iconView.setPreserveRatio(true);
        lblUser.setGraphic(iconView);
        lblUser.setGraphicTextGap(10);


        root.setTop(lblUser);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1100, 800);
        if (stylesheet != null) scene.getStylesheets().add(stylesheet);
        return scene;
    }

    private BorderPane CRUDVistaClientes() {
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
                if (Objects.requireNonNull(mem).esActualizable() && mem.diasRestantes() == 0)
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
        btnActualizar.setOnAction(e -> {
            UpdateClient(tableView);
        });

        Button btnEliminar = new Button("Eliminar Cliente");
        btnEliminar.setOnAction(e -> {
            deleteClient(tableView);
        });

        BorderPane panel = new BorderPane();
        panel.getStyleClass().add("content-pane");
        tableView.getStyleClass().add("table-view");
        panel.setCenter(tableView);

        HBox botones = new HBox(10, btnAgregar, btnActualizar, btnEliminar);
        botones.getStyleClass().add("button-row");
        botones.setPadding(new Insets(12));
        panel.setBottom(botones);
        return panel;
    }

    private void CreateClient(TableView<Cliente> tableView) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Agregar Cliente");
        dialog.setHeaderText("Ingrese los datos");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/create.png")));

        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }

        try {
            if (getClass().getResourceAsStream("/create.png") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(getClass().getResourceAsStream("/create.png"));
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
    
    private void UpdateClient(TableView<Cliente> tableView) {
        Cliente selectedClient = tableView.getSelectionModel().getSelectedItem();

        if (selectedClient == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "ALERTA", "Por favor, selecciona un cliente de la lista para editar.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Actualizar Cliente");
        dialog.setHeaderText("Modifique los datos del cliente");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/edit.jpg")));

        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }

        try {
            if (getClass().getResourceAsStream("/edit.jpg") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(getClass().getResourceAsStream("/edit.jpg"));
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

    private void deleteClient(TableView<Cliente> tableView) {
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
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/delete.png")));

        try {
            if (getClass().getResourceAsStream("/delete.png") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(getClass().getResourceAsStream("/delete.png"));
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

    private VBox crearVistaMembresias() {
        TextField txtClienteId = new TextField();
        txtClienteId.setPromptText("ID Cliente a Cobrar");
        ComboBox<TipoMembresia> cmbTipo = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(TipoMembresia.values()));
        cmbTipo.getSelectionModel().selectFirst();
        Spinner<Integer> spnMeses = new Spinner<>(1, 12, 1);


        Button btnInscribir = new Button("Inscribir Cliente (Pagar)");
        Button btnRenovar = new Button("Renovar Membresía");
        TextArea logArea = new TextArea();
        logArea.setEditable(false);

        btnInscribir.setOnAction(e -> {
            try {
                Cliente cliente = gestorClientes.buscar(txtClienteId.getText()).orElseThrow(() -> new GymException("Cliente no encontrado."));
                TipoMembresia tipo = cmbTipo.getValue();
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

                gestorMembresias.renovarMembresia(cliente, meses, "1234567890123456");
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

    private VBox crearVistaControlAcceso() {
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
                    gestorClientes.actualizar(cliente);
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
                lblResultado.setText("✅ Salida registrada para: " + cliente.getNombreCompleto());
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

    private BorderPane crearVistaReportes() {
        BorderPane panel = new BorderPane();
        Button btnGenerar = new Button("Generar Reporte Estadístico (Background Thread)");
        Label lblEstado = new Label("Estado: Esperando...");
        ProgressBar barra = new ProgressBar(0);
        barra.setVisible(false);

        btnGenerar.setOnAction(e -> {
            GeneradorReportesA tarea = new GeneradorReportesA();

            lblEstado.textProperty().bind(tarea.messageProperty());
            barra.visibleProperty().bind(tarea.runningProperty());
            barra.progressProperty().bind(tarea.progressProperty());

            tarea.setOnSucceeded(event -> {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Reporte Completo", "El reporte ha sido generado y guardado en archivo (Ver Log de Consola).");
            });
            tarea.setOnFailed(event -> {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Reporte", "Fallo la generación: " + tarea.getException().getMessage());
            });

            new Thread(tarea).start();
        });

        panel.setCenter(btnGenerar);
        panel.setBottom(new VBox(10, lblEstado, barra));
        return panel;
    }

    public BorderPane crearVistaCalendarioDeClases() {
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

    private void createClaseGrupal(TableView<ClaseGrupal> claseGrupalTableView) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Crear Clase");
        dialog.setHeaderText("Ingrese los datos de la clase grupal");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/create.png")));

        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }

        try {
            if (getClass().getResourceAsStream("/create.png") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(getClass().getResourceAsStream("/create.png"));
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

    private void updateClase(TableView<ClaseGrupal> claseGrupalTableView) {
        ClaseGrupal selectedClaseGrupal = claseGrupalTableView.getSelectionModel().getSelectedItem();

        if (selectedClaseGrupal == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "ALERTA", "Por favor, selecciona una clase de la lista para editar.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Clase");
        dialog.setHeaderText("Ingrese los datos para editar la clase grupal");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new javafx.scene.image.Image(getClass().getResourceAsStream("/edit.jpg")));

        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
            dialog.getDialogPane().getStyleClass().add("dialog-pane");
        }

        try {
            if (getClass().getResourceAsStream("/edit.jpg") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(getClass().getResourceAsStream("/edit.jpg"));
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

    private void deleteClase(TableView<ClaseGrupal> claseGrupalTableView) {
        ClaseGrupal selectedClaseGrupal = claseGrupalTableView.getSelectionModel().getSelectedItem();

        if (selectedClaseGrupal == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "ALERTA", "Por favor, selecciona una clase de la lista para eliminar.");
            return;
        }

        Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION);
        alertConfirm.setTitle("Confirmar Eliminación");
        alertConfirm.setHeaderText("¿Está seguro de eliminar la clase?");
        alertConfirm.setContentText("Clase: " + selectedClaseGrupal.getDescription());
        if (stylesheet != null) {
            alertConfirm.getDialogPane().getStylesheets().add(stylesheet);
            alertConfirm.getDialogPane().getStyleClass().add("dialog-pane");
        }

        Stage stage = (Stage) alertConfirm.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/delete.png")));

        try {
            if (getClass().getResourceAsStream("/delete.png") != null) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(getClass().getResourceAsStream("/delete.png"));
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
                controlCalendario.eliminar(selectedClaseGrupal.getId());
                showCalendario();
                mostrarClasesEnTabla(selectedClaseGrupal.getDate());
            } catch (GymException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al Eliminar", ex.getMessage());
            }
        }
    }

    private void showCalendario() {
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
                    btnDia.setStyle("-fx-border-color: red; -fx-border-width: 2;");
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

    private void mostrarClasesEnTabla(LocalDate fecha) {
        this.selectedDate = fecha;

        try {
            List<ClaseGrupal> todas = controlCalendario.getLista();
            List<ClaseGrupal> delDia = todas.stream().filter(c -> c.getDate() != null).filter(c -> c.getDate().isEqual(fecha)).collect(Collectors.toList());

            claseGrupalTableView.setItems(javafx.collections.FXCollections.observableList(delDia));
        } catch (GymException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Datos", "No se pudieron cargar las clases para la fecha: " + fecha);
        }
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (stylesheet != null) alert.getDialogPane().getStylesheets().add(stylesheet);

        try {
            Image img = new Image(getClass().getResourceAsStream("/error.png"));
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(img);
        } catch (Exception e) {}
        alert.showAndWait();
    }

    private void actualizarVistaClientes(TableView<Cliente> tableView) {
        if (tableView != null)
        {
            tableView.setItems(javafx.collections.FXCollections.observableList(gestorClientes.getLista()));
            tableView.refresh();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}


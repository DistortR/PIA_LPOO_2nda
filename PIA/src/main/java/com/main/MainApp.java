package com.main;

import com.controller.ControlAccesoIbarra;
import com.controller.GeneradorReportesA;
import com.controller.GestionClientesIbarra;
import com.controller.SistemaMembresias1412;
import com.controller.ProcesadorPagos4647;
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
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

// Nombre del Ejecutable Completo: GymPOS[InicialesApellido][Matricula]
public class MainApp extends Application {

    private TableView<Cliente> tableView = new TableView<>();
    private GestionClientesIbarra gestorClientes;
    private ControlAccesoIbarra controlAcceso;
    private SistemaMembresias1412 gestorMembresias;
    private ProcesadorPagos4647 procesadorPagos;

    // --- Datos de Sesión ---
    private UsuarioEmpleado usuarioLogeado = null;

    @Override
    public void start(Stage primaryStage) {

        try {
            gestorClientes = new GestionClientesIbarra();
            controlAcceso = new ControlAccesoIbarra();
            gestorMembresias = new SistemaMembresias1412();
            procesadorPagos = new ProcesadorPagos4647(); // Inicialización del Procesador
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error Fatal", "Fallo al cargar la base de datos o inicializar controladores: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        primaryStage.setTitle("GymPOSAI4647");
        primaryStage.setScene(crearVistaLogin(primaryStage));
        primaryStage.show();
    }

    private Scene crearVistaLogin(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);
        grid.setPadding(new Insets(25));

        TextField userField = new TextField("jibarra"); // Valores de prueba
        PasswordField passField = new PasswordField();
        passField.setText("admin123");
        Button btnLogin = new Button("Iniciar Sesión");

        grid.addRow(1, new Label("Usuario:"), userField);
        grid.addRow(2, new Label("Contraseña:"), passField);
        grid.add(btnLogin, 1, 4);

        btnLogin.setOnAction(e -> {
            try {
                // USO: Autenticación de UsuarioEmpleado
                usuarioLogeado = gestorClientes.autenticar(userField.getText(), passField.getText());
                primaryStage.setScene(crearVistaPrincipal(primaryStage));
                primaryStage.centerOnScreen();
            } catch (GymException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Fallo en Login", ex.getMessage());
            }
        });
        return new Scene(grid, 400, 300);
    }

    private Scene crearVistaPrincipal(Stage primaryStage) {
        BorderPane root = new BorderPane();
        TabPane tabPane = new TabPane();

        Tab tabClientes = new Tab("Clientes", CRUDVistaClientes());
        Tab tabMembresias = new Tab("Membresías & Pagos", crearVistaMembresias());
        Tab tabAcceso = new Tab("Control de Acceso", crearVistaControlAcceso());
        Tab tabReportes = new Tab("Reportes", crearVistaReportes());

        tabPane.getTabs().addAll(tabClientes, tabMembresias, tabAcceso, tabReportes);
        tabPane.getTabs().forEach(t -> t.setClosable(false));

        Label lblUser = new Label("SESIÓN: " + usuarioLogeado.getNombreCompleto() + " | ROL: " + usuarioLogeado.getRol());
        lblUser.setPadding(new Insets(5, 10, 5, 10));

        root.setTop(lblUser);
        root.setCenter(tabPane);

        return new Scene(root, 1100, 800);
    }

    private BorderPane CRUDVistaClientes() {
        tableView.setItems(javafx.collections.FXCollections.observableList(gestorClientes.getListaClientes()));

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
                    Membresia membresiaCliente = cellData.getValue().getMembresiaActual();
                    try {
                        gestorMembresias.inscribirCliente(cellData.getValue(),
                                membresiaCliente.getTipo(),
                                membresiaCliente.getMeses(),
                                "1234567890123456"); // Tarjeta simulada
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
            
        tableView.getColumns().addAll(idCol, nombreCol, emailCol,  fechaRegistroCol, membresiaCol);

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
        panel.setCenter(tableView);

        HBox botones = new HBox(10, btnAgregar, btnActualizar, btnEliminar);
        botones.setPadding(new Insets(10));
        panel.setBottom(botones);
        return panel;
    }

    private void CreateClient(TableView<Cliente> tableView) {
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
                    Cliente c = new Cliente("C" + (gestorClientes.getListaClientes().size() + 1), nameField.getText(), lastNameField.getText(), emailField.getText());
                    gestorClientes.registrarCliente(c);
                    tableView.setItems(javafx.collections.FXCollections.observableList(gestorClientes.getListaClientes()));
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

                    gestorClientes.actualizarCliente(selectedClient);
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

        Optional<ButtonType> result = alertConfirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gestorClientes.eliminarCliente(selectedClient.getId());
                tableView.setItems(javafx.collections.FXCollections.observableList(gestorClientes.getListaClientes()));
            } catch (GymException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al Eliminar", ex.getMessage());
            }
        }
    }

    private VBox crearVistaMembresias() {
        // Componentes para la simulación
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
                Cliente cliente = gestorClientes.buscarCliente(txtClienteId.getText()).orElseThrow(() -> new GymException("Cliente no encontrado."));
                TipoMembresia tipo = cmbTipo.getValue();
                int meses = spnMeses.getValue();

                gestorMembresias.inscribirCliente(cliente, tipo, meses, "1234567890123456"); // Tarjeta simulada
                logArea.appendText("Inscripción exitosa para " + cliente.getNombreCompleto() + ". Verifique en Clientes.\n");
                gestorClientes.actualizarCliente(cliente);
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
                Cliente cliente = gestorClientes.buscarCliente(txtClienteId.getText()).orElseThrow(() -> new GymException("Cliente no encontrado."));
                int meses = spnMeses.getValue();

                gestorMembresias.renovarMembresia(cliente, meses, "1234567890123456"); // Tarjeta simulada
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
                Cliente cliente = gestorClientes.buscarCliente(txtIdCliente.getText()).orElseThrow(() -> new GymException("Cliente con ID no encontrado."));

                if (controlAcceso.validarEntrada(cliente)) {
                    lblResultado.setText("ACCESO PERMITIDO: Bienvenido(a) " + cliente.getNombreCompleto());
                    lblResultado.setStyle("-fx-font-weight: bold; -fx-text-fill: green; -fx-font-size: 16px;");
                    cliente.agregarPuntos(20);
                    gestorClientes.actualizarCliente(cliente);
                }
            } catch (GymException ex) {
                lblResultado.setText("ACCESO DENEGADO: " + ex.getMessage());
                lblResultado.setStyle("-fx-font-weight: bold; -fx-text-fill: red; -fx-font-size: 16px;");
            } finally {
                // Actualiza la bitácora visible
                logListView.setItems(javafx.collections.FXCollections.observableList(controlAcceso.getHistorialAccesos()));
            }
        });

        btnSalida.setOnAction(e -> {
            try {
                Cliente cliente = gestorClientes.buscarCliente(txtIdCliente.getText()).orElseThrow(() -> new GymException("Cliente con ID no encontrado."));
                controlAcceso.registrarSalida(cliente); // USO: Registro de Salida
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

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void actualizarVistaClientes(TableView<Cliente> tableView) {
        if (tableView != null)
        {
            tableView.setItems(javafx.collections.FXCollections.observableList(gestorClientes.getListaClientes()));
            tableView.refresh();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
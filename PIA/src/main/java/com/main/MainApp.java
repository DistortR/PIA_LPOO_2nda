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
import java.time.LocalDate;
import java.util.Optional;

// Nombre del Ejecutable Completo: GymPOS[InicialesApellido][Matricula]
public class MainApp extends Application {

    // --- Instancias de Controladores (USO DE TODOS LOS MÓDULOS) ---
    private GestionClientesIbarra gestorClientes;
    private ControlAccesoIbarra controlAcceso;
    private SistemaMembresias1412 gestorMembresias;
    private ProcesadorPagos4647 procesadorPagos;

    // --- Datos de Sesión ---
    private UsuarioEmpleado usuarioLogeado = null;

    @Override
    public void start(Stage primaryStage) {

        // Inicialización de Controladores y persistencia
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

    // ----------------------------------------------------------------------
    // VISTA DE LOGIN (USO DE GestionClientesIbarra y UsuarioEmpleado)
    // ----------------------------------------------------------------------
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


    // ----------------------------------------------------------------------
    // VISTA PRINCIPAL (Contenedor de Módulos)
    // ----------------------------------------------------------------------
    private Scene crearVistaPrincipal(Stage primaryStage) {
        BorderPane root = new BorderPane();
        TabPane tabPane = new TabPane();

        Tab tabClientes = new Tab("1. Clientes", crearVistaClientes());
        Tab tabMembresias = new Tab("2. Membresías & Pagos", crearVistaMembresias());
        Tab tabAcceso = new Tab("3. Control de Acceso", crearVistaControlAcceso());
        Tab tabReportes = new Tab("4. Reportes", crearVistaReportes());

        tabPane.getTabs().addAll(tabClientes, tabMembresias, tabAcceso, tabReportes);
        tabPane.getTabs().forEach(t -> t.setClosable(false));

        Label lblUser = new Label("SESIÓN: " + usuarioLogeado.getNombreCompleto() + " | ROL: " + usuarioLogeado.getRol());
        lblUser.setPadding(new Insets(5, 10, 5, 10));

        root.setTop(lblUser);
        root.setCenter(tabPane);

        return new Scene(root, 1100, 800);
    }


    // ----------------------------------------------------------------------
    // MÓDULO DE CLIENTES (USO DE GestionClientesIbarra)
    // ----------------------------------------------------------------------
    private BorderPane crearVistaClientes() {
        TableView<Cliente> tableView = new TableView<>();
        tableView.setItems(javafx.collections.FXCollections.observableList(gestorClientes.getListaClientes()));

        TableColumn<Cliente, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getId()));

        // Configuración de columnas (solo una muestra, se necesita la clase Cliente completa)
        TableColumn<Cliente, String> nombreCol = new TableColumn<>("Nombre Completo");
        nombreCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNombreCompleto()));
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Cliente, String> membresiaCol = new TableColumn<>("Estado Membresía");
        membresiaCol.setCellValueFactory(cellData -> {
            Membresia mem = cellData.getValue().getMembresiaActual();
            return new javafx.beans.property.SimpleStringProperty(mem != null && mem.esValida() ? mem.getTipo().name() + " (Vence: " + mem.getFechaFin() + ")" : "INACTIVA");
        });

        tableView.getColumns().addAll(idCol, nombreCol, membresiaCol);
        Button btnAgregar = new Button("Registrar Cliente");

        btnAgregar.setOnAction(e -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Agregar Cliente");
            dialog.setHeaderText("Ingrese los datos");

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
            dialog.showAndWait();

            Optional<ButtonType> result = dialog.showAndWait();

            if(result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    Cliente c = new Cliente("C" + (gestorClientes.getListaClientes().size() + 1), nameField.getText(), lastNameField.getText(), emailField.getText());
                    gestorClientes.registrarCliente(c);
                    tableView.setItems(javafx.collections.FXCollections.observableList(gestorClientes.getListaClientes()));
                } catch (GymException ex) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error Registro", ex.getMessage());
                }
            }
        });

        BorderPane panel = new BorderPane();
        panel.setCenter(tableView);
        panel.setBottom(new HBox(10, btnAgregar));
        return panel;
    }

    private VBox crearVistaMembresias() {
        // Componentes para la simulación
        TextField txtClienteId = new TextField();
        txtClienteId.setPromptText("ID Cliente a Cobrar");
        ComboBox<TipoMembresia> cmbTipo = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(TipoMembresia.values()));
        cmbTipo.getSelectionModel().selectFirst();
        Spinner<Integer> spnMeses = new Spinner<>(1, 12, 1);

        Button btnInscribir = new Button("1. Inscribir Cliente (Pagar)");
        Button btnRenovar = new Button("2. Renovar Membresía");
        TextArea logArea = new TextArea();
        logArea.setEditable(false);

        btnInscribir.setOnAction(e -> {
            try {
                Cliente cliente = gestorClientes.buscarCliente(txtClienteId.getText()).orElseThrow(() -> new GymException("Cliente no encontrado."));
                TipoMembresia tipo = cmbTipo.getValue();
                int meses = spnMeses.getValue();

                // USO: Inscribir cliente (pago y asignación)
                gestorMembresias.inscribirCliente(cliente, tipo, meses, "1234567890123456"); // Tarjeta simulada
                logArea.appendText("Inscripción exitosa para " + cliente.getNombreCompleto() + ". Verifique en Clientes.\n");

            } catch (GymException ex) {
                logArea.appendText("ERROR: " + ex.getMessage() + "\n");
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Pago/Membresía", ex.getMessage());
            }
        });

        btnRenovar.setOnAction(e -> {
            try {
                Cliente cliente = gestorClientes.buscarCliente(txtClienteId.getText()).orElseThrow(() -> new GymException("Cliente no encontrado."));
                int meses = spnMeses.getValue();

                // USO: Renovar Membresía (pago y extensión de fecha)
                gestorMembresias.renovarMembresia(cliente, meses, "1234567890123456"); // Tarjeta simulada
                logArea.appendText("Renovación exitosa para " + cliente.getNombreCompleto() + ".\n");

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
        grid.addRow(3, btnInscribir, btnRenovar);

        VBox.setVgrow(logArea, Priority.ALWAYS);
        return new VBox(10, new Label("Configuración de Suscripción:"), grid, new Label("Log de Transacciones:"), logArea);
    }

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

                // USO: Validación de Entrada
                if (controlAcceso.validarEntrada(cliente)) {
                    lblResultado.setText("✅ ACCESO PERMITIDO: Bienvenido(a) " + cliente.getNombreCompleto());
                    lblResultado.setStyle("-fx-font-weight: bold; -fx-text-fill: green; -fx-font-size: 16px;");
                }
            } catch (GymException ex) {
                // Manejo de excepciones (Membresía vencida, etc.)
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
            // USO: GeneradorReportesA (Task, requiere GestionClientesIbarra en el constructor)
            GeneradorReportesA tarea = new GeneradorReportesA();

            // Vincular UI con el Hilo
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

    public static void main(String[] args) {
        launch(args);
    }
}
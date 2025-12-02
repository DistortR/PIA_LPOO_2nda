package com.main;

import com.controller.*;
import com.model.Cliente;
import com.model.UsuarioEmpleado;
import com.util.GymException;
import com.view.VistaClientes;
import com.view.VistaInventario;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import static com.view.VistaAcceso.crearVistaControlAcceso;
import static com.view.VistaMembresias.crearVistaMembresias;

public class MainApp extends Application {

    public static GestionClientesIbarra gestorClientes;
    public static ControlAccesoIbarra controlAcceso;
    public static SistemaMembresias1412 gestorMembresias;
    public static GestionInventario gestorInventario;
    private ProcesadorPagos4647 procesadorPagos;

    // --- Datos de Sesión ---
    protected static UsuarioEmpleado usuarioLogeado = null;

    @Override
    public void start(Stage primaryStage) {

        try {
            gestorInventario = new GestionInventario();
            gestorClientes = new GestionClientesIbarra();
            controlAcceso = new ControlAccesoIbarra();
            gestorMembresias = new SistemaMembresias1412();

            procesadorPagos = new ProcesadorPagos4647(); // Inicialización del Procesador
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error Fatal", "Fallo al cargar la base de datos o inicializar controladores: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        primaryStage.setTitle("Gimnasio");
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
                MainApp.usuarioLogeado = gestorClientes.autenticar(userField.getText(), passField.getText());
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

        Tab tabClientes = new Tab("Clientes", VistaClientes.CRUDVistaClientes());
        Tab tabMembresias = new Tab("Membresías & Pagos", crearVistaMembresias());
        Tab tabAcceso = new Tab("Control de Acceso", crearVistaControlAcceso());
        Tab tabReportes = new Tab("Reportes", crearVistaReportes());
        Tab tabInventarios = new Tab("Inventarios", VistaInventario.crearVistaInventario());

        tabPane.getTabs().addAll(tabClientes, tabMembresias, tabAcceso, tabInventarios, tabReportes);
        tabPane.getTabs().forEach(t -> t.setClosable(false));

        Label lblUser = new Label("SESIÓN: " + usuarioLogeado.getNombreCompleto() + " | ROL: " + usuarioLogeado.getRol());
        lblUser.setPadding(new Insets(5, 10, 5, 10));

        root.setTop(lblUser);
        root.setCenter(tabPane);

        return new Scene(root, 1100, 800);
    }

    private BorderPane crearVistaReportes() {
        BorderPane panel = new BorderPane();
        Button btnGenerar = new Button("Generar Reporte Estadístico");
        Label lblEstado = new Label("Estado: Esperando...");
        ProgressBar barra = new ProgressBar(0);
        barra.setVisible(false);

        btnGenerar.setOnAction(e -> {
            GeneradorReportesA tarea = new GeneradorReportesA(gestorClientes.getLista(), gestorInventario.getLista());

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

    public static void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void actualizarVistaClientes(TableView<Cliente> tableView) {
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
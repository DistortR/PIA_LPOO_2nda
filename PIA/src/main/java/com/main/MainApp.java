package com.main;

import com.controller.*;
import com.model.Cliente;
import com.model.UsuarioEmpleado;
import com.view.*;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import static com.view.VistaReportes.crearVistaReportes;

public class MainApp extends Application {

    public static TableView<Cliente> tableView;
    public static GestionClientesIbarra gestorClientes;
    public static ControlAccesoIbarra controlAcceso;
    public static GestionInventario gestorInventario;
    public static SistemaMembresias1412 gestorMembresias;

    public static CalendarioDeClase controlCalendario = null;
    private UsuarioEmpleado usuarioLogeado = null;
    public static String stylesheet;
    private Stage primaryStage;


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        try {
            gestorClientes = new GestionClientesIbarra();
            controlAcceso = new ControlAccesoIbarra();
            gestorMembresias = new SistemaMembresias1412();
            gestorInventario = new GestionInventario();
            controlCalendario = new CalendarioDeClase();
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
            if (ctrl instanceof LoginController) {
                ((LoginController) ctrl).init(this, gestorClientes);
            }

            Scene scene = new Scene(root);
            if (stylesheet != null) scene.getStylesheets().add(stylesheet);

            primaryStage.setTitle("GymPOSAI4647");
            if (getClass().getResourceAsStream("/gym.jpg") != null) {
                primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/gym.jpg")));
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

        Tab tabClientes = new Tab("Clientes", ClienteView.CRUDVistaClientes());
        Tab tabMembresias = new Tab("Membresías & Pagos", MembresiasView.crearVistaMembresias());
        Tab tabAcceso = new Tab("Control de Acceso", VistaControlAcceso.crearVistaControlAcceso());
        Tab tabInventario = new Tab("Inventario", VistaInventario.crearVistaInventario());
        Tab tabCalendario = new Tab("Calendario", VistaCalendario.crearVistaCalendarioDeClases());
        Tab tabReportes = new Tab("Reportes", crearVistaReportes());

        tabPane.getTabs().addAll(tabClientes, tabMembresias, tabAcceso, tabInventario, tabCalendario, tabReportes);
        tabPane.getTabs().forEach(t -> t.setClosable(false));

        Label lblUser = new Label("SESIÓN: " + usuarioLogeado.getNombreCompleto() + " | ROL: " + usuarioLogeado.getRol());
        lblUser.getStyleClass().add("session-label");
        lblUser.setPadding(new Insets(8, 12, 8, 12));

        Image icon = new Image(getClass().getResourceAsStream("/person.png"));
        ImageView iconView = new ImageView(icon);
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

    public static void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if (stylesheet != null) alert.getDialogPane().getStylesheets().add(stylesheet);
        try {
            Image img = new Image(MainApp.class.getResourceAsStream("/error.png"));
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(img);
        } catch (Exception e) {}
        alert.showAndWait();
    }

    public static void showDiscountPopup(Cliente cliente, TextArea logArea) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar descuento");
        alert.setHeaderText("Aplicar descuento con puntos");
        alert.setContentText(String.format("Cliente: %s\nPuntos disponibles: %d\n\n¿Usar 100 puntos para obtener 30%% de descuento\n en la próxima compra/renovación?",
                cliente.getNombreCompleto(), cliente.getPuntosFidelidad()));

        ButtonType btnSi = new ButtonType("Sí", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(btnSi, btnNo);

        alert.showAndWait().ifPresent(respuesta -> {
            if (respuesta == btnSi) {
                try {
                    cliente.setPuntosFidelidad(cliente.getPuntosFidelidad() - 100);
                    actualizarVistaClientes(tableView);
                    gestorClientes.actualizar(cliente);
                    logArea.appendText("Descuento del 30% activado para " + cliente.getNombreCompleto() + "\n");
                } catch (Exception ex) {
                    logArea.appendText("ERROR al aplicar descuento: " + ex.getMessage() + "\n");
                }
            }
        });

    }

    public static void actualizarVistaClientes(TableView<Cliente> tableView) {
        if (tableView != null)
        {
            tableView.setItems(FXCollections.observableList(gestorClientes.getLista()));
            tableView.refresh();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
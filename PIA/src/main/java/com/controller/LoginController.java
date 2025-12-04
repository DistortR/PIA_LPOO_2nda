package com.controller;

import com.main.MainApp;
import com.model.UsuarioEmpleado;
import com.util.GymException;
import com.controller.GestionClientesIbarra;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField userField;
    @FXML private PasswordField passField;
    @FXML private Button btnLogin;

    private GestionClientesIbarra gestorClientes;
    private MainApp mainApp;   // referencia al MainApp

    public void init(MainApp mainApp, GestionClientesIbarra gestorClientes) {
        this.mainApp = mainApp;
        this.gestorClientes = gestorClientes;
    }

    @FXML
    private void initialize() {
        // puedes inicializar cosas aquí si quieres
    }

    @FXML
    private void onLoginClick() {

        try {
            UsuarioEmpleado user = gestorClientes.autenticar(
                    userField.getText(),
                    passField.getText()
            );

            mainApp.setUsuarioLogeado(user); // guardamos el usuario
            mainApp.mostrarVistaPrincipal(); // cambiamos de escena

        } catch (GymException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
            alert.showAndWait();
        }
    }
}

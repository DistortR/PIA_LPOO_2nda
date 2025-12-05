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

    private GestionClientesIbarra gestorClientes;
    private MainApp mainApp;

    public void init(MainApp mainApp, GestionClientesIbarra gestorClientes) {
        this.mainApp = mainApp;
        this.gestorClientes = gestorClientes;
    }

    @FXML
    private void initialize() {
    }

    @FXML
    private void onLoginClick() {

        try {
            UsuarioEmpleado user = gestorClientes.autenticar(
                    userField.getText(),
                    passField.getText()
            );

            mainApp.setUsuarioLogeado(user);
            mainApp.mostrarVistaPrincipal();

        } catch (GymException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage());
            alert.showAndWait();
        }
    }
}

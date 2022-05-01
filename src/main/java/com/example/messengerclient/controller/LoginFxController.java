package com.example.messengerclient.controller;

import com.example.messengerclient.ClientApplicationInitializer;
import com.example.messengerclient.service.StompService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("singleton")
@FxmlView("../templates/login-scene.fxml")
public class LoginFxController {

    private final StompService stompService;
    private static ClientApplicationInitializer clientApplicationInitializer;
    @FXML
    private Label label1;
    @FXML
    private Button loginButton;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private VBox loginButtonVBox;

    @Autowired
    public LoginFxController(StompService stompService) {
        this.stompService = stompService;
    }

    public static void setClientApplicationInitializer(ClientApplicationInitializer clientApplicationInitializer) {
        LoginFxController.clientApplicationInitializer = clientApplicationInitializer;
    }


    @FXML
    protected void login() {

        stompService.connect(loginField.getText(), passwordField.getText());
    }

    public void successLogin() {
        label1.setText("Success Login!");
        label1.setVisible(true);
        clientApplicationInitializer.afterLogin();
    }


    public void failedLogin() {
        label1.setText("Incorrect Login Or Password");
        label1.setVisible(true);
    }


    public void failedConnection() {
        label1.setText("No Internet Connection Or Server Is Down Or Session Connection Is Fail Or Wrong Ip");
        label1.setVisible(true);
    }

}

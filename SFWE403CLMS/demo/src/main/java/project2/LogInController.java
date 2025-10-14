package project2;

import java.io.IOException;
import java.util.List;

import javafx.scene.layout.VBox;
import project2.LoginManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class LogInController {


    @FXML
    private VBox loginPanel, registerPanel;

    @FXML
    private TextField usernameText, regUsername;

    @FXML
    private PasswordField passwordText, regPassword, regPasswordConfirmation;

    @FXML
    private Label loginWarnings, registerWarnings;

    @FXML
    public void initialize() {
        usernameText.setOnKeyReleased(e -> loginWarnings.setVisible(false));
        passwordText.setOnKeyReleased(e -> loginWarnings.setVisible(false));

        regUsername.setOnKeyReleased(e -> registerWarnings.setVisible(false));
        regPassword.setOnKeyReleased(e -> registerWarnings.setVisible(false));
        regPasswordConfirmation.setOnKeyReleased(e -> registerWarnings.setVisible(false));
    }

    @FXML
    void doLogin(ActionEvent event) throws IOException {
        String usernameInput = usernameText.getText();
        String passwordInput = passwordText.getText();
        System.out.println("user input recognized" + usernameInput);

        if (LoginManager.INSTANCE.verifyLogin(usernameInput, passwordInput)) {
            App.setRoot("secondary");
        } else {
            System.out.println("wrong username");
            loginWarnings.setVisible(true);
            //set the label "wrong username" visbible
        }

    }

    @FXML
    void showRegisterPanel(ActionEvent event) {
        loginPanel.setVisible(false);
        registerPanel.setVisible(true);
    }

    @FXML
    void showLoginPanel(ActionEvent event) {
        registerPanel.setVisible(false);
        loginPanel.setVisible(true);
    }

    @FXML
    void doRegister(ActionEvent event) {
        String user = regUsername.getText();
        String pass = regPassword.getText();
        String confirmation = regPasswordConfirmation.getText();
        List<String> errors = LoginManager.INSTANCE.passwordChecks(user, pass, confirmation);

        if (errors.isEmpty()) {
            LoginManager.INSTANCE.register(user, pass);
            showLoginPanel(null); // return to login after successful registration
        } else {
            String warnings = "";
            for (String error: errors) {
                warnings += error + "\n";
            }

            registerWarnings.setText(warnings);
            registerWarnings.setVisible(true);
        }
    }
}



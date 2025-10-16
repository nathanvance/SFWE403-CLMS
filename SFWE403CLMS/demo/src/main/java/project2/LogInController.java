package project2;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class LogInController {


    @FXML
    private VBox loginPanel, registerPanel;

    @FXML
    private TextField usernameText, regUsername;

    @FXML
    private PasswordField passwordText, regPassword, regPasswordConfirmation;

    @FXML
    private Label loginWarnings, registerWarnings;

    private List<TextInputControl> loginFields;
    private List<TextInputControl> registerFields;

    @FXML
    public void initialize() {
        loginFields = Arrays.asList(usernameText, passwordText);
        registerFields = Arrays.asList(regUsername, regPassword, regPasswordConfirmation);

        for (TextInputControl field : loginFields) {
            enterHandler(field);
        }
        for (TextInputControl field : registerFields) {
            enterHandler(field);
        }
    }

    @FXML
    void doLogin(ActionEvent event) throws IOException {
        loginWarnings.setVisible(false);
        String usernameInput = usernameText.getText();
        String passwordInput = passwordText.getText();
        System.out.println("user input recognized" + usernameInput);

        String result = LoginManager.INSTANCE.verifyLogin(usernameInput, passwordInput);

        switch (result) {
            case "true":
                App.setRoot("secondary");
                break;
            case "false":
                System.out.println("wrong username");
                loginWarnings.setText("Username or password incorrect.");
                loginWarnings.setVisible(true);
                break;
            case "locked":
                loginWarnings.setText("Account locked. Please contact an administrator.");
                loginWarnings.setVisible(true);
                break;
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
        registerWarnings.setVisible(false);
        String user = regUsername.getText();
        String pass = regPassword.getText();
        String confirmation = regPasswordConfirmation.getText();
        List<String> errors = LoginManager.INSTANCE.passwordChecks(user, pass, confirmation);

        if (errors.isEmpty()) {
            LoginManager.INSTANCE.register(user, pass);
            showLoginPanel(null); // return to login after successful registration
        } else {
            String warnings = "";
            for (String error : errors) {
                warnings += error + "\n";
            }

            registerWarnings.setText(warnings);
            registerWarnings.setVisible(true);
        }
    }

    private void enterHandler(TextInputControl field) {
        loginFields = Arrays.asList(usernameText, passwordText);
        registerFields = Arrays.asList(regUsername, regPassword, regPasswordConfirmation);

        field.setOnKeyReleased(e -> {
            loginWarnings.setVisible(false);
            registerWarnings.setVisible(false);

            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (loginFields.contains(field))
                    try {
                        doLogin(null);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                if (registerFields.contains(field)) {
                    doRegister(null);
                }
            }
        });
    }
}



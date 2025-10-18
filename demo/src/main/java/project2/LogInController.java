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
        String usernameInput = usernameText.getText().trim();
        String passwordInput = passwordText.getText();

        LoginManager.LoginResult result = LoginManager.INSTANCE.verifyLogin(usernameInput, passwordInput);

        switch (result.getStatus()) {
            case SUCCESS -> {
                String role = result.getRole() != null ? result.getRole() : "Patient";
                SessionManager.INSTANCE.startSession(usernameInput.trim(), role);

                switch (role.toLowerCase()) {
                    case "patient" -> App.setRoot("patientDashboard");
                    case "pharmacist" -> App.setRoot("pharmacistDashboard");
                    case "admin" -> App.setRoot("adminDashboard");
                    default -> App.setRoot("login");
                }
            }
            case INVALID -> {
                int rem = LoginManager.INSTANCE.remainingAttempts(usernameInput.trim());
                String plural = (rem == 1 ? "" : "s");
                loginWarnings.setText(
                        "Username or password incorrect. " +
                                rem + " attempt" + plural + " remaining."
                );
                loginWarnings.setVisible(true);
            }
            case LOCKED -> {
                loginWarnings.setText("Account locked. Please contact an administrator.");
                loginWarnings.setVisible(true);
            }
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
        String user = regUsername.getText().trim();
        String pass = regPassword.getText().trim();
        String confirmation = regPasswordConfirmation.getText();

        List<String> errors = LoginManager.INSTANCE.passwordChecks(user, pass, confirmation);

        if (errors.isEmpty()) {
            boolean success = LoginManager.INSTANCE.register(user, pass);
            if (success) {
                showLoginPanel(null); // go back to login on success
            } else {
                registerWarnings.setText("Username already exists!");
                registerWarnings.setVisible(true);
            }
        } else {
            registerWarnings.setText(String.join("\n", errors));
            registerWarnings.setVisible(true);
        }
    }

    private void enterHandler(TextInputControl field) {
        field.setOnKeyReleased(e -> {
            loginWarnings.setVisible(false);
            registerWarnings.setVisible(false);

            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                if (loginFields.contains(field)) {
                    try {
                        doLogin(null);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else if (registerFields.contains(field)) {
                    doRegister(null);
                }
            }
        });
    }
}

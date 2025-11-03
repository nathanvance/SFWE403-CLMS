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

        // (Optional but nice UX) quick pre-checks
        if (!LoginManager.INSTANCE.usernameExists(usernameInput)) {
            loginWarnings.setText("Account does not exist.");
            loginWarnings.setVisible(true);
            return;
        }
        // If you added this helper in LoginManager (as you mentioned)
        if (LoginManager.isDeleted(usernameInput)) {
            loginWarnings.setText("This account was deleted. Contact an administrator to restore it.");
            loginWarnings.setVisible(true);
            return;
        }

        LoginManager.LoginResult result = LoginManager.INSTANCE.verifyLogin(usernameInput, passwordInput);

        switch (result.getStatus()) {
            case SUCCESS -> {
                String role = result.getRole() != null ? result.getRole() : "Patient";
                SessionManager.INSTANCE.startSession(usernameInput, role);
                switch (role.toLowerCase()) {
                    case "admin"       -> App.setRoot("adminDashboard");
                    case "pharmacist"  -> App.setRoot("pharmacistDashboard");
                    default            -> App.setRoot("patientDashboard");
                }
            }

            case RESET_REQUIRED -> {
                SessionManager.INSTANCE.startSession(usernameInput.trim(),
                        result.getRole() != null ? result.getRole() : "Patient");
                // optional: tell the user what's happening
                loginWarnings.setText("Password reset required. Please set a new password.");
                loginWarnings.setVisible(true);
                App.setRoot("changePassword");
            }

            case LOCKED -> {
                // Locked accounts can be recovered by an admin (your Admin dashboard "Recover" button)
                loginWarnings.setText("This account is locked. Ask an admin to recover/unlock it.");
                loginWarnings.setVisible(true);
            }

            case INVALID -> {
                int rem = LoginManager.INSTANCE.remainingAttempts(usernameInput);
                String plural = (rem == 1 ? "" : "s");
                loginWarnings.setText(
                        "Username or password incorrect. " + rem + " attempt" + plural + " remaining."
                );
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

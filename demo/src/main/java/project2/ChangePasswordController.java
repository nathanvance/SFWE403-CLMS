package project2;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;

public class ChangePasswordController {

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         changePasswordMessage;

    @FXML private Label titleLabel;
    @FXML private Label instructionsLabel;
    @FXML private Button backButton;

    private String username;
    private boolean resetRequired;

    @FXML
    public void initialize() {
        username = SessionManager.INSTANCE.getCurrentUsername();
        changePasswordMessage.setVisible(false);
        if (username == null || username.isBlank()) {
            showError("Not signed in.");
            return;
        }

        resetRequired = LoginManager.isResetRequired(username);

        if (resetRequired) {
            // Hide "current password" and switch copy to recovery wording
            oldPasswordField.setManaged(false);
            oldPasswordField.setVisible(false);

            if (titleLabel != null)        titleLabel.setText("Create a New Password");
            if (instructionsLabel != null) instructionsLabel.setText("Please enter and confirm a new password to continue.");
            if (backButton != null)        backButton.setDisable(true);   // optional, prevents backing out mid-reset

            showOk("Please set a new password to continue.");
            newPasswordField.requestFocus();
        } else {
            // (optional) ensure default wording in normal mode
            if (titleLabel != null)        titleLabel.setText("Change Your Password");
            if (instructionsLabel != null) instructionsLabel.setText("Please enter your current and new password below.");
            if (backButton != null)        backButton.setDisable(false);
            oldPasswordField.setManaged(true);
            oldPasswordField.setVisible(true);
        }
    }

    @FXML
    private void doChangePassword() {
        if (username == null || username.isBlank()) return;

        String oldPwd  = oldPasswordField.getText();
        String newPwd  = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (resetRequired) {
            // Admin-triggered reset flow (no current password, must honor the RESET REQUIRED flag)
            java.util.List<String> errs =
                    LoginManager.changePasswordAfterAdminReset(username, newPwd, confirm);

            if (errs.isEmpty()) {
                showOk("Password updated. Please log in again.");
                // end session and go back to login so user re-authenticates
                try {
                    SessionManager.INSTANCE.clear();
                    App.setRoot("login");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                showError(String.join("\n", errs));
            }
            return;
        }

        // Normal, self-change while logged in
        LoginManager.ChangePwdResult res =
                LoginManager.INSTANCE.changePasswordSelf(username, oldPwd, newPwd, confirm);

        switch (res) {
            case OK -> {
                showOk("Password updated.");
                oldPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
            }
            case BAD_CURRENT -> showError("Current password is incorrect.");
            case MISMATCH    -> showError("New password and confirmation do not match.");
            case WEAK        -> showError("New password must include upper, lower, digit, and special.");
            case SAME_AS_OLD -> showError("New password must be different from the current password.");
            default          -> showError("Could not update password.");
        }
    }

    private void clearFields() {
        if (!resetRequired) oldPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private void goBackToMainSilently() {
        try { goBackToMain(); } catch (IOException ignored) {}
    }

    @FXML
    private void goBackToMain() throws IOException {
        // send them back to their dashboard based on role
        String role = LoginManager.INSTANCE.getRole(username);
        if ("Admin".equalsIgnoreCase(role)) {
            App.setRoot("adminDashboard");
        } else if ("Pharmacist".equalsIgnoreCase(role)) {
            App.setRoot("pharmacistDashboard");
        } else {
            App.setRoot("patientDashboard");
        }
    }

    private void showError(String msg) {
        changePasswordMessage.setStyle("-fx-text-fill: #b21818;");
        changePasswordMessage.setText(msg);
        changePasswordMessage.setVisible(true);
    }

    private void showOk(String msg) {
        changePasswordMessage.setStyle("-fx-text-fill: #1d6f42;");
        changePasswordMessage.setText(msg);
        changePasswordMessage.setVisible(true);
    }
}
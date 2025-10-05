package project2;

import java.io.IOException;

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
    private Label wrongUsernameWarning;

    @FXML
    private Button LogInButton;

    @FXML
    private Pane PaneLabel;

    @FXML
    private PasswordField passwordText;

    @FXML
    private AnchorPane topLabel;

    @FXML
    private TextField usernameText;

    @FXML
    void doSomething(ActionEvent event) throws IOException  {
        String usernameInput = usernameText.getText().trim();
        String passwordInput = passwordText.getText();
        
        // If already locked, tell user and exit early
        if (BackEnd.isLocked(usernameInput)) {
            long secs = BackEnd.lockoutSecondsRemaining(usernameInput);
            wrongUsernameWarning.setText("Account locked. Try again in " + secs + " seconds.");
            wrongUsernameWarning.setVisible(true);
            return;
        }
    
        // Attempt login using lockout logic in BackEnd
        boolean ok = BackEnd.checkLoginWithLockout(usernameInput, passwordInput);
        if (ok) {
            wrongUsernameWarning.setVisible(false);
            App.setRoot("secondary");
            return;
        }

        // Failed login either locked now or remaining attempts
        if (BackEnd.isLocked(usernameInput)) {
            long secs = BackEnd.lockoutSecondsRemaining(usernameInput);
            wrongUsernameWarning.setText("Account locked.Try again in " + secs + " seconds.");
            wrongUsernameWarning.setVisible(true);
        }
        else {
            int attempts = BackEnd.remainingAttempts(usernameInput);
            wrongUsernameWarning.setText("Wrong username or password. " + attempts + "attempt" + (attempts == 1 ? "" : "s") + " remaining.");
            wrongUsernameWarning.setVisible(true);
        }
    }
    
}

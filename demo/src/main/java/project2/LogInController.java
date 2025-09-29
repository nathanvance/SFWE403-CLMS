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
        String usernameInput = usernameText.getText();
        String passwordInput = passwordText.getText();
        System.out.print("user input recognized" + usernameInput);

        if (BackEnd.checkLogin(usernameInput, passwordInput)) {
        App.setRoot("secondary");
        }
        else {
            System.out.print("wrong username");
            wrongUsernameWarning.setVisible(true);
            //set the label "wrong username" visbible
        }

        


    }
    


}

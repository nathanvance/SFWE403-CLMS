package project2;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;

public class SecondaryController {

    @FXML
    private MenuItem createAccountButtonMenu;

    @FXML
    private MenuItem createNewPrescripButtonMenu;

    @FXML
    private MenuItem searchUserPrescripsButtonMenu;

    @FXML
    private MenuItem viewUserListButtonMenu;

    @FXML
    private TextField searchUserPrescrips;

    @FXML
    private Button searchUserPrescripsButton;

    @FXML
    private Button logOutButton;

    @FXML
    private Button createAccountButton;

    @FXML
    private TextField enterAge;

    @FXML
    private TextField enterName;

    @FXML
    private TextField enterPassword;

    @FXML
    private TextField enterUsername;

    @FXML
    private AnchorPane setCreateNewAccountVisible;

    @FXML
    private AnchorPane createNewPrescriptionVisible;

    @FXML
    private AnchorPane findUserPrescriptionVisible;

    @FXML
    private Button createPrescriptionButton;

    @FXML
    private TextField prescriptionUsername;

    @FXML
    private TextField prescripPharmacistName;

    @FXML
    private TextField prescripName;

    @FXML
    private TextField prescripDrugName;

    @FXML
    private TextField prescripDrugQuantity;

    @FXML
    private Label usernameWarning;


    @FXML
    void createAccountSetVisible(ActionEvent event) {
        setCreateNewAccountVisible.setVisible(true);
        createNewPrescriptionVisible.setVisible(false);
        findUserPrescriptionVisible.setVisible(false);
    }

    @FXML
    void createNewPrescripSetVisible(ActionEvent event) {
        setCreateNewAccountVisible.setVisible(false);
        createNewPrescriptionVisible.setVisible(true);
        findUserPrescriptionVisible.setVisible(false);

    }

    @FXML
    void searchUserPrescripsSetVisible(ActionEvent event) {
        findUserPrescriptionVisible.setVisible(true);
        setCreateNewAccountVisible.setVisible(false);
        createNewPrescriptionVisible.setVisible(false);
    }

    @FXML
    void switchToViewReport(ActionEvent event) throws IOException  {
        App.setRoot("viewReport");
    }

    @FXML
    void doCreateAccount(ActionEvent event) throws IOException  {

        String username = enterUsername.getText();
        String password = enterPassword.getText();
        String name = enterName.getText();
        String age = enterAge.getText();


        if (BackEnd.checkLogin(username, password)) {
                usernameWarning.setVisible(true);
        }
        else {
            BackEnd.createNewAccount(username, password, name, age);
            App.setRoot("viewReport");
        }

    }

    @FXML
    void returnToLogin(ActionEvent event) throws IOException  {
        App.setRoot("sample1");
    }

    private static String userPrescripsSearch;

    @FXML
    void findUserPrescrips(ActionEvent event) throws IOException  {
        this.userPrescripsSearch = searchUserPrescrips.getText();
        App.setRoot("viewPrescriptions");
        
        
    }


    @FXML
    void doCreatePrescription(ActionEvent event) throws IOException  {
        String inputUsername = prescriptionUsername.getText();
        String inputPharmacist = prescripPharmacistName.getText();
        String inputPrescripName = prescripName.getText();
        String inputDrugName = prescripDrugName.getText();
        String inputDrugQuantity = prescripDrugQuantity.getText();

        BackEnd.addPrescriptionToCSV(inputUsername, inputPharmacist, inputPrescripName, inputDrugName, inputDrugQuantity);
        this.userPrescripsSearch = inputUsername;
        App.setRoot("viewPrescriptions");
        
    }

    public static String getUserPrescripsSearch() {
        return userPrescripsSearch;
    }
}
package project2;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;


public class viewPrescriptionsController {

    @FXML
    private Button goBack;

    @FXML
    private TableColumn<Prescription, String> drugName;

    @FXML
    private TableColumn<Prescription, String> drugQuantity;

    @FXML
    private TableColumn<Prescription, String> pharmacistName;

    @FXML
    private TableColumn<Prescription, String> prescriptionName;
    @FXML

    private TableView<Prescription> viewPrescriptions;

    @FXML
    private AnchorPane viewPrescriptionsPane;

    @FXML
    void goBackToSecondary(ActionEvent event) throws IOException  {
        App.setRoot("secondary");
    }

     private ObservableList<Prescription> prescriptions = FXCollections.observableArrayList();

    
     public void initialize() {

        pharmacistName.setCellValueFactory(new PropertyValueFactory<Prescription, String>("pharmacistName"));
        prescriptionName.setCellValueFactory(new PropertyValueFactory<Prescription, String>("prescriptionName"));
        drugName.setCellValueFactory(new PropertyValueFactory<Prescription, String>("drugName"));
        drugQuantity.setCellValueFactory(new PropertyValueFactory<Prescription, String>("drugQuantity"));

        
        String findThisUsername = SecondaryController.getUserPrescripsSearch();
        prescriptions = BackEnd.loadUserPrescriptions(findThisUsername);
        viewPrescriptions.setItems(prescriptions);

     }


}


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


public class viewReportController {

    @FXML
    private TableColumn<User, String> prescriptionAge;

    @FXML
    private TableColumn<User, String> prescriptionName;

    @FXML
    private TableColumn<User, String> prescriptionPassword;

    @FXML
    private TableColumn<User, String> prescriptionUsername;

    @FXML
    private TableView<User> prescriptionView;

    @FXML
    private AnchorPane viewReportPane;

    @FXML
    private Button viewReportGoBackButton;

    @FXML
    void switchBackToSecondary(ActionEvent event) throws IOException {
        App.setRoot("login");
    }

    private ObservableList<User> users = FXCollections.observableArrayList();


    public void initialize() {


        prescriptionPassword.setCellValueFactory(new PropertyValueFactory<User, String>("password"));
        prescriptionUsername.setCellValueFactory(new PropertyValueFactory<User, String>("username"));
        prescriptionName.setCellValueFactory(new PropertyValueFactory<User, String>("name"));
        prescriptionAge.setCellValueFactory(new PropertyValueFactory<User, String>("age"));


        //call a loadData function here to load the list users
        users = FXCollections.observableArrayList();

        users = BackEnd.loadUserData();
        prescriptionView.setItems(users);

    }


    // prescriptionUsername.setCellValueFactory(new PropertyValueFactory<User, String>("username"));
    // prescriptionPassword.setCellValueFactory(new PropertyValueFactory<User, String>("password"));

    //prescriptionView.setItems(users);


}


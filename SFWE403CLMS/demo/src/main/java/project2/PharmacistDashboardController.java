package project2;

import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PharmacistDashboardController {

    @FXML
    private Label pharmacistNameLabel;

    @FXML
    private Label pharmacistIdLabel;

    @FXML
    private TableView<PharmacistAssignment> assignmentsTable;

    @FXML
    private TableColumn<PharmacistAssignment, String> patientColumn;

    @FXML
    private TableColumn<PharmacistAssignment, String> medicationColumn;

    @FXML
    private TableColumn<PharmacistAssignment, Number> quantityColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button logoutButton;

    private final ObservableList<PharmacistAssignment> assignments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));
        medicationColumn.setCellValueFactory(new PropertyValueFactory<>("medication"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        assignmentsTable.setItems(assignments);
        assignmentsTable.setPlaceholder(new Label("No patients assigned yet."));

        String pharmacistId = SessionManager.INSTANCE.getUsername();
        if (pharmacistId == null || pharmacistId.isBlank()) {
            Platform.runLater(() -> {
                try {
                    App.setRoot("sample1");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        pharmacistIdLabel.setText(pharmacistId);
        String displayName = PatientDataService.getPharmacistDisplayName(pharmacistId)
                .orElse("Pharmacist");
        pharmacistNameLabel.setText(displayName);

        loadAssignments(pharmacistId);
    }

    @FXML
    void refreshAssignments(ActionEvent event) {
        String pharmacistId = SessionManager.INSTANCE.getUsername();
        if (pharmacistId == null || pharmacistId.isBlank()) {
            return;
        }
        loadAssignments(pharmacistId);
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        SessionManager.INSTANCE.clear();
        App.setRoot("sample1");
    }

    private void loadAssignments(String pharmacistId) {
        PatientDataService.invalidateCache();
        List<PharmacistAssignment> items = PatientDataService.getAssignmentsForPharmacist(pharmacistId);
        assignments.setAll(items);
    }
}


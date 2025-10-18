package project2;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PatientDashboardController {

    @FXML
    private Label pharmacistLabel;

    @FXML
    private Label patientNameLabel;

    @FXML
    private TableView<PrescriptionDetails> prescriptionsTable;

    @FXML
    private TableColumn<PrescriptionDetails, String> prescriptionNameColumn;

    @FXML
    private TableColumn<PrescriptionDetails, Number> prescriptionQuantityColumn;

    @FXML
    private TableView<HealthRecord> healthRecordsTable;

    @FXML
    private TableColumn<HealthRecord, String> recordDateColumn;

    @FXML
    private TableColumn<HealthRecord, String> recordSummaryColumn;

    @FXML
    private TableColumn<HealthRecord, String> recordNotesColumn;

    @FXML
    private ComboBox<String> insuranceComboBox;

    @FXML
    private Label insuranceStatusLabel;

    @FXML
    private Button saveInsuranceButton;

    @FXML
    private Button logoutButton;

    private ObservableList<PrescriptionDetails> prescriptionItems = FXCollections.observableArrayList();
    private ObservableList<HealthRecord> healthRecordItems = FXCollections.observableArrayList();
    private String currentUsername;

    @FXML
    public void initialize() {
        setupTables();
        currentUsername = SessionManager.INSTANCE.getUsername();

        if (currentUsername == null || currentUsername.isBlank()) {
            Platform.runLater(() -> {
                try {
                    App.setRoot("sample1");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        loadPatientInformation();
    }

    private void setupTables() {
        prescriptionNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        prescriptionQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        prescriptionsTable.setItems(prescriptionItems);

        recordDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        recordSummaryColumn.setCellValueFactory(new PropertyValueFactory<>("summary"));
        recordNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        healthRecordsTable.setItems(healthRecordItems);
    }

    private void loadPatientInformation() {
        Optional<PatientProfile> maybeProfile = PatientDataService.findPatient(currentUsername);

        if (maybeProfile.isEmpty()) {
            insuranceStatusLabel.setText("We could not find your patient record.");
            insuranceStatusLabel.setVisible(true);
            return;
        }

        PatientProfile profile = maybeProfile.get();

        String displayName = profile.getUsername() != null ? profile.getUsername() : currentUsername;
        patientNameLabel.setText(displayName);

        String pharmacist = profile.getPharmacist();
        pharmacistLabel.setText(pharmacist != null && !pharmacist.isBlank() ? pharmacist : "Not assigned");

        prescriptionItems.setAll(profile.getPrescriptions());
        healthRecordItems.setAll(profile.getHealthRecords());

        List<String> insuranceOptions = PatientDataService.getInsuranceOptions();
        String selectedInsurance = profile.getInsurance();
        if (selectedInsurance != null && !selectedInsurance.isBlank() && !insuranceOptions.contains(selectedInsurance)) {
            insuranceOptions.add(selectedInsurance);
        }

        insuranceComboBox.setItems(FXCollections.observableArrayList(insuranceOptions));
        if (selectedInsurance != null && !selectedInsurance.isBlank()) {
            insuranceComboBox.getSelectionModel().select(selectedInsurance);
        } else {
            insuranceComboBox.getSelectionModel().clearSelection();
        }
        insuranceStatusLabel.setVisible(false);
    }

    @FXML
    void saveInsurance(ActionEvent event) {
        insuranceStatusLabel.setVisible(false);
        insuranceStatusLabel.setStyle("-fx-text-fill: #d03535;");
        String selectedInsurance = insuranceComboBox.getSelectionModel().getSelectedItem();

        if (selectedInsurance == null || selectedInsurance.isBlank()) {
            insuranceStatusLabel.setText("Please choose an insurance option before saving.");
            insuranceStatusLabel.setVisible(true);
            return;
        }

        try {
            boolean updated = PatientDataService.updateInsurance(currentUsername, selectedInsurance);
            if (updated) {
                insuranceStatusLabel.setStyle("-fx-text-fill: #1d6f42;");
                insuranceStatusLabel.setText("Insurance updated to " + selectedInsurance + ".");
            } else {
                insuranceStatusLabel.setText("Unable to update insurance. Try again later.");
            }
        } catch (IOException e) {
            insuranceStatusLabel.setText("Failed to save insurance changes.");
            e.printStackTrace();
        }

        insuranceStatusLabel.setVisible(true);
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        SessionManager.INSTANCE.clear();
        App.setRoot("sample1");
    }
}

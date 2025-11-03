package project2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class PatientDashboardController {

    @FXML
    private Label pharmacistLabel;

    @FXML
    private Label patientNameLabel;

    @FXML
    private TableView<Prescription> prescriptionsTable;

    @FXML
    private TableColumn<Prescription, String> prescriptionNameColumn;

    @FXML
    private TableColumn<Prescription, Number> prescriptionQuantityColumn;

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
    private Button changePasswordButton;

    @FXML
    private Button saveInsuranceButton;

    @FXML
    private Button logoutButton;

    private ObservableList<Prescription> prescriptionItems = FXCollections.observableArrayList();
    private ObservableList<HealthRecord> healthRecordItems = FXCollections.observableArrayList();
    private String currentUsername;

    private Patient currentPatient;

    @FXML
    public void initialize() {
        setupTables();
        currentUsername = SessionManager.INSTANCE.getCurrentUsername();

        if (currentUsername == null || currentUsername.isBlank()) {
            Platform.runLater(() -> {
                try {
                    App.setRoot("login");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            return;
        }

        loadPatientInformation();
    }

    private void setupTables() {
        // Prescription table setup
        prescriptionNameColumn.setCellValueFactory(new PropertyValueFactory<>("prescriptionName"));
        prescriptionQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("drugQuantity"));
        prescriptionsTable.setItems(prescriptionItems);

        // Health records table setup
        recordDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        recordSummaryColumn.setCellValueFactory(new PropertyValueFactory<>("summary"));
        recordNotesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        healthRecordsTable.setItems(healthRecordItems);
    }

    private void loadPatientInformation() {
        UserKT user = UserManager.INSTANCE.getUser(currentUsername);

        if (!(user instanceof Patient)) {
            insuranceStatusLabel.setText("We could not find your patient record.");
            insuranceStatusLabel.setVisible(true);
            return;
        }

        currentPatient = (Patient) user;

        patientNameLabel.setText(currentPatient.getUsername());

        String pharmacist = currentPatient.getPharmacistName();
        pharmacistLabel.setText(
                pharmacist != null && !pharmacist.isBlank() ? pharmacist : "Not assigned"
        );

        prescriptionItems.setAll(currentPatient.getMedications());

        if (currentPatient.getHealthRecords() != null && !currentPatient.getHealthRecords().isEmpty()) {
            healthRecordItems.setAll(currentPatient.getHealthRecords());
        } else {
            healthRecordItems.clear();
        }

        List<String> insuranceOptions = new ArrayList<>(Patient.Companion.loadInsuranceOptions());
        if (insuranceOptions.isEmpty()) {
            insuranceOptions.add("Blue Cross");
            insuranceOptions.add("Aetna");
            insuranceOptions.add("UnitedHealth");
            insuranceOptions.add("No Insurance");
        }

        insuranceComboBox.setItems(FXCollections.observableArrayList(insuranceOptions));

        if (currentPatient.getInsurance() != null && !currentPatient.getInsurance().isBlank()) {
            insuranceComboBox.getSelectionModel().select(currentPatient.getInsurance());
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
            currentPatient.setInsurance(selectedInsurance);
            currentPatient.updatePatientInJson();

            insuranceStatusLabel.setStyle("-fx-text-fill: #1d6f42;");
            insuranceStatusLabel.setText("Insurance updated to " + selectedInsurance + ".");
        } catch (Exception e) {
            insuranceStatusLabel.setText("Failed to save insurance changes.");
            e.printStackTrace();
        }

        insuranceStatusLabel.setVisible(true);
    }

    @FXML
    void goChangePassword(ActionEvent event) throws IOException {
        App.setRoot("changePassword");
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        SessionManager.INSTANCE.clear();
        App.setRoot("login");
    }

    @FXML
    private void onDeleteMyAccount() throws IOException {
        String currentUser = SessionManager.INSTANCE.getCurrentUsername();
        if (currentUser == null || currentUser.isBlank()) {
            new Alert(Alert.AlertType.INFORMATION, "No active session.", ButtonType.OK).showAndWait();
            return;
        }

        // 1) Password prompt
        PasswordField pf = new PasswordField();
        pf.setPromptText("Password");
        VBox box = new VBox(8, new Label("Re-enter your password to delete this account:"), pf);

        Alert pwdDlg = new Alert(Alert.AlertType.CONFIRMATION);
        pwdDlg.setTitle("Confirm Deletion");
        pwdDlg.getDialogPane().setContent(box);
        pwdDlg.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        var r1 = pwdDlg.showAndWait();
        if (r1.isEmpty() || r1.get() != ButtonType.OK) return;
        String pwd = pf.getText();

        // 2) Are you sure?
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This will permanently delete your account.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Are you sure?");
        confirm.getButtonTypes().setAll(new ButtonType("Delete"), ButtonType.CANCEL);

        var r2 = confirm.showAndWait();
        if (r2.isEmpty() || r2.get().getButtonData().isCancelButton()) return;

        // 3) Call LoginManager
        boolean ok = LoginManager.deleteSelf(currentUser, pwd); // static call

        new Alert(Alert.AlertType.INFORMATION,
                ok ? "Account deleted." : "Delete failed. Check your password and try again.",
                ButtonType.OK).showAndWait();

        if (ok) {
            SessionManager.INSTANCE.clear();
            App.setRoot("login");
        }
    }
}

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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

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
    private Button changePasswordButton;

    @FXML
    private Button refreshButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Button viewInventoryButton;

    private final ObservableList<PharmacistAssignment> assignments = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));
        medicationColumn.setCellValueFactory(new PropertyValueFactory<>("medication"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        assignmentsTable.setItems(assignments);
        assignmentsTable.setPlaceholder(new Label("No patients assigned yet."));

        String pharmacistId = SessionManager.INSTANCE.getCurrentUsername();
        if (pharmacistId == null || pharmacistId.isBlank()) {
            Platform.runLater(() -> {
                try {
                    App.setRoot("login");
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
        String pharmacistId = SessionManager.INSTANCE.getCurrentUsername();
        if (pharmacistId == null || pharmacistId.isBlank()) {
            return;
        }
        loadAssignments(pharmacistId);
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
    void viewInventory(ActionEvent event) throws IOException {
        App.setRoot("inventoryDashboard");
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



    private void loadAssignments(String pharmacistId) {
        PatientDataService.invalidateCache();
        List<PharmacistAssignment> items = PatientDataService.getAssignmentsForPharmacist(pharmacistId);
        assignments.setAll(items);
    }
}


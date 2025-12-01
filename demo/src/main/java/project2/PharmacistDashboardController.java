package project2;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

public class PharmacistDashboardController {

    // ===================== TOP INFO ======================
    @FXML private Label pharmacistNameLabel;
    @FXML private Label pharmacistIdLabel;

    // ===================== ASSIGNMENTS TABLE =============
    @FXML private TableView<PharmacistAssignment> assignmentsTable;
    @FXML private TableColumn<PharmacistAssignment, String> patientColumn;
    @FXML private TableColumn<PharmacistAssignment, String> medicationColumn;
    @FXML private TableColumn<PharmacistAssignment, Number> quantityColumn;
    private final ObservableList<PharmacistAssignment> assignments = FXCollections.observableArrayList();

    // ===================== MY REQUESTS TABLE =============
    @FXML private TableView<InventoryRequestDisplay> myRequestsTable;
    @FXML private TableColumn<InventoryRequestDisplay, String> itemNameColumn;
    @FXML private TableColumn<InventoryRequestDisplay, Integer> quantityColumnRequest;
    @FXML private TableColumn<InventoryRequestDisplay, String> reasonColumn;
    @FXML private TableColumn<InventoryRequestDisplay, String> statusColumn;
    @FXML private TableColumn<InventoryRequestDisplay, String> dateColumn;
    @FXML private TableColumn<InventoryRequestDisplay, String> notesColumn;
    private final ObservableList<InventoryRequestDisplay> myRequests = FXCollections.observableArrayList();

    // ===================== REQUEST FORM ==================
    @FXML private ComboBox<String> requestItemName;
    @FXML private Spinner<Integer> requestQuantity;
    @FXML private TextArea requestReason;
    @FXML private Button submitRequestButton;

    // ===================== FOOTER BUTTONS =================
    @FXML private Button changePasswordButton;
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;
    @FXML private Button btnDeleteMyAccount;

    private String currentPharmacist;

    @FXML
    public void initialize() {

        // ---------- authentication ----------
        currentPharmacist = SessionManager.INSTANCE.getCurrentUsername();
        if (currentPharmacist == null || currentPharmacist.isBlank()) {
            Platform.runLater(() -> {
                try { App.setRoot("login"); }
                catch (IOException e) { e.printStackTrace(); }
            });
            return;
        }

        pharmacistIdLabel.setText(currentPharmacist);
        pharmacistNameLabel.setText(
                PatientDataService.getPharmacistDisplayName(currentPharmacist)
                        .orElse("Pharmacist")
        );

        // ---------- assignments table ----------
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientUsername"));
        medicationColumn.setCellValueFactory(new PropertyValueFactory<>("medication"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        assignmentsTable.setItems(assignments);
        assignmentsTable.setPlaceholder(new Label("No patients assigned."));
        loadAssignments(currentPharmacist);

        // ---------- new: request form ----------
        setupRequestForm();

        // ---------- new: requests table ----------
        setupRequestsTable();
        loadMyRequests();
    }

    // ============================================================
    // REQUEST FORM SETUP
    // ============================================================
    private void setupRequestForm() {
        requestQuantity.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 1)
        );
        requestQuantity.setEditable(true);

        var items = InventoryLogger.INSTANCE.getAllItems();
        ObservableList<String> names = FXCollections.observableArrayList();
        for (InventoryObject obj : items) names.add(obj.getName());

        requestItemName.setItems(names);
        requestItemName.setEditable(true);
    }

    // ============================================================
    // REQUEST TABLE SETUP
    // ============================================================
    private void setupRequestsTable() {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumnRequest.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateString"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("adminNotes"));

        myRequestsTable.setItems(myRequests);
        myRequestsTable.setPlaceholder(new Label("No requests submitted."));
    }

    // ============================================================
    // SUBMIT INVENTORY REQUEST
    // ============================================================
    @FXML
    private void submitRequest(ActionEvent event) {

        String item = requestItemName.getValue();
        String reason = requestReason.getText();
        Integer qty = requestQuantity.getValue();

        if (item == null || item.isBlank()) {
            showError("Please select or enter an item name.");
            return;
        }
        if (reason == null || reason.isBlank()) {
            showError("Please enter a reason.");
            return;
        }
        if (qty == null || qty <= 0) {
            showError("Quantity must be greater than zero.");
            return;
        }

        try {
            InventoryRequest req = InventoryRequestService.INSTANCE.createRequest(
                    currentPharmacist, item, qty, reason
            );

            showInfo("Request submitted!");

            requestItemName.setValue(null);
            requestReason.clear();
            requestQuantity.getValueFactory().setValue(1);

            loadMyRequests();

        } catch (Exception e) {
            showError("Failed to submit request: " + e.getMessage());
        }
    }

    // ============================================================
    // LOAD REQUESTS FOR THIS PHARMACIST
    // ============================================================
    private void loadMyRequests() {
        List<InventoryRequest> list =
                InventoryRequestService.INSTANCE.getRequestsByPharmacist(currentPharmacist);

        myRequests.clear();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (InventoryRequest req : list) {
            myRequests.add(new InventoryRequestDisplay(
                    req.getItemName(),
                    req.getRequestedQuantity(),
                    req.getReason(),
                    req.getStatus().name(),
                    fmt.format(req.getRequestDate()),
                    req.getAdminNotes() == null ? "" : req.getAdminNotes()
            ));
        }
    }

    // ============================================================
    // ASSIGNMENTS REFRESH
    // ============================================================
    @FXML
    void refreshAssignments(ActionEvent event) {
        loadAssignments(currentPharmacist);
        loadMyRequests();
    }

    private void loadAssignments(String pharmacistId) {
        PatientDataService.invalidateCache();
        assignments.setAll(
                PatientDataService.getAssignmentsForPharmacist(pharmacistId)
        );
    }

    // ============================================================
    // FOOTER NAVIGATION
    // ============================================================
    @FXML
    void goChangePassword(ActionEvent e) throws IOException {
        App.setRoot("changePassword");
    }

    @FXML
    void logout(ActionEvent e) throws IOException {
        SessionManager.INSTANCE.clear();
        App.setRoot("login");
    }

    // ============================================================
    // DELETE ACCOUNT
    // ============================================================
    @FXML
    private void onDeleteMyAccount() throws IOException {
        String currentUser = SessionManager.INSTANCE.getCurrentUsername();
        if (currentUser == null || currentUser.isBlank()) {
            showError("No active session.");
            return;
        }

        PasswordField pf = new PasswordField();
        pf.setPromptText("Password");
        VBox box = new VBox(8,
                new Label("Re-enter your password:"), pf);

        Alert pwdDlg = new Alert(Alert.AlertType.CONFIRMATION);
        pwdDlg.setTitle("Confirm Deletion");
        pwdDlg.getDialogPane().setContent(box);
        pwdDlg.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        if (!pwdDlg.showAndWait().orElse(ButtonType.CANCEL).equals(ButtonType.OK)) return;

        String pwd = pf.getText();

        if (!new Alert(Alert.AlertType.CONFIRMATION,
                "This will permanently delete your account.",
                new ButtonType("Delete"), ButtonType.CANCEL)
                .showAndWait().orElse(ButtonType.CANCEL)
                .getButtonData().isCancelButton()) {

            boolean ok = LoginManager.deleteSelf(currentUser, pwd);

            showInfo(ok ? "Account deleted." : "Delete failed.");

            if (ok) {
                SessionManager.INSTANCE.clear();
                App.setRoot("login");
            }
        }
    }

    // ============================================================
    // ALERT HELPERS
    // ============================================================
    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    // ============================================================
    // DISPLAY CLASS
    // ============================================================
    public static class InventoryRequestDisplay {
        private final String itemName;
        private final Integer quantity;
        private final String reason;
        private final String status;
        private final String dateString;
        private final String adminNotes;

        public InventoryRequestDisplay(
                String itemName, Integer quantity, String reason,
                String status, String dateString, String adminNotes) {

            this.itemName = itemName;
            this.quantity = quantity;
            this.reason = reason;
            this.status = status;
            this.dateString = dateString;
            this.adminNotes = adminNotes;
        }

        public String getItemName() { return itemName; }
        public Integer getQuantity() { return quantity; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }
        public String getDateString() { return dateString; }
        public String getAdminNotes() { return adminNotes; }
    }
}

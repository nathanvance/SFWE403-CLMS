package project2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InventoryDashboardController {

    // ================= STOCK VISUALIZATION UI =================

    @FXML private ComboBox<String> itemSelector;
    @FXML private LineChart<Number, Number> stockChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private Label currentStockLabel;
    @FXML private Label minStockLabel;
    @FXML private Label stockStatusLabel;

    // ================= INVENTORY REQUEST REVIEW UI =================

    @FXML private TableView<InventoryRequestRow> pendingRequestsTable;
    @FXML private TableColumn<InventoryRequestRow, String> pharmacistColumn;
    @FXML private TableColumn<InventoryRequestRow, String> itemColumn;
    @FXML private TableColumn<InventoryRequestRow, Integer> quantityColumn;
    @FXML private TableColumn<InventoryRequestRow, String> reasonColumn;
    @FXML private TableColumn<InventoryRequestRow, String> dateColumn;
    @FXML private TableColumn<InventoryRequestRow, Void> actionsColumn;

    @FXML private TableView<InventoryRequestRow> allRequestsTable;
    @FXML private TableColumn<InventoryRequestRow, String> allPharmacistColumn;
    @FXML private TableColumn<InventoryRequestRow, String> allItemColumn;
    @FXML private TableColumn<InventoryRequestRow, Integer> allQuantityColumn;
    @FXML private TableColumn<InventoryRequestRow, String> allStatusColumn;
    @FXML private TableColumn<InventoryRequestRow, String> allDateColumn;
    @FXML private TableColumn<InventoryRequestRow, String> reviewedByColumn;
    @FXML private TableColumn<InventoryRequestRow, String> adminNotesColumn;

    @FXML private Button backButton;
    @FXML private Button viewInventoryButton;
    @FXML private Button refreshButton;

    private final ObservableList<InventoryRequestRow> pendingRequests = FXCollections.observableArrayList();
    private final ObservableList<InventoryRequestRow> allRequests = FXCollections.observableArrayList();

    private String currentAdminUsername;

    // ==========================================================
    // INITIALIZE CONTROLLER
    // ==========================================================
    @FXML
    public void initialize() {

        currentAdminUsername = SessionManager.INSTANCE.getCurrentUsername();
        if (currentAdminUsername == null || currentAdminUsername.isBlank()) {
            showError("No active session. Please log in.");
            return;
        }

        setupStockChart();
        setupItemSelector();

        setupPendingRequestsTable();
        setupAllRequestsTable();

        loadAllRequestData();
    }

    // ==========================================================
    // STOCK VISUALIZATION SETUP
    // ==========================================================

    private void setupItemSelector() {
        List<InventoryObject> items = InventoryLogger.INSTANCE.getAllItems();
        ObservableList<String> itemNames = FXCollections.observableArrayList();
        items.forEach(i -> itemNames.add(i.getName()));

        itemSelector.setItems(itemNames);
        itemSelector.setPromptText("Select an item to view");

        itemSelector.setOnAction(e -> {
            String name = itemSelector.getValue();
            if (name != null && !name.isBlank()) {
                updateStockDisplay(name);
            }
        });

        if (!itemNames.isEmpty()) {
            itemSelector.setValue(itemNames.get(0));
            updateStockDisplay(itemNames.get(0));
        }
    }

    private void setupStockChart() {
        xAxis.setLabel("Days Ago");
        yAxis.setLabel("Stock Quantity");
        stockChart.setTitle("Stock Level History");
        stockChart.setCreateSymbols(true);
        stockChart.setLegendVisible(false);
    }

    private void updateStockDisplay(String itemName) {
        InventoryObject item = InventoryLogger.INSTANCE.findObjectElseNull(itemName);
        if (item == null) return;

        currentStockLabel.setText(String.valueOf(item.getQuantity()));
        minStockLabel.setText(String.valueOf(item.getMin()));

        if (item.isLowStock()) {
            stockStatusLabel.setText("⚠ LOW STOCK");
            stockStatusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            stockStatusLabel.setText("✓ Adequate Stock");
            stockStatusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: green; -fx-font-weight: bold;");
        }

        updateChart(item);
    }

    private void updateChart(InventoryObject item) {
        stockChart.getData().clear();

        Map<Date, Integer> history = item.getChangesHistory();
        XYChart.Series<Number, Number> series = new XYChart.Series<>();

        Date now = new Date();

        List<Map.Entry<Date, Integer>> entries = new ArrayList<>(history.entrySet());
        entries.sort(Map.Entry.comparingByKey());

        for (var entry : entries) {
            long diff = now.getTime() - entry.getKey().getTime();
            long daysAgo = diff / (1000 * 60 * 60 * 24);
            series.getData().add(new XYChart.Data<>(-daysAgo, entry.getValue()));
        }

        series.getData().add(new XYChart.Data<>(0, item.getQuantity()));
        stockChart.getData().add(series);
    }

    // ==========================================================
    // REQUEST REVIEW SETUP
    // ==========================================================

    private void setupPendingRequestsTable() {
        pharmacistColumn.setCellValueFactory(new PropertyValueFactory<>("pharmacist"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateString"));

        setupActionsColumn();
        pendingRequestsTable.setItems(pendingRequests);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {

            private final Button approveBtn = new Button("✓ Approve");
            private final Button denyBtn = new Button("✗ Deny");
            private final HBox hbox = new HBox(8, approveBtn, denyBtn);

            {
                approveBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                denyBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                approveBtn.setOnAction(e -> {
                    InventoryRequestRow row = getTableView().getItems().get(getIndex());
                    approveRequest(row);
                });

                denyBtn.setOnAction(e -> {
                    InventoryRequestRow row = getTableView().getItems().get(getIndex());
                    denyRequest(row);
                });
            }

            @Override
            protected void updateItem(Void ignore, boolean empty) {
                super.updateItem(ignore, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void setupAllRequestsTable() {
        allPharmacistColumn.setCellValueFactory(new PropertyValueFactory<>("pharmacist"));
        allItemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        allQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        allStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        allDateColumn.setCellValueFactory(new PropertyValueFactory<>("dateString"));
        reviewedByColumn.setCellValueFactory(new PropertyValueFactory<>("reviewedBy"));
        adminNotesColumn.setCellValueFactory(new PropertyValueFactory<>("adminNotes"));

        allRequestsTable.setItems(allRequests);
    }

    // ==========================================================
    // LOAD REQUESTS
    // ==========================================================

    private void loadAllRequestData() {
        pendingRequests.clear();
        allRequests.clear();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (InventoryRequest req : InventoryRequestService.INSTANCE.getPendingRequests()) {
            pendingRequests.add(new InventoryRequestRow(
                    req.getId(),
                    req.getPharmacistUsername(),
                    req.getItemName(),
                    req.getRequestedQuantity(),
                    req.getReason(),
                    req.getStatus().name(),
                    df.format(req.getRequestDate()),
                    req.getReviewedBy(),
                    req.getAdminNotes()
            ));
        }

        for (InventoryRequest req : InventoryRequestService.INSTANCE.getAllRequests()) {
            allRequests.add(new InventoryRequestRow(
                    req.getId(),
                    req.getPharmacistUsername(),
                    req.getItemName(),
                    req.getRequestedQuantity(),
                    req.getReason(),
                    req.getStatus().name(),
                    df.format(req.getRequestDate()),
                    req.getReviewedBy(),
                    req.getAdminNotes()
            ));
        }
    }

    // ==========================================================
    // APPROVE / DENY LOGIC
    // ==========================================================

    private void approveRequest(InventoryRequestRow row) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Approve Request");
        dialog.setHeaderText("Approve request for " + row.getItemName());
        dialog.setContentText("Admin Notes (optional):");

        dialog.showAndWait().ifPresent(notes -> {

            boolean ok = InventoryRequestService.INSTANCE.approveRequest(
                    row.getRequestId(),
                    currentAdminUsername,
                    notes
            );

            if (ok) {
                InventoryObject item = InventoryLogger.INSTANCE.findObjectElseNull(row.getItemName());
                if (item != null) {
                    InventoryLogger.INSTANCE.updateItem(
                            row.getItemName(),
                            new QuantityChange.DecreaseQuantity(row.getQuantity())
                    );
                }
                showInfo("Request approved.");
            } else {
                showError("Failed to approve request.");
            }

            loadAllRequestData();
        });
    }

    private void denyRequest(InventoryRequestRow row) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deny Request");
        dialog.setHeaderText("Deny request for " + row.getItemName());
        dialog.setContentText("Reason for denial:");

        dialog.showAndWait().ifPresent(notes -> {

            if (notes.isBlank()) {
                showError("Reason required.");
                return;
            }

            boolean ok = InventoryRequestService.INSTANCE.denyRequest(
                    row.getRequestId(),
                    currentAdminUsername,
                    notes
            );

            if (ok) {
                showInfo("Request denied.");
                loadAllRequestData();
            } else {
                showError("Failed to deny request.");
            }
        });
    }

    // ==========================================================
    // BUTTON ACTIONS
    // ==========================================================

    @FXML
    void refreshData(ActionEvent event) {
        InventoryLogger.INSTANCE.refresh();
        InventoryRequestService.INSTANCE.refresh();

        setupItemSelector();
        loadAllRequestData();

        showInfo("Data refreshed!");
    }

    @FXML
    void goBack(ActionEvent event) throws IOException {
        if (SessionManager.INSTANCE.getCurrentRole() == null) App.setRoot("login");
        if (SessionManager.INSTANCE.getCurrentRole().equalsIgnoreCase("admin")) {
            App.setRoot("adminDashboard");
        } else {
            App.setRoot("login");
        }
    }

    // ==========================================================
    // UTILITY METHODS
    // ==========================================================

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }

    // ==========================================================
// FOOTER NAVIGATION
// ==========================================================

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
    void goGoBack(ActionEvent even) throws IOException {
        if (!Objects.equals(SessionManager.INSTANCE.getCurrentRole(), "Admin")) {
            App.setRoot("login");
        } else {
            App.setRoot("adminDashboard");
        }
    }

    @FXML
    private void onDeleteMyAccount() throws IOException {
        String currentUser = SessionManager.INSTANCE.getCurrentUsername();
        if (currentUser == null || currentUser.isBlank()) {
            showError("No active session.");
            return;
        }

        // --- password prompt ---
        PasswordField pf = new PasswordField();
        pf.setPromptText("Password");
        VBox box = new VBox(8,
                new Label("Re-enter your password to delete this account:"),
                pf
        );

        Alert pwdDlg = new Alert(Alert.AlertType.CONFIRMATION);
        pwdDlg.setTitle("Confirm Deletion");
        pwdDlg.getDialogPane().setContent(box);
        pwdDlg.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        var r1 = pwdDlg.showAndWait();
        if (r1.isEmpty() || r1.get() != ButtonType.OK) return;

        String pwd = pf.getText();

        // --- final confirmation ---
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "This will permanently delete your account.",
                new ButtonType("Delete"),
                ButtonType.CANCEL
        );

        var r2 = confirm.showAndWait();
        if (r2.isEmpty() || r2.get().getButtonData().isCancelButton()) return;

        boolean ok = LoginManager.deleteSelf(currentUser, pwd);

        if (ok) {
            showInfo("Account deleted.");
            SessionManager.INSTANCE.clear();
            App.setRoot("login");
        } else {
            showError("Delete failed. Check password and try again.");
        }
    }


    // ==========================================================
    // ROW MODEL
    // ==========================================================

    public static class InventoryRequestRow {
        private final String requestId;
        private final String pharmacist;
        private final String itemName;
        private final Integer quantity;
        private final String reason;
        private final String status;
        private final String dateString;
        private final String reviewedBy;
        private final String adminNotes;

        public InventoryRequestRow(String requestId, String pharmacist, String itemName,
                                   Integer quantity, String reason, String status,
                                   String dateString, String reviewedBy, String adminNotes) {

            this.requestId = requestId;
            this.pharmacist = pharmacist;
            this.itemName = itemName;
            this.quantity = quantity;
            this.reason = reason;
            this.status = status;
            this.dateString = dateString;
            this.reviewedBy = reviewedBy == null ? "" : reviewedBy;
            this.adminNotes = adminNotes == null ? "" : adminNotes;
        }

        public String getRequestId() { return requestId; }
        public String getPharmacist() { return pharmacist; }
        public String getItemName() { return itemName; }
        public Integer getQuantity() { return quantity; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }
        public String getDateString() { return dateString; }
        public String getReviewedBy() { return reviewedBy; }
        public String getAdminNotes() { return adminNotes; }
    }
}

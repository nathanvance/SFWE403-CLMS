package project2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public class AdminInventoryRequestsController {

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
    @FXML private Button refreshButton;
    @FXML private Button viewInventoryButton;

    private final ObservableList<InventoryRequestRow> pendingRequests = FXCollections.observableArrayList();
    private final ObservableList<InventoryRequestRow> allRequests = FXCollections.observableArrayList();

    private String currentAdminUsername;

    @FXML
    public void initialize() {
        currentAdminUsername = SessionManager.INSTANCE.getCurrentUsername();
        if (currentAdminUsername == null || currentAdminUsername.isBlank()) {
            showError("No active session. Please log in.");
            return;
        }

        setupPendingRequestsTable();
        setupAllRequestsTable();
        loadRequests();
    }

    private void setupPendingRequestsTable() {
        pharmacistColumn.setCellValueFactory(new PropertyValueFactory<>("pharmacist"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateString"));

        setupActionsColumn();

        pendingRequestsTable.setItems(pendingRequests);
        pendingRequestsTable.setPlaceholder(new Label("No pending requests."));
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button approveButton = new Button("✓ Approve");
            private final Button denyButton = new Button("✗ Deny");
            private final HBox hbox = new HBox(8, approveButton, denyButton);

            {
                approveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                denyButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

                approveButton.setOnAction(e -> {
                    InventoryRequestRow row = getTableView().getItems().get(getIndex());
                    if (row != null) {
                        handleApprove(row);
                    }
                });

                denyButton.setOnAction(e -> {
                    InventoryRequestRow row = getTableView().getItems().get(getIndex());
                    if (row != null) {
                        handleDeny(row);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(hbox);
                }
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
        allRequestsTable.setPlaceholder(new Label("No requests found."));
    }

    private void handleApprove(InventoryRequestRow row) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Approve Request");
        dialog.setHeaderText("Approve inventory request for " + row.getItemName());
        dialog.setContentText("Notes (optional):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String notes = result.get();
            boolean success = InventoryRequestService.INSTANCE.approveRequest(
                row.getRequestId(),
                currentAdminUsername,
                notes
            );

            if (success) {
                // Update inventory - decrease quantity when approved
                InventoryObject item = InventoryLogger.INSTANCE.findObjectElseNull(row.getItemName());
                if (item != null) {
                    InventoryLogger.INSTANCE.updateItem(
                        row.getItemName(),
                        new QuantityChange.DecreaseQuantity(row.getQuantity())
                    );
                    showInfo("Request approved and inventory updated!");
                } else {
                    showInfo("Request approved, but item not found in inventory.");
                }
                loadRequests();
            } else {
                showError("Failed to approve request.");
            }
        }
    }

    private void handleDeny(InventoryRequestRow row) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deny Request");
        dialog.setHeaderText("Deny inventory request for " + row.getItemName());
        dialog.setContentText("Reason for denial (required):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isBlank()) {
            String notes = result.get();
            boolean success = InventoryRequestService.INSTANCE.denyRequest(
                row.getRequestId(),
                currentAdminUsername,
                notes
            );

            if (success) {
                showInfo("Request denied.");
                loadRequests();
            } else {
                showError("Failed to deny request.");
            }
        } else {
            showError("Denial reason is required.");
        }
    }

    @FXML
    void refreshData(ActionEvent event) {
        InventoryRequestService.INSTANCE.refresh();
        loadRequests();
        showInfo("Data refreshed!");
    }

    @FXML
    void goBack(ActionEvent event) throws IOException {
        App.setRoot("adminDashboard");
    }

    @FXML
    void viewInventory(ActionEvent event) throws IOException {
        App.setRoot("inventoryDashboard");
    }

    private void loadRequests() {
        // Load pending requests
        List<InventoryRequest> pending = InventoryRequestService.INSTANCE.getPendingRequests();
        pendingRequests.clear();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (InventoryRequest req : pending) {
            pendingRequests.add(new InventoryRequestRow(
                req.getId(),
                req.getPharmacistUsername(),
                req.getItemName(),
                req.getRequestedQuantity(),
                req.getReason(),
                req.getStatus().name(),
                dateFormat.format(req.getRequestDate()),
                req.getReviewedBy() != null ? req.getReviewedBy() : "",
                req.getAdminNotes() != null ? req.getAdminNotes() : ""
            ));
        }

        // Load all requests
        List<InventoryRequest> all = InventoryRequestService.INSTANCE.getAllRequests();
        allRequests.clear();

        for (InventoryRequest req : all) {
            allRequests.add(new InventoryRequestRow(
                req.getId(),
                req.getPharmacistUsername(),
                req.getItemName(),
                req.getRequestedQuantity(),
                req.getReason(),
                req.getStatus().name(),
                dateFormat.format(req.getRequestDate()),
                req.getReviewedBy() != null ? req.getReviewedBy() : "",
                req.getAdminNotes() != null ? req.getAdminNotes() : ""
            ));
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Display class for TableView
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
            this.reviewedBy = reviewedBy;
            this.adminNotes = adminNotes;
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

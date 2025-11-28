package project2;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class InventoryDashboardController {

    @FXML
    private ComboBox<String> itemSelector;
    @FXML
    private LineChart<Number, Number> stockChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private Label currentStockLabel;
    @FXML
    private Label minStockLabel;
    @FXML
    private Label stockStatusLabel;

    @FXML
    private ComboBox<String> requestItemName;
    @FXML
    private Spinner<Integer> requestQuantity;
    @FXML
    private TextArea requestReason;
    @FXML
    private Button submitRequestButton;

    @FXML
    private TableView<InventoryRequestDisplay> myRequestsTable;
    @FXML
    private TableColumn<InventoryRequestDisplay, String> itemNameColumn;
    @FXML
    private TableColumn<InventoryRequestDisplay, Integer> quantityColumn;
    @FXML
    private TableColumn<InventoryRequestDisplay, String> reasonColumn;
    @FXML
    private TableColumn<InventoryRequestDisplay, String> statusColumn;
    @FXML
    private TableColumn<InventoryRequestDisplay, String> dateColumn;
    @FXML
    private TableColumn<InventoryRequestDisplay, String> notesColumn;

    @FXML
    private Button backButton;
    @FXML
    private Button refreshButton;

    private final ObservableList<InventoryRequestDisplay> myRequests = FXCollections.observableArrayList();
    private String currentUsername;

    @FXML
    public void initialize() {
        currentUsername = SessionManager.INSTANCE.getCurrentUsername();
        if (currentUsername == null || currentUsername.isBlank()) {
            showError("No active session. Please log in.");
            return;
        }

        setupItemSelector();
        setupStockChart();
        setupRequestForm();
        setupRequestsTable();
        loadMyRequests();
    }

    private void setupItemSelector() {
        List<InventoryObject> items = InventoryLogger.INSTANCE.getAllItems();
        ObservableList<String> itemNames = FXCollections.observableArrayList();

        for (InventoryObject item : items) {
            itemNames.add(item.getName());
        }

        itemSelector.setItems(itemNames);
        itemSelector.setPromptText("Select an item to view");

        itemSelector.setOnAction(e -> {
            String selected = itemSelector.getValue();
            if (selected != null && !selected.isBlank()) {
                displayItemData(selected);
            }
        });

        // Auto-select first item if available
        if (!itemNames.isEmpty()) {
            itemSelector.setValue(itemNames.get(0));
            displayItemData(itemNames.get(0));
        }
    }

    private void setupStockChart() {
        xAxis.setLabel("Days Ago");
        yAxis.setLabel("Stock Quantity");
        stockChart.setTitle("Stock Level History");
        stockChart.setCreateSymbols(true);
        stockChart.setLegendVisible(false);
    }

    private void setupRequestForm() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 1);
        requestQuantity.setValueFactory(valueFactory);
        requestQuantity.setEditable(true);

        // Populate item dropdown with existing inventory items
        List<InventoryObject> items = InventoryLogger.INSTANCE.getAllItems();
        ObservableList<String> itemNames = FXCollections.observableArrayList();

        for (InventoryObject item : items) {
            itemNames.add(item.getName());
        }

        requestItemName.setItems(itemNames);
        requestItemName.setPromptText("Select or type item name");
        requestItemName.setEditable(true); // Allow typing new items too
    }

    private void setupRequestsTable() {
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateString"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("adminNotes"));

        myRequestsTable.setItems(myRequests);
        myRequestsTable.setPlaceholder(new Label("No requests submitted yet."));
    }

    private void displayItemData(String itemName) {
        InventoryObject item = InventoryLogger.INSTANCE.findObjectElseNull(itemName);
        if (item == null) {
            showError("Item not found: " + itemName);
            return;
        }

        // Update labels
        currentStockLabel.setText(String.valueOf(item.getQuantity()));
        minStockLabel.setText(String.valueOf(item.getMin()));

        // Update status
        boolean isLowStock = item.isLowStock();
        if (isLowStock) {
            stockStatusLabel.setText("⚠ LOW STOCK");
            stockStatusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            stockStatusLabel.setText("✓ Adequate Stock");
            stockStatusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: green; -fx-font-weight: bold;");
        }

        // Update chart
        updateChart(item);
    }

    private void updateChart(InventoryObject item) {
        stockChart.getData().clear();

        Map<Date, Integer> changes = item.getChangesHistory();
        if (changes.isEmpty()) {
            // If no history, show current stock only
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName("Stock Level");
            series.getData().add(new XYChart.Data<>(0, item.getQuantity()));
            stockChart.getData().add(series);
            return;
        }

        // Create series for stock levels
        XYChart.Series<Number, Number> stockSeries = new XYChart.Series<>();
        stockSeries.setName("Stock Level");

        XYChart.Series<Number, Number> minSeries = new XYChart.Series<>();
        minSeries.setName("Minimum Level");

        // Sort changes by date
        List<Map.Entry<Date, Integer>> sortedChanges = new ArrayList<>(changes.entrySet());
        sortedChanges.sort(Map.Entry.comparingByKey());

        // Calculate days ago for each point
        Date now = new Date();
        for (Map.Entry<Date, Integer> entry : sortedChanges) {
            long diffInMillis = now.getTime() - entry.getKey().getTime();
            long daysAgo = diffInMillis / (1000 * 60 * 60 * 24);

            stockSeries.getData().add(new XYChart.Data<>(-daysAgo, entry.getValue()));
        }

        // Add current stock
        stockSeries.getData().add(new XYChart.Data<>(0, item.getQuantity()));

        stockChart.getData().addAll(stockSeries);
        stockChart.setLegendVisible(true);
    }

    @FXML
    void submitRequest(ActionEvent event) {
        String itemName = requestItemName.getValue();
        String reason = requestReason.getText();
        Integer quantity = requestQuantity.getValue();

        if (itemName == null || itemName.isBlank()) {
            showError("Please select or enter an item name.");
            return;
        }

        if (reason == null || reason.isBlank()) {
            showError("Please provide a reason for this request.");
            return;
        }

        if (quantity == null || quantity <= 0) {
            showError("Please enter a valid quantity.");
            return;
        }

        try {
            InventoryRequest request = InventoryRequestService.INSTANCE.createRequest(
                    currentUsername,
                    itemName,
                    quantity,
                    reason
            );

            showInfo("Request submitted successfully!");

            // Clear form
            requestItemName.setValue(null);
            requestReason.clear();
            requestQuantity.getValueFactory().setValue(1);

            // Refresh requests table
            loadMyRequests();

        } catch (Exception e) {
            showError("Failed to submit request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void refreshData(ActionEvent event) {
        InventoryLogger.INSTANCE.refresh();
        InventoryRequestService.INSTANCE.refresh();

        setupItemSelector();
        loadMyRequests();

        String selected = itemSelector.getValue();
        if (selected != null && !selected.isBlank()) {
            displayItemData(selected);
        }

        showInfo("Data refreshed!");
    }

    @FXML
    void goBack(ActionEvent event) {
        try {
            String role = LoginManager.INSTANCE.getRole(currentUsername);
            if (role == null) role = "Patient";

            switch (role.toLowerCase()) {
                case "admin":
                    App.setRoot("adminDashboard");
                    break;
                case "pharmacist":
                    App.setRoot("pharmacistDashboard");
                    break;
                default:
                    App.setRoot("patientDashboard");
                    break;
            }
        } catch (IOException e) {
            showError("Failed to navigate back: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadMyRequests() {
        List<InventoryRequest> requests = InventoryRequestService.INSTANCE.getRequestsByPharmacist(currentUsername);
        myRequests.clear();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (InventoryRequest req : requests) {
            myRequests.add(new InventoryRequestDisplay(
                    req.getItemName(),
                    req.getRequestedQuantity(),
                    req.getReason(),
                    req.getStatus().name(),
                    dateFormat.format(req.getRequestDate()),
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
    public static class InventoryRequestDisplay {
        private final String itemName;
        private final Integer quantity;
        private final String reason;
        private final String status;
        private final String dateString;
        private final String adminNotes;

        public InventoryRequestDisplay(String itemName, Integer quantity, String reason,
                                       String status, String dateString, String adminNotes) {
            this.itemName = itemName;
            this.quantity = quantity;
            this.reason = reason;
            this.status = status;
            this.dateString = dateString;
            this.adminNotes = adminNotes;
        }

        public String getItemName() {
            return itemName;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public String getReason() {
            return reason;
        }

        public String getStatus() {
            return status;
        }

        public String getDateString() {
            return dateString;
        }

        public String getAdminNotes() {
            return adminNotes;
        }
    }
}

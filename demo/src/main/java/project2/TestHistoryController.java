package project2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Displays the history of lab tests recorded in the system.
 */
public class TestHistoryController {

    private static final String FILE_PATH = "demo/src/data/TestEntries.csv";

    @FXML
    private TableView<TestHistoryEntry> historyTable;

    @FXML
    private TableColumn<TestHistoryEntry, String> reportIdColumn;

    @FXML
    private TableColumn<TestHistoryEntry, String> reportDateColumn;

    @FXML
    private TableColumn<TestHistoryEntry, String> patientIdColumn;

    @FXML
    private TableColumn<TestHistoryEntry, String> patientNameColumn;

    @FXML
    private TableColumn<TestHistoryEntry, String> testNameColumn;

    @FXML
    private TableColumn<TestHistoryEntry, String> resultValueColumn;

    @FXML
    private TableColumn<TestHistoryEntry, String> unitsColumn;

    @FXML
    private TableColumn<TestHistoryEntry, String> resultStatusColumn;

    @FXML
    private TableColumn<TestHistoryEntry, String> resultReceivedColumn;

    @FXML
    private Label statusLabel;

    private final ObservableList<TestHistoryEntry> entries = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureColumns();
        historyTable.setItems(entries);
        loadEntries();
    }

    @FXML
    void refreshHistory(ActionEvent event) {
        loadEntries();
    }

    @FXML
    void goBack(ActionEvent event) throws IOException {
        App.setRoot("pharmacistDashboard");
    }

    private void configureColumns() {
        reportIdColumn.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        reportDateColumn.setCellValueFactory(new PropertyValueFactory<>("reportDateTime"));
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        testNameColumn.setCellValueFactory(new PropertyValueFactory<>("testName"));
        resultValueColumn.setCellValueFactory(new PropertyValueFactory<>("resultValue"));
        unitsColumn.setCellValueFactory(new PropertyValueFactory<>("units"));
        resultStatusColumn.setCellValueFactory(new PropertyValueFactory<>("resultStatus"));
        resultReceivedColumn.setCellValueFactory(new PropertyValueFactory<>("resultsReceivedDateTime"));
    }

    private void loadEntries() {
        entries.clear();
        Path path = Paths.get(FILE_PATH);
        if (Files.notExists(path)) {
            statusLabel.setText("No test history file found.");
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 21) {
                    continue;
                }
                TestHistoryEntry entry = new TestHistoryEntry(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        parts[15].trim(),
                        parts[16].trim(),
                        parts[17].trim(),
                        parts[19].trim(),
                        parts[20].trim()
                );
                entries.add(entry);
            }
            statusLabel.setText(entries.isEmpty()
                    ? "No tests recorded yet."
                    : "Loaded " + entries.size() + " test record(s).");
        } catch (IOException e) {
            statusLabel.setText("Failed to read test history.");
            e.printStackTrace();
        }
    }
}

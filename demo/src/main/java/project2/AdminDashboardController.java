package project2;

import java.io.IOException;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

public class AdminDashboardController {

    @FXML
    private AnchorPane adminDashboardPane;

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> ageColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, Void> actionColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button logoutButton;

    private ObservableList<User> users;

    public void initialize() {
        // Set up table columns
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Set up the action column with ComboBox for role selection
        setupActionColumn();

        // Load users
        refreshUserList();
    }

    private void setupActionColumn() {
        Callback<TableColumn<User, Void>, TableCell<User, Void>> cellFactory = new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                final TableCell<User, Void> cell = new TableCell<User, Void>() {

                    private final ComboBox<String> roleComboBox = new ComboBox<>();

                    {
                        roleComboBox.getItems().addAll("admin", "user", "pharmacist");
                        roleComboBox.setOnAction((ActionEvent event) -> {
                            User user = getTableView().getItems().get(getIndex());
                            String newRole = roleComboBox.getValue();
                            if (newRole != null && !newRole.equals(user.getRole())) {
                                // Update the role
                                boolean success = BackEnd.updateUserRole(user.getUsername(), newRole);
                                if (success) {
                                    user.setRole(newRole);
                                    getTableView().refresh();
                                    System.out.println("Updated role for user: " + user.getUsername() + " to " + newRole);
                                } else {
                                    System.err.println("Failed to update role for user: " + user.getUsername());
                                    roleComboBox.setValue(user.getRole()); // Reset to original value
                                }
                            }
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            User user = getTableView().getItems().get(getIndex());
                            roleComboBox.setValue(user.getRole());
                            setGraphic(roleComboBox);
                        }
                    }
                };
                return cell;
            }
        };

        actionColumn.setCellFactory(cellFactory);
    }

    @FXML
    void refreshUserList() {
        String filePath = "demo/output.csv";
        users = BackEnd.loadUserData(filePath);
        userTable.setItems(users);
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        App.setRoot("sample1");
    }
}
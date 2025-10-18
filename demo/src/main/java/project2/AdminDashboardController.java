package project2;

import java.io.IOException;
import java.util.Collection;

import javafx.collections.FXCollections;
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
    private TableView<UserKT> userTable;

    @FXML
    private TableColumn<UserKT, String> usernameColumn;

    @FXML
    private TableColumn<UserKT, String> typeColumn;

    @FXML
    private TableColumn<UserKT, Void> actionColumn;

    @FXML
    private Button refreshButton;

    @FXML
    private Button logoutButton;

    private ObservableList<UserKT> users;

    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        typeColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getClass().getSimpleName()
                )
        );

        setupActionColumn();
        refreshUserList();
    }


    private void setupActionColumn() {
        Callback<TableColumn<UserKT, Void>, TableCell<UserKT, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<UserKT, Void> call(final TableColumn<UserKT, Void> param) {
                final TableCell<UserKT, Void> cell = new TableCell<>() {

                    private final ComboBox<String> typeComboBox = new ComboBox<>();

                    {
                        typeComboBox.getItems().addAll("Patient", "Pharmacist", "Admin");
                        typeComboBox.setOnAction((ActionEvent event) -> {
                            UserKT user = getTableView().getItems().get(getIndex());
                            String currentType = user.getClass().getSimpleName();
                            String newType = typeComboBox.getValue();

                            if (newType != null && !newType.equals(currentType)) {
                                boolean success = LoginManager.updateUserType(user.getUsername(), newType);
                                if (success) {
                                    System.out.println("✅ Updated type for " + user.getUsername() + " to " + newType);
                                    refreshUserList(); // reload with new subclass type
                                } else {
                                    System.err.println("❌ Failed to update type for " + user.getUsername());
                                    typeComboBox.setValue(currentType);
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
                            UserKT user = getTableView().getItems().get(getIndex());
                            typeComboBox.setValue(user.getClass().getSimpleName());
                            setGraphic(typeComboBox);
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
        // load from UserManager instead of BackEnd
        Collection<UserKT> allUsers = UserManager.INSTANCE.getAllUsers();
        users = FXCollections.observableArrayList(allUsers);
        userTable.setItems(users);
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        App.setRoot("login");
    }
}

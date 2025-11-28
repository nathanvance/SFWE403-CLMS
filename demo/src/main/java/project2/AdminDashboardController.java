package project2;

import java.io.IOException;
import java.util.Collection;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;   // <-- needed for onDeleteMyAccount()

public class AdminDashboardController {

    @FXML private AnchorPane adminDashboardPane;
    @FXML private TableView<UserKT> userTable;
    @FXML private TableColumn<UserKT, String> usernameColumn;
    @FXML private TableColumn<UserKT, String> typeColumn;
    @FXML private TableColumn<UserKT, Void>   actionColumn;

    @FXML private Button changePasswordButton;
    @FXML private Button logoutButton;
    @FXML private Button btnDeleteMyAccount;
    @FXML private Button refreshButton;

    private ObservableList<UserKT> users;

    @FXML
    public void initialize() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        typeColumn.setCellValueFactory(cd -> new SimpleStringProperty(getRoleFor(cd.getValue())));
        setupActionColumn();
        refreshUserList();
    }

    /** Role select + conditional Recover/Restore buttons. */
    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<UserKT, Void>() {

            private final ComboBox<String> typeCombo =
                    new ComboBox<>(FXCollections.observableArrayList("Patient", "Pharmacist", "Admin"));

            {
                typeCombo.setOnAction(e -> {
                    UserKT user = getTableView().getItems().get(getIndex());
                    if (user == null) return;

                    String uname       = user.getUsername();
                    String currentRole = getRoleFor(user);
                    String newRole     = typeCombo.getValue();
                    if (newRole == null || newRole.equalsIgnoreCase(currentRole)) return;

                    if (!willStillHaveAnAdmin(uname, newRole)) {
                        System.err.println("At least one Admin must exist.");
                        typeCombo.setValue(currentRole);
                        return;
                    }

                    boolean ok = LoginManager.updateUserType(uname, newRole);
                    if (ok) refreshUserList(); else typeCombo.setValue(currentRole);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                UserKT user = getTableView().getItems().get(getIndex());
                if (user == null) { setGraphic(null); return; }

                final String uname = user.getUsername();
                typeCombo.setValue(getRoleFor(user));

                // Deleted → show Restore, disable role editing
                if (LoginManager.isDeleted(uname)) {
                    typeCombo.setDisable(true);
                    Button restoreBtn = new Button("Restore");
                    restoreBtn.setOnAction(e -> {
                        String admin = SessionManager.INSTANCE.getCurrentUsername();
                        if (LoginManager.restoreUser(admin, uname)) refreshUserList();
                    });
                    setGraphic(new HBox(8, typeCombo, restoreBtn));
                    return;
                }

                // Locked → show Recover (unlock + mark RESET REQUIRED)
                if (LoginManager.isLocked(uname)) {
                    typeCombo.setDisable(false);
                    Button recoverBtn = new Button("Recover");
                    recoverBtn.setOnAction(e -> {
                        String admin = SessionManager.INSTANCE.getCurrentUsername();
                        boolean unlocked = LoginManager.INSTANCE.unlockUser(uname);   // <-- INSTANCE
                        boolean flagged  = LoginManager.requirePasswordReset(admin, uname);
                        if (unlocked && flagged) refreshUserList();
                    });
                    setGraphic(new HBox(8, typeCombo, recoverBtn));
                    return;
                }

                // Normal row
                typeCombo.setDisable(false);
                setGraphic(typeCombo);
            }
        });
    }

    /** Ensure at least one Admin remains after role changes. */
    private boolean willStillHaveAnAdmin(String targetUser, String newRole) {
        long admins = LoginManager.INSTANCE.getAllUsers().entrySet().stream()
                .map(e -> e.getKey().equals(targetUser) ? newRole : e.getValue().getRole())
                .filter(r -> r != null && r.equalsIgnoreCase("Admin"))
                .count();
        return admins >= 1;
    }

    @FXML
    void refreshUserList() {
        Collection<UserKT> allUsers = UserManager.INSTANCE.getAllUsers();
        users = FXCollections.observableArrayList(allUsers);
        userTable.setItems(users);
    }

    private String getRoleFor(UserKT u) {
        String role = LoginManager.INSTANCE.getRole(u.getUsername());
        return role != null ? role : "Patient";
    }

    @FXML
    void goChangePassword(ActionEvent event) throws IOException {
        App.setRoot("changePassword");
    }

    @FXML
    void logout(ActionEvent event) throws IOException {
        App.setRoot("login");
    }

    // Self-delete for the currently-logged-in admin account.
    @FXML
    private void onDeleteMyAccount() throws IOException {
        String currentUser = SessionManager.INSTANCE.getCurrentUsername();
        if (currentUser == null || currentUser.isBlank()) {
            new Alert(Alert.AlertType.INFORMATION, "No active session.", ButtonType.OK).showAndWait();
            return;
        }

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

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This will permanently delete your account.",
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Are you sure?");
        confirm.getButtonTypes().setAll(new ButtonType("Delete"), ButtonType.CANCEL);

        var r2 = confirm.showAndWait();
        if (r2.isEmpty() || r2.get().getButtonData().isCancelButton()) return;

        boolean ok = LoginManager.deleteSelf(currentUser, pwd);
        new Alert(Alert.AlertType.INFORMATION,
                ok ? "Account deleted." : "Delete failed. Check your password and try again.",
                ButtonType.OK).showAndWait();

        if (ok) {
            SessionManager.INSTANCE.clear();
            App.setRoot("login");
        }
    }
}
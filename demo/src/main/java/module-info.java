module project2 {
    requires javafx.controls;
    requires javafx.fxml;

    opens project2 to javafx.fxml;
    exports project2;
}

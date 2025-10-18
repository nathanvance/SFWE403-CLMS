module project2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires org.json;

    opens project2 to javafx.fxml;
    exports project2;
}

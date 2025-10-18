 module project2 {
     requires javafx.controls;
     requires javafx.fxml;
     requires kotlin.stdlib;
     requires com.google.gson;
     requires org.json;

     opens project2 to javafx.fxml;
     exports project2;
 }

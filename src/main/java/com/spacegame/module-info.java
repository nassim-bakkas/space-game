module com.spacegame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    opens com.spacegame to javafx.fxml;
    exports com.spacegame;
}

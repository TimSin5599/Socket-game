module ru.hse.hw.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires java.desktop;

    opens ru.hse.hw.client to javafx.fxml;
    exports ru.hse.hw.client;
}
module ru.hse.hw.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires ru.hse.hw.server;
    requires java.xml;


    opens ru.hse.hw.client to javafx.fxml;
    exports ru.hse.hw.client;
}
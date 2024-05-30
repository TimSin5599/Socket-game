module ru.hse.hw.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires ru.hse.hw.server;


    opens ru.hse.hw.client to javafx.fxml;
    exports ru.hse.hw.client;
}
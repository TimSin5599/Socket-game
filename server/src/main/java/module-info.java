module ru.hse.hw.server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.compiler;
    requires java.xml;

    opens ru.hse.hw.server to javafx.fxml;
    exports ru.hse.hw.server;
}
module server {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.compiler;
    requires java.xml;
    requires java.sql;

    opens ru.hse.hw.server to javafx.fxml;
    exports ru.hse.hw.server;
}
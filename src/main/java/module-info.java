module com.example.da {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires javafx.swing;
    requires java.net.http;
    requires com.google.gson;

    opens com.example.da to javafx.fxml;
    opens com.example.da.controller to javafx.fxml;
    opens com.example.da.model to javafx.base, javafx.fxml;

    exports com.example.da;
    opens com.example.da.dialog to javafx.fxml;
}

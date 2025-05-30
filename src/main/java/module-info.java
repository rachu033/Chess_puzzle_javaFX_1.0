module code.chess {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jfr;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires jdk.httpserver;
    requires java.net.http;
    requires org.apache.commons.configuration2;

    requires org.apache.commons.logging;

    requires java.sql;
    requires org.apache.commons.lang3;
    requires org.apache.logging.log4j;

    opens code.chess to javafx.fxml;
    opens code.chess.controller to javafx.fxml;
    exports code.chess;
}

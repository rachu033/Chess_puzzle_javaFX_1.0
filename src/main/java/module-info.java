module code.chess {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jfr;
    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires jdk.httpserver;
    requires java.net.http;
    requires commons.logging;
    requires org.apache.logging.log4j;
    requires java.sql;
    requires org.apache.commons.lang3;


    opens code.chess to javafx.fxml;
    exports code.chess;
}
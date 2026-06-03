module br.edu.ifg.luziania.p3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires javax.mail.api;
    requires java.desktop;
    requires io.github.cdimascio.dotenv.java;
    requires jdk.jsobject;

    exports br.edu.ifg.luziania.p3.mvc.app;
    opens br.edu.ifg.luziania.p3.mvc.controller to javafx.fxml;
    exports br.edu.ifg.luziania.p3.mvc.model;
    opens br.edu.ifg.luziania.p3.mvc.model to javafx.base;
}
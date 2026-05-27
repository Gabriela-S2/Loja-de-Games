package br.edu.ifg.luziania.p3.mvc.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MainController {

    @FXML
    private Pane contentArea; // O Pane onde as telas serão trocadas

    @FXML
    public void abrirLoja() throws IOException {
        trocarTela("/br/edu/ifg/luziania/p3/mvc/view/Store.fxml");
    }

    @FXML
    public void abrirBiblioteca() throws IOException {
        trocarTela("/br/edu/ifg/luziania/p3/mvc/view/Biblioteca.fxml");
    }

    private void trocarTela(String fxmlPath) throws IOException {
        Parent screen = FXMLLoader.load(getClass().getResource(fxmlPath));
        contentArea.getChildren().setAll(screen);
    }
}
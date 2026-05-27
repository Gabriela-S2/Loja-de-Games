package br.edu.ifg.luziania.p3.mvc.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        java.net.URL fxmlLocation = getClass().getResource("/br/edu/ifg/luziania/p3/mvc/view/Login.fxml");

        if (fxmlLocation == null) {
            System.err.println("ERRO FATAL: O Java não encontrou o arquivo FXML!");
            System.err.println("Verifique se a pasta 'view' está EXATAMENTE dentro de: src/main/resources/br/edu/ifg/luziania/p3/mvc/");
            System.exit(1);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Steam - Login");
        stage.setScene(scene);

        // --- OTIMIZAÇÕES DE TELA ---

        // Define o tamanho mínimo (baseado no prefWidth/prefHeight do seu Login.fxml)
        stage.setMinWidth(650);
        stage.setMinHeight(430); // 400 do FXML + 30px da barra de título do Windows/Mac

        // (Opcional) Centraliza a janela ao abrir
        stage.centerOnScreen();

        // (Opcional) Se quiser impedir totalmente que o usuário maximize a tela de Login:
        // stage.setResizable(false);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
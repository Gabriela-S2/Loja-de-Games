package br.edu.ifg.luziania.p3.mvc.controller;

import br.edu.ifg.luziania.p3.mvc.dao.JogoDAO;
import br.edu.ifg.luziania.p3.mvc.model.Jogo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class DetalhesJogoController {

    @FXML
    private ImageView imgCapa;
    @FXML
    private Label lblTitulo;
    @FXML
    private Label lblPreco;
    @FXML
    private Button btnAcao;
    @FXML
    private Label lblTempoJogado;

    private Jogo jogoAtual;
    private Instant inicioSessao;
    private JogoDAO jogoDAO = new JogoDAO(); // Conexão com o banco

    public void setJogo(Jogo jogo) {
        this.jogoAtual = jogo;
        lblTitulo.setText(jogo.getTitulo());

        try {
            imgCapa.setImage(new Image(getClass().getResourceAsStream(jogo.getCaminhoImagem())));
        } catch (Exception e) {
            registrarLogExcecao("Erro ao carregar imagem", e);
        }

        atualizarInterfaceBotao();
    }

    private void atualizarInterfaceBotao() {
        if (jogoAtual.getStatus() == Jogo.StatusJogo.INSTALADO) {
            lblTempoJogado.setText("Tempo de Jogo: " + jogoAtual.getTempoFormatado());
            lblTempoJogado.setVisible(true);
        } else {
            lblTempoJogado.setVisible(false);
        }

        if (inicioSessao != null) {
            btnAcao.setText("Encerrar Jogo");
            btnAcao.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-font-weight: bold;"); // Vermelho
            return;
        }

        switch (jogoAtual.getStatus()) {
            case NAO_COMPRADO:
                lblPreco.setText(String.format("R$ %.2f", jogoAtual.getPreco()));
                lblPreco.setVisible(true);
                btnAcao.setText("Comprar");
                btnAcao.setStyle("-fx-background-color: #66c0f4; -fx-text-fill: white; -fx-font-weight: bold;");
                break;
            case COMPRADO:
                lblPreco.setVisible(false);
                btnAcao.setText("Instalar");
                btnAcao.setStyle("-fx-background-color: #4c6b22; -fx-text-fill: white; -fx-font-weight: bold;");
                break;
            case INSTALADO:
                lblPreco.setVisible(false);
                btnAcao.setText("Jogar");
                btnAcao.setStyle("-fx-background-color: #5c7e10; -fx-text-fill: white; -fx-font-weight: bold;");
                break;
        }
    }

    @FXML
    public void executarAcao(ActionEvent event) {
        switch (jogoAtual.getStatus()) {
            case NAO_COMPRADO:
                registrarLogUso("Usuário comprou o jogo: " + jogoAtual.getTitulo());
                jogoAtual.setStatus(Jogo.StatusJogo.COMPRADO);
                jogoDAO.atualizarStatus(jogoAtual);
                break;

            case COMPRADO:
                registrarLogUso("Usuário instalou o jogo: " + jogoAtual.getTitulo());
                simularInstalacao();
                jogoAtual.setStatus(Jogo.StatusJogo.INSTALADO);
                jogoDAO.atualizarStatus(jogoAtual);
                break;

            case INSTALADO:
                if (inicioSessao == null) {
                    inicioSessao = Instant.now();
                    registrarLogUso("Usuário iniciou o jogo: " + jogoAtual.getTitulo());
                    System.out.println("Jogo " + jogoAtual.getTitulo() + " em execução...");
                } else {
                    Instant fimSessao = Instant.now();
                    long segundosJogados = Duration.between(inicioSessao, fimSessao).getSeconds();
                    jogoAtual.adicionarTempoJogado(segundosJogados);
                    jogoDAO.atualizarTempoJogado(jogoAtual);

                    registrarLogUso("Usuário encerrou o jogo: " + jogoAtual.getTitulo() + ". Tempo da sessão: " + segundosJogados + "s");
                    System.out.println("Sessão encerrada!");
                    inicioSessao = null;
                }
                break;
        }

        atualizarInterfaceBotao();
    }

    private void simularInstalacao() {
        try {
            File diretorio = new File("JogosInstalados");
            if (!diretorio.exists()) {
                diretorio.mkdir();
            }

            File arquivoJogo = new File(diretorio, jogoAtual.getTitulo().replaceAll(" ", "_") + ".txt");
            FileWriter writer = new FileWriter(arquivoJogo);
            writer.write("Jogo instalado com sucesso!\n");
            writer.write("Título: " + jogoAtual.getTitulo() + "\n");
            writer.write("Arquivo de simulação para o trabalho de Programação III.");
            writer.close();

            System.out.println("Arquivo de instalação gerado: " + arquivoJogo.getAbsolutePath());

        } catch (IOException e) {
            registrarLogExcecao("Erro ao simular instalação de " + jogoAtual.getTitulo(), e);
        }
    }

    @FXML
    public void voltarParaLoja(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/br/edu/ifg/luziania/p3/mvc/view/Store.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            registrarLogExcecao("Erro navegação", e);
        }
    }

    private void registrarLogUso(String acao) {
        System.out.println("[LOG AUDITORIA] " + acao);
    }

    private void registrarLogExcecao(String acao, Exception e) {
        System.err.println("[LOG EXCEÇÃO] " + acao + " - " + e.getMessage());
    }
}
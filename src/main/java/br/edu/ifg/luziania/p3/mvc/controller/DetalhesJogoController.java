package br.edu.ifg.luziania.p3.mvc.controller;

import br.edu.ifg.luziania.p3.mvc.dao.JogoDAO;
import br.edu.ifg.luziania.p3.mvc.model.Jogo;
import br.edu.ifg.luziania.p3.mvc.model.JogoMidia;
import br.edu.ifg.luziania.p3.mvc.util.LogUtil;
import br.edu.ifg.luziania.p3.mvc.session.SessaoUsuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;

public class DetalhesJogoController implements Initializable {

    // ── TopBar ──
    @FXML private ImageView imgAvatar;
    @FXML private Label     lblNomeUsuario;

    // ── Lateral ──
    @FXML private VBox      boxSeusJogos;

    // ── Área principal ──
    @FXML private StackPane painelImagem;
    @FXML private ImageView imgCapaGrande;

    // ── Rodapé da imagem ──
    @FXML private Label     lblTitulo;
    @FXML private Label     lblTempoJogado;
    @FXML private Label     lblPreco;
    @FXML private Button    btnAcao;

    // ── Seção inferior ──
    @FXML private Label     lblDescricao;
    @FXML private VBox      painelCarrossel;
    @FXML private HBox      hboxMidias;
    @FXML private StackPane painelMidiaSelecionada;
    @FXML private ImageView imgMidiaSelecionada;

    private Jogo jogoAtual;
    private Instant inicioSessao;
    private final JogoDAO jogoDAO = new JogoDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var usuario = SessaoUsuario.getInstancia().getUsuario();
        if (usuario != null) {
            lblNomeUsuario.setText(usuario.getUsername());
            if (usuario.getCaminhoFoto() != null && !usuario.getCaminhoFoto().isBlank()) {
                carregarImagem(imgAvatar, usuario.getCaminhoFoto());
            }
        }
    }

    // ──────────────────────────────────────────
    //  API pública: chamada pelo StoreController
    // ──────────────────────────────────────────

    public void setJogo(Jogo jogo) {
        this.jogoAtual = jogo;

        // Capa grande (prefere imagem vertical; fallback padrão)
        String caminhoCapa = (jogo.getCaminhoImagemVertical() != null
                && !jogo.getCaminhoImagemHorizontal().isBlank())
                ? jogo.getCaminhoImagemHorizontal()
                : jogo.getCaminhoImagem();
        carregarImagem(imgCapaGrande, caminhoCapa);

        // Título e descrição
        lblTitulo.setText(jogo.getTitulo());
        if (jogo.getDescricao() != null && !jogo.getDescricao().isBlank()) {
            lblDescricao.setText(jogo.getDescricao());
        } else {
            lblDescricao.setText("Sem descrição disponível.");
        }

        // Carrossel de mídia
        carregarMidias(jogo.getId());

        // Lateral: jogos instalados
        preencherLateral();

        atualizarBotao();
    }

    // ──────────────────────────────────────────
    //  Botão dinâmico
    // ──────────────────────────────────────────

    private void atualizarBotao() {
        // Se há sessão aberta → botão "Encerrar Jogo"
        if (inicioSessao != null) {
            btnAcao.setText("Encerrar Jogo");
            btnAcao.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 4;");
            lblTempoJogado.setVisible(true);
            lblTempoJogado.setManaged(true);
            return;
        }

        switch (jogoAtual.getStatus()) {
            case NAO_COMPRADO:
                lblPreco.setText(String.format("R$ %.2f", jogoAtual.getPreco()));
                lblPreco.setVisible(true);   lblPreco.setManaged(true);
                lblTempoJogado.setVisible(false); lblTempoJogado.setManaged(false);
                btnAcao.setText("Comprar");
                btnAcao.setStyle("-fx-background-color: #1a9fff; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 4;");
                break;

            case COMPRADO:
                lblPreco.setVisible(false);  lblPreco.setManaged(false);
                lblTempoJogado.setVisible(false); lblTempoJogado.setManaged(false);
                btnAcao.setText("Instalar");
                btnAcao.setStyle("-fx-background-color: #4c6b22; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 4;");
                break;

            case INSTALADO:
                lblPreco.setVisible(false);  lblPreco.setManaged(false);
                lblTempoJogado.setText("Tempo jogado: " + jogoAtual.getTempoFormatado());
                lblTempoJogado.setVisible(true);  lblTempoJogado.setManaged(true);
                btnAcao.setText("Jogar");
                btnAcao.setStyle("-fx-background-color: #5c7e10; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-background-radius: 4;");
                break;
        }
    }

    // ──────────────────────────────────────────
    //  Ação do botão principal
    // ──────────────────────────────────────────

    @FXML
    public void executarAcao(ActionEvent event) {
        switch (jogoAtual.getStatus()) {

            case NAO_COMPRADO:
                // Simula compra (sem pagamento real)
                jogoAtual.setStatus(Jogo.StatusJogo.COMPRADO);
                jogoDAO.atualizarStatus(jogoAtual);
                LogUtil.registrarUso("DetalhesJogoController", "Comprado: " + jogoAtual.getTitulo());
                break;

            case COMPRADO:
                instalarJogo();
                break;

            case INSTALADO:
                if (inicioSessao == null) {
                    // Inicia sessão de jogo
                    inicioSessao = Instant.now();
                    LogUtil.registrarUso("DetalhesJogoController", "Iniciado: " + jogoAtual.getTitulo());
                    abrirArquivoDoJogo();
                } else {
                    // Encerra sessão
                    long seg = Duration.between(inicioSessao, Instant.now()).getSeconds();
                    jogoAtual.adicionarTempoJogado(seg);
                    jogoDAO.atualizarTempoJogado(jogoAtual);
                    LogUtil.registrarUso("DetalhesJogoController",
                            "Encerrado: " + jogoAtual.getTitulo() + " | sessão: " + seg + "s");
                    inicioSessao = null;
                }
                break;
        }
        atualizarBotao();
    }

    // ──────────────────────────────────────────
    //  Instalar: cria arquivo .txt em ~/Downloads/jogos/
    // ──────────────────────────────────────────

    private void instalarJogo() {
        try {
            Path downloads = Paths.get(System.getProperty("user.home"), "Downloads", "jogos");
            Files.createDirectories(downloads);

            String nomeArquivo = jogoAtual.getTitulo().replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".txt";
            Path arquivo = downloads.resolve(nomeArquivo);

            try (BufferedWriter bw = Files.newBufferedWriter(arquivo)) {
                bw.write(jogoAtual.getTitulo());
            }

            jogoAtual.setStatus(Jogo.StatusJogo.INSTALADO);
            jogoDAO.atualizarStatus(jogoAtual);
            LogUtil.registrarUso("DetalhesJogoController",
                    "Instalado: " + jogoAtual.getTitulo() + " → " + arquivo);

        } catch (IOException e) {
            LogUtil.registrarErro("DetalhesJogoController.instalarJogo", e);
        }
    }

    // ──────────────────────────────────────────
    //  Jogar: abre o arquivo .txt gerado
    // ──────────────────────────────────────────

    private void abrirArquivoDoJogo() {
        try {
            String nomeArquivo = jogoAtual.getTitulo().replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".txt";
            Path arquivo = Paths.get(System.getProperty("user.home"), "Downloads", "jogos", nomeArquivo);

            if (Files.exists(arquivo) && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(arquivo.toFile());
            } else {
                LogUtil.registrarUso("DetalhesJogoController",
                        "Arquivo não encontrado para abrir: " + arquivo);
            }
        } catch (IOException e) {
            LogUtil.registrarErro("DetalhesJogoController.abrirArquivoDoJogo", e);
        }
    }

    // ──────────────────────────────────────────
    //  Carrossel de mídia
    // ──────────────────────────────────────────

    private void carregarMidias(int jogoId) {
        List<JogoMidia> midias = jogoDAO.listarMidias(jogoId);
        hboxMidias.getChildren().clear();

        if (midias.isEmpty()) {
            painelCarrossel.setVisible(false);
            painelCarrossel.setManaged(false);
            return;
        }

        painelCarrossel.setVisible(true);
        painelCarrossel.setManaged(true);

        for (JogoMidia midia : midias) {
            // Thumb clicável
            ImageView thumb = new ImageView();
            thumb.setFitWidth(160);
            thumb.setFitHeight(90);
            thumb.setPreserveRatio(true);
            thumb.setSmooth(true);
            thumb.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian,black,4,0.3,0,1);");

            if ("IMAGEM".equals(midia.getTipo())) {
                carregarImagem(thumb, midia.getCaminho());
                thumb.setOnMouseClicked(e -> exibirMidiaSelecionada(midia.getCaminho()));
            } else {
                // Para vídeo: exibe um placeholder (ícone de play)
                // Num app real, usaria javafx.scene.media.MediaView
                thumb.setStyle(thumb.getStyle() + " -fx-background-color: #0a0f18;");
                Label play = new Label("▶ Vídeo");
                play.setStyle("-fx-text-fill: white; -fx-font-size: 12;");
                // Coloca dentro de um StackPane
                StackPane sp = new StackPane(thumb, play);
                sp.setStyle("-fx-cursor: hand;");
                sp.setOnMouseClicked(e -> exibirMidiaSelecionada(midia.getCaminho()));
                hboxMidias.getChildren().add(sp);
                continue;
            }
            hboxMidias.getChildren().add(thumb);
        }

        // Abre a primeira mídia por padrão se for imagem
        midias.stream()
                .filter(m -> "IMAGEM".equals(m.getTipo()))
                .findFirst()
                .ifPresent(m -> exibirMidiaSelecionada(m.getCaminho()));
    }

    private void exibirMidiaSelecionada(String caminho) {
        carregarImagem(imgMidiaSelecionada, caminho);
        painelMidiaSelecionada.setVisible(true);
        painelMidiaSelecionada.setManaged(true);
    }

    // ──────────────────────────────────────────
    //  Barra lateral (jogos instalados)
    // ──────────────────────────────────────────

    private void preencherLateral() {
        boxSeusJogos.getChildren().clear();
        jogoDAO.listarTodos().stream()
                .filter(j -> j.getStatus() == Jogo.StatusJogo.INSTALADO)
                .forEach(j -> {
                    Hyperlink link = new Hyperlink(j.getTitulo());
                    link.setStyle("-fx-text-fill: #c6d4df; -fx-font-size: 12;");
                    link.setWrapText(true);
                    link.setMaxWidth(112);
                    link.setOnAction(e -> setJogo(j));
                    boxSeusJogos.getChildren().add(link);
                });
    }

    // ──────────────────────────────────────────
    //  Navegação
    // ──────────────────────────────────────────

    @FXML
    public void voltarParaLoja(ActionEvent event) {
        navegarPara(event, "/br/edu/ifg/luziania/p3/mvc/view/Store.fxml");
    }

    @FXML
    public void voltarParaBiblioteca(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/br/edu/ifg/luziania/p3/mvc/view/Store.fxml"));
            Parent root = loader.load();
            StoreController ctrl = loader.getController();
            ctrl.mostrarBiblioteca();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root,
                    stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.show();
        } catch (IOException e) {
            LogUtil.registrarErro("DetalhesJogoController.voltarParaBiblioteca", e);
        }
    }

    private void navegarPara(ActionEvent event, String fxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root,
                    stage.getScene().getWidth(), stage.getScene().getHeight()));
            stage.show();
        } catch (IOException e) {
            LogUtil.registrarErro("DetalhesJogoController.navegarPara", "Destino: " + fxml, e);
        }
    }

    // ──────────────────────────────────────────
    //  Utilidade
    // ──────────────────────────────────────────

    private void carregarImagem(ImageView iv, String caminho) {
        if (caminho == null || caminho.isBlank()) return;
        try {
            var stream = getClass().getResourceAsStream(caminho);
            if (stream != null) iv.setImage(new Image(stream));
        } catch (Exception e) {
            LogUtil.registrarErro("DetalhesJogoController.carregarImagem",
                    "Imagem não encontrada: " + caminho, e);
        }
    }
}

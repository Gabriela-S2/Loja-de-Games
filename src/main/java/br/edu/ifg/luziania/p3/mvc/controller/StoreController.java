package br.edu.ifg.luziania.p3.mvc.controller;

import javafx.geometry.Side;
import javafx.scene.shape.Circle;
import br.edu.ifg.luziania.p3.mvc.dao.JogoDAO;
import br.edu.ifg.luziania.p3.mvc.model.Jogo;
import br.edu.ifg.luziania.p3.mvc.util.LogUtil;
import br.edu.ifg.luziania.p3.mvc.session.SessaoUsuario;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StoreController implements Initializable {

    // ── TopBar ──
    @FXML private Button  btnAbaStore;
    @FXML private Button  btnAbaBiblioteca;
    @FXML private TextField txtBusca;
    @FXML private Circle circuloUsuario;
    @FXML private Label   lblNomeUsuario;

    // ── Lateral ──
    @FXML private VBox    boxSeusJogos;

    // ── Store view ──
    @FXML private VBox    viewStore;
    @FXML private ImageView imgBannerPrincipal;
    @FXML private ImageView imgBannerSecundario1;
    @FXML private ImageView imgBannerSecundario2;
    @FXML private FlowPane  containerCatalogo;

    // ── Biblioteca view ──
    @FXML private VBox    viewBiblioteca;
    @FXML private Label   lblSemJogos;
    @FXML private FlowPane containerBiblioteca;

    // Jogos carregados para filtro
    private List<Jogo> todosOsJogos;

    // Referências aos jogos dos banners (para navegação ao clicar)
    private Jogo jogoBannerPrincipal;
    private Jogo jogoBannerSecundario1;
    private Jogo jogoBannerSecundario2;

    private final JogoDAO jogoDAO = new JogoDAO();

    @FXML
    private void abrirMenuPerfil(MouseEvent event) {
        // Cria o menu de contexto (o menuzinho suspenso)
        ContextMenu contextMenu = new ContextMenu();

        // --- Opção 1: Editar Perfil ---
        MenuItem itemEditarPerfil = new MenuItem("Editar Perfil");
        itemEditarPerfil.setOnAction(e -> {
            try {
                LogUtil.registrarUso("StoreController","Usuário abriu a tela de Editar Perfil");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/edu/ifg/luziania/p3/mvc/view/EditarPerfil.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
                LogUtil.registrarErro("Erro ao tentar abrir a tela de edição de perfil", ex);
            }
        });

        // --- Opção 2: Fazer Logoff ---
        MenuItem itemLogoff = new MenuItem("Fazer Logoff");
        itemLogoff.setOnAction(e -> {
            try {
                // Limpa o usuário salvo em memória para encerrar a sessão
                SessaoUsuario.getInstancia().encerrarSessao();
                LogUtil.registrarAutenticacao(String.valueOf(lblNomeUsuario.getText()), "Logoff realizado com sucesso");
                // Carrega a tela de Login
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/edu/ifg/luziania/p3/mvc/view/Login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
                LogUtil.registrarErro("Erro ao tentar fazer logoff", ex);
            }
        });

        // Adiciona os dois itens criados para dentro do menu
        contextMenu.getItems().addAll(itemEditarPerfil, itemLogoff);

        // Pega o elemento que foi clicado (o círculo da foto) e exibe o menu logo abaixo dele
        Node nodeClicado = (Node) event.getSource();
        contextMenu.show(nodeClicado, Side.BOTTOM, 0, 5); // O '5' dá um pequeno espaçamento visual
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Exibe nome do usuário logado
        var usuario = SessaoUsuario.getInstancia().getUsuario();
        if (usuario != null) {
            // Define o nome ao lado da foto
            lblNomeUsuario.setText(" "+usuario.getUsername());

            String caminhoFoto = usuario.getCaminhoFoto();
            if (caminhoFoto != null && !caminhoFoto.isEmpty()) {
                try {
                    Image imgPerfil;

                    // Verifica se a imagem é um recurso interno do projeto (classpath) ou um arquivo externo
                    if (caminhoFoto.startsWith("/")) {
                        // Carrega a imagem padrão embutida no projeto
                        imgPerfil = new Image(getClass().getResourceAsStream(caminhoFoto));
                    } else {
                        // Carrega a imagem personalizada da pasta "uploads/perfil"
                        imgPerfil = new Image("file:" + caminhoFoto);
                    }

                    // A mágica: preenche o círculo com a foto do usuário
                    circuloUsuario.setFill(new ImagePattern(imgPerfil));
                } catch (Exception e) {
                    LogUtil.registrarErro("Erro ao carregar foto de perfil na Store", e);
                }
            }
        }
        carregarJogos();
    }

    // ──────────────────────────────────────────
    //  Carregamento de dados
    // ──────────────────────────────────────────

    private void carregarJogos() {
        todosOsJogos = jogoDAO.listarTodos();
        preencherBanners();
        preencherCatalogo(todosOsJogos);
        preencherBiblioteca(todosOsJogos);
        preencherLateral(todosOsJogos);
    }

    /** Preenche os três banners com jogos aleatórios (horizontal). */
    private void preencherBanners() {
        if (todosOsJogos.isEmpty()) return;

        // Usa os três primeiros jogos que tiverem imagem horizontal;
        // fallback: imagem padrão (caminhoImagem).
        List<Jogo> comHorizontal = todosOsJogos.stream()
                .filter(j -> j.getCaminhoImagemHorizontal() != null
                        && !j.getCaminhoImagemHorizontal().isBlank())
                .collect(Collectors.toList());

        List<Jogo> fonte = comHorizontal.isEmpty() ? todosOsJogos : comHorizontal;

        jogoBannerPrincipal   = fonte.get(0);
        jogoBannerSecundario1 = fonte.size() > 1 ? fonte.get(1) : fonte.get(0);
        jogoBannerSecundario2 = fonte.size() > 2 ? fonte.get(2) : fonte.get(0);

        carregarImagem(imgBannerPrincipal,   imagemHorizontalOuPadrao(jogoBannerPrincipal));
        carregarImagem(imgBannerSecundario1, imagemHorizontalOuPadrao(jogoBannerSecundario1));
        carregarImagem(imgBannerSecundario2, imagemHorizontalOuPadrao(jogoBannerSecundario2));
    }

    private String imagemHorizontalOuPadrao(Jogo j) {
        return (j.getCaminhoImagemHorizontal() != null && !j.getCaminhoImagemHorizontal().isBlank())
                ? j.getCaminhoImagemHorizontal()
                : j.getCaminhoImagem();
    }

    private void preencherCatalogo(List<Jogo> jogos) {
        containerCatalogo.getChildren().clear();
        for (Jogo jogo : jogos) {
            containerCatalogo.getChildren().add(criarCardVertical(jogo));
        }
    }

    private void preencherBiblioteca(List<Jogo> jogos) {
        containerBiblioteca.getChildren().clear();
        List<Jogo> instalados = jogos.stream()
                .filter(j -> j.getStatus() == Jogo.StatusJogo.INSTALADO)
                .collect(Collectors.toList());

        lblSemJogos.setVisible(instalados.isEmpty());
        lblSemJogos.setManaged(instalados.isEmpty());

        for (Jogo jogo : instalados) {
            containerBiblioteca.getChildren().add(criarCardVertical(jogo));
        }
    }

    private void preencherLateral(List<Jogo> jogos) {
        boxSeusJogos.getChildren().clear();
        jogos.stream()
                .filter(j -> j.getStatus() == Jogo.StatusJogo.INSTALADO)
                .forEach(j -> {
                    Hyperlink link = new Hyperlink(j.getTitulo());
                    link.setTextFill(Color.web("#c6d4df"));
                    link.setMaxWidth(112.0);
                    link.setStyle("-fx-font-size: 12;");
                    link.setWrapText(true);
                    link.setOnAction(e -> abrirDetalhesDe(e, j));
                    boxSeusJogos.getChildren().add(link);
                });
    }

    // ──────────────────────────────────────────
    //  Card vertical (110×160 px)
    // ──────────────────────────────────────────

    private VBox criarCardVertical(Jogo jogo) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #2a475e; -fx-background-radius: 4; -fx-padding: 4;");
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setPrefWidth(115);

        ImageView img = new ImageView();
        img.setFitWidth(107);
        img.setFitHeight(155);
        img.setPreserveRatio(true);
        img.setSmooth(true);
        carregarImagem(img, jogo.getCaminhoImagem());

        Label titulo = new Label(jogo.getTitulo());
        titulo.setTextFill(Color.WHITE);
        titulo.setMaxWidth(107);
        titulo.setWrapText(false);
        titulo.setStyle("-fx-font-size: 11; -fx-text-overrun: ellipsis;");

        Label preco = new Label();
        if (jogo.getStatus() == Jogo.StatusJogo.NAO_COMPRADO) {
            preco.setText(String.format("R$ %.2f", jogo.getPreco()));
            preco.setTextFill(Color.web("#66c0f4"));
        } else {
            preco.setText("Adquirido");
            preco.setTextFill(Color.web("#4c6b22"));
        }
        preco.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");

        card.getChildren().addAll(img, titulo, preco);
        card.setOnMouseClicked(e -> abrirDetalhesDe(e, jogo));
        return card;
    }

    // ──────────────────────────────────────────
    //  Navegação
    // ──────────────────────────────────────────

    @FXML private void abrirJogoBannerPrincipal(MouseEvent e)   { abrirDetalhesDe(e, jogoBannerPrincipal); }
    @FXML private void abrirJogoBannerSecundario1(MouseEvent e) { abrirDetalhesDe(e, jogoBannerSecundario1); }
    @FXML private void abrirJogoBannerSecundario2(MouseEvent e) { abrirDetalhesDe(e, jogoBannerSecundario2); }

    private void abrirDetalhesDe(Event event, Jogo jogo) {
        if (jogo == null) return;
        try {
            LogUtil.registrarUso("StoreController", "Abrindo detalhes: " + jogo.getTitulo());
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/br/edu/ifg/luziania/p3/mvc/view/DetalhesJogo.fxml"));
            Parent root = loader.load();

            DetalhesJogoController ctrl = loader.getController();
            ctrl.setJogo(jogo);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LogUtil.registrarErro("StoreController.abrirDetalhesDe", e);
        }
    }

    // ──────────────────────────────────────────
    //  Abas Store / Biblioteca
    // ──────────────────────────────────────────

    @FXML
    public void mostrarStore() {
        viewStore.setVisible(true);    viewStore.setManaged(true);
        viewBiblioteca.setVisible(false); viewBiblioteca.setManaged(false);
        btnAbaStore.setStyle(
                "-fx-background-color: #c7d5e0; -fx-text-fill: #1b2838; " +
                        "-fx-font-weight: bold; -fx-background-radius: 0; -fx-font-size: 13;");
        btnAbaBiblioteca.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #c7d5e0; " +
                        "-fx-font-weight: bold; -fx-background-radius: 0; -fx-font-size: 13;");
    }

    @FXML
    public void mostrarBiblioteca() {
        viewStore.setVisible(false);   viewStore.setManaged(false);
        viewBiblioteca.setVisible(true);  viewBiblioteca.setManaged(true);
        btnAbaBiblioteca.setStyle(
                "-fx-background-color: #c7d5e0; -fx-text-fill: #1b2838; " +
                        "-fx-font-weight: bold; -fx-background-radius: 0; -fx-font-size: 13;");
        btnAbaStore.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #c7d5e0; " +
                        "-fx-font-weight: bold; -fx-background-radius: 0; -fx-font-size: 13;");
        preencherBiblioteca(todosOsJogos);
    }

    // ──────────────────────────────────────────
    //  Busca/Filtro
    // ──────────────────────────────────────────

    @FXML
    public void filtrarJogos() {
        String termo = txtBusca.getText().trim().toLowerCase();
        List<Jogo> filtrados = todosOsJogos.stream()
                .filter(j -> j.getTitulo().toLowerCase().contains(termo))
                .collect(Collectors.toList());

        if (viewStore.isVisible()) {
            preencherCatalogo(filtrados);
        } else {
            preencherBiblioteca(filtrados);
        }
    }

    // ──────────────────────────────────────────
    //  Utilidade de imagem
    // ──────────────────────────────────────────

    private void carregarImagem(ImageView iv, String caminho) {
        if (caminho == null || caminho.isBlank()) return;
        try {
            var stream = getClass().getResourceAsStream(caminho);
            if (stream != null) iv.setImage(new Image(stream));
        } catch (Exception e) {
            LogUtil.registrarErro("StoreController.carregarImagem",
                    "Imagem não encontrada: " + caminho, e);
        }
    }
}

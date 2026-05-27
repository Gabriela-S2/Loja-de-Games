package br.edu.ifg.luziania.p3.mvc.controller;

import br.edu.ifg.luziania.p3.mvc.dao.JogoDAO;
import br.edu.ifg.luziania.p3.mvc.model.Jogo;
import br.edu.ifg.luziania.p3.mvc.model.Usuario;
import br.edu.ifg.luziania.p3.mvc.session.SessaoUsuario;
import br.edu.ifg.luziania.p3.mvc.util.LogUtil;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.geometry.Side;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StoreController implements Initializable {

    @FXML
    private FlowPane containerCatalogo; // Onde os cards vão ficar

    @FXML
    private VBox boxSeusJogos; // Onde os links da barra lateral vão ficar

    @FXML
    private VBox viewStore; // Container que envolve os banners e o catálogo

    @FXML
    private VBox viewBiblioteca; // Container que envolve os jogos do usuário

    @FXML
    private javafx.scene.control.TextField txtBusca;

    @FXML
    private Circle circuloUsuario;

    @FXML
    private Label lblNomeUsuario;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        carregarDadosUsuario(); // Chama a função para puxar foto e nome
        carregarJogosDoBanco();
    }

    private void carregarDadosUsuario() {
        // Puxa o usuário que fez login e está salvo na sessão
        Usuario usuarioLogado = SessaoUsuario.getInstancia().getUsuario();

        if (usuarioLogado != null) {
            // Define o nome ao lado da foto
            lblNomeUsuario.setText(usuarioLogado.getUsername());

            String caminhoFoto = usuarioLogado.getCaminhoFoto();
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
                    registrarLogExcecao("Erro ao carregar foto de perfil na Store", e);
                }
            }
        }
    }

    @FXML
    private void abrirEditarPerfil(MouseEvent event) {
        // Cria o menu de contexto (o menuzinho suspenso)
        ContextMenu contextMenu = new ContextMenu();

        // --- Opção 1: Editar Perfil ---
        MenuItem itemEditarPerfil = new MenuItem("Editar Perfil");
        itemEditarPerfil.setOnAction(e -> {
            try {
                registrarLogUso("Usuário abriu a tela de Editar Perfil");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/edu/ifg/luziania/p3/mvc/view/EditarPerfil.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
                registrarLogExcecao("Erro ao tentar abrir a tela de edição de perfil", ex);
            }
        });

        // --- Opção 2: Fazer Logoff ---
        MenuItem itemLogoff = new MenuItem("Fazer Logoff");
        itemLogoff.setOnAction(e -> {
            try {
                registrarLogUso("Usuário fez logoff");

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
                registrarLogExcecao("Erro ao tentar fazer logoff", ex);
            }
        });

        // Adiciona os dois itens criados para dentro do menu
        contextMenu.getItems().addAll(itemEditarPerfil, itemLogoff);

        // Pega o elemento que foi clicado (o círculo da foto) e exibe o menu logo abaixo dele
        Node nodeClicado = (Node) event.getSource();
        contextMenu.show(nodeClicado, Side.BOTTOM, 0, 5); // O '5' dá um pequeno espaçamento visual
    }

    @FXML
    private void mostrarStore(javafx.event.ActionEvent event) {
        // Mostra a loja
        viewStore.setVisible(true);
        viewStore.setManaged(true);

        // Esconde a biblioteca
        viewBiblioteca.setVisible(false);
        viewBiblioteca.setManaged(false);
    }

    @FXML
    private void mostrarBiblioteca(javafx.event.ActionEvent event) {
        // Esconde a loja
        viewStore.setVisible(false);
        viewStore.setManaged(false);

        // Mostra a biblioteca
        viewBiblioteca.setVisible(true);
        viewBiblioteca.setManaged(true);
    }

    @FXML
    private void filtrarJogos(javafx.scene.input.KeyEvent event) {
        String termoPesquisa = txtBusca.getText().toLowerCase();
        // A lógica de filtragem da lista de jogos pode ser implementada aqui posteriormente
    }

    private void carregarJogosDoBanco() {
        // Simulação de dados vindo do banco (substitua futuramente pelo JogoDAO)
        JogoDAO jogoDAO = new JogoDAO();
        List<Jogo> listaJogos = jogoDAO.listarTodos();

        // Limpa as áreas para evitar duplicação caso o método seja chamado novamente
        containerCatalogo.getChildren().clear();
        boxSeusJogos.getChildren().clear();

        for (Jogo jogo : listaJogos) {
            // 1. Cria o Card para o Catálogo Central
            VBox card = criarCardJogo(jogo);
            containerCatalogo.getChildren().add(card);

            // 2. Se o jogo estiver "instalado", cria o link rápido na lateral esquerda
            if (jogo.getStatus() == Jogo.StatusJogo.INSTALADO) {
                Hyperlink link = new Hyperlink(jogo.getTitulo());
                link.setTextFill(Color.WHITE);
                link.setMaxWidth(100.0);

                // Passa o evento de clique 'e' e o 'jogo' para o método
                link.setOnAction(e -> abrirPaginaDoJogo(e, jogo));
                boxSeusJogos.getChildren().add(link);
            }
        }
    }

    // Método que desenha o bloco do jogo "estilo Epic"
    private VBox criarCardJogo(Jogo jogo) {
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setStyle("-fx-background-color: #171a21;");
        vbox.setCursor(javafx.scene.Cursor.HAND);

        // Imagem
        ImageView imgView = new ImageView();
        imgView.setFitHeight(150.0);
        imgView.setFitWidth(110.0);
        try {
            imgView.setImage(new Image(getClass().getResourceAsStream(jogo.getCaminhoImagem())));
        } catch (Exception e) {
            registrarLogExcecao("Imagem não encontrada: " + jogo.getCaminhoImagem(), e);
        }

        // Título
        Hyperlink link = new Hyperlink(jogo.getTitulo());
        link.setTextFill(Color.WHITE);
        link.setMaxWidth(110.0);
        link.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS); // Suas reticências!

        // Preço dinâmico baseado no Status (NAO_COMPRADO, COMPRADO, INSTALADO)
        Label lblPreco = new Label();
        if (jogo.getStatus() == Jogo.StatusJogo.NAO_COMPRADO) {
            lblPreco.setText(String.format("R$ %.2f", jogo.getPreco()));
        } else {
            lblPreco.setText("Adquirido");
        }
        lblPreco.setTextFill(Color.web("#66c0f4"));
        lblPreco.setStyle("-fx-padding: 5;");

        vbox.getChildren().addAll(imgView, link, lblPreco);

        // Passa o evento de clique do mouse 'e' e o 'jogo'
        vbox.setOnMouseClicked(e -> abrirPaginaDoJogo(e, jogo));

        return vbox;
    }

    private void abrirPaginaDoJogo(Event event, Jogo jogoClicado) {
        try {
            registrarLogUso("Usuário clicou para ver os detalhes do jogo: " + jogoClicado.getTitulo());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/edu/ifg/view/DetalhesJogo.fxml"));
            Parent root = loader.load();

            // MÁGICA: Pegamos o controller da tela que acabou de ser carregada
            DetalhesJogoController controller = loader.getController();

            // Passamos o jogo para ele configurar os botões e imagens da tela de detalhes
            controller.setJogo(jogoClicado);

            // Troca a cena atual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            registrarLogExcecao("Erro ao tentar abrir DetalhesJogo.fxml", e);
        }
    }

    // --- Métodos de Log (Substitua depois pela escrita real em arquivo TXT) ---

    private void registrarLogUso(String acao) {
        System.out.println("[LOG AUDITORIA] " + acao);
    }

    private void registrarLogExcecao(String acao, Exception e) {
        System.err.println("[LOG EXCEÇÃO] " + acao + " - Detalhes: " + e.getMessage());
    }
}
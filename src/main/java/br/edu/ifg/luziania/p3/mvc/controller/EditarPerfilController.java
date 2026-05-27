package br.edu.ifg.luziania.p3.mvc.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import br.edu.ifg.luziania.p3.mvc.dao.UsuarioDAO;
import br.edu.ifg.luziania.p3.mvc.model.Usuario;
import br.edu.ifg.luziania.p3.mvc.session.SessaoUsuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class EditarPerfilController {

    @FXML private ImageView imgPerfil;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenhaAntiga;
    @FXML private PasswordField txtNovaSenha;
    @FXML private Label lblMensagem;

    private String caminhoNovaImagem = null;

    @FXML
    public void initialize() {
        Usuario usuarioLogado = SessaoUsuario.getInstancia().getUsuario();

        if (usuarioLogado != null) {
            txtUsername.setPromptText(usuarioLogado.getUsername());
            txtEmail.setPromptText(usuarioLogado.getEmail());

            if (usuarioLogado.getCaminhoFoto() != null && !usuarioLogado.getCaminhoFoto().trim().isEmpty()) {
                try {
                    // Se começar com "/", busca do classpath (como o avatar padrão).
                    // Se não, busca como um arquivo relativo ("file:uploads/perfil/...")
                    String prefix = usuarioLogado.getCaminhoFoto().startsWith("/") ? "" : "file:";
                    imgPerfil.setImage(new Image(prefix + usuarioLogado.getCaminhoFoto()));
                } catch (Exception e) {
                    System.out.println("Não foi possível carregar a imagem de perfil atual.");
                }
            }
        }
    }

    @FXML
    public void alterarFoto(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Escolha sua nova foto de perfil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            caminhoNovaImagem = file.getAbsolutePath();
            imgPerfil.setImage(new Image("file:" + caminhoNovaImagem));
        }
    }

    @FXML
    public void salvarAlteracoes(ActionEvent event) {
        Usuario usuarioLogado = SessaoUsuario.getInstancia().getUsuario();

        if (usuarioLogado == null) {
            exibirMensagem("Erro interno: Usuário não autenticado na sessão.", true);
            return;
        }

        String inputUsername = txtUsername.getText().trim();
        String novoUsername = inputUsername.isEmpty() ? usuarioLogado.getUsername() : inputUsername;

        String inputEmail = txtEmail.getText().trim();
        String novoEmail = inputEmail.isEmpty() ? usuarioLogado.getEmail() : inputEmail;

        String senhaAntiga = txtSenhaAntiga.getText();
        String novaSenha = txtNovaSenha.getText();

        if (!inputEmail.isEmpty()) {
            String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (!novoEmail.matches(emailRegex)) {
                exibirMensagem("O e-mail informado possui um formato inválido.", true);
                return;
            }
        }

        UsuarioDAO dao = new UsuarioDAO();

        if (!inputUsername.isEmpty() && !novoUsername.equals(usuarioLogado.getUsername())) {
            if (dao.existeUsername(novoUsername, usuarioLogado.getId())) {
                exibirMensagem("Este nome de usuário já está sendo utilizado.", true);
                return;
            }
        }

        if (!novaSenha.isEmpty()) {
            if (senhaAntiga.isEmpty()) {
                exibirMensagem("A senha atual é obrigatória para definir uma nova senha.", true);
                return;
            }

            if (!dao.validarSenhaAntiga(usuarioLogado.getId(), senhaAntiga)) {
                exibirMensagem("A senha atual informada está incorreta.", true);
                return;
            }
        }

        String caminhoParaOBanco = null;

        if (caminhoNovaImagem != null) {
            try {
                String pastaDestino = "uploads/perfil";
                File diretorio = new File(pastaDestino);
                if (!diretorio.exists()) {
                    diretorio.mkdirs();
                }

                String extensao = caminhoNovaImagem.substring(caminhoNovaImagem.lastIndexOf("."));

                // 1. Formata a data e hora atual (Ex: 20260527_153025)
                // Usamos hora/minuto/segundo para evitar conflito se o usuário trocar de foto 2x no mesmo dia
                String dataAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

                // 2. Limpa o nome de usuário (troca espaços e caracteres especiais por "_")
                // Isso evita que um usuário chamado "João Vitor /" quebre a criação do arquivo
                String usernameLimpo = novoUsername.replaceAll("[^a-zA-Z0-9.-]", "_");

                // 3. Monta o novo nome padronizado (Ex: gabriela_20260527_153025.png)
                String nomeArquivo = usernameLimpo + "_" + dataAtual + extensao;

                Path origem = Paths.get(caminhoNovaImagem);
                // ... continua o código normalmente ...

                Path destino = Paths.get(pastaDestino, nomeArquivo);

                Files.copy(origem, destino, StandardCopyOption.REPLACE_EXISTING);

                // MODIFICAÇÃO AQUI: Salva apenas o caminho relativo dentro do projeto
                // e substitui barras invertidas do Windows "\" por barras normais "/"
                caminhoParaOBanco = destino.toString().replace("\\", "/");

            } catch (IOException e) {
                e.printStackTrace();
                exibirMensagem("Erro ao processar e salvar a imagem no sistema.", true);
                return;
            }
        }

        boolean sucesso = dao.atualizarPerfil(
                usuarioLogado.getId(),
                novoUsername,
                novoEmail,
                novaSenha.isEmpty() ? null : novaSenha,
                caminhoParaOBanco
        );

        if (sucesso) {
            usuarioLogado.setUsername(novoUsername);
            usuarioLogado.setEmail(novoEmail);
            if (caminhoParaOBanco != null) {
                usuarioLogado.setCaminhoFoto(caminhoParaOBanco);
            }

            txtUsername.clear();
            txtEmail.clear();
            txtSenhaAntiga.clear();
            txtNovaSenha.clear();

            txtUsername.setPromptText(novoUsername);
            txtEmail.setPromptText(novoEmail);

            caminhoNovaImagem = null;

            exibirMensagem("Perfil updated com sucesso!", false);
        } else {
            exibirMensagem("Erro ao salvar as alterações no banco de dados.", true);
        }
    }

    @FXML
    public void voltar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/edu/ifg/luziania/p3/mvc/view/Store.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Loja de Jogos");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            exibirMensagem("Erro ao tentar voltar para a tela anterior.", true);
        }
    }

    private void exibirMensagem(String mensagem, boolean erro) {
        lblMensagem.setText(mensagem);
        lblMensagem.setTextFill(erro ? Color.RED : Color.GREEN);
    }
}
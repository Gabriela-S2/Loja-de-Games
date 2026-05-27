package br.edu.ifg.luziania.p3.mvc.controller;

import br.edu.ifg.luziania.p3.mvc.dao.UsuarioDAO;
import br.edu.ifg.luziania.p3.mvc.util.LogUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class CadastroController {

    @FXML private TextField txtUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenha;
    @FXML private PasswordField txtConfirmarSenha;

    @FXML private ImageView imgUserVerificado;
    @FXML private ImageView imgEmailVerificado;
    @FXML private ImageView imgSenhaVerificada;

    @FXML private Label lblMensagemErro;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void initialize() {
        imgUserVerificado.setVisible(false);
        imgEmailVerificado.setVisible(false);
        imgSenhaVerificada.setVisible(false);
        lblMensagemErro.setText("");
    }

    @FXML
    private void salvarUsuario() {
        // Reseta estado visual
        lblMensagemErro.setText("");
        imgUserVerificado.setVisible(false);
        imgEmailVerificado.setVisible(false);
        imgSenhaVerificada.setVisible(false);

        try {
            String username = txtUsuario.getText().trim();
            String email    = txtEmail.getText().trim();
            String senha    = txtSenha.getText();
            String confirma = txtConfirmarSenha.getText();

            // 1. Username
            if (username.isEmpty())
                throw new IllegalArgumentException("O nome de usuário não pode estar vazio.");
            if (usuarioDAO.existeUsername(username))
                throw new IllegalArgumentException("Esse nome de usuário já está em uso.");
            imgUserVerificado.setVisible(true);

            // 2. Email
            if (!email.matches("^[\\w\\-.]+@([\\w-]+\\.)+[\\w-]{2,4}$"))
                throw new IllegalArgumentException("Formato de e-mail inválido.");
            if (usuarioDAO.existeEmail(email))
                throw new IllegalArgumentException("Esse e-mail já está cadastrado.");
            imgEmailVerificado.setVisible(true);

            // 3. Senha
            if (!senha.matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$"))
                throw new IllegalArgumentException("Senha deve ter no mínimo 8 caracteres, com letras e números.");
            if (!senha.equals(confirma))
                throw new IllegalArgumentException("As senhas não coincidem.");
            imgSenhaVerificada.setVisible(true);

            // Tudo válido → cadastra
            usuarioDAO.cadastrar(username, email, senha);

            LogUtil.registrarUso("CadastroController", "Novo usuário cadastrado: " + username);
            exibirSucesso("Conta criada com sucesso! Faça login para continuar.");

        } catch (IllegalArgumentException e) {
            exibirErro(e.getMessage());
        }
    }

    @FXML
    private void irParaLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/br/edu/ifg/luziania/p3/mvc/view/Login.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            LogUtil.registrarErro("CadastroController.irParaLogin", e);
        }
    }

    private void exibirErro(String msg) {
        lblMensagemErro.setStyle("-fx-text-fill: #d9534f;");
        lblMensagemErro.setText(msg);
    }

    private void exibirSucesso(String msg) {
        lblMensagemErro.setStyle("-fx-text-fill: #4c6b22;");
        lblMensagemErro.setText(msg);
    }
}

package br.edu.ifg.luziania.p3.mvc.controller;

import br.edu.ifg.luziania.p3.mvc.dao.UsuarioDAO;
import br.edu.ifg.luziania.p3.mvc.util.LogUtil;
import br.edu.ifg.luziania.p3.mvc.session.SessaoUsuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private Button btnLogin;
    @FXML private Label lblMensagem;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    private void realizarLogin(ActionEvent event) {
        lblMensagem.setText("");

        String login = txtUsuario.getText().trim();
        String senha = txtSenha.getText();

        if (login.isEmpty() || senha.isEmpty()) {
            exibirErro("Preencha todos os campos.");
            return;
        }

        if (usuarioDAO.autenticar(login, senha)) {
            // Salva o usuário na sessão global
            SessaoUsuario.getInstancia().setUsuario(usuarioDAO.buscarPorLoginOuEmail(login));
            LogUtil.registrarAutenticacao(login, "Login realizado com sucesso");
            trocarTela(event, "/br/edu/ifg/luziania/p3/mvc/view/Store.fxml");
        } else {
            LogUtil.registrarUso("LoginController", "Tentativa de login falhou para: " + login);
            exibirErro("Usuário ou senha inválidos.");
        }
    }

    @FXML
    private void irParaCadastro(ActionEvent event) {
        trocarTela(event, "/br/edu/ifg/luziania/p3/mvc/view/CadastrarUsuario.fxml");
    }

    @FXML
    private void irParaTrocarSenha(ActionEvent event) {
        trocarTela(event, "/br/edu/ifg/luziania/p3/mvc/view/TrocarSenha.fxml");
    }

    private void trocarTela(ActionEvent event, String caminhoFxml) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(caminhoFxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            LogUtil.registrarErro("LoginController.trocarTela", "Erro ao carregar " + caminhoFxml, e);
            exibirErro("Erro ao carregar a tela. Tente novamente.");
        }
    }

    private void exibirErro(String msg) {
        lblMensagem.setStyle("-fx-text-fill: #d9534f;");
        lblMensagem.setText(msg);
    }
}
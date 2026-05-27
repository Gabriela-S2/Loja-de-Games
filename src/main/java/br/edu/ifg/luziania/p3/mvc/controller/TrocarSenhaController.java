package br.edu.ifg.luziania.p3.mvc.controller;

import br.edu.ifg.luziania.p3.mvc.dao.UsuarioDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Random;

public class TrocarSenhaController {

    @FXML private TextField txtIdentificacao;
    @FXML private VBox etapa1;
    @FXML private VBox etapa2;

    @FXML private TextField txtCodigo;
    @FXML private PasswordField txtNovaSenha;
    @FXML private PasswordField txtConfirmarNovaSenha;

    @FXML private Label lblMensagem;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private String emailDestino; // Email que vai receber o código

    @FXML
    private void enviarCodigo(ActionEvent event) {
        lblMensagem.setText("");
        String identificacao = txtIdentificacao.getText().trim();

        if (identificacao.isEmpty()) {
            exibirErro("Informe seu email ou nome de usuário.");
            return;
        }

        // Verifica se o identificador existe no banco
        boolean existe = usuarioDAO.existeEmail(identificacao) ||
                usuarioDAO.existeUsername(identificacao);

        if (!existe) {
            exibirErro("Nenhuma conta encontrada com esse email ou nome de usuário.");
            return;
        }

        // Gera código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));

        // Salva o código no banco (com expiração de 15 minutos)
        boolean salvo = usuarioDAO.salvarCodigoRecuperacao(identificacao, codigo);

        if (!salvo) {
            exibirErro("Erro ao gerar o código. Tente novamente.");
            return;
        }

        // TODO: Enviar email real com o código (JavaMail / SMTP)
        // Por ora, exibe no console para testes
        System.out.println("[EMAIL SIMULADO] Código de recuperação: " + codigo);

        emailDestino = identificacao;

        // Avança para a etapa 2
        etapa1.setVisible(false);
        etapa1.setManaged(false);
        etapa2.setVisible(true);
        etapa2.setManaged(true);

        exibirSucesso("Código enviado! Verifique sua caixa de entrada.");
    }

    @FXML
    private void redefinirSenha(ActionEvent event) {
        lblMensagem.setText("");
        String codigo         = txtCodigo.getText().trim();
        String novaSenha      = txtNovaSenha.getText();
        String confirmarSenha = txtConfirmarNovaSenha.getText();

        if (codigo.isEmpty() || novaSenha.isEmpty() || confirmarSenha.isEmpty()) {
            exibirErro("Preencha todos os campos.");
            return;
        }

        // Valida código no banco
        String emailValidado = usuarioDAO.validarCodigoRecuperacao(codigo);
        if (emailValidado == null) {
            exibirErro("Código inválido ou expirado. Solicite um novo.");
            return;
        }

        // Valida nova senha
        if (!novaSenha.matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$")) {
            exibirErro("A senha deve ter no mínimo 8 caracteres, com letras e números.");
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            exibirErro("As senhas não coincidem.");
            return;
        }

        boolean atualizado = usuarioDAO.redefinirSenhaPorEmail(emailValidado, novaSenha);
        if (atualizado) {
            exibirSucesso("Senha redefinida com sucesso! Você já pode fazer login.");
            // Navega para login após 2s (ou pode deixar o usuário clicar)
        } else {
            exibirErro("Erro ao redefinir senha. Tente novamente.");
        }
    }

    @FXML
    private void reenviarCodigo(ActionEvent event) {
        // Volta para etapa 1 e limpa campos
        etapa2.setVisible(false);
        etapa2.setManaged(false);
        etapa1.setVisible(true);
        etapa1.setManaged(true);
        txtCodigo.clear();
        txtNovaSenha.clear();
        txtConfirmarNovaSenha.clear();
        lblMensagem.setText("");
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
            System.err.println("[LOG EXCEÇÃO] Erro ao voltar para Login: " + e.getMessage());
        }
    }

    // --- Helpers visuais ---

    private void exibirErro(String msg) {
        lblMensagem.setStyle("-fx-text-fill: #d9534f;");
        lblMensagem.setText(msg);
        lblMensagem.setVisible(true);
        lblMensagem.setManaged(true);
    }

    private void exibirSucesso(String msg) {
        lblMensagem.setStyle("-fx-text-fill: #4c6b22;");
        lblMensagem.setText(msg);
        lblMensagem.setVisible(true);
        lblMensagem.setManaged(true);
    }
}


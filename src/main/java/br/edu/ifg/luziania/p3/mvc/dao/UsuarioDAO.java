package br.edu.ifg.luziania.p3.mvc.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import br.edu.ifg.luziania.p3.mvc.model.Usuario;

public class UsuarioDAO {

    /**
     * Verifica se já existe um username cadastrado.
     */
    public boolean existeUsername(String username) {
        String sql = "SELECT 1 FROM usuarios WHERE username = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            registrarLogExcecao("existeUsername", e);
            return true; // Bloqueia por segurança em caso de erro
        }
    }

    /**
     * Verifica se já existe um email cadastrado.
     */
    public boolean existeEmail(String email) {
        String sql = "SELECT 1 FROM usuarios WHERE email = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            registrarLogExcecao("existeEmail", e);
            return true;
        }
    }

    /**
     * Autentica e retorna o objeto Usuario se as credenciais estiverem corretas,
     * ou null caso contrário.
     * Login aceita tanto username quanto email.
     */
    public Usuario autenticarRetornandoUsuario(String login, String senha) {
        String sql = "SELECT id, username, email, caminho_foto " +
                "FROM usuarios " +
                "WHERE (email = ? OR username = ?) AND senha = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, login);
            stmt.setString(3, senha); // TODO: usar hash (ex: BCrypt) em produção

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("caminho_foto")
                );
            }
        } catch (SQLException e) {
            registrarLogExcecao("autenticarRetornandoUsuario", e);
        }
        return null;
    }

    /**
     * Versão booleana mantida para compatibilidade (usada na validação de senha atual em EditarPerfil).
     */
    public boolean autenticar(String login, String senha) {
        return autenticarRetornandoUsuario(login, senha) != null;
    }

    /**
     * Cadastra um novo usuário no banco.
     */
    public boolean cadastrar(String username, String email, String senha) {
        String sql = "INSERT INTO usuarios (username, email, senha) VALUES (?, ?, ?)";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, senha); // TODO: hash antes de salvar

            stmt.executeUpdate();
            registrarLogUso("Usuário '" + username + "' cadastrado com sucesso.");
            return true;

        } catch (SQLException e) {
            registrarLogExcecao("cadastrar", e);
            return false;
        }
    }

    /**
     * Atualiza o email de um usuário.
     */
    public void atualizarEmail(Usuario usuario) {
        String sql = "UPDATE usuarios SET email = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getEmail());
            stmt.setInt(2, usuario.getId());
            stmt.executeUpdate();
            registrarLogUso("Email do usuário id=" + usuario.getId() + " atualizado.");

        } catch (SQLException e) {
            registrarLogExcecao("atualizarEmail", e);
        }
    }

    /**
     * Atualiza a senha de um usuário.
     */
    public void atualizarSenha(Usuario usuario, String novaSenha) {
        String sql = "UPDATE usuarios SET senha = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novaSenha); // TODO: hash
            stmt.setInt(2, usuario.getId());
            stmt.executeUpdate();
            registrarLogUso("Senha do usuário id=" + usuario.getId() + " atualizada.");

        } catch (SQLException e) {
            registrarLogExcecao("atualizarSenha", e);
        }
    }

    /**
     * Atualiza o caminho da foto de perfil.
     */
    public void atualizarFotoPerfil(Usuario usuario) {
        String sql = "UPDATE usuarios SET caminho_foto = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getCaminhoFoto());
            stmt.setInt(2, usuario.getId());
            stmt.executeUpdate();
            registrarLogUso("Foto de perfil do usuário id=" + usuario.getId() + " atualizada.");

        } catch (SQLException e) {
            registrarLogExcecao("atualizarFotoPerfil", e);
        }
    }

    /**
     * Atualiza o código de recuperação de senha gerado para o email informado.
     * O código deve ser salvo temporariamente no banco para comparação posterior.
     */
    public boolean salvarCodigoRecuperacao(String email, String codigo) {
        String sql = "UPDATE usuarios SET codigo_recuperacao = ?, codigo_expiracao = NOW() + INTERVAL '15 minutes' WHERE email = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigo);
            stmt.setString(2, email);
            int linhasAfetadas = stmt.executeUpdate();
            return linhasAfetadas > 0;

        } catch (SQLException e) {
            registrarLogExcecao("salvarCodigoRecuperacao", e);
            return false;
        }
    }

    /**
     * Valida o código de recuperação informado pelo usuário.
     * Retorna o email associado ao código, ou null se inválido/expirado.
     */
    public String validarCodigoRecuperacao(String codigo) {
        String sql = "SELECT email FROM usuarios WHERE codigo_recuperacao = ? AND codigo_expiracao > NOW()";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }

        } catch (SQLException e) {
            registrarLogExcecao("validarCodigoRecuperacao", e);
        }
        return null;
    }

    /**
     * Redefine a senha pelo email após validação do código.
     * Limpa o código de recuperação após uso.
     */
    public boolean redefinirSenhaPorEmail(String email, String novaSenha) {
        String sql = "UPDATE usuarios SET senha = ?, codigo_recuperacao = NULL, codigo_expiracao = NULL WHERE email = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novaSenha); // TODO: hash
            stmt.setString(2, email);
            int linhas = stmt.executeUpdate();
            registrarLogUso("Senha redefinida para o email: " + email);
            return linhas > 0;

        } catch (SQLException e) {
            registrarLogExcecao("redefinirSenhaPorEmail", e);
            return false;
        }
    }

    // --- Logs ---

    private void registrarLogUso(String acao) {
        System.out.println("[DAO LOG] " + acao);
    }

    private void registrarLogExcecao(String metodo, Exception e) {
        System.err.println("[DAO ERRO] UsuarioDAO." + metodo + " - " + e.getMessage());
    }

    public Usuario buscarPorLoginOuEmail(String login) {
        String sql = "SELECT id, username, email, caminho_foto " +
                "FROM usuarios " +
                "WHERE email = ? OR username = ?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, login);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String caminhoFoto = rs.getString("caminho_foto");

                // CORREÇÃO: Previne o erro do FXML definindo um avatar padrão
                // para usuários recém-criados que ainda não possuem foto
                if (caminhoFoto == null || caminhoFoto.trim().isEmpty()) {
                    caminhoFoto = "/br/edu/ifg/luziania/p3/mvc/view/img/novo_usuario.jpg";
                }

                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        caminhoFoto
                );
            }
        } catch (SQLException e) {
            registrarLogExcecao("buscarPorLoginOuEmail", e);
        }

        return null;
    }

    /**
     * Valida se a senha antiga informada corresponde à senha do usuário no banco.
     */
    public boolean validarSenhaAntiga(int id, String senhaAntiga) {
        String sql = "SELECT 1 FROM usuarios WHERE id = ? AND senha = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, senhaAntiga);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            registrarLogExcecao("validarSenhaAntiga", e);
            return false;
        }
    }

    /**
     * Verifica se o username já existe, IGNORANDO o ID do próprio usuário que está editando.
     */
    public boolean existeUsername(String username, int idUsuarioAtual) {
        String sql = "SELECT 1 FROM usuarios WHERE username = ? AND id != ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, idUsuarioAtual);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            registrarLogExcecao("existeUsername(ignorarId)", e);
            return true;
        }
    }

    /**
     * Atualiza o perfil do usuário de forma dinâmica. Atualiza a senha e a foto apenas se informadas.
     */
    public boolean atualizarPerfil(int id, String username, String email, String novaSenha, String caminhoFoto) {
        // Monta a query dinamicamente baseada nos campos preenchidos
        StringBuilder sql = new StringBuilder("UPDATE usuarios SET username = ?, email = ?");

        if (novaSenha != null && !novaSenha.trim().isEmpty()) {
            sql.append(", senha = ?");
        }
        if (caminhoFoto != null && !caminhoFoto.trim().isEmpty()) {
            sql.append(", caminho_foto = ?");
        }
        sql.append(" WHERE id = ?");

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            stmt.setString(index++, username);
            stmt.setString(index++, email);

            if (novaSenha != null && !novaSenha.trim().isEmpty()) {
                stmt.setString(index++, novaSenha); // TODO: Lembre-se de aplicar o Hash aqui depois
            }
            if (caminhoFoto != null && !caminhoFoto.trim().isEmpty()) {
                stmt.setString(index++, caminhoFoto);
            }

            stmt.setInt(index, id);

            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                registrarLogUso("Perfil do usuário id=" + id + " atualizado com sucesso.");
                return true;
            }
            return false;

        } catch (SQLException e) {
            registrarLogExcecao("atualizarPerfil", e);
            return false;
        }
    }
}

package br.edu.ifg.luziania.p3.mvc.dao;

import br.edu.ifg.luziania.p3.mvc.model.Jogo;
import br.edu.ifg.luziania.p3.mvc.model.Jogo.StatusJogo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JogoDAO {

    // Método para carregar o catálogo na StoreController
    public List<Jogo> listarTodos() {
        List<Jogo> jogos = new ArrayList<>();
        String sql = "SELECT * FROM jogos";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String titulo = rs.getString("titulo");
                double preco = rs.getDouble("preco");
                String caminhoImagem = rs.getString("caminho_imagem");
                String statusStr = rs.getString("status");
                long tempoJogado = rs.getLong("tempo_jogado_segundos");

                // Converte a String do banco para o Enum do Java
                StatusJogo status;

                try {
                    // Tenta converter, garantindo que esteja em maiúsculo e sem espaços
                    status = StatusJogo.valueOf(statusStr != null ? statusStr.trim().toUpperCase() : "NAO_COMPRADO");
                } catch (IllegalArgumentException ex) {
                    // Se vier um lixo do banco, assume o padrão para não travar a aplicação
                    status = StatusJogo.NAO_COMPRADO;
                }

                // Instancia o jogo e seta o tempo
                Jogo jogo = new Jogo(id, titulo, preco, caminhoImagem, status);
                jogo.adicionarTempoJogado(tempoJogado);

                jogos.add(jogo);
            }

            registrarLogUso("Listagem de jogos carregada com sucesso.");

        } catch (SQLException e) {
            registrarLogExcecao("Erro ao listar jogos do catálogo", e); // Requisito 6
        }

        return jogos;
    }

    // Método para atualizar se o jogo foi comprado ou instalado
    public void atualizarStatus(Jogo jogo) {
        String sql = "UPDATE jogos SET status = ? WHERE id = ?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, jogo.getStatus().name()); // Salva o Enum como String
            stmt.setInt(2, jogo.getId());

            stmt.executeUpdate();
            registrarLogUso("Status do jogo '" + jogo.getTitulo() + "' atualizado para: " + jogo.getStatus());

        } catch (SQLException e) {
            registrarLogExcecao("Erro ao atualizar status do jogo: " + jogo.getTitulo(), e); // Requisito 6[cite: 2]
        }
    }

    // Método para salvar os segundos jogados quando a sessão for encerrada
    public void atualizarTempoJogado(Jogo jogo) {
        String sql = "UPDATE jogos SET tempo_jogado_segundos = ? WHERE id = ?";

        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, jogo.getTempoJogadoSegundos());
            stmt.setInt(2, jogo.getId());

            stmt.executeUpdate();
            registrarLogUso("Tempo jogado do jogo '" + jogo.getTitulo() + "' atualizado no banco.");

        } catch (SQLException e) {
            registrarLogExcecao("Erro ao atualizar tempo jogado do jogo: " + jogo.getTitulo(), e); // Requisito 6[cite: 2]
        }
    }

    // --- Métodos de Log de Arquivo ---

    private void registrarLogUso(String acao) {
        // Implementar a gravação no arquivo .txt aqui (Requisito 5)[cite: 2]
        System.out.println("[DAO LOG] " + acao);
    }

    private void registrarLogExcecao(String acao, Exception e) {
        // Implementar a gravação no arquivo de erro .txt aqui (Requisito 6)[cite: 2]
        System.err.println("[DAO ERRO] " + acao + " - " + e.getMessage());
    }
}
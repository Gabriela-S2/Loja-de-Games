package br.edu.ifg.luziania.p3.mvc.dao;

import br.edu.ifg.luziania.p3.mvc.model.Jogo;
import br.edu.ifg.luziania.p3.mvc.model.JogoMidia;
import br.edu.ifg.luziania.p3.mvc.util.LogUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JogoDAO {

    // ──────────────────────────────────────────
    //  Listar todos os jogos
    // ──────────────────────────────────────────

    public List<Jogo> listarTodos() {
        List<Jogo> lista = new ArrayList<>();
        String sql = """
            SELECT id, titulo, descricao, preco, caminho_imagem,
                   caminho_imagem_horizontal, caminho_imagem_vertical,
                   status, tempo_jogado_segundos, genero
            FROM jogos
            ORDER BY titulo
            """;
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapearJogo(rs));

        } catch (SQLException e) {
            LogUtil.registrarErro("JogoDAO.listarTodos", e);
        }
        return lista;
    }

    // ──────────────────────────────────────────
    //  Buscar jogo por ID
    // ──────────────────────────────────────────

    public Jogo buscarPorId(int id) {
        String sql = """
            SELECT id, titulo, descricao, preco, caminho_imagem,
                   caminho_imagem_horizontal, caminho_imagem_vertical,
                   status, tempo_jogado_segundos, genero
            FROM jogos WHERE id = ?
            """;
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearJogo(rs);
            }
        } catch (SQLException e) {
            LogUtil.registrarErro("JogoDAO.buscarPorId", e);
        }
        return null;
    }

    // ──────────────────────────────────────────
    //  Atualizar status
    // ──────────────────────────────────────────

    public void atualizarStatus(Jogo jogo) {
        String sql = "UPDATE jogos SET status = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, jogo.getStatus().name());
            ps.setInt(2, jogo.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            LogUtil.registrarErro("JogoDAO.atualizarStatus", e);
        }
    }

    // ──────────────────────────────────────────
    //  Atualizar tempo jogado
    // ──────────────────────────────────────────

    public void atualizarTempoJogado(Jogo jogo) {
        String sql = "UPDATE jogos SET tempo_jogado_segundos = ? WHERE id = ?";
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, jogo.getTempoJogadoSegundos());
            ps.setInt(2, jogo.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            LogUtil.registrarErro("JogoDAO.atualizarTempoJogado", e);
        }
    }

    // ──────────────────────────────────────────
    //  Listar mídias de um jogo (carrossel)
    // ──────────────────────────────────────────

    public List<JogoMidia> listarMidias(int jogoId) {
        List<JogoMidia> lista = new ArrayList<>();
        String sql = """
            SELECT id, jogo_id, tipo, caminho, ordem
            FROM jogo_midias
            WHERE jogo_id = ?
            ORDER BY ordem
            """;
        try (Connection conn = ConexaoDB.getConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, jogoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new JogoMidia(
                            rs.getInt("id"),
                            rs.getInt("jogo_id"),
                            rs.getString("tipo"),
                            rs.getString("caminho"),
                            rs.getInt("ordem")
                    ));
                }
            }
        } catch (SQLException e) {
            LogUtil.registrarErro("JogoDAO.listarMidias", e);
        }
        return lista;
    }

    // ──────────────────────────────────────────
    //  Mapeamento ResultSet → Jogo
    // ──────────────────────────────────────────

    private Jogo mapearJogo(ResultSet rs) throws SQLException {
        Jogo j = new Jogo(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getDouble("preco"),
                rs.getString("caminho_imagem"),
                Jogo.StatusJogo.valueOf(rs.getString("status"))
        );
        j.setDescricao(rs.getString("descricao"));
        j.setGenero(rs.getString("genero"));
        j.setCaminhoImagemHorizontal(rs.getString("caminho_imagem_horizontal"));
        j.setCaminhoImagemVertical(rs.getString("caminho_imagem_vertical"));
        j.setTempoJogadoSegundos(rs.getLong("tempo_jogado_segundos"));
        return j;
    }
}

package br.edu.ifg.luziania.p3.mvc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

public class ConexaoDB {

    public static Connection getConexao() throws SQLException {
        try {

            Dotenv dotenv = Dotenv.configure().load();

            final String URL = dotenv.get("URL");
            final String USUARIO = dotenv.get("USUARIO");
            final String SENHA = dotenv.get("SENHA");

            return DriverManager.getConnection(URL, USUARIO, SENHA);

        } catch (DotenvException e) {
            System.err.println("CRÍTICO: Arquivo .env não encontrado na raiz do projeto ou erro na leitura!");
            throw new RuntimeException("Erro ao carregar variáveis de ambiente", e);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco de dados. Verifique suas credenciais no .env.");
            throw e;
        }
    }
}
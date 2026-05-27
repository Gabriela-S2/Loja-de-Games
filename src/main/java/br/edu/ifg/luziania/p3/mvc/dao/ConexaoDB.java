package br.edu.ifg.luziania.p3.mvc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoDB {
    // Ajuste o nome do banco, usuário e senha conforme sua configuração local
    private static final String URL = "jdbc:postgresql://192.168.110.128:5432/p3";
    private static final String USUARIO = "gabi";
    private static final String SENHA = "1G@briela";

    public static Connection getConexao() throws SQLException {
        try {
            // O driver já está no seu pom.xml
            return DriverManager.getConnection(URL, USUARIO, SENHA);
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao banco: " + e.getMessage());
            throw e;
        }
    }
}
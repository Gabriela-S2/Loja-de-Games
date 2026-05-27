package br.edu.ifg.luziania.p3.mvc.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilitário centralizado de log do sistema Steam Clone.
 *
 * Gera dois arquivos na pasta "logs/" dentro do diretório de execução:
 *   - logs/uso.txt      → ações normais do sistema (login, compra, navegação…)
 *   - logs/erros.txt    → exceções e falhas capturadas
 *
 * Uso (em qualquer DAO ou Controller):
 *   LogUtil.registrarUso("LoginController", "Usuário 'joao' autenticado com sucesso.");
 *   LogUtil.registrarErro("JogoDAO.listarTodos", "Conexão recusada", e);
 */
public class LogUtil {

    // -------------------------------------------------------------------------
    // Configuração
    // -------------------------------------------------------------------------

    /** Pasta onde os arquivos de log serão criados. */
    private static final String PASTA_LOGS = "logs";

    /** Arquivo de log de uso geral (ações normais). */
    private static final String ARQUIVO_USO = PASTA_LOGS + "/uso.txt";

    /** Arquivo de log de erros e exceções. */
    private static final String ARQUIVO_ERROS = PASTA_LOGS + "/erros.txt";

    /** Formato de timestamp usado em todas as entradas. */
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // -------------------------------------------------------------------------
    // Inicialização da pasta de logs
    // -------------------------------------------------------------------------

    static {
        try {
            Path pasta = Paths.get(PASTA_LOGS);
            if (!Files.exists(pasta)) {
                Files.createDirectories(pasta);
            }
        } catch (IOException e) {
            System.err.println("[LogUtil] Não foi possível criar a pasta de logs: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    /**
     * Registra uma ação normal do sistema no arquivo de uso.
     *
     * @param origem  Nome da classe/método que gerou o log (ex.: "LoginController").
     * @param mensagem Descrição da ação realizada.
     */
    public static void registrarUso(String origem, String mensagem) {
        String linha = formatarLinha("USO", origem, mensagem);
        System.out.println(linha);          // mantém saída no console durante o desenvolvimento
        gravarNoArquivo(ARQUIVO_USO, linha);
    }

    /**
     * Registra uma exceção no arquivo de erros, sem stack trace.
     *
     * @param origem   Nome da classe/método que capturou o erro.
     * @param mensagem Descrição do contexto em que ocorreu o erro.
     * @param e        Exceção capturada (pode ser null).
     */
    public static void registrarErro(String origem, String mensagem, Exception e) {
        String detalhe = (e != null) ? e.getMessage() : "sem detalhe";
        String linha = formatarLinha("ERRO", origem, mensagem + " | Causa: " + detalhe);
        System.err.println(linha);
        gravarNoArquivo(ARQUIVO_ERROS, linha);

        // Grava também o stack trace completo no arquivo de erros
        if (e != null) {
            gravarStackTrace(e);
        }
    }

    /**
     * Registra uma exceção no arquivo de erros sem mensagem adicional.
     * Conveniente quando o próprio getMessage() da exceção já é suficiente.
     */
    public static void registrarErro(String origem, Exception e) {
        registrarErro(origem, "Exceção capturada", e);
    }

    /**
     * Registra uma ação de autenticação (login ou logoff) com o nome do usuário.
     *
     * @param username Nome do usuário que realizou a ação.
     * @param acao     Descrição da ação (ex.: "Login realizado", "Logoff realizado").
     */
    public static void registrarAutenticacao(String username, String acao) {
        registrarUso("Autenticação", "Usuário '" + username + "' — " + acao);
    }

    /**
     * Registra uma navegação entre telas.
     *
     * @param origem  Tela de origem.
     * @param destino Tela de destino.
     */
    public static void registrarNavegacao(String origem, String destino) {
        registrarUso("Navegação", "Tela: " + origem + " → " + destino);
    }

    // -------------------------------------------------------------------------
    // Métodos internos
    // -------------------------------------------------------------------------

    /**
     * Formata uma linha de log padronizada:
     * [dd/MM/yyyy HH:mm:ss] [TIPO] [origem] mensagem
     */
    private static String formatarLinha(String tipo, String origem, String mensagem) {
        String timestamp = LocalDateTime.now().format(FORMATO);
        return String.format("[%s] [%s] [%s] %s", timestamp, tipo, origem, mensagem);
    }

    /**
     * Abre o arquivo em modo append e grava a linha.
     * Cria o arquivo se não existir.
     */
    private static synchronized void gravarNoArquivo(String caminho, String linha) {
        try (FileWriter fw = new FileWriter(caminho, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(linha);
        } catch (IOException e) {
            System.err.println("[LogUtil] Falha ao gravar log em '" + caminho + "': " + e.getMessage());
        }
    }

    /**
     * Grava o stack trace completo da exceção no arquivo de erros,
     * precedido por uma linha separadora para facilitar leitura.
     */
    private static synchronized void gravarStackTrace(Exception e) {
        try (FileWriter fw = new FileWriter(ARQUIVO_ERROS, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("--- Stack Trace ---");
            e.printStackTrace(pw);
            pw.println("-------------------");
        } catch (IOException ex) {
            System.err.println("[LogUtil] Falha ao gravar stack trace: " + ex.getMessage());
        }
    }
}
package br.edu.ifg.luziania.p3.mvc.model;

/**
 * Representa um item de mídia (imagem ou vídeo) associado a um jogo.
 * Mapeia a tabela jogo_midias do banco de dados.
 */
public class JogoMidia {

    private int    id;
    private int    jogoId;
    /** "IMAGEM" ou "VIDEO" */
    private String tipo;
    /** Caminho no classpath (recursos) ou URL absoluta */
    private String caminho;
    private int    ordem;

    public JogoMidia() {}

    public JogoMidia(int id, int jogoId, String tipo, String caminho, int ordem) {
        this.id      = id;
        this.jogoId  = jogoId;
        this.tipo    = tipo;
        this.caminho = caminho;
        this.ordem   = ordem;
    }

    public int    getId()         { return id; }
    public int    getJogoId()     { return jogoId; }
    public String getTipo()       { return tipo; }
    public String getCaminho()    { return caminho; }
    public int    getOrdem()      { return ordem; }

    public void   setId(int i)         { this.id = i; }
    public void   setJogoId(int j)     { this.jogoId = j; }
    public void   setTipo(String t)    { this.tipo = t; }
    public void   setCaminho(String c) { this.caminho = c; }
    public void   setOrdem(int o)      { this.ordem = o; }
}

package br.edu.ifg.luziania.p3.mvc.model;

import java.util.List;
import java.util.ArrayList;

public class Jogo {

    public enum StatusJogo { NAO_COMPRADO, COMPRADO, INSTALADO }

    private int         id;
    private String      titulo;
    private String      descricao;
    private double      preco;
    private String      genero;

    // Imagem vertical (card / capa principal)
    private String      caminhoImagem;
    // Imagem horizontal (banners da Store)
    private String      caminhoImagemHorizontal;
    // Imagem vertical alternativa (usada na página de detalhes se disponível)
    private String      caminhoImagemVertical;

    private StatusJogo  status;
    private long        tempoJogadoSegundos;

    // Mídias do carrossel (preenchidas sob demanda pelo DAO)
    private List<JogoMidia> midias = new ArrayList<>();

    public Jogo(int id, String titulo, double preco, String caminhoImagem, StatusJogo status) {
        this.id             = id;
        this.titulo         = titulo;
        this.preco          = preco;
        this.caminhoImagem  = caminhoImagem;
        this.status         = status;
        this.tempoJogadoSegundos = 0;
    }

    // ── Getters / Setters ──

    public int         getId()                          { return id; }
    public String      getTitulo()                      { return titulo; }
    public String      getDescricao()                   { return descricao; }
    public void        setDescricao(String d)           { this.descricao = d; }
    public double      getPreco()                       { return preco; }
    public String      getGenero()                      { return genero; }
    public void        setGenero(String g)              { this.genero = g; }

    public String      getCaminhoImagem()               { return caminhoImagem; }
    public void        setCaminhoImagem(String c)       { this.caminhoImagem = c; }

    public String      getCaminhoImagemHorizontal()     { return caminhoImagemHorizontal; }
    public void        setCaminhoImagemHorizontal(String c) { this.caminhoImagemHorizontal = c; }

    public String      getCaminhoImagemVertical()       { return caminhoImagemVertical; }
    public void        setCaminhoImagemVertical(String c) { this.caminhoImagemVertical = c; }

    public StatusJogo  getStatus()                      { return status; }
    public void        setStatus(StatusJogo s)          { this.status = s; }

    public long        getTempoJogadoSegundos()         { return tempoJogadoSegundos; }
    public void        setTempoJogadoSegundos(long t)   { this.tempoJogadoSegundos = t; }
    public void        adicionarTempoJogado(long seg)   { this.tempoJogadoSegundos += seg; }

    public List<JogoMidia> getMidias()                  { return midias; }
    public void        setMidias(List<JogoMidia> m)     { this.midias = m; }

    public String getTempoFormatado() {
        long h = tempoJogadoSegundos / 3600;
        long m = (tempoJogadoSegundos % 3600) / 60;
        return h > 0 ? h + "h " + m + "m" : m + "m";
    }
}
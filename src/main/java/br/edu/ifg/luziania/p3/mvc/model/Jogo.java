package br.edu.ifg.luziania.p3.mvc.model;

public class Jogo {

    public enum StatusJogo {
        NAO_COMPRADO, COMPRADO, INSTALADO
    }

    private int id;
    private String titulo;
    private double preco;
    private String caminhoImagem;
    private StatusJogo status;
    private long tempoJogadoSegundos;

    public Jogo(int id, String titulo, double preco, String caminhoImagem, StatusJogo status) {
        this.id = id;
        this.titulo = titulo;
        this.preco = preco;
        this.caminhoImagem = caminhoImagem;
        this.status = status;
        this.tempoJogadoSegundos = 0; // Inicializa com 0
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public double getPreco() { return preco; }
    public String getCaminhoImagem() { return caminhoImagem; }
    public StatusJogo getStatus() { return status; }
    public void setStatus(StatusJogo status) { this.status = status; }

    public long getTempoJogadoSegundos() { return tempoJogadoSegundos; }

    public void adicionarTempoJogado(long segundos) {
        this.tempoJogadoSegundos += segundos;
    }

    public String getTempoFormatado() {
        long horas = tempoJogadoSegundos / 3600;
        long minutos = (tempoJogadoSegundos % 3600) / 60;
        if (horas > 0) {
            return horas + "h " + minutos + "m";
        } else {
            return minutos + "m";
        }
    }
}
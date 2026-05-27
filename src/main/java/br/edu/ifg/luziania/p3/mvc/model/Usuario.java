package br.edu.ifg.luziania.p3.mvc.model;

public class Usuario {

    private int id;
    private String username;
    private String email;
    private String senha;          // Armazenada como hash no banco
    private String caminhoFoto;   // Caminho local ou URL da foto de perfil

    public Usuario() {}

    public Usuario(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public Usuario(int id, String username, String email, String caminhoFoto) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.caminhoFoto = caminhoFoto;
    }

    // Getters e Setters
    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getUsername()           { return username; }
    public void setUsername(String u)     { this.username = u; }

    public String getEmail()              { return email; }
    public void setEmail(String email)    { this.email = email; }

    public String getSenha()              { return senha; }
    public void setSenha(String senha)    { this.senha = senha; }

    public String getCaminhoFoto()        { return caminhoFoto; }
    public void setCaminhoFoto(String c)  { this.caminhoFoto = c; }

    @Override
    public String toString() {
        return "Usuario{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}

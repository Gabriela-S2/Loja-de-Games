package br.edu.ifg.luziania.p3.mvc.session;

import br.edu.ifg.luziania.p3.mvc.model.Usuario;

/**
 * Singleton que mantém o usuário autenticado em memória durante toda a sessão.
 * Evita ter que passar o objeto Usuario de tela em tela manualmente.
 *
 * Uso:
 *   SessaoUsuario.getInstancia().setUsuario(usuario);   // ao fazer login
 *   SessaoUsuario.getInstancia().getUsuario();          // em qualquer tela
 *   SessaoUsuario.getInstancia().encerrarSessao();            // ao fazer logoff
 */
public class SessaoUsuario {

        private static SessaoUsuario instancia;
        private Usuario usuarioLogado;

        private SessaoUsuario() {}

        public static SessaoUsuario getInstancia() {
            if (instancia == null) {
                instancia = new SessaoUsuario();
            }
            return instancia;
        }

        public Usuario getUsuario() {
            return usuarioLogado;
        }

        public void setUsuario(Usuario usuario) {
            this.usuarioLogado = usuario;
        }

        public boolean estaLogado() {
            return usuarioLogado != null;
        }

        public void encerrarSessao() {
            usuarioLogado = null;
        }
    }


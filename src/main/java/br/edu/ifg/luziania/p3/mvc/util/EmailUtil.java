package br.edu.ifg.luziania.p3.mvc.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;

public class EmailUtil {

    // Substitua pelo seu e-mail e pela Senha de App gerada no passo anterior
    private static final String EMAIL_REMETENTE = "seu.email@gmail.com";
    private static final String SENHA_APP = "sua_senha_de_aplicativo_aqui";

    public static void enviarEmailRecuperacao(String emailDestino, String codigo) {
        // Configurações do servidor SMTP do Gmail
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // Autenticação
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_REMETENTE, SENHA_APP);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_REMETENTE));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestino));
            message.setSubject("Código de Recuperação de Senha - Biblioteca de Jogos");

            // Corpo do e-mail
            String textoEmail = "Olá!\n\nVocê solicitou a recuperação de senha.\n"
                    + "Seu código é: " + codigo + "\n\n"
                    + "Se você não solicitou isso, ignore este e-mail.";
            message.setText(textoEmail);

            Transport.send(message);
            System.out.println("E-mail enviado com sucesso para: " + emailDestino);

        } catch (MessagingException e) {
            System.err.println("Erro ao enviar e-mail: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
package com.anyservice.user.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends transactional emails (verification codes, password resets) via SMTP. */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** Sends the account verification code to the given email address. */
    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@anyservice.com");
        message.setTo(to);
        message.setSubject("AnyService - Código de Verificação");
        message.setText("Olá!\n\nSeu código de verificação é: " + code + "\n\nEle expira em 15 minutos.");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erro ao enviar email de verificação: " + e.getMessage());
        }
    }

    /** Sends the password recovery code to the given email address. */
    public void sendPasswordResetCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@anyservice.com");
        message.setTo(to);
        message.setSubject("AnyService - Recuperação de Senha");
        message.setText("Olá!\n\nVocê solicitou a recuperação de senha.\nSeu código de recuperação é: " + code + "\n\nEle expira em 15 minutos.");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Erro ao enviar email de recuperação: " + e.getMessage());
        }
    }
}

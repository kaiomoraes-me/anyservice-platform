package com.anyservice.backend.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.phone-number:}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        if (!accountSid.isEmpty() && !authToken.isEmpty()) {
            Twilio.init(accountSid, authToken);
            System.out.println("✅ Twilio inicializado com sucesso!");
        } else {
            System.out.println("⚠️ Twilio NÃO configurado — SMS será apenas logado no console.");
        }
    }

    /**
     * Envia um código de verificação por SMS via Twilio.
     * Se o Twilio não estiver configurado, faz log no console.
     */
    public void sendVerificationCode(String phoneNumber, String code) {
        // Remover espaços do número para formato internacional (ex: "+55 91234..." → "+5591234...")
        String cleanNumber = phoneNumber.replaceAll("\\s+", "");

        if (accountSid.isEmpty() || authToken.isEmpty() || fromPhoneNumber.isEmpty()) {
            // Fallback: log no console (modo desenvolvimento sem Twilio)
            System.out.println("=========================================");
            System.out.println("  SMS SIMULADO (Twilio não configurado)");
            System.out.println("  Para: " + cleanNumber);
            System.out.println("  Código: " + code);
            System.out.println("=========================================");
            return;
        }

        try {
            // Enviar SMS real via Twilio
            Message message = Message.creator(
                    new PhoneNumber(cleanNumber),      // Para
                    new PhoneNumber(fromPhoneNumber),   // De (número Twilio)
                    "AnyService - Seu código de verificação é: " + code + ". Válido por 2 minutos."
            ).create();

            System.out.println("📱 SMS enviado via Twilio! SID: " + message.getSid() + " → " + cleanNumber);
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar SMS pelo Twilio: " + e.getMessage());
            throw new RuntimeException("Número de telefone inválido ou formato incorreto para o país selecionado.");
        }
    }
}

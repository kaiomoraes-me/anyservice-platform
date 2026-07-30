package com.anyservice.backend.service;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.SmsSubmissionResponseMessage;
import com.vonage.client.sms.messages.TextMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Manages sending SMS messages (e.g., phone verification) using the Vonage API. */
@Service
public class SmsService {

    @Value("${vonage.api-key:}")
    private String apiKey;

    @Value("${vonage.api-secret:}")
    private String apiSecret;

    @Value("${vonage.from-name:AnyService}")
    private String fromName;

    private VonageClient vonageClient;

    /** Initializes the Vonage client if API keys are provided. */
    @PostConstruct
    public void init() {
        if (!apiKey.isEmpty() && !apiSecret.isEmpty()) {
            vonageClient = VonageClient.builder()
                    .apiKey(apiKey)
                    .apiSecret(apiSecret)
                    .build();
            System.out.println("✅ Vonage (Nexmo) inicializado com sucesso!");
        } else {
            System.out.println("⚠️ Vonage NÃO configurado — SMS será apenas logado no console.");
        }
    }

    /**
     * Sends a verification code via SMS to the provided phone number.
     * If Vonage is not configured, logs the message to the console for development.
     */
    public void sendVerificationCode(String phoneNumber, String code) {
        String cleanNumber = phoneNumber.replaceAll("\\s+", "");

        if (vonageClient == null) {
            System.out.println("=========================================");
            System.out.println("  SMS SIMULADO (Vonage não configurado)");
            System.out.println("  Para: " + cleanNumber);
            System.out.println("  Código: " + code);
            System.out.println("=========================================");
            return;
        }

        try {
            TextMessage message = new TextMessage(
                    fromName,
                    cleanNumber,
                    "AnyService - Seu código de verificação é: " + code + ". Válido por 2 minutos."
                );

            SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(message);

            for (SmsSubmissionResponseMessage responseMessage : response.getMessages()) {
                if (responseMessage.getStatus() == MessageStatus.OK) {
                    System.out.println("📱 SMS enviado via Vonage! ID: " + responseMessage.getId() + " → " + cleanNumber);
                } else {
                    System.err.println("❌ Vonage erro: " + responseMessage.getErrorText());
                    throw new RuntimeException("Não foi possível enviar o SMS. Verifique o número de telefone.");
                }
            }
        } catch (RuntimeException e) {
            throw e; 
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar SMS pelo Vonage: " + e.getMessage());
            throw new RuntimeException("Número de telefone inválido ou formato incorreto para o país selecionado.");
        }
    }
}

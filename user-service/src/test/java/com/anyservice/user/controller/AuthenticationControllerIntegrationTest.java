package com.anyservice.user.controller;

import com.anyservice.user.model.User;
import com.anyservice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Garante o estado limpo
    }

    @Test
    void shouldRegisterUserWithPendingStatusAndVerificationCode() throws Exception {
        String registerPayload = """
                {
                    "name": "Kaio Silva",
                    "email": "kaio.test@gmail.com",
                    "password": "Password123!",
                    "role": "CLIENT"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Conta criada com sucesso. Verifique seu e-mail para ativar."));

        User savedUser = userRepository.findByEmail("kaio.test@gmail.com").orElseThrow();
        
        assertFalse(savedUser.isEnabled(), "Usuário deve estar desabilitado (PENDING) ao ser criado");
        assertNotNull(savedUser.getVerificationCode(), "Código de verificação deve ser gerado");
        assertEquals(6, savedUser.getVerificationCode().length(), "O código de verificação deve ter exatos 6 dígitos");
    }
}

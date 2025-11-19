/**
 * Pruebas de integración para todos los endpoints de 2FA.
 * Estas pruebas verifican el flujo completo del sistema.
 */
package com.argy.twofactorauth.integration;

import com.argy.twofactorauth.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(locations = "classpath:application-test.properties")
class TwoFactorAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String testUsername = "integration_test_user";
    private static String qrCodeData;
    private static String recoveryCode;

    @SuppressWarnings("null")
    @Test
    @Order(1)
    void testCreateUser_Success() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(testUsername);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.twoFactorEnabled").value(false));
    }

    @Test
    @Order(2)
    void testGetUserStatus_InitiallyDisabled() throws Exception {
        mockMvc.perform(get("/api/users/{username}/status", testUsername))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.twoFactorEnabled").value(false))
                .andExpect(jsonPath("$.remainingRecoveryCodes").value(0));
    }

    @Test
    @Order(3)
    void testEnable2FA_Success() throws Exception {
        Enable2FARequest request = new Enable2FARequest();
        request.setUsername(testUsername);

        @SuppressWarnings("null")
        String response = mockMvc.perform(post("/api/2fa/enable")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode").exists())
                .andExpect(jsonPath("$.recoveryCodes").isArray())
                .andExpect(jsonPath("$.recoveryCodes").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        Enable2FAResponse enableResponse = objectMapper.readValue(response, Enable2FAResponse.class);
        qrCodeData = enableResponse.getQrCode();
        recoveryCode = enableResponse.getRecoveryCodes().get(0);
    }

    @Test
    @Order(4)
    void testGetUserStatus_AfterEnabling() throws Exception {
        mockMvc.perform(get("/api/users/{username}/status", testUsername))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.twoFactorEnabled").value(true))
                .andExpect(jsonPath("$.remainingRecoveryCodes").value(10));
    }

    @SuppressWarnings("null")
    @Test
    @Order(5)
    void testVerify2FA_WithInvalidCode_Fails() throws Exception {
        Verify2FARequest request = new Verify2FARequest();
        request.setUsername(testUsername);
        request.setCode("000000"); // Código inválido

        mockMvc.perform(post("/api/2fa/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false));
    }

    @SuppressWarnings("null")
    @Test
    @Order(6)
    void testVerifyRecoveryCode_Success() throws Exception {
        mockMvc.perform(post("/api/2fa/verify-recovery/{username}", testUsername)
                .contentType("text/plain")
                .content(recoveryCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));
    }

    @SuppressWarnings("null")
    @Test
    @Order(7)
    void testVerifyRecoveryCode_UsedCode_Fails() throws Exception {
        mockMvc.perform(post("/api/2fa/verify-recovery/{username}", testUsername)
                .contentType("text/plain")
                .content(recoveryCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    @Order(8)
    void testRotate2FA_Success() throws Exception {
        mockMvc.perform(post("/api/2fa/rotate/{username}", testUsername))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode").exists())
                .andExpect(jsonPath("$.recoveryCodes").isArray())
                .andExpect(jsonPath("$.recoveryCodes").isNotEmpty());
    }

    @SuppressWarnings("null")
    @Test
    @Order(9)
    void testGetSystemStats_ReturnsData() throws Exception {
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(greaterThan(0)))
                .andExpect(jsonPath("$.usersWith2FA").value(greaterThan(0)))
                .andExpect(jsonPath("$.totalRecoveryCodes").value(greaterThan(0)))
                .andExpect(jsonPath("$.twoFactorAdoptionRate").value(greaterThan(0.0)));
    }

    @Test
    @Order(10)
    void testGetUsersWith2FA_ReturnsList() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[0].twoFactorEnabled").value(true));
    }

    @Test
    @Order(11)
    void testDisable2FA_Success() throws Exception {
        mockMvc.perform(post("/api/2fa/disable/{username}", testUsername))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(12)
    void testGetUserStatus_AfterDisabling() throws Exception {
        mockMvc.perform(get("/api/users/{username}/status", testUsername))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUsername))
                .andExpect(jsonPath("$.twoFactorEnabled").value(false))
                .andExpect(jsonPath("$.remainingRecoveryCodes").value(0));
    }

    @Test
    @Order(13)
    void testDeleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/users/{username}", testUsername))
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(14)
    void testGetUser_AfterDeletion_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/users/{username}", testUsername))
                .andExpect(status().isNotFound());
    }
}
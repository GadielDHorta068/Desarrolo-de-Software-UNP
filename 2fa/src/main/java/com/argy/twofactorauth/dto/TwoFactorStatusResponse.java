/**
 * Respuesta con estado 2FA del usuario.
 */
package com.argy.twofactorauth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorStatusResponse {
    private String username;
    private boolean twoFactorEnabled;
    private int remainingRecoveryCodes;
}
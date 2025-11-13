/**
 * Respuesta con estadísticas del sistema.
 */
package com.argy.twofactorauth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemStatsResponse {
    private long totalUsers;
    private long usersWith2FA;
    private long totalRecoveryCodes;
    private long usedRecoveryCodes;
    private double twoFactorAdoptionRate;
}
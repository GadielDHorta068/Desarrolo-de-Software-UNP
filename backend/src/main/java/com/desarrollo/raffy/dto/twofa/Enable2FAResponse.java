package com.desarrollo.raffy.dto.twofa;

import java.util.List;

public class Enable2FAResponse {
    private String qrCode;
    private List<String> recoveryCodes;

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public List<String> getRecoveryCodes() {
        return recoveryCodes;
    }

    public void setRecoveryCodes(List<String> recoveryCodes) {
        this.recoveryCodes = recoveryCodes;
    }
}


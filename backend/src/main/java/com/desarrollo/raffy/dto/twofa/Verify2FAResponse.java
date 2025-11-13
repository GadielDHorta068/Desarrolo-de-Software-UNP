package com.desarrollo.raffy.dto.twofa;

public class Verify2FAResponse {
    private boolean verified;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}


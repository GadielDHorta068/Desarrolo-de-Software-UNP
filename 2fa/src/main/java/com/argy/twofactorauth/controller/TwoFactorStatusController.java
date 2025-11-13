package com.argy.twofactorauth.controller;

import com.argy.twofactorauth.dto.TwoFactorStatusResponse;
import com.argy.twofactorauth.entity.User;
import com.argy.twofactorauth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/2fa")
public class TwoFactorStatusController {

    private final UserRepository userRepository;

    public TwoFactorStatusController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/status/{username}")
    public ResponseEntity<TwoFactorStatusResponse> status(@PathVariable("username") String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        boolean enabled = user.isEnabled();
        int remaining = 0;
        if (user.getRecoveryCodes() != null) {
            remaining = (int) user.getRecoveryCodes().stream().filter(rc -> !rc.isUsed()).count();
        }
        return ResponseEntity.ok(new TwoFactorStatusResponse(username, enabled, remaining));
    }
}

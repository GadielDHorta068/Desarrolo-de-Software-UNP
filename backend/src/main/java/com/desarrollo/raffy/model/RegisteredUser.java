package com.desarrollo.raffy.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "registered_user")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class RegisteredUser extends User implements UserDetails {

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;
    
    @Column(name = "imagen", columnDefinition = "bytea")
    private byte[] imagen;

    @Column(name = "description", length = 500, nullable = true)
    private String description;

    // Social media links (optional)
    @Column(name = "twitter", length = 255)
    private String twitter;

    @Column(name = "facebook", length = 255)
    private String facebook;

    @Column(name = "instagram", length = 255)
    private String instagram;

    @Column(name = "linkedin", length = 255)
    private String linkedin;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false, name = "account_non_expired")
    private boolean accountNonExpired = true;

    @Column(nullable = false, name = "account_non_locked")
    private boolean accountNonLocked = true;

    @Column(nullable = false, name = "credentials_non_expired")
    private boolean credentialsNonExpired = true;

    public RegisteredUser(String name, String surname, String email, String cellphone, String nickname, String password) {
        super(name, surname, email, cellphone);
        this.nickname = nickname;
        this.password = password;
        this.userType = UserType.NORMAL;
        this.enabled = true;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
    }

    // Implementación de UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userType.name()));
    }

    @Override
    public String getUsername() {
        return this.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getPassword() {
        return this.password;
    }
}
package com.desarrollo.raffy.business.services;

import java.util.List;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.RegisteredUserRepository;
import com.desarrollo.raffy.business.repository.UserFollowerRepository;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.model.UserFollower;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final RegisteredUserRepository registeredUserRepository;
    private final UserFollowerRepository userFollowerRepository;

    private RegisteredUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        return registeredUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario actual no encontrado"));
    }

    public long follow(Long targetUserId) {
        // Crear relación follower -> followed
        RegisteredUser follower = getCurrentUser();
        RegisteredUser followed = registeredUserRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Usuario objetivo no encontrado"));

        if (Objects.equals(follower.getId(), followed.getId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "No puedes seguirte a ti mismo"
            );
        }

        boolean exists = userFollowerRepository.existsByFollower_IdAndFollowed_Id(follower.getId(), followed.getId());
        if (!exists) {
            UserFollower uf = new UserFollower();
            uf.setFollower(follower);
            uf.setFollowed(followed);
            userFollowerRepository.save(uf);
        }
        return userFollowerRepository.countByFollowed_Id(followed.getId());
    }

    @Transactional
    public long unfollow(Long targetUserId) {
        RegisteredUser follower = getCurrentUser();
        RegisteredUser followed = registeredUserRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Usuario objetivo no encontrado"));

        userFollowerRepository.deleteByFollower_IdAndFollowed_Id(follower.getId(), followed.getId());
        return userFollowerRepository.countByFollowed_Id(followed.getId());
    }

    @Transactional(readOnly = true)
    public long getFollowersCount(Long userId) {
        return userFollowerRepository.countByFollowed_Id(userId);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long targetUserId) {
        RegisteredUser follower = getCurrentUser();
        return userFollowerRepository.existsByFollower_IdAndFollowed_Id(follower.getId(), targetUserId);
    }

    @Transactional(readOnly = true)
    public long getFollowingCount(Long userId) {
        return userFollowerRepository.countByFollower_Id(userId);
    }

    @Transactional(readOnly = true)
    public List<String> getFollowersNicknames(Long userId) {
        return userFollowerRepository.findByFollowed_Id(userId).stream()
                .map(uf -> {
                    RegisteredUser u = uf.getFollower();
                    return u != null ? u.getNickname() : null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getFollowingNicknames(Long userId) {
        return userFollowerRepository.findByFollower_Id(userId).stream()
                .map(uf -> {
                    RegisteredUser u = uf.getFollowed();
                    return u != null ? u.getNickname() : null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
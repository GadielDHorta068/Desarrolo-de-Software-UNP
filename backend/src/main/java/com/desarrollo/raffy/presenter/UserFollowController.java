package com.desarrollo.raffy.presenter;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.services.FollowService;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/users")
@Tag(name = "Seguidores", description = "Gestión de seguidores y seguidos para usuarios")
@RequiredArgsConstructor
public class UserFollowController {

    private final FollowService followService;

    @PostMapping("/{userId}/followers")
    @Operation(summary = "Seguir usuario", description = "El usuario actual sigue al usuario especificado y devuelve el total de seguidores")
    public ResponseEntity<Long> follow(@PathVariable Long userId) {
        long count = followService.follow(userId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{userId}/followers")
    @Operation(summary = "Dejar de seguir", description = "El usuario actual deja de seguir al usuario y devuelve el total de seguidores")
    public ResponseEntity<Long> unfollow(@PathVariable Long userId) {
        long count = followService.unfollow(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{userId}/followers/count")
    @Operation(summary = "Cantidad de seguidores", description = "Devuelve la cantidad de seguidores del usuario")
    public ResponseEntity<Long> getFollowersCount(@PathVariable Long userId) {
        long count = followService.getFollowersCount(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{userId}/followers/is-following")
    @Operation(summary = "Está siguiendo", description = "Verifica si el usuario actual sigue al usuario especificado")
    public ResponseEntity<Boolean> isFollowing(@PathVariable Long userId) {
        boolean following = followService.isFollowing(userId);
        return ResponseEntity.ok(following);
    }

    @GetMapping("/{userId}/following/count")
    @Operation(summary = "Cantidad de seguidos", description = "Devuelve la cantidad de usuarios a los que el usuario sigue")
    public ResponseEntity<Long> getFollowingCount(@PathVariable Long userId) {
        long count = followService.getFollowingCount(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{userId}/followers")
    @Operation(summary = "Listado de seguidores", description = "Devuelve los nicknames de los seguidores del usuario")
    public ResponseEntity<List<String>> getFollowersNicknames(@PathVariable Long userId) {
        List<String> users = followService.getFollowersNicknames(userId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}/following")
    @Operation(summary = "Listado de seguidos", description = "Devuelve los nicknames de los usuarios que el usuario sigue")
    public ResponseEntity<List<String>> getFollowingNicknames(@PathVariable Long userId) {
        List<String> users = followService.getFollowingNicknames(userId);
        return ResponseEntity.ok(users);
    }
}
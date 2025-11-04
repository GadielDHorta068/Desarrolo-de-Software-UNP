package com.desarrollo.raffy.presenter;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.business.services.FollowService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserFollowController {

    private final FollowService followService;

    @PostMapping("/{userId}/followers")
    public ResponseEntity<Long> follow(@PathVariable Long userId) {
        long count = followService.follow(userId);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{userId}/followers")
    public ResponseEntity<Long> unfollow(@PathVariable Long userId) {
        long count = followService.unfollow(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<Long> getFollowersCount(@PathVariable Long userId) {
        long count = followService.getFollowersCount(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{userId}/followers/is-following")
    public ResponseEntity<Boolean> isFollowing(@PathVariable Long userId) {
        boolean following = followService.isFollowing(userId);
        return ResponseEntity.ok(following);
    }
}
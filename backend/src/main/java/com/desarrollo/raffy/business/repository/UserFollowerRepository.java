package com.desarrollo.raffy.business.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.desarrollo.raffy.model.UserFollower;

@Repository
public interface UserFollowerRepository extends JpaRepository<UserFollower, Long> {
    long countByFollowed_Id(Long followedId);
    boolean existsByFollower_IdAndFollowed_Id(Long followerId, Long followedId);
    void deleteByFollower_IdAndFollowed_Id(Long followerId, Long followedId);

    List<UserFollower> findByFollowed_Id(Long followedId);
    List<UserFollower> findByFollower_Id(Long followerId);
}
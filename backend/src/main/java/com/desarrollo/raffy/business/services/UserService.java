package com.desarrollo.raffy.business.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.UserRepository;
import com.desarrollo.raffy.model.User;

@Service

public class UserService {
    
    @Autowired
    UserRepository userRepository;
    
    public List<User> findAll() {
        List<User> result = new ArrayList<>();
        userRepository.findAll().forEach(user -> result.add(user));
        return result;
    }

    public User findByEmail(String aNemail) {
        return userRepository.findByEmail(aNemail).orElse(null);
    }

    @Transactional
    public User save(User aUser) {
        return userRepository.save(aUser);
    }
}

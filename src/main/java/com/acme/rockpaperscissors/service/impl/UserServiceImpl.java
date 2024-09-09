package com.acme.rockpaperscissors.service.impl;

import com.acme.rockpaperscissors.dao.User;
import com.acme.rockpaperscissors.repository.UserRepository;
import com.acme.rockpaperscissors.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    @Override
    public User findOrCreateUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setUsername(username);
                return userRepository.save(newUser);
            });
    }
}

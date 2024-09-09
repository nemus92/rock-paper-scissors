package com.acme.rockpaperscissors.service;

import com.acme.rockpaperscissors.dao.User;

public interface UserService {
    User findUserByUsername(String username);

    User findOrCreateUserByUsername(String username);
}

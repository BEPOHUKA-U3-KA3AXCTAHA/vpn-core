package com.vpnservice.service.user;

import com.vpnservice.model.User;

public interface UserService {
    User register(User user);
    User login(String email, String password);
    void buyVPNAccess(String username);
    User findByUsername(String username);
    void delete(Long id);
}

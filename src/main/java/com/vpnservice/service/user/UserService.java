package com.vpnservice.service.user;

import com.vpnservice.model.User;

public interface UserService {
    User register(User user);
    void buyVPNAccess(String username);
    User findByUsername(String username);
}

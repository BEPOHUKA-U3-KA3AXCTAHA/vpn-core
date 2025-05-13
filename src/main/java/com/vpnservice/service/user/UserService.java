package com.vpnservice.service.user;

import com.vpnservice.model.User;
import java.util.List;

public interface UserService {
    User register(User user);
    User login(String email, String password);
    void buyVPNAccess(Long userId);
    String getVPNConfig(Long userId);
    User findById(Long id);
    List<User> findAll();
    void delete(Long id);
}

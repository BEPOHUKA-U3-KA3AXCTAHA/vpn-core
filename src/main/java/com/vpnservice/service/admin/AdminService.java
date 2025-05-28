package com.vpnservice.service.admin;

import com.vpnservice.model.Admin;
import com.vpnservice.model.User;

import java.util.List;

public interface AdminService {
    Admin login(String email, String password);
    void setVPNPrice(Double price);
    void deleteUserVPN(Long userId);
    void createVPNKeyForUser(Long userId);
    List<User> getUsersList();
}

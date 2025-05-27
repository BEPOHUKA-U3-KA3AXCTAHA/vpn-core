package com.vpnservice.service;

import com.vpnservice.model.User;
import com.vpnservice.model.Admin;  // Здесь предполагаем, что у тебя есть сущность Admin
import com.vpnservice.repository.UserRepository;
import com.vpnservice.repository.AdminRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public CustomUserDetailsService(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Пытаемся найти пользователя в AdminRepository (для администраторов)
        Admin admin = adminRepository.findByEmail(email).orElse(null); // Применяем .orElse(null) для явного получения null, если не найдено
        if (admin != null) {
            // Если нашли, возвращаем пользователя с ролью ROLE_ADMIN
            return new org.springframework.security.core.userdetails.User(
                    admin.getEmail(),
                    admin.getPasswordHash(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
            );
        }

        // Если не нашли в AdminRepository, ищем в UserRepository
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Если нашли, возвращаем пользователя с ролью ROLE_USER
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}

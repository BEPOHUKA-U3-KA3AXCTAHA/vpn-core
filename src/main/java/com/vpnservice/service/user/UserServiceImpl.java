package com.vpnservice.service.user;

import com.vpnservice.exception.NotEnoughBalanceException;
import com.vpnservice.exception.NotFoundException;
import com.vpnservice.model.User;
import com.vpnservice.model.VPNKey;
import com.vpnservice.repository.UserRepository;
import com.vpnservice.repository.VPNSettingsRepository;
import com.vpnservice.service.transaction.TransactionService;
import com.vpnservice.service.vpnkey.VPNKeyService;
import com.vpnservice.config.ConfigKeys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VPNKeyService vpnKeyService;

    @Autowired
    private VPNSettingsRepository vpnSettingsRepository;

    @Autowired
    private TransactionService transactionService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setBalance(0.0);
        return userRepository.save(user);
    }

    @Override
    public void buyVPNAccess(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        double price = vpnSettingsRepository.findByKey(ConfigKeys.VPN_PRICE)
                .map(s -> Double.parseDouble(s.getValue()))
                .orElseThrow(() -> new RuntimeException("Цена VPN не задана"));

        if (user.getBalance() < price) {
            throw new NotEnoughBalanceException("Недостаточно средств");
        }

        user.setBalance(user.getBalance() - price);
        userRepository.save(user);

        // Проверяем, есть ли у пользователя VPN-ключ
        Optional<VPNKey> existingKeyOpt = vpnKeyService.getKeyByUser(user.getId());
        if (existingKeyOpt.isPresent()) {
            // Если ключ есть, обновляем дату
            VPNKey existingKey = existingKeyOpt.get();
            existingKey.setCreatedAt(java.time.LocalDateTime.now());
            vpnKeyService.updateKey(existingKey);
        } else {
            // Если ключа нет, создаём новый
            vpnKeyService.generateKey(user.getId());
        }
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}

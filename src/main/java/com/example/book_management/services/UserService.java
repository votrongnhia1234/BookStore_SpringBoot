package com.example.book_management.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.example.book_management.constants.Provider;
import com.example.book_management.models.Role;
import com.example.book_management.models.User;
import com.example.book_management.repositories.IRoleRepository;
import com.example.book_management.repositories.IUserRepository;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IRoleRepository roleRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
    public void save(@NotNull User user) {
        user.setPassword(new BCryptPasswordEncoder()
                .encode(user.getPassword()));
        userRepository.save(user);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
    public void setDefaultRole(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            Role userRole = roleRepository.findByName(Role.USER);
            if (userRole == null) {
                userRole = new Role();
                userRole.setName(Role.USER);
                roleRepository.save(userRole);
            }
            user.getRoles().add(userRole);
            userRepository.save(user);
        });
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getAuthorities())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = { Exception.class, Throwable.class })
    public void saveOauthUser(String email, String name) {
        log.info("saveOauthUser called with email: {}, name: {}", email, name);

        Optional<User> existingUser = userRepository.findByUsername(email);
        if (existingUser.isPresent()) {
            log.info("User already exists with email: {}", email);
            return;
        }

        User user = new User();
        user.setUsername(email);
        user.setEmail(email);
        user.setFullName(name);
        user.setProvider(Provider.GOOGLE.value);
        user.setPassword(new BCryptPasswordEncoder().encode("oauth_" + email));

        Role userRole = roleRepository.findByName(Role.USER);
        if (userRole == null) {
            log.info("USER role not found, creating new one");
            userRole = new Role();
            userRole.setName(Role.USER);
            userRole = roleRepository.save(userRole);
        }
        user.getRoles().add(userRole);

        log.info("Saving new OAuth user: {}", email);
        User savedUser = userRepository.save(user);
        log.info("User saved successfully with ID: {}", savedUser.getId());
    }
}
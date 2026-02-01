package com.example.book_management.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.Collection;

@Service
@Slf4j
public class OAuthService extends DefaultOAuth2UserService {
    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("=== OAuth2 loadUser called ===");
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");

            log.info("OAuth2 email: {}, name: {}", email, name);

            if (email != null) {
                log.info("Saving OAuth user with email: {}", email);
                userService.saveOauthUser(email, name);

                log.info("Attempting to find user by email: {}", email);
                com.example.book_management.models.User user = userService.findByUsername(email)
                        .orElseThrow(() -> {
                            log.error("Failed to find user after save: {}", email);
                            return new OAuth2AuthenticationException("Failed to find user");
                        });

                log.info("User found: {}, roles: {}", email, user.getRoles());

                Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                user.getAuthorities().forEach(grantedAuthority -> {
                    log.info("Adding authority: {}", grantedAuthority.getAuthority());
                    authorities.add(new SimpleGrantedAuthority(grantedAuthority.getAuthority()));
                });

                log.info("Final authorities: {}", authorities);

                return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                        authorities,
                        oAuth2User.getAttributes(),
                        "email");
            }
            log.warn("Email is null from OAuth2User");
            return oAuth2User;
        } catch (Exception e) {
            log.error("Exception in OAuth2 loadUser: ", e);
            throw new OAuth2AuthenticationException("OAuth2 user loading failed: " + e.getMessage());
        }
    }
}

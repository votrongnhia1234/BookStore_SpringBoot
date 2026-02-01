package com.example.book_management.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.book_management.models.User;
import com.example.book_management.services.UserService;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Component
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("Loading OIDC user");

        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        log.info("OIDC user: email={}, name={}", email, name);

        if (email != null) {
            try {
                // Save or get existing user
                userService.saveOauthUser(email, name);

                // Load user with database roles
                User user = userService.findByUsername(email)
                        .orElseThrow(() -> new RuntimeException("Failed to find user: " + email));

                log.info("User loaded from DB: {}, roles: {}", email, user.getRoles());

                // Get authorities from database
                Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
                user.getAuthorities().forEach(grantedAuthority -> {
                    log.info("Adding DB authority: {}", grantedAuthority.getAuthority());
                    authorities.add(new SimpleGrantedAuthority(grantedAuthority.getAuthority()));
                });

                log.info("Final authorities: {}", authorities);

                return new DefaultOidcUser(
                        authorities,
                        oidcUser.getIdToken(),
                        oidcUser.getUserInfo());
            } catch (Exception e) {
                log.error("Error loading OIDC user: ", e);
                throw new OAuth2AuthenticationException("OIDC user loading failed: " + e.getMessage());
            }
        }

        log.warn("Email is null from OIDC user, returning original user");
        return oidcUser;
    }
}

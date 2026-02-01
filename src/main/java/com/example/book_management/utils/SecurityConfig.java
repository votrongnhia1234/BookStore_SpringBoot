package com.example.book_management.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.book_management.services.OAuthService;
import com.example.book_management.services.UserService;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
        private final OAuthService oAuthService;
        private final UserService userService;
        private final CustomOidcUserService customOidcUserService;

        @Bean
        public UserDetailsService userDetailsService() {
                return userService;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider auth = new DaoAuthenticationProvider(userDetailsService());
                auth.setPasswordEncoder(passwordEncoder());
                return auth;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(@NotNull HttpSecurity http) throws Exception {
                return http
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/register"))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/css/**", "/js/**", "/",
                                                                "/login", "/oauth/**", "/register", "/error")
                                                .permitAll()

                                                .requestMatchers("/books/edit/**",

                                                                "/books/add", "/books/delete")
                                                .hasAnyAuthority("ADMIN")
                                                .requestMatchers("/books", "/cart", "/cart/**")
                                                .hasAnyAuthority("ADMIN", "USER")
                                                .requestMatchers("/api/**")
                                                .hasAnyAuthority("ADMIN", "USER")
                                                .anyRequest().authenticated())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login")
                                                .deleteCookies("JSESSIONID")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .permitAll())
                                .formLogin(formLogin -> formLogin
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/")
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .oauth2Login(
                                                oauth2Login -> oauth2Login
                                                                .loginPage("/login")
                                                                .failureUrl("/login?error")
                                                                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                                                                .oidcUserService(customOidcUserService))
                                                                .successHandler(
                                                                                (request, response,
                                                                                                authentication) -> {
                                                                                        response.sendRedirect("/");
                                                                                })
                                                                .permitAll()

                                ).rememberMe(rememberMe -> rememberMe
                                                .key("hutech")
                                                .rememberMeCookieName("hutech")
                                                .tokenValiditySeconds(24 * 60 * 60)
                                                .userDetailsService(userDetailsService()))
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .accessDeniedPage("/403"))
                                .sessionManagement(sessionManagement -> sessionManagement
                                                .maximumSessions(1)
                                                .expiredUrl("/login"))
                                .httpBasic(httpBasic -> httpBasic
                                                .realmName("hutech"))
                                .build();
        }
}

package com.Validation.payments.controller;

import com.Validation.payments.Entity.AppUser;
import com.Validation.payments.pojo.LoginRequest;
import com.Validation.payments.pojo.LoginResponse;
import com.Validation.payments.repository.interfaces.AppUserRepository;
import com.Validation.payments.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import com.Validation.payments.security.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.Validation.payments.pojo.RefreshTokenRequest;
import com.Validation.payments.Entity.AppUser;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            AppUserRepository appUserRepository,
            RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.appUserRepository = appUserRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/refresh")
    public LoginResponse refresh(
            @RequestBody RefreshTokenRequest request) {

        if (request.getRefreshToken() == null
                || request.getRefreshToken().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired refresh token");
        }

        try {
            RefreshTokenService.RefreshTokenRotation rotation =
                    refreshTokenService.rotateRefreshToken(
                            request.getRefreshToken());

            AppUser user = rotation.user();

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            List.of(new SimpleGrantedAuthority(user.getRole()))
                    );

            String newAccessToken =
                    jwtService.generateToken(authentication);

            return new LoginResponse(
                    newAccessToken,
                    "Bearer",
                    900,
                    rotation.refreshToken()
            );

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired refresh token");
        }
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse register(@RequestBody LoginRequest request) {
        String username = request.getUsername() == null
                ? ""
                : request.getUsername().trim();

        String password = request.getPassword();

        if (!username.matches("^[A-Za-z0-9_]{3,50}$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username must be 3–50 characters and contain only letters, numbers, or underscores."
            );
        }

        if (password == null || password.length() < 8) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters."
            );
        }

        if (appUserRepository.existsByUsername(username)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username is already taken."
            );
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");

        AppUser savedUser = appUserRepository.save(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        savedUser.getUsername(),
                        null,
                        List.of(new SimpleGrantedAuthority(savedUser.getRole()))
                );

        String accessToken = jwtService.generateToken(authentication);
        String refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return new LoginResponse(
                accessToken,
                "Bearer",
                900,
                refreshToken
        );
    }
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );
        String username = authentication.getName();

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        String accessToken = jwtService.generateToken(authentication);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new LoginResponse(
                accessToken,
                "Bearer",
                900,
                refreshToken
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody RefreshTokenRequest request) {

        try {
            refreshTokenService.revokeRefreshToken(
                    request.getRefreshToken());

            return ResponseEntity.noContent().build();

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid refresh token");
        }
    }

}

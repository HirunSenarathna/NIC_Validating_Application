package com.mobios.authservice.service.impl;

import com.mobios.authservice.dto.Request.LoginRequest;
import com.mobios.authservice.dto.Request.RegistrationRequest;
import com.mobios.authservice.dto.Response.JwtResponse;
import com.mobios.authservice.dto.Response.RegistrationResponse;
import com.mobios.authservice.entity.User;
import com.mobios.authservice.repository.UserRepository;
import com.mobios.authservice.security.JwtTokenProvider;
import com.mobios.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public JwtResponse login(LoginRequest loginRequest) {
        try {
            // Find user by identifier (username, email, or mobile number)
            User user = userRepository.findByUsername(loginRequest.getIdentifier())
                    .or(() -> userRepository.findByEmail(loginRequest.getIdentifier()))
                    .or(() -> userRepository.findByPhone(loginRequest.getIdentifier()))
                    .orElseThrow(() -> new BadCredentialsException("User not found with the provided identifier"));

            System.out.println(user);

            // Authenticate using the username and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            return JwtResponse.builder()
                    .token(jwt)
                    .id(user.getId())
                    .username(user.getUsername())
                    .firstname(user.getFirstname())
                    .lastname(user.getLastname())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .build();

        } catch (Exception e) {
            throw new BadCredentialsException("Login failed: " + e.getMessage(), e);
        }
    }

    @Override
    public RegistrationResponse registerUser(RegistrationRequest registrationRequest) {

        // Check if username or email already exists
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if(userRepository.existsByPhone(registrationRequest.getPhone())){
            throw new IllegalArgumentException("Phone already exists");
        }

        // Create and save new customer
        User customer = User.builder()
                .firstname(registrationRequest.getFirstname())
                .lastname(registrationRequest.getLastname())
                .email(registrationRequest.getEmail())
                .phone(registrationRequest.getPhone())
                .address(registrationRequest.getAddress())
                .username(registrationRequest.getUsername())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .build();

        User savedUser = userRepository.save(customer);

        System.out.println("saved user "+ savedUser);
        return mapToCustomerResponse(savedUser);
    }

    private RegistrationResponse mapToCustomerResponse(User user) {
        return RegistrationResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .username(user.getUsername())
                .build();

    }
}

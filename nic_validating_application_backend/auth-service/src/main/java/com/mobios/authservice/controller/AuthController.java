package com.mobios.authservice.controller;

import com.mobios.authservice.dto.Request.ForgotPasswordRequest;
import com.mobios.authservice.dto.Request.LoginRequest;
import com.mobios.authservice.dto.Request.RegistrationRequest;
import com.mobios.authservice.dto.Request.ResetPasswordRequest;
import com.mobios.authservice.dto.Response.ForgotPasswordResponse;
import com.mobios.authservice.dto.Response.JwtResponse;
import com.mobios.authservice.dto.Response.RegistrationResponse;
import com.mobios.authservice.dto.Response.ResetPasswordResponse;
import com.mobios.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/test")
    public String test(){
        System.out.println("test");
        return "test" ;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println(loginRequest);
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        System.out.println(registrationRequest + "in auth controller");
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(registrationRequest));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        System.out.println("Forgot password request: " + forgotPasswordRequest);
        return ResponseEntity.ok(authService.forgotPassword(forgotPasswordRequest));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        System.out.println("Reset password request: " + resetPasswordRequest);
        return ResponseEntity.ok(authService.resetPassword(resetPasswordRequest));
    }
}

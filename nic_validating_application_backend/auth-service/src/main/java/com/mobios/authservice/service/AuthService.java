package com.mobios.authservice.service;

import com.mobios.authservice.dto.Request.LoginRequest;
import com.mobios.authservice.dto.Request.RegistrationRequest;
import com.mobios.authservice.dto.Response.JwtResponse;
import com.mobios.authservice.dto.Response.RegistrationResponse;
import com.mobios.authservice.entity.User;

public interface AuthService {

    JwtResponse login(LoginRequest loginRequest);

    RegistrationResponse registerUser(RegistrationRequest registrationRequest);
}

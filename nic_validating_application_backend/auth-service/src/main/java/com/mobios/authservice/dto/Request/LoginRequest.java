package com.mobios.authservice.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Identifier is required (username, email, or mobile number)")
    @Pattern(
            regexp = "^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})|" +
                    "([a-zA-Z0-9_]{3,20})|" +
                    "|(\\+?[0-9]{8,15})$",
            message = "Identifier must be a valid email, username (3-20 alphanumeric characters or underscores), or mobile number (8-15 digits, optional + prefix)"
    )
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}

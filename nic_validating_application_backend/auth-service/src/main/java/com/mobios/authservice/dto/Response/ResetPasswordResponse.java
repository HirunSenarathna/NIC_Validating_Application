package com.mobios.authservice.dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordResponse {

    private String message;
}

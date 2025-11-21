package com.careforall.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateTokenResponse {

    private boolean valid;
    private Long userId;
    private String email;
    private String role;
    private String message;

    public ValidateTokenResponse(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }
}

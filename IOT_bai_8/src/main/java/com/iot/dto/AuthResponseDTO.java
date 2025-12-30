package com.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Authentication response DTO
 * Refactored with Builder pattern and expiresIn field
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private Integer userId;
    private String username;
    private String role;
    private String accessToken;
    private int expiresIn; //  thời gian hết hạn tính của token
}

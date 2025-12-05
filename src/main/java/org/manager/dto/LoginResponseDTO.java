package org.manager.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private String email;
    private String username;
    private  String role;
    private Long companyId;
    private Long userId;
}

package org.manager.mapper;

import org.manager.dto.LoginRequestDTO;
import org.manager.model.LoginRequest;

public class LoginMapper {

    // DTO -> Entity
    public static LoginRequest toEntity(LoginRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(dto.getEmail());
        loginRequest.setPassword(dto.getPassword()); // importante!
        return loginRequest;
    }

    // Entity -> DTO
    public static LoginRequestDTO toDTO(LoginRequest loginRequest) {
        if (loginRequest == null) {
            return null;
        }
        // Retorna sem expor senha
        return new LoginRequestDTO(loginRequest.getEmail(), null);
    }
}

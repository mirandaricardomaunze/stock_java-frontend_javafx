package org.manager.mapper;

import org.manager.dto.CompanyDTO;
import org.manager.dto.UserDTO;
import org.manager.model.User;

public class UserMapper {

    /**
     * Converte User + CompanyDTO em UserDTO pronto para frontend.
     * Adiciona o nome da empresa para exibição na TableView.
     */
    public static UserDTO toDTO(User user, CompanyDTO companyDTO) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setCompanyId(user.getCompanyId());
        dto.setCompanyName(companyDTO != null ? companyDTO.getName() : null); // Campo apenas para frontend
        return dto;
    }

    /**
     * Converte UserDTO → User (para enviar ao backend).
     * O ID da empresa deve estar setado no UserDTO antes de enviar.
     */
    public static User toEntity(UserDTO dto, CompanyDTO company) {
        if (dto == null) return null;

        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setActive(dto.isActive());
        user.setCompanyId(company != null ? company.getId() : dto.getCompanyId());
        return user;
    }

}

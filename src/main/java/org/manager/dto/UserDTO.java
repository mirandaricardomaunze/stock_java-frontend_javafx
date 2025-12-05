package org.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.manager.enums.Role;
import org.manager.model.User;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String password;
    private Role role;
    private boolean active = true;
    private Long companyId;
    private String companyName; // novo campo

    public static UserDTO fromEntity(User user, CompanyDTO company) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setCompanyId(user.getCompanyId());
        dto.setCompanyName(company != null ? company.getName() : null); // aqui
        return dto;
    }

    public User toEntity(){
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setRole(this.role);
        user.setActive(this.active);
        user.setCompanyId(this.companyId);
        return user;
    }
}

package org.manager.model;

import jakarta.persistence.*;
import lombok.*;
import org.manager.enums.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private  boolean active = true;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column (nullable = false)
    private Long companyId;
    private String companyName;
}

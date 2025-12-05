package org.manager.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {
    private Long id;
    private String name;
    private String location;
    private String description;
    private int capacity;
    private String email;
    private String phone;
    private String manager;
    private boolean active;      // indica se está ativo
    private boolean principal;   // indica armazém principal/default
    private Long companyId;
    private String companyName;

}


package org.manager.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseRequestDTO {
    private String name;
    private String location;
    private String description;
    private int capacity;
    private String email;
    private String phone;
    private String manager;
    private boolean active;
    private boolean principal;
    private Long companyId;
}


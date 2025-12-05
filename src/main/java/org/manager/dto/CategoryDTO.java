package org.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor   // construtor vazio
@AllArgsConstructor  // construtor com todos os atributos
public class CategoryDTO {
    private Long id;
    private String name;
    private Long companyId; // id da empresa
    private String companyName;
}

package org.manager.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    private Long id;
    private String name;            // Nome do fornecedor
    private String email;           // Email do fornecedor
    private String phone;           // Telefone do fornecedor
    private String nuit;            // NUIT do fornecedor
    private String address;         // Endereço
    private String website;         // Website (opcional)
    private String notes;
    private Long companyId;
    private String company;
}

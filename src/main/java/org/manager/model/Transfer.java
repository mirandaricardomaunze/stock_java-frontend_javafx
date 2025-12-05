package org.manager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {
    private Long id;

    // Empresa
    private Long companyId;
    private String companyName;

    // Produto
    private Long productId;
    private String productName;

    // Armazéns
    private Long sourceWarehouseId;
    private String sourceWarehouseName;
    private Long destinationWarehouseId;
    private String destinationWarehouseName;

    private int quantity;
    private LocalDateTime transferDate;
    private String reference;
    private String userName;
}

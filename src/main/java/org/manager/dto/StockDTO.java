package org.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {
    private Long id;
    // Produto
    private Long productId;
    private String productName;

    // Armazém
    private Long warehouseId;
    private String warehouseName;

    // Quantidade
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
}

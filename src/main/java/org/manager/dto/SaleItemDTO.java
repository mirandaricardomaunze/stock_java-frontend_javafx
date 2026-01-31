package org.manager.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItemDTO {
    private Long productId;
    private String productName;
    private String productCode; // ADICIONADO
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
    private BigDecimal taxAmount;// ADICIONADO
}

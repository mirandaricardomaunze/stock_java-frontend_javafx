package org.manager.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItemRequestDTO {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice; // Opcional - se null, usar preço padrão do produto
    private BigDecimal subtotal;
    private BigDecimal taxAmount; // NOVO CAMPO

    public BigDecimal getSubtotalWithTax() {
        if (taxAmount == null) return subtotal;
        return subtotal.add(taxAmount);
    }
    // Validação simples
    public boolean isValid() {
        return productId != null && productId > 0 &&
                quantity != null && quantity > 0 &&
                (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) > 0);
    }
}

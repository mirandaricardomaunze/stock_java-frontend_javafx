package org.manager.dto;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleResponseDTO {
    private Long id;
    private String saleCode;
    private String clientName;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal amountPaid;
    private BigDecimal change;
    private String paymentMethod; // ou Enum se quiser consistência
    private String status;        // ou Enum
    private String saleDate;      // pode ser LocalDateTime com @JsonFormat
    private List<SaleItemDTO> items;
    private Long companyId;
    private  Long userId;
    private String userName;

}


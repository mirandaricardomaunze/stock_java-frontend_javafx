package org.manager.dto;

import lombok.*;
import org.manager.enums.PaymentMethod;


import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleRequestDTO {
    private String clientName;
    private PaymentMethod paymentMethod;
    private BigDecimal discount;
    private BigDecimal amountPaid;
    private Long companyId;
    private List<SaleItemRequestDTO> items;

    public boolean isValid() {
        return clientName != null && !clientName.isBlank()
                && paymentMethod != null
                && items != null && !items.isEmpty()
                && discount != null
                && amountPaid != null;
    }

}

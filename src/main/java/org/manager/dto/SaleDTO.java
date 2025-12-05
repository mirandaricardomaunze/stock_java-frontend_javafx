package org.manager.dto;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleDTO {
    private Long id;
    private String saleCode;
    private String clientName;
    private Double totalAmount;
    private Double discount;
    private Double amountPaid;
    private Double change;
    private String paymentMethod;
    private String status;
    private String saleDate;
    private List<SaleItemDTO> items;
    private Long companyId;

}
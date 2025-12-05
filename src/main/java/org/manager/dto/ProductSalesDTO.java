package org.manager.dto;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSalesDTO {
    private String productName;
    private Long unitsSold;
    private BigDecimal revenue;

}

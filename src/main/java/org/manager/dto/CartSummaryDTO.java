package org.manager.dto;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartSummaryDTO {
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private Integer itemCount;
    private List<String> validationErrors;
    private Boolean isValid;

}
package org.manager.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long companyId;
    private String companyName;
    private Long warehouseId;
    private String warehouseName;
    private String customerName;
    private String customerEmail;
    private String customerContact;
    private String customerNuit;
    private String paymentMethod;
    private String deliveryAddress;
    private String status;
    private String notes;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;

    @Builder.Default
    private List<OrderItemDTO> items = new ArrayList<>();

    public void calculateTotalOrder() {
        if (items == null || items.isEmpty()) {
            totalAmount = BigDecimal.ZERO;
            return;
        }
        totalAmount = items.stream()
                .map(OrderItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

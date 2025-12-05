
package org.manager.model;

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
public class Order {
    private Long id;
    private String orderNumber;
    private Long companyId;
    private String companyName;
    private  String deliveryAddress;
    private Long warehouseId;
    private String warehouseName;
    private String customerName;
    private String customerEmail;
    private String customerContact;
    private String customerNuit;
    private String paymentMethod;
    private String status;
    private String notes;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;

    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    public void calculateTotalOrder() {
        if (orderItems == null || orderItems.isEmpty()) {
            totalAmount = BigDecimal.ZERO;
            return;
        }
        totalAmount = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

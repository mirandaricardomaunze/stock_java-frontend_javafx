// OrderItemMapper.java
package org.manager.mapper;

import org.manager.dto.OrderItemDTO;
import org.manager.model.Order;
import org.manager.model.OrderItem;

import java.math.BigDecimal;

public class OrderItemMapper {

    public static OrderItemDTO toDTO(OrderItem item) {
        if (item == null) return null;

        return OrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }

    public static OrderItem toEntity(OrderItemDTO dto, Order order) {
        if (dto == null) return null;

        OrderItem item = OrderItem.builder()
                .id(dto.getId())
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : BigDecimal.ZERO)
                .order(order)
                .build();

        item.calculateTotalPrice();
        return item;
    }
}

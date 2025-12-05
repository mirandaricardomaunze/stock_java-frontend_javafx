package org.manager.mapper;

import org.manager.dto.OrderDTO;
import org.manager.model.Order;

import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDTO toDTO(Order order) {
        if (order == null) return null;

        OrderDTO dto = OrderDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .companyId(order.getCompanyId())
                .companyName(order.getCompanyName())
                .warehouseId(order.getWarehouseId())
                .warehouseName(order.getWarehouseName())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerContact(order.getCustomerContact())
                .customerNuit(order.getCustomerNuit())
                .paymentMethod(order.getPaymentMethod())
                .deliveryAddress(order.getDeliveryAddress()) // ✅ adicionado
                .status(order.getStatus())
                .notes(order.getNotes())
                .orderDate(order.getOrderDate())
                .totalAmount(order.getTotalAmount())
                .items(order.getOrderItems().stream()
                        .map(OrderItemMapper::toDTO)
                        .collect(Collectors.toList()))
                .build();

        return dto;
    }

    public static Order toEntity(OrderDTO dto) {
        if (dto == null) return null;

        Order order = Order.builder()
                .id(dto.getId())
                .orderNumber(dto.getOrderNumber())
                .companyId(dto.getCompanyId())
                .companyName(dto.getCompanyName())
                .warehouseId(dto.getWarehouseId())
                .warehouseName(dto.getWarehouseName())
                .customerName(dto.getCustomerName())
                .customerEmail(dto.getCustomerEmail())
                .customerContact(dto.getCustomerContact())
                .customerNuit(dto.getCustomerNuit())
                .paymentMethod(dto.getPaymentMethod())
                .deliveryAddress(dto.getDeliveryAddress()) // ✅ adicionado
                .status(dto.getStatus())
                .notes(dto.getNotes())
                .orderDate(dto.getOrderDate())
                .totalAmount(dto.getTotalAmount())
                .orderItems(dto.getItems().stream()
                        .map(itemDTO -> OrderItemMapper.toEntity(itemDTO, null))
                        .collect(Collectors.toList()))
                .build();

        order.getOrderItems().forEach(item -> item.setOrder(order));

        return order;
    }
}

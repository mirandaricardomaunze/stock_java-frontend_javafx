package org.manager.mapper;
import org.manager.dto.InvoiceDTO;
import org.manager.model.Invoice;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceMapper {

    // 🔹 Converter Invoice → InvoiceDTO
    public static InvoiceDTO toDTO(Invoice entity) {
        if (entity == null) return null;

        return InvoiceDTO.builder()
                .id(entity.getId())
                .invoiceNumber(entity.getInvoiceNumber())
                .invoiceDate(entity.getInvoiceDate())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .customerName(entity.getCustomerName())
                .deliveryAddress(entity.getDeliveryAddress())
                .customerEmail(entity.getCustomerEmail())
                .customerContact(entity.getCustomerContact())
                .customerNuit(entity.getCustomerNuit())
                .paymentMethod(entity.getPaymentMethod())
                .companyName(entity.getCompanyName())
                .warehouseName(entity.getWarehouseName())
                .orderId(entity.getOrderId())
                .items(entity.getItems()) // já são DTOs
                .notes(entity.getNotes())
                .build();
    }

    // 🔹 Converter InvoiceDTO → Invoice
    public static Invoice toEntity(InvoiceDTO dto) {
        if (dto == null) return null;

        Invoice entity = new Invoice();
        entity.setId(dto.getId());
        entity.setInvoiceNumber(dto.getInvoiceNumber());
        entity.setInvoiceDate(dto.getInvoiceDate());
        entity.setTotalAmount(dto.getTotalAmount());
        entity.setStatus(dto.getStatus());
        entity.setCustomerName(dto.getCustomerName());
        entity.setDeliveryAddress(dto.getDeliveryAddress());
        entity.setCustomerEmail(dto.getCustomerEmail());
        entity.setCustomerContact(dto.getCustomerContact());
        entity.setCustomerNuit(dto.getCustomerNuit());
        entity.setPaymentMethod(dto.getPaymentMethod());
        entity.setCompanyName(dto.getCompanyName());
        entity.setWarehouseName(dto.getWarehouseName());
        entity.setOrderId(dto.getOrderId());
        entity.setItems(dto.getItems()); // já são DTOs
        entity.setNotes(dto.getNotes());
        return entity;
    }

    // 🔹 Converter lista de Invoice → lista de InvoiceDTO
    public static List<InvoiceDTO> toDTOList(List<Invoice> invoices) {
        if (invoices == null) return null;
        return invoices.stream().map(InvoiceMapper::toDTO).collect(Collectors.toList());
    }

    // 🔹 Converter lista de InvoiceDTO → lista de Invoice
    public static List<Invoice> toEntityList(List<InvoiceDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(InvoiceMapper::toEntity).collect(Collectors.toList());
    }
}

package org.manager.model;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.manager.dto.InvoiceItemDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    private Long id;
    private String invoiceNumber;
    private LocalDateTime invoiceDate;
    private BigDecimal totalAmount;
    private String status;
    private String customerName;
    private String deliveryAddress;
    private String customerEmail;
    private String customerContact;
    private String customerNuit;
    private String paymentMethod;
    private String companyName;
    private String warehouseName;
    private Long orderId;
    private List<InvoiceItemDTO> items;
    private String notes;
}

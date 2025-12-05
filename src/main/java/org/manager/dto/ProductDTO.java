package org.manager.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private String sku;
    private String barcode;
    private String referenceNumber;
    private Integer boxes;
    private BigDecimal sellingPrice;
    private BigDecimal costPrice;
    private Integer quantityInStock;
    private Integer minimumStockLevel;
    private String unitOfMeasure;

    private Long companyId;
    private String companyName;

    private Long warehouseId;
    private String warehouseName;

    private Long categoryId;
    private String categoryName;

    private Long supplierId;
    private String supplierName;

    // Campos calculados para exibição
    private String stockDetail;                // ex: "3 caixas + 5 unidades"
    private BigDecimal profitMargin;           // valor unitário
    private BigDecimal profitMarginPercentage; // valor percentual
}

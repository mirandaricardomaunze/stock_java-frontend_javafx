package org.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    // ===================== DADOS BÁSICOS =====================
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

    // ===================== CAMPOS EMPRESARIAIS =====================
    private BigDecimal lastPurchasePrice;
    private BigDecimal averageCost;
    private Integer maximumStockLevel;
    private Integer reorderPoint;
    private Boolean isActive;
    // ===================== AUDITORIA =====================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    // ===================== LOGÍSTICOS =====================
    private String locationCode;
    private Double weight;
    private Double volume;
    private LocalDate expirationDate;
    private String batchNumber;

    // ===================== FISCAIS =====================
    private BigDecimal taxPercentage;
    private Boolean isTaxIncluded;
    private String accountingCode;

    // ===================== CATÁLOGO =====================
    private String brand;
    private String model;
    private String tags;
    private String imageUrl;

    // ===================== RELACIONAMENTOS =====================
    private Long companyId;
    private Long warehouseId;
    private Long categoryId;
    private Long supplierId;

    private String companyName;
    private String warehouseName;
    private String categoryName;
    private String supplierName;

    // ===================== MÉTODOS CALCULADOS =====================
    private Integer fullBoxes;
    private Integer remainingItems;
    private String stockDetail;
    private BigDecimal profitMargin;
    private BigDecimal profitMarginPercentage;
    private Boolean belowMinimum;

}

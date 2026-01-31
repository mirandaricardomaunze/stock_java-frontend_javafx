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

    // ===== Básico =====
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
    private Integer maximumStockLevel;
    private Integer reorderPoint;
    private String unitOfMeasure;
    private Boolean isActive;

    // ===== Logísticos =====
    private String locationCode;
    private Double weight;
    private Double volume;
    private LocalDate expirationDate;
    private String batchNumber;

    // ===== Fiscais =====
    private BigDecimal taxPercentage;
    private Boolean isTaxIncluded;
    private String accountingCode;

    // ===== Catálogo / e-commerce =====
    private String brand;
    private String model;
    private String tags;
    private String imageUrl;

    // ===== Relacionamentos =====
    private Long companyId;
    private String companyName;
    private Long warehouseId;
    private String warehouseName;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;

    // ===== Auditoria =====
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // ===== Campos calculados / avançados =====
    private Integer fullBoxes;
    private Integer remainingItems;
    private String stockDetail;
    private Boolean belowMinimum;
    private BigDecimal profitMargin;
    private BigDecimal profitMarginPercentage;

}

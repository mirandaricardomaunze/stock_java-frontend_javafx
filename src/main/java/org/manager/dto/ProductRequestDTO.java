package org.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
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

    // Empresariais
    private BigDecimal lastPurchasePrice;
    private BigDecimal averageCost;
    private Integer maximumStockLevel;
    private Integer reorderPoint;
    private Boolean isActive;

    // Logísticos
    private String locationCode;
    private Double weight;
    private Double volume;
    private LocalDate expirationDate;
    private String batchNumber;

    // Fiscais
    private BigDecimal taxPercentage;
    private Boolean isTaxIncluded;
    private String accountingCode;

    // Catálogo
    private String brand;
    private String model;
    private String tags;
    private String imageUrl;

    // Relacionamentos (IDs apenas)
    private Long companyId;
    private Long warehouseId;
    private Long categoryId;
    private Long supplierId;

}
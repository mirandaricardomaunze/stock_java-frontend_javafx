package org.manager.model;

import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private Long id;

    // ===================== DADOS BÁSICOS =====================
    private String name;
    private String description;
    private String sku;
    private String barcode;
    private String referenceNumber;
    private Integer boxes; // Itens por caixa
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
    private Boolean isActive = true;

    // ===================== CAMPOS LOGÍSTICOS =====================
    private String locationCode;
    private Double weight;
    private Double volume;
    private LocalDate expirationDate;
    private String batchNumber;

    // ===================== CAMPOS FISCAIS / FINANCEIROS =====================
    private BigDecimal taxPercentage;
    private Boolean isTaxIncluded;
    private String accountingCode;

    // ===================== E-COMMERCE / CATÁLOGO =====================
    private String brand;
    private String model;
    private String tags;
    private String imageUrl;

    // ===================== RELACIONAMENTOS (apenas IDs + nomes) =====================
    private Long companyId;
    private String companyName;

    private Long warehouseId;
    private String warehouseName;

    private Long categoryId;
    private String categoryName;

    private Long supplierId;
    private String supplierName;

    // ===================== AUDITORIA =====================
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // ===================== MÉTODOS AVANÇADOS =====================
    public int getFullBoxes() {
        if (boxes == null || boxes <= 0 || quantityInStock == null) return 0;
        return quantityInStock / boxes;
    }

    public int getRemainingItems() {
        if (boxes == null || boxes <= 0 || quantityInStock == null) return quantityInStock != null ? quantityInStock : 0;
        return quantityInStock % boxes;
    }

    public String getStockDetail() {
        return String.format("%d caixas + %d %s", getFullBoxes(), getRemainingItems(), unitOfMeasure);
    }

    public boolean isBelowMinimum() {
        if (quantityInStock == null || minimumStockLevel == null) return false;
        return quantityInStock < minimumStockLevel;
    }

    public BigDecimal getProfitMargin() {
        if (sellingPrice == null || costPrice == null) return BigDecimal.ZERO;
        return sellingPrice.subtract(costPrice).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getProfitMarginPercentage() {
        if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0 || sellingPrice == null) return BigDecimal.ZERO;
        return sellingPrice.subtract(costPrice)
                .divide(costPrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
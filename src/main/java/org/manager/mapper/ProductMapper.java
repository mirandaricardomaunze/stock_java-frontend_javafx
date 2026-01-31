package org.manager.mapper;

import org.manager.dto.ProductRequestDTO;
import org.manager.dto.ProductResponseDTO;
import org.manager.model.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProductMapper {

    // ===================== REQUEST → MODEL =====================
    public static Product toModel(ProductRequestDTO dto) {
        if (dto == null) return null;

        return Product.builder()
                .id(null) // ID gerado no backend
                .name(dto.getName())
                .description(dto.getDescription())
                .sku(dto.getSku())
                .barcode(dto.getBarcode())
                .referenceNumber(dto.getReferenceNumber())
                .boxes(dto.getBoxes())
                .sellingPrice(dto.getSellingPrice())
                .costPrice(dto.getCostPrice())
                .quantityInStock(dto.getQuantityInStock())
                .minimumStockLevel(dto.getMinimumStockLevel())
                .maximumStockLevel(dto.getMaximumStockLevel())
                .reorderPoint(dto.getReorderPoint())
                .unitOfMeasure(dto.getUnitOfMeasure())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true) // padrão ativo

                // Logísticos
                .locationCode(dto.getLocationCode())
                .weight(dto.getWeight())
                .volume(dto.getVolume())
                .expirationDate(dto.getExpirationDate())
                .batchNumber(dto.getBatchNumber())

                // Fiscais
                .taxPercentage(dto.getTaxPercentage())
                .isTaxIncluded(dto.getIsTaxIncluded())
                .accountingCode(dto.getAccountingCode())

                // Catálogo / e-commerce
                .brand(dto.getBrand())
                .model(dto.getModel())
                .tags(dto.getTags())
                .imageUrl(dto.getImageUrl())

                // Relacionamentos (IDs)
                .companyId(dto.getCompanyId())
                .warehouseId(dto.getWarehouseId())
                .categoryId(dto.getCategoryId())
                .supplierId(dto.getSupplierId())
                .build();
    }

    // ===================== UPDATE EXISTENTE =====================
    public static void updateModel(ProductRequestDTO dto, Product product) {
        if (dto == null || product == null) return;

        if (dto.getName() != null) product.setName(dto.getName());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getSku() != null) product.setSku(dto.getSku());
        if (dto.getBarcode() != null) product.setBarcode(dto.getBarcode());
        if (dto.getReferenceNumber() != null) product.setReferenceNumber(dto.getReferenceNumber());
        if (dto.getBoxes() != null) product.setBoxes(dto.getBoxes());
        if (dto.getSellingPrice() != null) product.setSellingPrice(dto.getSellingPrice());
        if (dto.getCostPrice() != null) product.setCostPrice(dto.getCostPrice());
        if (dto.getQuantityInStock() != null) product.setQuantityInStock(dto.getQuantityInStock());
        if (dto.getMinimumStockLevel() != null) product.setMinimumStockLevel(dto.getMinimumStockLevel());
        if (dto.getMaximumStockLevel() != null) product.setMaximumStockLevel(dto.getMaximumStockLevel());
        if (dto.getReorderPoint() != null) product.setReorderPoint(dto.getReorderPoint());
        if (dto.getUnitOfMeasure() != null) product.setUnitOfMeasure(dto.getUnitOfMeasure());
        if (dto.getIsActive() != null) product.setIsActive(dto.getIsActive());

        // Logísticos
        if (dto.getLocationCode() != null) product.setLocationCode(dto.getLocationCode());
        if (dto.getWeight() != null) product.setWeight(dto.getWeight());
        if (dto.getVolume() != null) product.setVolume(dto.getVolume());
        if (dto.getExpirationDate() != null) product.setExpirationDate(dto.getExpirationDate());
        if (dto.getBatchNumber() != null) product.setBatchNumber(dto.getBatchNumber());

        // Fiscais
        if (dto.getTaxPercentage() != null) product.setTaxPercentage(dto.getTaxPercentage());
        if (dto.getIsTaxIncluded() != null) product.setIsTaxIncluded(dto.getIsTaxIncluded());
        if (dto.getAccountingCode() != null) product.setAccountingCode(dto.getAccountingCode());

        // Catálogo
        if (dto.getBrand() != null) product.setBrand(dto.getBrand());
        if (dto.getModel() != null) product.setModel(dto.getModel());
        if (dto.getTags() != null) product.setTags(dto.getTags());
        if (dto.getImageUrl() != null) product.setImageUrl(dto.getImageUrl());

        // Relacionamentos
        if (dto.getCompanyId() != null) product.setCompanyId(dto.getCompanyId());
        if (dto.getWarehouseId() != null) product.setWarehouseId(dto.getWarehouseId());
        if (dto.getCategoryId() != null) product.setCategoryId(dto.getCategoryId());
        if (dto.getSupplierId() != null) product.setSupplierId(dto.getSupplierId());
    }

    // ===================== MODEL → RESPONSE =====================
    public static ProductResponseDTO toResponseDTO(Product product) {
        if (product == null) return null;

        ProductResponseDTO dto = new ProductResponseDTO();

        // Básico
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setSku(product.getSku());
        dto.setBarcode(product.getBarcode());
        dto.setReferenceNumber(product.getReferenceNumber());
        dto.setBoxes(product.getBoxes());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setCostPrice(product.getCostPrice());
        dto.setQuantityInStock(product.getQuantityInStock());
        dto.setMinimumStockLevel(product.getMinimumStockLevel());
        dto.setMaximumStockLevel(product.getMaximumStockLevel());
        dto.setReorderPoint(product.getReorderPoint());
        dto.setUnitOfMeasure(product.getUnitOfMeasure());
        dto.setIsActive(product.getIsActive());

        // Logísticos
        dto.setLocationCode(product.getLocationCode());
        dto.setWeight(product.getWeight());
        dto.setVolume(product.getVolume());
        dto.setExpirationDate(product.getExpirationDate());
        dto.setBatchNumber(product.getBatchNumber());

        // Fiscais
        dto.setTaxPercentage(product.getTaxPercentage());
        dto.setIsTaxIncluded(product.getIsTaxIncluded());
        dto.setAccountingCode(product.getAccountingCode());

        // Catálogo / e-commerce
        dto.setBrand(product.getBrand());
        dto.setModel(product.getModel());
        dto.setTags(product.getTags());
        dto.setImageUrl(product.getImageUrl());

        // Relacionamentos
        dto.setCompanyId(product.getCompanyId());
        dto.setCompanyName(product.getCompanyName());
        dto.setWarehouseId(product.getWarehouseId());
        dto.setWarehouseName(product.getWarehouseName());
        dto.setCategoryId(product.getCategoryId());
        dto.setCategoryName(product.getCategoryName());
        dto.setSupplierId(product.getSupplierId());
        dto.setSupplierName(product.getSupplierName());

        // Auditoria
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setCreatedBy(product.getCreatedBy());
        dto.setUpdatedBy(product.getUpdatedBy());

        // Campos calculados / avançados
        dto.setFullBoxes(product.getFullBoxes());
        dto.setRemainingItems(product.getRemainingItems());
        dto.setStockDetail(product.getStockDetail());
        dto.setBelowMinimum(product.isBelowMinimum()); // correção
        dto.setProfitMargin(product.getProfitMargin());
        dto.setProfitMarginPercentage(product.getProfitMarginPercentage());

        return dto;
    }
}

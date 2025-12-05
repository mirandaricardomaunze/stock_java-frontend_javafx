package org.manager.mapper;

import org.manager.dto.StockDTO;
import org.manager.dto.StockRequestDTO;
import org.manager.model.Product;
import org.manager.model.Stock;
import org.manager.model.Warehouse;

public class StockMapper {


    // ================================
// Entity -> DTO
// ================================
    public StockDTO toDTO(Stock stock) {
        if (stock == null) return null;

        StockDTO dto = new StockDTO();
        dto.setId(stock.getId());

        // Produto
        dto.setProductId(stock.getProductId());
        dto.setProductName(stock.getProductName());

        // Armazém
        dto.setWarehouseId(stock.getWarehouseId());
        dto.setWarehouseName(stock.getWarehouseName());

        dto.setQuantity(stock.getQuantity());

        return dto;
    }

    // ================================
// RequestDTO -> Entity (para criar novo)
// ================================
    public Stock toEntity(StockRequestDTO dto, Product product, Warehouse warehouse) {
        Stock stock= new Stock();
                stock.setProductName(String.valueOf(product.getName()));
                stock.setWarehouseName(warehouse.getName());
                dto.setQuantity(dto.getQuantity());

        return stock;
    }

    // ================================
// Atualizar Entity existente
// ================================
    public void updateEntity(Stock stock, StockRequestDTO dto, Product product, Warehouse warehouse) {
        stock.setProductName(product.getName());
        stock.setWarehouseName(warehouse.getName());
        stock.setQuantity(dto.getQuantity());
    }


}

package org.manager.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockRequestDTO {
    private Long id;
    private Long productId;
    private Long warehouseId;
    private Integer quantity;
}

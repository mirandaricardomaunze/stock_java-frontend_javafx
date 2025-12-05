package org.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranferRequestDTO {
    private Long id;
    private Long productId;
    private Long companyId;
    private Long sourceWarehouseId;
    private Long destinationWarehouseId;
    private Integer quantity;
    private String reference; // opcional
}

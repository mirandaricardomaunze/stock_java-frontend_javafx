package org.manager.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Stock {
    private Long id;

    private Long productId;
    private String productName;

    private Long warehouseId;
    private String warehouseName;

    private Integer quantity;
}

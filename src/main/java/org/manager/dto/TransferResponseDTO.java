package org.manager.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDTO {
    private Long id;
    private Long productId;                // novo
    private String productName;
    private Long sourceWarehouseId;        // novo
    private String sourceWarehouse;
    private Long destinationWarehouseId;   // novo
    private String destinationWarehouse;
    private Long companyId;
    private String companyName;
    private Integer quantity;
    private StockDTO stockSource;
    private StockDTO stockDestination;
    private LocalDateTime transferDate;
    private String reference;
    private Long userId;
    private String user;
}


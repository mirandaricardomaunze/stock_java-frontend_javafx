package org.manager.dto;

import lombok.*;
import java.time.LocalDateTime;

// DTO para receber dados do frontend
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementRequestDTO {

    private String description;
    private String type;       // pode ser MovementType em String
    private String origin;     // MovementOrigin em String
    private String status;     // MovementStatusType em String
    private Integer quantity;
    private LocalDateTime date;
    private String userId;
    private String username;
    private String referenceNumber;
    private Long companyId;
    private Long warehouseId;
    private Long productId;
}

package org.manager.dto;

import lombok.*;
import java.time.LocalDateTime;

// DTO para enviar dados para o frontend
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementResponseDTO {

    private Long id;
    private String description;
    private String type;
    private String origin;
    private String status;
    private Integer quantity;
    private LocalDateTime date;
    private String userId;
    private String username;
    private String referenceNumber;
    private Long companyId;
    private Long warehouseId;
    private Long productId;
}

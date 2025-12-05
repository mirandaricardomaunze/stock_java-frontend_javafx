package org.manager.dto;

import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSummaryDTO {
    private String name; // Produto ou Armazém
    private Integer totalQuantity;
}


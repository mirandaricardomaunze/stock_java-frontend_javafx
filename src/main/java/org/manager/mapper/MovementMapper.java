package org.manager.mapper;

import org.manager.dto.MovementResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class MovementMapper {

    /**
     * Mapeia um MovementResponseDTO para outro MovementResponseDTO.
     * Útil se você quiser clonar ou aplicar ajustes antes de enviar para o frontend.
     */
    public MovementResponseDTO map(MovementResponseDTO dto) {
        if (dto == null) return null;

        return MovementResponseDTO.builder()
                .id(dto.getId())
                .description(dto.getDescription())
                .type(dto.getType()) // já vem em português do backend
                .origin(dto.getOrigin()) // já vem em português
                .status(dto.getStatus()) // já vem em português
                .quantity(dto.getQuantity())
                .date(dto.getDate())
                .userId(dto.getUserId())
                .username(dto.getUsername()) // campo de usuário preenchido
                .referenceNumber(dto.getReferenceNumber())
                .companyId(dto.getCompanyId())
                .warehouseId(dto.getWarehouseId())
                .productId(dto.getProductId())
                .build();
    }

    /**
     * Mapeia uma lista de MovementResponseDTOs para uma nova lista.
     */
    public List<MovementResponseDTO> mapList(List<MovementResponseDTO> list) {
        if (list == null) return null;
        return list.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}

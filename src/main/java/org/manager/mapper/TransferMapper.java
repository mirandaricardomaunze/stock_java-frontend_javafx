package org.manager.mapper;

import org.manager.dto.TranferRequestDTO;
import org.manager.dto.TransferResponseDTO;
import org.manager.model.Transfer;

import java.time.LocalDateTime;

public class TransferMapper {


    // REQUEST DTO → FRONTEND ENTITY
    public static Transfer toFrontendEntity(
            TranferRequestDTO dto,
            String productName,
            String sourceWarehouseName,
            String destinationWarehouseName,
            String userName,
            String companyName
    ) {
        if (dto == null) return null;

        return Transfer.builder()
                .id(dto.getId()) // se tiver
                .companyId(dto.getCompanyId())
                .companyName(companyName != null ? companyName : "")
                .productId(dto.getProductId())
                .productName(productName != null ? productName : "")
                .sourceWarehouseId(dto.getSourceWarehouseId())
                .sourceWarehouseName(sourceWarehouseName != null ? sourceWarehouseName : "")
                .destinationWarehouseId(dto.getDestinationWarehouseId())
                .destinationWarehouseName(destinationWarehouseName != null ? destinationWarehouseName : "")
                .quantity(dto.getQuantity() >= 0 ? dto.getQuantity() : 0)
                .transferDate(LocalDateTime.now())
                .reference(dto.getReference() != null ? dto.getReference() : "")
                .userId(dto.getUserId())
                .userName(userName != null ? userName : "")
                .build();
    }

    // ENTIDADE → RESPONSE DTO
    public static TransferResponseDTO toResponseDTO(Transfer transfer) {
        if (transfer == null) return null;

        TransferResponseDTO dto = new TransferResponseDTO();
        dto.setId(transfer.getId());
        dto.setCompanyName(transfer.getCompanyName());
        dto.setProductId(transfer.getProductId());
        dto.setProductName(transfer.getProductName());
        dto.setSourceWarehouseId(transfer.getSourceWarehouseId());
        dto.setSourceWarehouse(transfer.getSourceWarehouseName());
        dto.setDestinationWarehouseId(transfer.getDestinationWarehouseId());
        dto.setDestinationWarehouse(transfer.getDestinationWarehouseName());
        dto.setQuantity(Math.max(transfer.getQuantity(), 0));
        dto.setTransferDate(transfer.getTransferDate());
        dto.setReference(transfer.getReference() != null ? transfer.getReference() : "");
        dto.setUserId(transfer.getUserId());
        dto.setUser(transfer.getUserName() != null ? transfer.getUserName() : "");
        return dto;
    }


}

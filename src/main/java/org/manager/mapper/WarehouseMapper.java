package org.manager.mapper;

import org.manager.model.Warehouse;
import org.manager.dto.WarehouseRequestDTO;
import org.manager.dto.WarehouseResponseDTO;

public class WarehouseMapper {

    // RequestDTO -> Model
    public static Warehouse toModel(WarehouseRequestDTO dto) {
        if (dto == null) return null;

        Warehouse warehouse = new Warehouse();
        warehouse.setName(dto.getName());
        warehouse.setLocation(dto.getLocation());
        warehouse.setDescription(dto.getDescription());
        warehouse.setCapacity(dto.getCapacity());
        warehouse.setEmail(dto.getEmail());
        warehouse.setPhone(dto.getPhone());
        warehouse.setManager(dto.getManager());
        warehouse.setActive(dto.isActive());
        warehouse.setPrincipal(dto.isPrincipal());
        warehouse.setCompanyId(dto.getCompanyId());
        return warehouse;
    }

    // Model -> ResponseDTO
    public static WarehouseResponseDTO toResponseDTO(Warehouse model) {
        if (model == null) return null;

        WarehouseResponseDTO dto = new WarehouseResponseDTO();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setLocation(model.getLocation());
        dto.setDescription(model.getDescription());
        dto.setCapacity(model.getCapacity());
        dto.setEmail(model.getEmail());
        dto.setPhone(model.getPhone());
        dto.setManager(model.getManager());
        dto.setActive(model.isActive());
        dto.setPrincipal(model.isPrincipal());
        dto.setCompanyId(model.getCompanyId());
        dto.setCompanyName(model.getCompanyName());
        return dto;
    }

    // ResponseDTO -> Model
    public static Warehouse toModel(WarehouseResponseDTO dto) {
        if (dto == null) return null;

        Warehouse warehouse = new Warehouse();
        warehouse.setId(dto.getId());
        warehouse.setName(dto.getName());
        warehouse.setLocation(dto.getLocation());
        warehouse.setDescription(dto.getDescription());
        warehouse.setCapacity(dto.getCapacity());
        warehouse.setEmail(dto.getEmail());
        warehouse.setPhone(dto.getPhone());
        warehouse.setManager(dto.getManager());
        warehouse.setActive(dto.isActive());
        warehouse.setPrincipal(dto.isPrincipal());
        warehouse.setCompanyId(dto.getCompanyId());
        warehouse.setCompanyName(dto.getCompanyName());
        return warehouse;
    }

    // Model -> RequestDTO
    public static WarehouseRequestDTO toRequestDTO(Warehouse model) {
        if (model == null) return null;

        WarehouseRequestDTO dto = new WarehouseRequestDTO();
        dto.setName(model.getName());
        dto.setLocation(model.getLocation());
        dto.setDescription(model.getDescription());
        dto.setCapacity(model.getCapacity());
        dto.setEmail(model.getEmail());
        dto.setPhone(model.getPhone());
        dto.setManager(model.getManager());
        dto.setActive(model.isActive());
        dto.setPrincipal(model.isPrincipal());
        dto.setCompanyId(model.getCompanyId());
        return dto;
    }
}

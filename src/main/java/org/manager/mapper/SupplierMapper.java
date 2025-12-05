package org.manager.mapper;

import org.manager.dto.SupplierDTO;
import org.manager.model.Supplier;

public class SupplierMapper {
    public static SupplierDTO toDTO(Supplier supplier){
        if (supplier == null) return null;
            SupplierDTO supplierDTO=new SupplierDTO();
            supplierDTO.setId(supplier.getId());
            supplierDTO.setName(supplier.getName());
            supplierDTO.setEmail(supplier.getEmail());
            supplierDTO.setPhone(supplier.getPhone());
            supplierDTO.setNuit(supplier.getNuit());
            supplierDTO.setAddress(supplier.getAddress());
            supplierDTO.setWebsite(supplier.getWebsite());
            supplierDTO.setNotes(supplier.getNotes());
            supplierDTO.setCompanyId(supplier.getCompanyId());
            supplierDTO.setCompany(supplier.getCompany());
            return supplierDTO;
    }

    public static Supplier toEntity(SupplierDTO supplierDTO){
        if (supplierDTO == null) return null;
        Supplier supplier=new Supplier();
        supplier.setId(supplierDTO.getId());
        supplier.setName(supplierDTO.getName());
        supplier.setEmail(supplierDTO.getEmail());
        supplier.setPhone(supplierDTO.getPhone());
        supplier.setNuit(supplierDTO.getNuit());
        supplier.setAddress(supplierDTO.getAddress());
        supplier.setWebsite(supplierDTO.getWebsite());
        supplier.setNotes(supplierDTO.getNotes());
        supplier.setCompanyId(supplierDTO.getCompanyId());
        supplier.setCompany(supplierDTO.getCompany());
        return supplier;
    }
}

package org.manager.mapper;

import org.manager.dto.CategoryDTO;
import org.manager.model.Category;

public class CategoryMapper {

    public static CategoryDTO toDTO(Category category) {
        if (category == null) return null;
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setCompanyId(category.getCompanyId());
        dto.setCompanyName(category.getCompanyName());
        return dto;
    }

    public static Category toEntity(CategoryDTO dto) {
        if (dto == null) return null;
        Category entity = new Category();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setCompanyId(dto.getCompanyId());
        entity.setCompanyName(dto.getCompanyName());
        return entity;
    }
}

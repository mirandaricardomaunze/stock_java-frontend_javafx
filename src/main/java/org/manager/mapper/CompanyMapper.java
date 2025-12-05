package org.manager.mapper;
import org.manager.dto.CompanyDTO;
import org.manager.model.Company;

public class CompanyMapper {

    public static CompanyDTO toDTO(Company company) {
        if (company == null) return null;

        return CompanyDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .address(company.getAddress())
                .email(company.getEmail())
                .phoneNumber(company.getPhoneNumber())
                .taxId(company.getTaxId())
                .website(company.getWebsite())
                .description(company.getDescription())
                .registrationNumber(company.getRegistrationNumber())
                .logoUrl(company.getLogoUrl())
                .country(company.getCountry())
                .city(company.getCity())
                .postalCode(company.getPostalCode())
                .industry(company.getIndustry())
                .contactEmail(company.getContactEmail())
                .contactPhone(company.getContactPhone())
                .build();
    }

    public static Company toEntity(CompanyDTO dto) {
        if (dto == null) return null;

        return Company.builder()
                .id(dto.getId())
                .name(dto.getName())
                .address(dto.getAddress())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .taxId(dto.getTaxId())
                .website(dto.getWebsite())
                .description(dto.getDescription())
                .registrationNumber(dto.getRegistrationNumber())
                .logoUrl(dto.getLogoUrl())
                .country(dto.getCountry())
                .city(dto.getCity())
                .postalCode(dto.getPostalCode())
                .industry(dto.getIndustry())
                .contactEmail(dto.getContactEmail())
                .contactPhone(dto.getContactPhone())
                .build();
    }
}

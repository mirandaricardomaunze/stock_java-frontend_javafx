package org.manager.dto;

import lombok.*;

/**
 * DTO usado para transferência de dados da Company.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDTO {

    private  Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String website;
    private String taxId;
    private String registrationNumber;
    private String logoUrl;
    private String description;
    private String country;
    private String city;
    private String postalCode;
    private String industry;
    private String contactEmail;
    private String contactPhone;
}

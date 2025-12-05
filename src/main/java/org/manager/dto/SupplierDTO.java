package org.manager.dto;
import lombok.*;
import org.manager.model.Company;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private String email;
    @NonNull
    private String phone;
    @NonNull
    private String nuit;
    @NonNull
    private String address;
    @NonNull
    private String website;
    @NonNull
    private String notes;
    @NonNull
    private Long companyId;
    private String company;
}

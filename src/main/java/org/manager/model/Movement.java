package org.manager.model;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.manager.enums.MovementOrigin;
import org.manager.enums.MovementStatusType;
import org.manager.enums.MovementType;

import java.time.LocalDateTime;

@Entity
@Table(name = "movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    @Enumerated(EnumType.STRING)
    private MovementType type;
    @Enumerated(EnumType.STRING)
    private MovementOrigin origin;
    @Enumerated(EnumType.STRING)
    private MovementStatusType status;
    private Integer quantity;
    private LocalDateTime date;

    private String userId;
    private String username;// Utilizador que realizou o movimento
    private String referenceNumber; // Número de referência opcional

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @PrePersist
    public void prePersist() {
        if (date == null) date = LocalDateTime.now();
        if (status == null) status = MovementStatusType.PENDING;
    }
}

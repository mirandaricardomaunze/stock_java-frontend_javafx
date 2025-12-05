package org.manager.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum MovementStatusType {
    PENDING("Pendente"),
    COMPLETED("Concluído"),
    CANCELLED("Cancelado");

    private final String label;

    MovementStatusType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static List<String> labels() {
        return Arrays.stream(values())
                .map(MovementStatusType::getLabel)
                .collect(Collectors.toList());
    }

    public static MovementStatusType fromLabel(String label) {
        for (MovementStatusType s : values()) {
            if (s.getLabel().equals(label)) return s;
        }
        throw new IllegalArgumentException("Label inválido: " + label);
    }
}

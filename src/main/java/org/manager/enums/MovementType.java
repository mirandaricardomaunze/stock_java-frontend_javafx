package org.manager.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum MovementType {
    IN("Entrada"),
    OUT("Saída"),
    TRANSFER("Transferência"),
    RETURN("Devolução"),
    STOCK_ADJUST("Ajuste de Estoque");

    private final String label;

    MovementType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static List<String> labels() {
        return Arrays.stream(values())
                .map(MovementType::getLabel)
                .collect(Collectors.toList());
    }

    public static MovementType fromLabel(String label) {
        for (MovementType t : values()) {
            if (t.getLabel().equals(label)) return t;
        }
        throw new IllegalArgumentException("Label inválido: " + label);
    }
}

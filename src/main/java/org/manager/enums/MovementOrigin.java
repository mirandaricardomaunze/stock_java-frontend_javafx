package org.manager.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum MovementOrigin {
    ORDER("Pedido"),
    INVOICE("Fatura"),
    POS("PDV"),
    SYSTEM("Sistema");

    private final String label;

    MovementOrigin(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static List<String> labels() {
        return Arrays.stream(values())
                .map(MovementOrigin::getLabel)
                .collect(Collectors.toList());
    }

    public static MovementOrigin fromLabel(String label) {
        for (MovementOrigin o : values()) {
            if (o.getLabel().equals(label)) return o;
        }
        throw new IllegalArgumentException("Label inválido: " + label);
    }
}

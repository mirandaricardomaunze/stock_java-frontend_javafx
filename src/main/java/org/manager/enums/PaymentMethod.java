package org.manager.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PaymentMethod {

    CASH("Dinheiro"),
    CREDIT_CARD("Cartão de Crédito"),
    DEBIT_CARD("Cartão de Débito"),
    MPESA("MPesa"),
    EMOLA("Emola"),
    BANK_TRANSFER("Transferência Bancária");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    @JsonCreator
    public static PaymentMethod fromJson(String value) {
        if (value == null) return null;

        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.name().equalsIgnoreCase(value)
                    || method.getDescription().equalsIgnoreCase(value)) {
                return method;
            }
        }

        throw new IllegalArgumentException("Método de pagamento inválido: " + value);
    }

    @JsonValue
    public String toJson() {
        return this.name();
    }

    @Override
    public String toString() {
        return description; // importante para ComboBox
    }
}

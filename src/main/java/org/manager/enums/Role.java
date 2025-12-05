package org.manager.enums;

public enum Role {
    ADMIN("Administrador"),
    USER("Usuário"),
    MANAGER ("Gerente"),
    SUPERVISOR ("Supervisor")   ,
    OPERATOR ("Operador"),
    SELLER ("Vendedor");

    private final String label;

    Role(String label) {
        this.label = label;
    }
    public String getLabel() {
        return label;
    }
}

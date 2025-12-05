package org.manager.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtil {

    // Formata com separadores e 2 casas decimais
    public static String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "0,00";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt", "MZ")); // Português de Moçambique
        return formatter.format(value);
    }

    // Formata como número simples (sem símbolo monetário)
    public static String formatDecimal(BigDecimal value, int decimalPlaces) {
        if (value == null) {
            return "0";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("pt", "MZ"));
        formatter.setMinimumFractionDigits(decimalPlaces);
        formatter.setMaximumFractionDigits(decimalPlaces);
        return formatter.format(value);
    }
}

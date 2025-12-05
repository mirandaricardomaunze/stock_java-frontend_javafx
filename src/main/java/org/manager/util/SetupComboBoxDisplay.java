package org.manager.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

public class SetupComboBoxDisplay {
    public static <T> void setupComboBoxDisplay(ComboBox<T> comboBox, java.util.function.Function<T, String> nameExtractor) {
        comboBox.setCellFactory(cl -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : nameExtractor.apply(item));
            }
        });
    comboBox.setButtonCell(new ListCell<>() {
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? null : nameExtractor.apply(item));
        }
    });
  }
}

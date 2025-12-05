package org.manager.appContext;

import org.manager.controller.MainController;

public class AppContext {
    private static MainController mainController;

    public static void setMainController(MainController controller) {
        mainController = controller;
    }

    public static MainController getMainController() {
        return mainController;
    }
}

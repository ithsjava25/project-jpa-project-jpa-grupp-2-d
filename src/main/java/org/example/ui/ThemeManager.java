package org.example.ui;

import javafx.scene.Scene;

public final class ThemeManager {

    private static final String DARK_THEME = "/styles/app.css";
    private static final String LIGHT_THEME = "/styles/lightmode.css";

    private static boolean lightMode = false;

    private ThemeManager() {

    }

    public static void apply(Scene scene) {
        scene.getStylesheets().clear();

        String cssPath = lightMode ? LIGHT_THEME : DARK_THEME;
        var cssUrl = ThemeManager.class.getResource(cssPath);

        if (cssUrl == null) {
            System.err.println("Theme CSS not found: " + cssPath);
            return;
        }

        scene.getStylesheets().add(cssUrl.toExternalForm());
    }


    public static void applyLight(Scene scene) {
        lightMode = true;
        apply(scene);
    }

    public static void applyDark(Scene scene) {
        lightMode = false;
        apply(scene);
    }

    public static boolean isLightMode() {
        return lightMode;
    }

    public static boolean isDarkMode() {
        return !lightMode;
    }
}

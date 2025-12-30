package org.example.ui;

import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImageCache {
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();

    private ImageCache() {}

    public static Image get(String url) {
        return CACHE.computeIfAbsent(
            url,
            key -> new Image(key, true) // background loading
        );
    }
}

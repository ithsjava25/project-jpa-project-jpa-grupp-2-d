package org.example.ui;

import javafx.scene.image.Image;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ImageCache {

    private static final int MAX_CACHE_SIZE = 100;

    private static final Map<String, Image> CACHE =
        Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Image> eldest) {
                    return size() > MAX_CACHE_SIZE;
                }
            }
        );

    private ImageCache() {}

    public static Image get(String url) {
        return CACHE.computeIfAbsent(
            url,
            key -> new Image(key, true) // background loading
        );
    }
}

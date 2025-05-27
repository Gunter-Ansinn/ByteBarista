package net.ansinn.ByteBarista.caching;

import net.ansinn.ByteBarista.annotations.UnsignedInteger;

import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class CacheManager {
    private CacheManager() {}

    public static Path getDefaultCachePath() {
        var project = "bytebarista";
        var system = System.getProperty("os.name").toLowerCase();

        if(system.contains("win")) {
            return Paths.get(System.getenv("LOCALAPPDATA"), project, "cache");
        }
        else if (system.contains("osx")) {
            return Paths.get(System.getProperty("user.home"), "Library", "Caches", project);
        } else {
            var xdg = System.getenv("XDG_CACHE_HOME");
            if (xdg != null) {
                return Paths.get(xdg, project);
            }
            return Paths.get(System.getProperty("user.home"), ".cache", project);
        }
    }

}

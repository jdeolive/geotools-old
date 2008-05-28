package org.geotools.gce.imagemosaic.jdbc;

import java.net.URL;

import java.util.HashMap;
import java.util.Map;


public class Configurations {
    private static Map<String, Config> configMap = new HashMap<String, Config>();

    static synchronized public Config getConfig(String urlString)
        throws Exception {
        Config config = configMap.get(urlString);

        if (config != null) {
            return config;
        }

        config = Config.readFrom(new URL(urlString));
        configMap.put(urlString, config);

        return config;
    }
}

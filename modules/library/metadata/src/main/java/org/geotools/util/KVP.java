package org.geotools.util;

import java.util.LinkedHashMap;

/**
 * A linked HashMap set up for easy construction.
 * <p>
 * Example: <code>KVP map = new KVP("foo",1,"bar,2);</code>
 * 
 * @author jody
 */
public class KVP extends LinkedHashMap<String, Object> {
    /**
     * 
     */
    private static final long serialVersionUID = -387821381125137128L;

    /**
     * A linked HashMap set up for easy construction.
     * <p>
     * Example: <code>KVP map = new KVP("foo",1,"bar,2);</code>
     * 
     * @param pairs
     */
    public KVP(Object... pairs) {
        if ((pairs.length & 1) != 0) {
            throw new IllegalArgumentException("Pairs was not an even number");
        }
        for (int i = 0; i < pairs.length; i += 2) {
            String key = (String) pairs[i];
            Object value = pairs[i + 1];
            put(key, value);
        }
    }
}

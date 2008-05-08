package org.geotools.util.logging;

import java.util.logging.Logger;

public class Logging {

    public static Logger getLogger(final String name) {
        return Logger.getLogger( name );
    }
}

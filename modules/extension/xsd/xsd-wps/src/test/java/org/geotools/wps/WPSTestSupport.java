package org.geotools.wps;

import org.geotools.xml.Configuration;
import org.geotools.xml.test.XMLTestSupport;

public class WPSTestSupport extends XMLTestSupport {

    protected Configuration createConfiguration() {
        return new WPSConfiguration();
    }

}

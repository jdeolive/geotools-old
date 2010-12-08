package org.geotools.se.v1_1;

import org.geotools.xml.Configuration;
import org.geotools.xml.test.XMLTestSupport;

public class SETestSupport extends XMLTestSupport {

    @Override
    protected Configuration createConfiguration() {
        return new SEConfiguration();
    }

}

package org.geotools.wfs.v_1_1_0.data;

import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.wfs.WFSConfiguration;
import org.geotools.xml.Configuration;

/**
 * A WFS configuration for unit test support, that resolves schemas to the test
 * data dir.
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @source $URL$
 */
public class TestWFSConfiguration extends ApplicationSchemaConfiguration {

    private static Configuration wfsConfiguration;

    public TestWFSConfiguration(String namespace, String schemaLocation) {
        super(namespace, schemaLocation);
        synchronized (TestWFSConfiguration.class) {
            if (wfsConfiguration == null) {
                wfsConfiguration = new WFSConfiguration();
            }
        }
        addDependency(wfsConfiguration);
    }

}

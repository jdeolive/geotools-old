package org.geotools.data.wfs;

import org.geotools.data.ServiceInfo;

/**
 * Extends the standard {@link ServiceInfo} interface to provide WFS specific
 * metadata.
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @source $URL$
 */
public interface WFSServiceInfo extends ServiceInfo {

    /**
     * @return the WFS protocol version
     */
    public String getVersion();
}

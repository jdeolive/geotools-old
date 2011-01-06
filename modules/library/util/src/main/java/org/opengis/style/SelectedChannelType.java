/*$************************************************************************************************
 **
 ** $Id: SelectedChannelType.java 1289 2008-07-29 10:32:20Z eclesia $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi-pending/src/main/java/org/opengis/style/SelectedChannelType.java $
 **
 ** Copyright (C) 2008 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.style;

import org.opengis.annotation.Extension;
import org.opengis.annotation.XmlElement;


/**
 * A class to hold Channel information for use in ChannelSelection objects.
 *
 * @version <A HREF="http://www.opengeospatial.org/standards/symbol">Symbology Encoding Implementation Specification 1.1.0</A>
 * @author Open Geospatial Consortium
 * @author Ian Turton, CCG
 * @author Johann Sorel (Geomatys)
 * @since GeoAPI 2.2
 */
@XmlElement("SelectedChannelType")
public interface SelectedChannelType {

    /**
     * Returns the channel's name.
     *
     * @return String
     */
    @XmlElement("SourceChannelName")
    public String getChannelName();

    /**
     * Contrast enhancement may be applied to each channel in isolation.
     *
     * @return ContrastEnhancement
     */
    @XmlElement("SelectedChannelType")
    public ContrastEnhancement getContrastEnhancement();

    /**
     * calls the visit method of a StyleVisitor
     *
     * @param visitor the style visitor
     */
    @Extension
    Object accept(StyleVisitor visitor, Object extraData);
    
}

/*
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Center for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; 
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */

package org.geotools.styling;

import java.net.URL;

/**
 * Holds a reference to an external graphics file with a URL to its location
 * and its expected MIME type.
 * Knowing the MIME type in advance allows stylers to select best-supported
 * formats from a list of external graphics.<p>
 *
 * The details of this object are taken from the OGC Styled-Layer Descriptor
 * Report (OGC 01-077) version 0.7.2.
 * Renderers can use this information when displaying styled features, though
 * it must be remembered that not all renderers will be able to fully represent
 * strokes as set out by this interface.  For example, opacity may not be
 * supported.
 *
 * @version $Id: ExternalGraphic.java,v 1.5 2002/10/17 16:55:23 ianturton Exp $
 * @author James Macgill, CCG
 */
public interface ExternalGraphic {
    
    /** converts a URI in a string to the location URL
     *  @param uri the uri of the external graphic
     */
    public void setURI(String uri);
    /**
     * Provides the URL for where the external graphic resource can be located.
     * @return The URL of the ExternalGraphic
     */
    URL getLocation();
    /**
     * Provides the URL for where the external graphic resource can be located.
     * @param The URL of the ExternalGraphic
     */
    void setLocation(URL url);
    /**
     * Provides the format of the external graphic.
     * @return The format of the external graphic.  Reported as its MIME type
     * in a String object.
     */
    String getFormat();
    /**
     * Provides the format of the external graphic.
     * @param The format of the external graphic.  Reported as its MIME type
     * in a String object.
     */
    void setFormat(String format);
}


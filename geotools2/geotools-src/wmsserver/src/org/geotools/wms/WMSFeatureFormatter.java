/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 */
package org.geotools.wms;

import org.geotools.feature.Feature;
import java.io.OutputStream;

/**
 * An interface for formatting a list of geotools Feature objects into a
 * stream. IE, application/vnd.ogc.gml requests that the feature information
 * be formatted in Geography Markup Language (GML).
 * FeatureFormatters are attached to the WMSServlet on initialization, and
 * an arbitrary number can be attached.
 *
 * @version $Id: WMSFeatureFormatter.java,v 1.2 2002/07/15 17:09:59 loxnard Exp $
 * @author Ray Gallagher
 */
public interface WMSFeatureFormatter
{
	/**
         * Gets the mime-type of the stream written to by formatFeatures().
	 */
	public String getMimeType();
	
	/**
         * Formats the given array of Features as this Formatter's mime-type
         * and writes it to the given OutputStream.
	 */
	public void formatFeatures(Feature [] features, OutputStream out);
}


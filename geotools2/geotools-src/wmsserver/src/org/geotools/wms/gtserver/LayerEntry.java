/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.wms.gtserver;

import java.util.HashMap;
import java.util.Properties;


/**
 * A single entry for a Layer in the layers.xml file
 */
public class LayerEntry {
    /** The unique id of the Layer */
    public String id;

    /** A description for the layer */
    public String description;

    /** The native spatial reference system for this layer */
    public String srs = "EPSG:4326"; //should not be setting a default here

    /** The classname for the DataSource to use to load maps for this layer */
    public String datasource;

    /** The properties for the DataSource */
    public Properties properties;

    /** The styles for this layer */
    public HashMap styles;

    /** The default style for this layer */
    public String defaultStyle;

    /** the bounding box of this layer */
    double[] bbox;
}

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2005, Open Geospatial Consortium Inc.
 *    
 *    All Rights Reserved. http://www.opengis.org/legal/
 */
package org.opengis.filter.spatial;

// Annotations
import org.opengis.annotation.XmlElement;


/**
 * Concrete {@linkplain BinarySpatialOperator binary spatial operator} that evaluates to
 * {@code true} if the the first geometric operand contains the second.
 *
 *
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/library/opengis/src/main/java/org/opengis/filter/spatial/Contains.java $
 * @version <A HREF="http://www.opengis.org/docs/02-059.pdf">Implementation specification 1.0</A>
 * @author Chris Dillard (SYS Technologies)
 * @since GeoAPI 2.0
 */
@XmlElement("Contains")
public interface Contains extends BinarySpatialOperator, BoundedSpatialOperator {
	/** Operator name used to check FilterCapabilities */
	public static String NAME = "Contains";
}

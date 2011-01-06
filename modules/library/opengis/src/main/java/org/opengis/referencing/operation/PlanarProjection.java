/*$************************************************************************************************
 **
 ** $Id: PlanarProjection.java 1265 2008-07-09 18:24:37Z desruisseaux $
 **
 ** $URL: https://geoapi.svn.sourceforge.net/svnroot/geoapi/tags/2.3-M2/geoapi/src/main/java/org/opengis/referencing/operation/PlanarProjection.java $
 **
 ** Copyright (C) 2003-2005 Open GIS Consortium, Inc.
 ** All Rights Reserved. http://www.opengis.org/legal/
 **
 *************************************************************************************************/
package org.opengis.referencing.operation;

import org.opengis.annotation.Extension;


/**
 * Base interface for for azimuthal (or planar) map projections.
 *
 * <p>&nbsp;</p>
 * <p align="center"><img src="../doc-files/PlanarProjection.png"></p>
 *
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 1.0
 *
 * @see org.opengis.referencing.crs.ProjectedCRS
 * @see <A HREF="http://mathworld.wolfram.com/AzimuthalProjection.html">Azimuthal projection on MathWorld</A>
 */
@Extension
public interface PlanarProjection extends Projection {
}

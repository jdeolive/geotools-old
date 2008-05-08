/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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
 */
package org.geotools.display.canvas;

// Geotools dependencies
import org.geotools.referencing.crs.DefaultEngineeringCRS;


/**
 * A dummy graphic implementation for testing purpose.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class DummyGraphic extends ReferencedGraphic implements Cloneable {
    /**
     * Creates a dummy graphic.
     */
    DummyGraphic() {
        super(DefaultEngineeringCRS.CARTESIAN_2D);
    }
}

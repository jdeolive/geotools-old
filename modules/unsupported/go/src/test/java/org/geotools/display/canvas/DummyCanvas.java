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

// J2SE dependencies
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

// OpenGIS dependencies
import org.opengis.display.canvas.CanvasController;
import org.opengis.go.display.primitive.Graphic;


/**
 * A dummy graphic implementation for testing purpose.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class DummyCanvas extends ReferencedCanvas2D {
    /**
     * Creates a new canvas.
     */
    DummyCanvas() {
//        super(null); ----------------ADDED TO COMPILE -----------------------------------------
    }

    /**
     * Dummy method: ignores the repaint call.
     */
    public void repaint(Graphic graphic, Rectangle2D objectiveArea, Rectangle displayArea) {
    }

    
    
    
    //----------------ADDED TO COMPILE -----------------------------------------
    
    
    
    @Override
    public void repaint(org.opengis.display.primitive.Graphic graphic, Rectangle2D objectiveArea, Rectangle displayArea) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void repaint() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public CanvasController getController() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

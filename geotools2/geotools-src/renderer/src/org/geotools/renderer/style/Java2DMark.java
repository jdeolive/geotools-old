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
package org.geotools.renderer.style;

import java.awt.*;
import java.awt.geom.*;
import java.util.logging.Logger;


/**
 * Utility class that will return the Shape for well known marks
 *
 * @author Ian Turton
 * @version $Id: Java2DMark.java,v 1.2 2003/10/17 21:37:38 jmacgill Exp $
 */
public class Java2DMark {
    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");
    static GeneralPath cross;
    static GeneralPath star;
    static GeneralPath triangle;
    static GeneralPath arrow;
    static Shape X;

    static {
        cross = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        cross.moveTo(0.5f, 0.125f);
        cross.lineTo(0.125f, 0.125f);
        cross.lineTo(0.125f, 0.5f);
        cross.lineTo(-0.125f, 0.5f);
        cross.lineTo(-0.125f, 0.125f);
        cross.lineTo(-0.5f, 0.125f);
        cross.lineTo(-0.5f, -0.125f);
        cross.lineTo(-0.125f, -0.125f);
        cross.lineTo(-0.125f, -0.5f);
        cross.lineTo(0.125f, -0.5f);
        cross.lineTo(0.125f, -0.125f);
        cross.lineTo(0.5f, -0.125f);
        cross.lineTo(0.5f, 0.125f);

        AffineTransform at = new AffineTransform();
        at.rotate(Math.PI / 4.0);
        X = cross.createTransformedShape(at);
        star = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        star.moveTo(0.191f, 0.0f);
        star.lineTo(0.25f, 0.344f);
        star.lineTo(0.0f, 0.588f);
        star.lineTo(0.346f, 0.638f);
        star.lineTo(0.5f, 0.951f);
        star.lineTo(0.654f, 0.638f);
        star.lineTo(1.0f, 0.588f); // max = 7.887
        star.lineTo(0.75f, 0.344f);
        star.lineTo(0.89f, 0f);
        star.lineTo(0.5f, 0.162f);
        star.lineTo(0.191f, 0.0f);
        at = new AffineTransform();
        at.translate(-.5, -.5);
        star.transform(at);
        triangle = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        triangle.moveTo(0f, 1f);
        triangle.lineTo(0.866f, -.5f);
        triangle.lineTo(-0.866f, -.5f);
        triangle.lineTo(0f, 1f);
        at = new AffineTransform();

        at.translate(0, -.25);
        at.scale(.5, .5);

        triangle.transform(at);

        arrow = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        arrow.moveTo(0f, -.5f);
        arrow.lineTo(.5f, 0f);
        arrow.lineTo(0f, .5f);
        arrow.lineTo(0f, .1f);
        arrow.lineTo(-.5f, .1f);
        arrow.lineTo(-.5f, -.1f);
        arrow.lineTo(0f, -.1f);
        arrow.lineTo(0f, -.5f);
    }

    public static Shape getWellKnownMark(String wellKnownName) {
        LOGGER.finer("fetching mark of name " + wellKnownName);

        if (wellKnownName.equalsIgnoreCase("cross")) {
            LOGGER.finer("returning cross");

            return cross;
        }

        if (wellKnownName.equalsIgnoreCase("circle")) {
            LOGGER.finer("returning circle");

            return new java.awt.geom.Ellipse2D.Double(-.5, -.5, 1., 1.);
        }

        if (wellKnownName.equalsIgnoreCase("triangle")) {
            LOGGER.finer("returning triangle");

            return triangle;
        }

        if (wellKnownName.equalsIgnoreCase("X")) {
            LOGGER.finer("returning X");

            return X;
        }

        if (wellKnownName.equalsIgnoreCase("star")) {
            LOGGER.finer("returning star");

            return star;
        }

        if (wellKnownName.equalsIgnoreCase("arrow")) {
            LOGGER.finer("returning arrow");

            return arrow;
        }

        // failing that return a square?
        LOGGER.finer("returning square");

        return new Rectangle2D.Double(-.5, -.5, 1., 1.);
    }
}

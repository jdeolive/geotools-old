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

import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.font.GlyphVector;


/**
 * Style used to represent labels over lines, polygons and points
 *
 * @author Andrea Aime
 * @version $Id: TextStyle2D.java,v 1.2 2003/11/08 11:23:36 aaime Exp $
 */
public class TextStyle2D extends Style2D {
    GlyphVector textGlyphVector;
    Shape haloShape;
    String label;
    Font font;
    double rotation;
    boolean absoluteLineDisplacement;
    double anchorX;
    double anchorY;
    double displacementX;
    double displacementY;
    Paint haloFill;
    Composite haloComposite;
    float haloRadius;

    /** Holds value of property fill. */
    private Paint fill;

    /** Holds value of property composite. */
    private Composite composite;

    /**
     * @return
     */
    public double getAnchorX() {
        return anchorX;
    }

    /**
     * @return
     */
    public double getAnchorY() {
        return anchorY;
    }

    /**
     * @return
     */
    public Font getFont() {
        return font;
    }

    /**
     * @return
     */
    public Composite getHaloComposite() {
        return haloComposite;
    }

    /**
     * @return
     */
    public Paint getHaloFill() {
        return haloFill;
    }

    /**
     * @return
     */
    public float getHaloRadius() {
        return haloRadius;
    }

    /**
     * @return
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * @return
     */
    public GlyphVector getTextGlyphVector(Graphics2D graphics) {
        if (textGlyphVector == null) {
            textGlyphVector = font.createGlyphVector(graphics.getFontRenderContext(), label);
        }

        return textGlyphVector;
    }

    /**
     * @return
     */
    public Shape getHaloShape(Graphics2D graphics) {
        if (haloShape == null) {
            GlyphVector gv = getTextGlyphVector(graphics);
            haloShape = new BasicStroke(2f * haloRadius, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND).createStrokedShape(gv.getOutline());
        }

        return haloShape;
    }

    /**
     * @param i
     */
    public void setAnchorX(double f) {
        anchorX = f;
    }

    /**
     * @param i
     */
    public void setAnchorY(double f) {
        anchorY = f;
    }

    /**
     * @param font
     */
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * @param composite
     */
    public void setHaloComposite(Composite composite) {
        haloComposite = composite;
    }

    /**
     * @param paint
     */
    public void setHaloFill(Paint paint) {
        haloFill = paint;
    }

    /**
     * @param f
     */
    public void setHaloRadius(float f) {
        haloRadius = f;
    }

    /**
     * @param f
     */
    public void setRotation(double f) {
        rotation = f;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return Returns the absoluteLineDisplacement.
     */
    public boolean isAbsoluteLineDisplacement() {
        return absoluteLineDisplacement;
    }

    /**
     * @param absoluteLineDisplacement The absoluteLineDisplacement to set.
     */
    public void setAbsoluteLineDisplacement(boolean absoluteLineDisplacement) {
        this.absoluteLineDisplacement = absoluteLineDisplacement;
    }

    /**
     * @return Returns the displacementX.
     */
    public double getDisplacementX() {
        return displacementX;
    }

    /**
     * @param displacementX The displacementX to set.
     */
    public void setDisplacementX(double displacementX) {
        this.displacementX = displacementX;
    }

    /**
     * @return Returns the displacementY.
     */
    public double getDisplacementY() {
        return displacementY;
    }

    /**
     * @param displacementY The displacementY to set.
     */
    public void setDisplacementY(double displacementY) {
        this.displacementY = displacementY;
    }

    /**
     * Getter for property fill.
     *
     * @return Value of property fill.
     */
    public Paint getFill() {
        return this.fill;
    }

    /**
     * Setter for property fill.
     *
     * @param fill New value of property fill.
     */
    public void setFill(Paint fill) {
        this.fill = fill;
    }

    /**
     * Getter for property composite.
     *
     * @return Value of property composite.
     */
    public Composite getComposite() {
        return this.composite;
    }

    /**
     * Setter for property composite.
     *
     * @param composite New value of property composite.
     */
    public void setComposite(Composite composite) {
        this.composite = composite;
    }
}

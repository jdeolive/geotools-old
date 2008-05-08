/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.styling;



/**
 * A NamedStyle is used to refer to a style that has a name in a WMS.
 * 
 * <p>
 * A NamedStyle is a Style that has only Name, so all setters other than
 * setName will throw an <code>UnsupportedOperationException</code>
 * </p>
 *
 * @author jamesm
 * @source $URL$
 */
public class NamedStyleImpl implements NamedStyle {
    /** Style name */
    private String name;

    /**
     * Style name
     *
     * @return style name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set name.
     *
     * @param name style name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Human readable title.
     *
     * @return Human readable title, or null
     */
    public String getTitle() {
        return null;
    }

    /**
     * Human readable title.
     *
     * @param title Human readable title.
     *
     * @throws UnsupportedOperationException Cannot be changed
     */
    public void setTitle(String title) {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAbstract() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param abstractStr DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void setAbstract(String abstractStr) {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isDefault() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param isDefault DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void setDefault(boolean isDefault) {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FeatureTypeStyle[] getFeatureTypeStyles() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param types DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void setFeatureTypeStyles(FeatureTypeStyle[] types) {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void addFeatureTypeStyle(FeatureTypeStyle type) {
        throw new UnsupportedOperationException();
    }

    /**
     * DOCUMENT ME!
     *
     * @param visitor DOCUMENT ME!
     */
    public void accept(StyleVisitor visitor) {
        visitor.visit(this);
    }
}

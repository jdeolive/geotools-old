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
package org.geotools.graph;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.DataUtilities;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import java.util.Collection;


/**
 * A GraphElement represents a component of a graph.
 *
 * @author Justin Deoliveira
 */
public abstract class GraphComponent implements Feature {
    /** FeatureSchema used to represent the component as a Feature */
    private static FeatureType schema;

    /** Used to generate id's for graph components */
    private static int id = 0;

    /** Underyling feature of the component */
    private Feature m_feature;

    /** flag to indicate wether the component has been visited */
    private boolean m_visited;

    /** a counter to track how many times a component has been visited */
    private int m_count;

    /** debugging variables */
    private Geometry m_geometry;

    /** DOCUMENT ME! */
    private int m_id;
    private FeatureCollection parent;

    /**
     * Creates a graph component.
     *
     * @param feature Underlying Feature represented by the component.
     *
     * @see Feature
     */
    public GraphComponent(Feature feature) {
        m_feature = feature;
        m_visited = false;
        m_count = -1;
        m_id = id++;
    }

    /**
     * Returns a FeatureSchema used to represent the graph component as a
     * Feature.
     *
     * @return FeatureSchema
     *
     * @see FeatureSchema
     * @see Feature
     */
    public static FeatureType schema() {
        if (schema == null) {
            try {
                schema = DataUtilities.createType("graph:network",
                        "geometry:Geometry,id:0,count:0,visted:0");
            } catch (SchemaException e) {
                // very bad should not happen
            }
        }

        return (schema);
    }

    /**
     * Returns the underlying feature represented by the component.
     *
     * @return Feature
     */
    public Feature getFeature() {
        return (m_feature);
    }

    /**
     * Determines if the componenet has been marked as visited.
     *
     * @return True if visited, false if unvisited.
     */
    public boolean isVisited() {
        return (m_visited);
    }

    /**
     * Marks the component as being visited/unvisited.
     *
     * @param visited True = visited, false = unvisited.
     */
    public void setVisited(boolean visited) {
        m_visited = visited;
    }

    /**
     * Returns the count associated with this component. Used mostly by
     * traversals  that visit components more then once.
     *
     * @return int
     */
    public int getCount() {
        return (m_count);
    }

    /**
     * Sets the count associated with the component.
     *
     * @param count
     */
    public void setCount(int count) {
        m_count = count;
    }

    /**
     * Not supported.
     *
     * @see Feature#setAttributes(Object[])
     */
    /**
     * Returns the FeatureSchema used to represent the graph component as a
     * Feature.
     *
     * @see Feature#getSchema()
     */
    public FeatureType getSchema() {
        return (schema());
    }

    /**
     * Returns an id for the component.
     *
     * @see Feature#getID()
     */
    /**
     * Compares this graph component to another
     *
     * @see Comparable#compareTo(jObject)
     */
    public int compareTo(Object other) {
        if (equals(other)) {
            return (0);
        }

        return ((hashCode() > other.hashCode()) ? 1 : (-1));
    }

    /*
     * Throws a UnsupportedOperationException.
     */
    private void noop(String string) {
        throw new UnsupportedOperationException(string);
    }

    /**
     * Builds the geometry used to represent the component spatially.
     *
     * @return Geometry
     */
    public abstract Geometry buildGeometry();

    /**
     * Returns any adjacent components of similar type.
     *
     * @return Collection
     */
    public abstract Collection getAdjacentElements();

    //
    // Implement Feature
    //

    /**
     * Implementation of getParent.
     *
     * @return
     *
     * @see org.geotools.feature.Feature#getParent()
     */
    public FeatureCollection getParent() {
        return parent;
    }

    /**
     * Implementation of setParent.
     *
     * @param collection
     *
     * @see org.geotools.feature.Feature#setParent(org.geotools.feature.FeatureCollection)
     */
    public void setParent(FeatureCollection collection) {
        this.parent = collection;
    }

    /**
     * Implementation of getFeatureType.
     *
     * @return
     *
     * @see org.geotools.feature.Feature#getFeatureType()
     */
    public FeatureType getFeatureType() {
        return getSchema();
    }

    /**
     * Implementation of getID.
     *
     * @return
     *
     * @see org.geotools.feature.Feature#getID()
     */
    public String getID() {
        return getFeatureType().getTypeName() + m_id;
    }

    /**
     * Implementation of getAttributes.
     *
     * @param attributes
     *
     * @return
     *
     * @see org.geotools.feature.Feature#getAttributes(java.lang.Object[])
     */
    public Object[] getAttributes(Object[] attributes) {
        Object[] atts = new Object[getSchema().getAttributeCount()];

        for (int i = 0; i < getSchema().getAttributeCount(); i++) {
            atts[i] = getAttribute(i);
        }

        return atts;
    }

    /**
     * Implementation of getAttribute.
     *
     * @param xPath
     *
     * @return
     *
     * @see org.geotools.feature.Feature#getAttribute(java.lang.String)
     */
    public Object getAttribute(String xPath) {
        int index = getSchema().find(getSchema().getAttributeType(xPath));

        return getAttribute(xPath);
    }

    /**
     * Implementation of getAttribute.
     *
     * @param index
     *
     * @return
     *
     * @see org.geotools.feature.Feature#getAttribute(int)
     */
    public Object getAttribute(int index) {
        //"geometry:Geometry,id:0,count:0,visted:0"		
        switch (index) {
        case 0:
            return m_geometry;

        case 1:
            return new Integer(m_id);

        case 2:
            return new Integer(m_count);

        case 3:
            return new Integer(m_visited ? 1 : 0);
        }

        return null;
    }

    /**
     * Implementation of setAttribute.
     *
     * @param position
     * @param val
     *
     * @throws IllegalAttributeException
     * @throws ArrayIndexOutOfBoundsException
     *
     * @see org.geotools.feature.Feature#setAttribute(int, java.lang.Object)
     */
    public void setAttribute(int position, Object val)
        throws IllegalAttributeException, ArrayIndexOutOfBoundsException {
        switch (position) {
        case 0:
            m_geometry = (Geometry) val;

            break;

        case 1:
            m_id = ((Integer) val).intValue();

            break;

        case 2:
            m_count = ((Integer) val).intValue();

            break;

        case 3:
            m_visited = ((Integer) val).intValue() == 1;
        }
    }

    /**
     * Implementation of getNumberOfAttributes.
     *
     * @return
     *
     * @see org.geotools.feature.Feature#getNumberOfAttributes()
     */
    public int getNumberOfAttributes() {
        return getSchema().getAttributeCount();
    }

    /**
     * Implementation of setAttributes.
     *
     * @param attributes
     *
     * @throws IllegalAttributeException
     *
     * @see org.geotools.feature.Feature#setAttributes(java.lang.Object[])
     */
    public void setAttributes(Object[] attributes)
        throws IllegalAttributeException {
        Object[] atts = new Object[getSchema().getAttributeCount()];

        for (int i = 0; i < getSchema().getAttributeCount(); i++) {
            setAttribute(i, attributes[i]);
        }
    }

    /**
     * Implementation of setAttribute.
     *
     * @param xPath
     * @param attribute
     *
     * @throws IllegalAttributeException
     *
     * @see org.geotools.feature.Feature#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    public void setAttribute(String xPath, Object attribute)
        throws IllegalAttributeException {
        int index = getSchema().find(getSchema().getAttributeType(xPath));
        setAttribute(xPath, attribute);
    }

    /**
     * Implementation of getDefaultGeometry.
     *
     * @return
     *
     * @see org.geotools.feature.Feature#getDefaultGeometry()
     */
    public Geometry getDefaultGeometry() {
        return m_geometry;
    }

    /**
     * Implementation of setDefaultGeometry.
     *
     * @param geometry
     *
     * @throws IllegalAttributeException
     *
     * @see org.geotools.feature.Feature#setDefaultGeometry(com.vividsolutions.jts.geom.Geometry)
     */
    public void setDefaultGeometry(Geometry geometry)
        throws IllegalAttributeException {
        m_geometry = geometry;
    }

    /**
     * Implementation of getBounds.
     *
     * @return
     *
     * @see org.geotools.feature.Feature#getBounds()
     */
    public Envelope getBounds() {
        return m_geometry.getEnvelopeInternal();
    }
}

package org.geotools.datasource;

import com.vividsolutions.jts.geom.Geometry;
/** Represents one row of a table. A Feature is a primary Object (can be any Object, usually a Geography)
 * plus any attributes which are associated with the Object.
 */
public interface Feature {
    public Object[] getAttributes();
    public String[] getAttributeNames();
    public Geometry getGeometry();
    public void setGeometry(Geometry geom);
    public void setAttributes(Object[] a);
}


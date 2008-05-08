package org.geotools.data.shapefile.indexed.attribute;

/**
 * Record stored in attribute index file
 * 
 * @author Manuele Ventoruzzo
 */
public class IndexRecord implements Comparable {

    private Comparable attribute;
    private long featureID;

    public IndexRecord(Comparable attribute, long featureID) {
        this.attribute = attribute;
        this.featureID = featureID;
    }

    public Object getAttribute() {
        return attribute;
    }

    public long getFeatureID() {
        return featureID;
    }

    public int compareTo(Object o) {
        if (o instanceof IndexRecord) {
            return attribute.compareTo(((IndexRecord) o).getAttribute());
        }
        if (attribute.getClass().isInstance(o)) {
            // compare just attribute with o
            return attribute.compareTo(o);
        }
        throw new ClassCastException("Object " + o.toString() + " is not of Record type");
    }

    public String toString() {
        return "(" + attribute.toString() + "," + featureID + ")";
    }
    }
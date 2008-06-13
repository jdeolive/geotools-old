/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.firstdraft.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;


/** Simple marshaller that can write features to an ObjectOutputStream.
 * Feature is not Serializable, but this is based on the idea that most attributes object are Serializable
 * (JTS geometries are Serializable), and that attributes which are not simple, are either a collection we can iterate through, or another Feature.
 * Serialization is then achieved recursively.
 * Unmarshalling implies to know the FeatureType of the marshalled feature.
 *
 * Storage format : Header,
 *                  Attributes
 *
 * Header := int     : FeatureType hashCode,
 *           String  : FeatureType name,
 *           String  : Feature ID,
 *           int     : number of attributes
 * Attributes := [Attribute]
 * Attribute  := int : multiplicity, or O if simple, or -1 if FeatureAttribute,
 *               Object|Feature|[Attribute] : attribute value
 *
 * This implementation does not have the ambition of being robust.
 *
 * @task test with other FeatureType than DefaultFeatureType
 * @task add method marshall(Feature, ByteArrayOutputStream) and unmarshall(ByteArrayOutputStream), or create sub class.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class FeatureMarshaller {
    /**
     * marker to indicate an attribute is a feature in the serialized form
     */
    public static final int FEATURE = -1;

    /**
     * @task should be made final, and remove default constructor and  type setter ?
     */
    private FeatureType type;

    /** Default constructor.
     *  User must call setType() to set the FeatureType
     *  before any attempt to marshall/unmarshall a feature.
     */
    public FeatureMarshaller() {
        type = null;
    }

    /** Creates a new instance of this marshaller
     *
     * @param t the FeatureType this marshaller can marshall/unmarsahll
     */
    public FeatureMarshaller(FeatureType t) {
        this.type = t;
    }

    /** FeatureType getter
     * @return the FeatureType this marshaller marshalls/unmarshalls
     */
    public FeatureType getType() {
        return type;
    }

    /** FeatureType setter
     * @param t the FeatureType to use for next marshall/unmarshall ops.
     */
    public void setType(FeatureType t) {
        this.type = t;
    }

    /** Marshall a feature into a stream.
     *
     * @param f the Feature to marshall
     * @param s the stream to write to
     * @throws IOException
     */
    public void marshall(Feature f, ObjectOutputStream s)
        throws IOException {
        if (!type.equals(f.getFeatureType())) {
            throw new IOException("Wrong feature type : " + f.getFeatureType());
        }

        s.writeInt(f.getFeatureType().hashCode());
        s.writeObject(f.getFeatureType().getTypeName());
        s.writeObject(f.getID());

        int natt = f.getNumberOfAttributes();
        s.writeInt(natt);

        for (int i = 0; i < natt; i++) {
            marshallComplexAttribute(f.getAttribute(i), s);
        }
    }

    /** Marshall an attribute into a stream.
     *
     * @task test object is instance of Serializable
     *
     * @param o an attribute value which is Serializable, or a feature, or a collection
     * @param s the stream to write to
     * @throws IOException
     */
    protected void marshallComplexAttribute(Object o, ObjectOutputStream s)
        throws IOException {
        if (o instanceof Collection) {
            Collection c = (Collection) o;
            s.writeInt(c.size());

            for (Iterator it = c.iterator(); it.hasNext();) {
                Object nxt = it.next();
                marshallComplexAttribute(nxt, s);
            }
        } else if (o instanceof Feature) {
            s.writeInt(FEATURE);
            marshall((Feature) o, s);
        } else {
            s.writeInt(0);
            s.writeObject(o);
        }
    }

    /** Inverse operation of marshall : read a feature from a stream.
     *
     * @param s the stream to read from
     * @return the unmarshalled feature
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalAttributeException
     */
    public Feature unmarshall(ObjectInputStream s)
        throws IOException, ClassNotFoundException, IllegalAttributeException {
        int typeHash = s.readInt();
        String typeName = (String) s.readObject();
        String fid = (String) s.readObject();
        int natt = s.readInt();

        if (!(typeHash == type.hashCode()) && (typeName.equals(type.getTypeName()))
                && (natt == type.getAttributeCount())) {
            throw new IOException("Schema error");
        }

        List atts = new ArrayList();

        for (int i = 0; i < natt; i++) {
            atts.addAll(unmarshallComplexAttribute(s));
        }

        return type.create(atts.toArray(), fid);
    }

    /** Read attribute values from a stream.
     *
     * @param s the stream to read from
     * @return a list of attribute values, possibly a singleton, if attribute's multiplicity is 1
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalAttributeException
     */
    protected List unmarshallComplexAttribute(ObjectInputStream s)
        throws IOException, ClassNotFoundException, IllegalAttributeException {
        int m = s.readInt();
        List atts = new ArrayList();

        if (m == 0) {
            atts.add(s.readObject());
        } else if (m == FEATURE) {
            Feature f = unmarshall(s);
            atts.add(f);
        } else {
            for (int i = 0; i < m; i++) {
                atts.addAll(unmarshallComplexAttribute(s));
            }
        }

        return atts;
    }
}

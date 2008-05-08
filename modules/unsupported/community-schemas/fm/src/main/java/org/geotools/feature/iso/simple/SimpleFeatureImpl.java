package org.geotools.feature.iso.simple;

import java.util.Iterator;
import java.util.List;

import org.geotools.feature.iso.FeatureImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;

/**
 * An implementation of the SimpleFeature convience methods ontop of
 * FeatureImpl.
 * 
 * @author Justin
 */
public class SimpleFeatureImpl extends FeatureImpl implements SimpleFeature {

    public SimpleFeatureImpl(List values, AttributeDescriptor desc, String id) {
        super(values, desc, id);
    }

    public SimpleFeatureImpl(List values, SimpleFeatureType type, String id) {
        super(values, type, id);
    }

    /**
     * Create a Feature with the following content.
     * 
     * @param values
     *            Values in agreement with provided type
     * @param type
     *            Type of feature to be created
     * @param id
     *            Feature ID
     */
    public SimpleFeatureImpl(SimpleFeatureType type, String id, Object[] values) {
        this(SimpleFeatureFactoryImpl.attributes(type, values), type, id);
    }

    /**
     * Retrive value by attribute name.
     * 
     * @param name
     * @return Attribute Value associated with name
     */
    public Object get(String name) {
        for (Iterator itr = super.properties.iterator(); itr.hasNext();) {
            Attribute att = (Attribute) itr.next();
            AttributeType type = att.getType();
            String attName = type.getName().getLocalPart();
            if (attName.equals(name)) {
                return att.get();
            }
        }
        return null;
    }

    public Object get(AttributeType type) {
        if (!super.types().contains(type)) {
            throw new IllegalArgumentException(
                    "this feature content model has no type " + type);
        }
        for (Iterator itr = super.properties.iterator(); itr.hasNext();) {
            Attribute att = (Attribute) itr.next();
            if (att.getType().equals(type)) {
                return att.get();
            }
        }
        throw new Error();
    }

    /**
     * Access attribute by "index" indicated by SimpleFeatureType.
     * 
     * @param index
     * @return
     */
    public Object get(int index) {
        Attribute att = (Attribute) super.properties.get(index);
        return att == null ? null : att.get();
        // return values().get(index);
    }

    /**
     * Modify attribute with "name" indicated by SimpleFeatureType.
     * 
     * @param name
     * @param value
     */
    public void set(String name, Object value) {
        AttributeType type = ((SimpleFeatureType) getType()).get(name);
        List/* <AttributeType> */types = types();
        int idx = types.indexOf(type);
        if (idx == -1) {
            throw new IllegalArgumentException(name
                    + " is not a feature attribute");
        }
        set(idx, value);
    }

    /**
     * Modify attribute at the "index" indicated by SimpleFeatureType.
     * 
     * @param index
     * @param value
     */
    public void set(int index, Object value) {
        List/* <Attribute> */contents = (List) get();
        Attribute attribute = (Attribute) contents.get(index);
        attribute.set(value);
        this.set(contents);
    }

    public List getAttributes() {
        return (List) get();
    }

    public int getNumberOfAttributes() {
        return types().size();
    }

    public List types() {
        return super.types();
    }

    public Object defaultGeometry() {
        return getDefaultGeometry() != null ? getDefaultGeometry().get() : null;
    }

    public void defaultGeometry(Object geometry) {
        if (getDefaultGeometry() != null) {
            getDefaultGeometry().set(geometry);
        }
    }

    public Object operation(String arg0, Object arg1) {
        throw new UnsupportedOperationException("operation not supported yet");
    }
}

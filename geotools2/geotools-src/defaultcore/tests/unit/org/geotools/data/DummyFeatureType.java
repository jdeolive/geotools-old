/*
 * DummyFeatureType.java
 *
 * Created on 20 novembre 2003, 21.31
 */

package org.geotools.data;

import org.geotools.feature.FeatureType;

/**
 * An empty FeatureType implementation used in the AbstractDataSourceTest
 * @author  wolf
 */
public class DummyFeatureType implements FeatureType {
    private String typeName;
    
    /** Creates a new instance of DummyFeatureType */
    public DummyFeatureType(String typeName) {
    }
    
    public org.geotools.feature.Feature create(Object[] attributes) throws org.geotools.feature.IllegalAttributeException {
        return null;
    }
    
    public org.geotools.feature.Feature create(Object[] attributes, String featureID) throws org.geotools.feature.IllegalAttributeException {
        return null;
    }
    
    public org.geotools.feature.Feature duplicate(org.geotools.feature.Feature feature) throws org.geotools.feature.IllegalAttributeException {
        return null;
    }
    
    public int find(org.geotools.feature.AttributeType type) {
        return 0;
    }
    
    public FeatureType[] getAncestors() {
        return new FeatureType[] {};
    }
    
    public int getAttributeCount() {
        return 0;
    }
    
    public org.geotools.feature.AttributeType getAttributeType(String xPath) {
        return null;
    }
    
    public org.geotools.feature.AttributeType getAttributeType(int position) {
        return null;
    }
    
    public org.geotools.feature.AttributeType[] getAttributeTypes() {
        return new org.geotools.feature.AttributeType[] {};
    }
    
    public org.geotools.feature.AttributeType getDefaultGeometry() {
        return null;
    }
    
    public String getNamespace() {
        return null;
    }
    
    public String getTypeName() {
        return typeName;
    }
    
    public boolean hasAttributeType(String xPath) {
        return false;
    }
    
    public boolean isAbstract() {
        return true;
    }
    
    public boolean isDescendedFrom(FeatureType type) {
        return false;
    }
    
    public boolean isDescendedFrom(String nsURI, String typeName) {
        return false;
    }
    
}

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
package org.geotools.data;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BetweenFilter;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.FidFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterVisitor;
import org.geotools.filter.FunctionExpression;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.LikeFilter;
import org.geotools.filter.LiteralExpression;
import org.geotools.filter.LogicFilter;
import org.geotools.filter.MathExpression;
import org.geotools.filter.NullFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;


/**
 * Utility functions for use when implementing working with data classes.
 *
 * @author Jody Garnett, Refractions Research
 */
public class DataUtilities {
    static Map typeMap = new HashMap();

    static {
        typeMap.put("String", String.class);
        typeMap.put("string", String.class);
        typeMap.put("\"\"", String.class);        
        typeMap.put("Integer", Integer.class);
        typeMap.put("int", Integer.class);
        typeMap.put("0", Integer.class);               
        typeMap.put("Double", Double.class);
        typeMap.put("double", Double.class);
        typeMap.put("0.0", Double.class);
        typeMap.put("Float", Float.class);
        typeMap.put("float", Float.class);
        typeMap.put("0.0f", Float.class);        
        typeMap.put("Geometry", Geometry.class);
        typeMap.put("Point", Point.class);
        typeMap.put("LineString", LineString.class);
        typeMap.put("Polygon", Polygon.class);
        typeMap.put("MultiPoint", MultiPoint.class);
        typeMap.put("MultiLineString", MultiLineString.class);
        typeMap.put("MultiPolygon", MultiPolygon.class);
        typeMap.put("GeometryCollection", GeometryCollection.class);        
    }

    public static String[] attributeNames(FeatureType featureType) {
        String[] names = new String[featureType.getAttributeCount()];

        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            names[i] = featureType.getAttributeType(i).getName();
        }

        return names;
    }

    public static String[] attributeNames(Filter filter) {
        if (filter == null) {
            return null;
        }

        final Set set = new HashSet();
        traverse(filter,
            new DataUtilities.AbstractFilterVisitor() {
                public void visit(AttributeExpression attributeExpression) {
                    set.add(attributeExpression.getAttributePath());
                }
            });

        if (set.size() == 0) {
            return new String[0];
        }

        String[] names = new String[set.size()];
        int index = 0;

        for (Iterator i = set.iterator(); i.hasNext(); index++) {
            names[index] = (String) i.next();
        }

        return names;
    }

    public static void traverse(Filter filter, FilterVisitor visitor) {
        traverse(traverseDepth(filter), visitor);
    }

    /**
     * Performs a depth first traversal on Filter.
     * 
     * <p>
     * Filters can contain Expressions and other Filters, this method will call
     * visitor.visit( Filter ) and visitor.visit( Expression )
     * </p>
     *
     * @param set Set of Filter and Expression information
     * @param visitor Vistor to traverse across set
     */
    public static void traverse(Set set, final FilterVisitor visitor) {
        for (Iterator i = set.iterator(); i.hasNext();) {
            Object here = i.next();

            if (here instanceof BetweenFilter) {
                visitor.visit((BetweenFilter) here);
            } else if (here instanceof CompareFilter) {
                visitor.visit((CompareFilter) here);
            } else if (here instanceof GeometryFilter) {
                visitor.visit((GeometryFilter) here);
            } else if (here instanceof LikeFilter) {
                visitor.visit((LikeFilter) here);
            } else if (here instanceof LogicFilter) {
                visitor.visit((LogicFilter) here);
            } else if (here instanceof NullFilter) {
                visitor.visit((NullFilter) here);
            } else if (here instanceof FidFilter) {
                visitor.visit((FidFilter) here);
            } else if (here instanceof Filter) {
                visitor.visit((Filter) here);
            } else if (here instanceof AttributeExpression) {
                visitor.visit((AttributeExpression) here);
            } else if (here instanceof LiteralExpression) {
                visitor.visit((LiteralExpression) here);
            } else if (here instanceof MathExpression) {
                visitor.visit((MathExpression) here);
            } else if (here instanceof FunctionExpression) {
                visitor.visit((FunctionExpression) here);
            } else if (here instanceof Expression) {
                visitor.visit((Filter) here);
            }
        }
    }

    /**
     * Performs a depth first traversal of Filter.
     *
     * @param filter
     *
     * @return Set of Filters in traversing filter
     */
    public static Set traverseDepth(Filter filter) {
        final Set set = new HashSet();
        FilterVisitor traverse = new Traversal() {
                void traverse(Filter filter) {
                    set.add(filter);
                }

                void traverse(Expression expression) {
                    set.add(expression);
                }
            };

        filter.accept(traverse);

        return set;
    }

    /**
     * Compare operation for FeatureType.
     * 
     * <p>
     * Results in:
     * </p>
     * 
     * <ul>
     * <li>
     * 1: if typeA is a sub type/reorder/renamespace of typeB
     * </li>
     * <li>
     * 0: if typeA and typeB are the same type
     * </li>
     * <li>
     * -1: if typeA is not subtype of typeB
     * </li>
     * </ul>
     * 
     * <p>
     * Comparison is based on AttributeTypes, an IOException is thrown if the
     * AttributeTypes are not compatiable.
     * </p>
     * 
     * <p>
     * Namespace is not considered in this opperations. You may still need to
     * reType to get the correct namesapce, or reorder.
     * </p>
     *
     * @param typeA FeatureType beind compared
     * @param typeB FeatureType being compared against
     *
     * @return
     */
    public static int compare(FeatureType typeA, FeatureType typeB) {
        if (typeA == typeB) {
            return 0;
        }

        if (typeA == null) {
            return -1;
        }

        if (typeB == null) {
            return -1;
        }

        int countA = typeA.getAttributeCount();
        int countB = typeB.getAttributeCount();

        if (countA > countB) {
            return -1;
        }

        // may still be the same featureType
        // (Perhaps they differ on namespace?)
        AttributeType a;

        // may still be the same featureType
        // (Perhaps they differ on namespace?)
        AttributeType b;
        int match = 0;

        for (int i = 0; i < countA; i++) {
            a = typeA.getAttributeType(i);

            if (isMatch(a, typeB.getAttributeType(i))) {
                match++;
            } else if (isMatch(a, typeB.getAttributeType(a.getName()))) {
                // match was found in a different position
            } else {
                // cannot find any match for Attribute in typeA
                return -1;
            }
        }

        if ((countA == countB) && (match == countA)) {
            // all attributes in typeA agreed with typeB
            // (same order and type)
            if (typeA.getNamespace().equals(typeB.getNamespace())) {
                return 1;
            } else {
                // everything matches up
                return 0;
            }
        }

        return 1;
    }

    public static boolean isMatch(AttributeType a, AttributeType b) {
        if (a == b) {
            return true;
        }

        if (b == null) {
            return false;
        }

        if (a == null) {
            return false;
        }

        if (a.equals(b)) {
            return true;
        }

        if (a.getName().equals(b.getName())
                && a.getClass().equals(b.getClass())) {
            return true;
        }

        return false;
    }

    /**
     * Creates duplicate of feature adjusted to the provided featureType.
     *
     * @param featureType FeatureType requested
     * @param feature Origional Feature from DataStore
     *
     * @return An instance of featureType based on feature
     *
     * @throws IllegalAttributeException If opperation could not be performed
     */
    public static Feature reType(FeatureType featureType, Feature feature)
        throws IllegalAttributeException {
        FeatureType origional = feature.getFeatureType();

        if (featureType.equals(origional)) {
            return featureType.duplicate(feature);
        }

        String id = feature.getID();
        int numAtts = featureType.getAttributeCount();
        Object[] attributes = new Object[numAtts];
        String xpath;

        for (int i = 0; i < numAtts; i++) {
            AttributeType curAttType = featureType.getAttributeType(i);
            xpath = curAttType.getName();
            attributes[i] = curAttType.duplicate(feature.getAttribute(xpath));
        }

        return featureType.create(attributes, id);
    }

    /**
     * Constructs an empty feature to use as a Template for new content.
     * 
     * <p>
     * We may move this functionality to FeatureType.create( null )?
     * </p>
     *
     * @param featureType Type of feature we wish to create
     *
     * @return A new Feature of type featureType
     *
     * @throws IllegalAttributeException if we could not create featureType
     *         instance with acceptable default values
     */
    public static Feature template(FeatureType featureType)
        throws IllegalAttributeException {
        return featureType.create(defaultValues(featureType));
    }

    public static Feature template(FeatureType featureType, String featureID)
        throws IllegalAttributeException {
        return featureType.create(defaultValues(featureType), featureID);
    }

    public static Object[] defaultValues(FeatureType featureType)
        throws IllegalAttributeException {
        return defaultValues(featureType,null);
    }
    
    public static Feature template(FeatureType featureType,Object[] atts)
        throws IllegalAttributeException {
        return featureType.create(defaultValues(featureType,atts));
    }

    public static Feature template(FeatureType featureType, String featureID,Object[] atts)
        throws IllegalAttributeException {
        return featureType.create(defaultValues(featureType,atts), featureID);
    }
    
    public static Object[] defaultValues(FeatureType featureType,Object[] values) 
        throws IllegalAttributeException {
        if (values == null)
            values = new Object[featureType.getAttributeCount()];
        else if (values.length !=  featureType.getAttributeCount())
            throw new ArrayIndexOutOfBoundsException("values");
        for (int i = 0; i < featureType.getAttributeCount(); i++) {
            values[i] = defaultValue(featureType.getAttributeType(i));
        }
        return values;
    }

    /**
     * Provides a defautlValue for attributeType.
     * 
     * <p>
     * Will return null if attributeType isNillable(), or attempt to use
     * Reflection, or attributeType.parse( null )
     * </p>
     *
     * @param attributeType
     *
     * @return null for nillable attributeType, attempt at reflection
     *
     * @throws IllegalAttributeException If value cannot be constructed for
     *         attribtueType
     */
    public static Object defaultValue(AttributeType attributeType)
        throws IllegalAttributeException {
        if (attributeType.isNillable()) {
            return null;
        }

        // Flight of Fancy here - I need to get a non null value
        // lets try reflection
        //    
        Class type = attributeType.getType();
        Object value;

        try {
            Constructor constractor;
            constractor = type.getConstructor(new Class[0]);

            value = constractor.newInstance(new Object[0]);
            attributeType.validate(value);

            return value;
        } catch (Exception e) {
            // flight of fancy ended
        }

        try {
            value = attributeType.parse(null);

            if (value != null) {
                // hey the AttributeType new what to do!
                return value;
            }
        } catch (NullPointerException notReallyExpected) {
            // not sure if parse was expected to handle this
        }

        throw new IllegalAttributeException(
            "Could not create a default value for " + attributeType.getName());
    }

    /**
     * Creates a FeatureReader for testing.
     *
     * @param features Array of features
     *
     * @return FeatureReader spaning provided feature array
     *
     * @throws IOException If provided features Are null or empty
     */
    public static FeatureReader reader(final Feature[] features)
        throws IOException {
        if ((features == null) || (features.length == 0)) {
            throw new IOException("Provided features where empty");
        }

        return new FeatureReader() {
            Feature[] array = features;
            int offset = -1;

            public FeatureType getFeatureType() {
                return features[0].getFeatureType();
            }

            public Feature next() throws IOException {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more features");
                }
                return array[++offset];
            }

            public boolean hasNext() throws IOException {
                return (array != null) && (offset < (array.length - 1));
            }

            public void close() throws IOException {
                array = null;
                offset = -1;
            }
        };
    }
    public static FeatureResults results( final FeatureCollection collection ) throws IOException{
        if (collection.size() == 0) {
            throw new IOException("Provided collection was empty");
        }        
        return new FeatureResults(){
            public FeatureType getSchema() throws IOException {
                return collection.features().next().getFeatureType();                
            }

            public FeatureReader reader() throws IOException {
                return DataUtilities.reader(collection);
            }

            public Envelope getBounds() throws IOException {
                return collection.getBounds();
            }

            public int getCount() throws IOException {
                return collection.size();
            }

            public FeatureCollection collection() throws IOException {
                return collection;
            }
        };
    }
    public static FeatureReader reader( Collection collection) throws IOException {
        return reader(
            (Feature[]) collection.toArray( new Feature[ collection.size() ] )
        );    
    }
    public static FeatureCollection collection(Feature[] features) {
        FeatureCollection collection = FeatureCollections.newCollection();

        for (int i = 0; i < features.length; i++) {
            collection.add(features[i]);
        }

        return collection;
    }

    public static boolean attributesEqual(Object att, Object otherAtt){
	if (att == null) {
	    if (otherAtt != null) {
                return false;
            }
        } else {
            if (! att.equals(otherAtt)){
                if( att instanceof Geometry &&
                    otherAtt instanceof Geometry ){
                    // we need to special case Geometry
                    // as JTS is broken
                   // Geometry.equals( Object ) and Geometry.equals( Geometry )
                    // are different 
                    // (We should fold this knowledge into AttributeType...)
                    // 
                    if( !((Geometry)att).equals( (Geometry) otherAtt )){
                	return false;   
                    }
        	}
                else {
                    return false;
                }            
            }
        }
	
	return true;
    }

    public static FeatureType createSubType( FeatureType featureType, String properties[]) throws SchemaException{
        if( properties == null ){
            return featureType;
        }
        boolean same = featureType.getAttributeCount() == properties.length;
        for( int i=0; i<featureType.getAttributeCount() && same; i++ ){
                same = featureType.getAttributeType( i ).getName().equals( properties[i] );
        }
        if( same ){
            return featureType;
        }
        
        AttributeType types[] = new AttributeType[ properties.length ];
        for( int i=0; i<properties.length;i++){
            types[i]= featureType.getAttributeType( properties[i] );            
        }
        return FeatureTypeFactory.newFeatureType(
            types,
            featureType.getTypeName(),
            featureType.getNamespace()
        );
    }
    /**
     * Utility method for FeatureType construction.
     * 
     * <p>
     * Will parse a String of the form: <i>"name:Type,name2:Type2,..."</i>
     * </p>
     * 
     * <p>
     * Where <i>Type</i> is defined by createAttribute.
     * </p>
     * 
     * <p>
     * You may indicate the default Geometry with an astrix.
     * </p>
     * 
     * <p>
     * Example:<code>name:"",age:0,geom:Geometry,centroid:Point,url:java.io.URL"</code>
     * </p>
     *
     * @param identification identification of FeatureType:
     *        (<i>namesapce</i>).<i>typeName</i>
     * @param typeSpec Specification for FeatureType
     *
     * @return
     *
     * @throws SchemaException
     */
    public static FeatureType createType(String identification, String typeSpec)
        throws SchemaException {
        int split = identification.lastIndexOf('.');
        String namespace = (split == -1) ? null
                                         : identification.substring(0, split);
        String typeName = (split == -1) ? identification
                                        : identification.substring(split + 1);

        FeatureTypeFactory typeFactory = FeatureTypeFactory.newInstance(typeName);
        typeFactory.setNamespace(namespace);
        typeFactory.setName(typeName);

        String[] types = typeSpec.split(",");
        int geometryIndex = -1; // records * specified goemetry 
        AttributeType attributeType;
        AttributeType geometryAttribute = null; // records guess 

        for (int i = 0; i < types.length; i++) {
            if (types[i].startsWith("*")) {
                types[i] = types[i].substring(1);
                geometryIndex = i;
            }

            attributeType = createAttribute(types[i]);
            typeFactory.addType(attributeType);

            if (geometryIndex == i) {
                geometryAttribute = attributeType;
            } else if ((geometryIndex == -1) && (geometryAttribute == null)
                    && Geometry.class.isAssignableFrom(attributeType.getType())) {
                geometryAttribute = attributeType;
            }
        }

        if (geometryAttribute != null) {
            typeFactory.setDefaultGeometry(geometryAttribute);
        }

        return typeFactory.getFeatureType();
    }
    public static Feature parse( FeatureType type, String fid, String text[] )
        throws IllegalAttributeException {
        Object attributes[] = new Object[ text.length ];
        for( int i=0; i < text.length; i++ ){
            attributes[i] = type.getAttributeType(i).parse( text[i] );
        }
        return type.create( attributes, fid );            
    }
    /** Record typeSpec for the provided featureType */
    public static String spec( FeatureType featureType ){
        AttributeType types[] = featureType.getAttributeTypes();
        StringBuffer buf = new StringBuffer();
        for( int i=0; i<types.length; i++ ){
            buf.append( types[i].getName() );
            buf.append(":");
            buf.append( typeMap( types[i].getType() ) );
            if(i<types.length-1){
                buf.append(",");
            }
        }
        return buf.toString();
    }
    static Class type(String typeName) throws ClassNotFoundException {
        if (typeMap.containsKey(typeName)) {
            return (Class) typeMap.get(typeName);
        }
        return Class.forName(typeName);
    }
    static String typeMap( Class type){
        for( Iterator i=typeMap.entrySet().iterator(); i.hasNext(); ){
            Map.Entry entry = (Entry) i.next();
            if( entry.getValue().equals(type) ){
                return (String) entry.getKey();
            }
        }
        return type.getName();
    }
    /**
     * Returns AttributeType based on String specification (based on UML).
     * 
     * <p>
     * Will parse a String of the form: <i>"name:Type"</i>
     * </p>
     * 
     * <p>
     * Where <i>Type</i> is:
     * </p>
     * 
     * <ul>
     * <li>
     * 0,Interger,int: represents Interger
     * </li>
     * <li>
     * 0.0, Double, double: represents Double
     * </li>
     * <li>
     * "",String,string: represents String
     * </li>
     * <li>
     * Geometry: represents Geometry
     * </li>
     * <li>
     * <i>full.class.path</i>: represents java type
     * </li>
     * </ul>
     * 
     *
     * @param typeSpec
     *
     * @return
     *
     * @throws SchemaException If typeSpect could not be interpreted
     */
    static AttributeType createAttribute(String typeSpec)
        throws SchemaException {
        int split = typeSpec.indexOf(':');
        String name = typeSpec.substring(0, split);
        String type = typeSpec.substring(split + 1);

        try {
            return AttributeTypeFactory.newAttributeType(name, type(type));
        } catch (ClassNotFoundException e) {
            throw new SchemaException("Could not type " + name + " as:" + type);
        }
    }

    /**
     * A quick and dirty FilterVisitor.
     * 
     * <p>
     * This is useful when creating FilterVisitors for use with traverseDepth(
     * Filter, FilterVisitor ) method.
     * </p>
     * 
     * <p>
     * visit( Filter ) and visit( Expression ) will pass their arguments off to
     * more specialized functions.
     * </p>
     */
    public abstract static class AbstractFilterVisitor implements FilterVisitor {
        public void visit(Filter filter) {
            if (filter instanceof BetweenFilter) {
                visit((BetweenFilter) filter);
            } else if (filter instanceof CompareFilter) {
                visit((CompareFilter) filter);
            } else if (filter instanceof GeometryFilter) {
                visit((GeometryFilter) filter);
            } else if (filter instanceof LikeFilter) {
                visit((LikeFilter) filter);
            } else if (filter instanceof LogicFilter) {
                visit((LogicFilter) filter);
            } else if (filter instanceof NullFilter) {
                visit((NullFilter) filter);
            } else if (filter instanceof FidFilter) {
                visit((FidFilter) filter);
            }
        }

        public void visit(BetweenFilter betweenFilter) {
        }

        public void visit(CompareFilter comparefilter) {
        }

        public void visit(GeometryFilter geometryFilter) {
        }

        public void visit(LikeFilter likeFilter) {
        }

        public void visit(LogicFilter logicFilter) {
        }

        public void visit(NullFilter nullFilter) {
        }

        public void visit(FidFilter fidFilter) {
        }

        public void visit(AttributeExpression attributeExpression) {
        }

        public void visit(Expression expression) {
            if (expression instanceof AttributeExpression) {
                visit((AttributeExpression) expression);
            } else if (expression instanceof LiteralExpression) {
                visit((LiteralExpression) expression);
            } else if (expression instanceof MathExpression) {
                visit((MathExpression) expression);
            } else if (expression instanceof FunctionExpression) {
                visit((FunctionExpression) expression);
            }
        }

        public void visit(LiteralExpression literalExpression) {
        }

        public void visit(MathExpression mathExpression) {
        }

        public void visit(FunctionExpression functionExpression) {
        }
    }

    public abstract static class Traversal extends AbstractFilterVisitor {
        abstract void traverse(Filter filter);

        abstract void traverse(Expression expression);

        public void visit(BetweenFilter betweenFilter) {
            traverse(betweenFilter.getLeftValue());
            visit(betweenFilter.getLeftValue());

            traverse(betweenFilter.getMiddleValue());
            visit(betweenFilter.getMiddleValue());

            traverse(betweenFilter.getRightValue());
            visit(betweenFilter.getRightValue());
        }

        public void visit(CompareFilter compareFilter) {
            traverse(compareFilter.getLeftValue());
            visit(compareFilter.getLeftValue());

            traverse(compareFilter.getRightValue());
            visit(compareFilter.getRightValue());
        }

        public void visit(GeometryFilter geometryFilter) {
            traverse(geometryFilter.getLeftGeometry());
            visit(geometryFilter.getLeftGeometry());

            traverse(geometryFilter.getRightGeometry());
            visit(geometryFilter.getRightGeometry());
        }

        public void visit(LikeFilter likeFilter) {
            traverse(likeFilter.getValue());
            visit(likeFilter.getValue());
        }

        public void visit(LogicFilter logicFilter) {
            for (Iterator i = logicFilter.getFilterIterator(); i.hasNext();) {
                traverse((Expression) i.next());
                visit((Expression) i.next());
            }
        }

        public void visit(NullFilter nullFilter) {
            traverse(nullFilter.getNullCheckValue());
            visit(nullFilter.getNullCheckValue());
        }

        public void visit(MathExpression mathExpression) {
            traverse(mathExpression.getLeftValue());
            visit(mathExpression.getLeftValue());

            traverse(mathExpression.getRightValue());
            visit(mathExpression.getRightValue());
        }

        public void visit(FunctionExpression functionExpression) {
            Expression[] args = functionExpression.getArgs();

            for (int i = 0; i < args.length; i++) {
                traverse(args[i]);
                visit(args[i]);
            }
        }
    }
}

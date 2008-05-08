/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data.complex.config;

import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDTypeDefinition;
import org.geotools.feature.iso.Types;
import org.geotools.feature.type.TypeName;
import org.geotools.gml3.GML;
import org.geotools.test.TestData;
import org.geotools.xs.XS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class EmfAppSchemaReaderTest extends TestCase {

    /**
     * Namespace URI of parsed types
     */
    private static final String NS_URI = "http://online.socialchange.net.au";

    private EmfAppSchemaReader schemaLoader;

    protected void setUp() throws Exception {
        super.setUp();
        schemaLoader = EmfAppSchemaReader.newInstance();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        schemaLoader = null;
    }

    public void testParseSimpleFeatureType() throws Exception {
        String res = "/test-data/simpleFeature.xsd";
        URL resource = getClass().getResource(res);

        schemaLoader.parse(resource);

        Map parsedTypes = schemaLoader.getTypeRegistry();
        assertNotNull(parsedTypes);

        TypeName typeName = new TypeName(NS_URI, "simpleFeatureType");
        AttributeType type = (AttributeType) parsedTypes.get(typeName);
        assertNotNull(type);
        assertTrue(type.getClass().getName(), type instanceof SimpleFeatureType);
        assertTrue(type.getUserData(XSDTypeDefinition.class) instanceof XSDComplexTypeDefinition);

        SimpleFeatureType ft = (SimpleFeatureType) type;
        String local = ft.getName().getLocalPart();
        String uri = ft.getName().getNamespaceURI();
        assertEquals("simpleFeatureType", local);
        assertEquals(NS_URI, uri);

        List/* <AttributeType> */attributes = (List) ft.getProperties();
        assertEquals(8, attributes.size());
        AttributeDescriptor descriptor;

        descriptor = (AttributeDescriptor) attributes.get(5);
        org.opengis.feature.type.Name name = Types.attributeName(NS_URI, "the_geom");
        typeName = new TypeName(GML.NAMESPACE, "GeometryPropertyType");
        assertTrue(descriptor.getType() instanceof GeometryType);

        assertSimpleAttribute(descriptor, name, typeName, Geometry.class, 1, 1);

        descriptor = (AttributeDescriptor) attributes.get(6);
        name = Types.attributeName(NS_URI, "stringAtt");
        typeName = new TypeName(XS.NAMESPACE, XS.STRING.getLocalPart());

        assertSimpleAttribute(descriptor, name, typeName, String.class, 1, 1);

        descriptor = (AttributeDescriptor) attributes.get(7);
        name = Types.attributeName(NS_URI, "intAtt");
        typeName = new TypeName(XS.NAMESPACE, XS.INT.getLocalPart());
        assertSimpleAttribute(descriptor, name, typeName, Integer.class, 1, 1);
    }

    private void assertSimpleAttribute(AttributeDescriptor descriptor,
            org.opengis.feature.type.Name name, org.opengis.feature.type.TypeName typeName,
            Class binding, int minOccurs, int maxOccurs) {
        AttributeType type;
        assertEquals(name, descriptor.getName());
        assertEquals(minOccurs, descriptor.getMinOccurs());
        assertEquals(maxOccurs, descriptor.getMaxOccurs());
        assertTrue(descriptor.getUserData(XSDElementDeclaration.class) instanceof XSDElementDeclaration);

        type = descriptor.getType();
        assertNotNull(type);
        assertFalse(type instanceof ComplexType);
        assertEquals(typeName, type.getName());
        assertEquals(binding, type.getBinding());
        // they're prebuilt types, does not contains the emf information
        // assertTrue(type.getUserData(EmfAppSchemaReader.EMF_USERDATA_KEY)
        // instanceof XSDTypeDefinition);
    }

    public void testComplexFeatureType() throws Exception {
        String res = "/test-data/complexFeature.xsd";
        URL resource = getClass().getResource(res);
        schemaLoader.parse(resource);

        Map registry = schemaLoader.getTypeRegistry();
        assertNotNull(registry);

        TypeName typeName = new TypeName(NS_URI, "wq_plus_Type");
        AttributeType type = (AttributeType) registry.get(typeName);
        assertTrue(type instanceof FeatureType);
        assertFalse(type instanceof SimpleFeatureType);
        assertEquals(typeName, type.getName());
        assertTrue(type.getUserData(XSDTypeDefinition.class) instanceof XSDComplexTypeDefinition);

        FeatureType wq_plus_Type = (FeatureType) type;

        assertNotNull(wq_plus_Type.getDefaultGeometry());
        assertNotNull(wq_plus_Type.getSuper());
        typeName = new TypeName(GML.NAMESPACE, GML.AbstractFeatureType.getLocalPart());
        assertEquals(typeName, wq_plus_Type.getSuper().getName());
        assertNotNull(wq_plus_Type.getProperties());
        assertEquals(8, wq_plus_Type.getProperties().size());

        org.opengis.feature.type.Name name = Types.attributeName(NS_URI, "wq_plus");
        AttributeDescriptor wqPlusDescriptor = (AttributeDescriptor) registry.get(name);
        assertNotNull(wqPlusDescriptor);
        assertEquals(name, wqPlusDescriptor.getName());
        assertSame(wq_plus_Type, wqPlusDescriptor.getType());
        assertTrue(wqPlusDescriptor.getUserData(XSDElementDeclaration.class) instanceof XSDElementDeclaration);

        typeName = new TypeName(NS_URI, "measurementType");
        type = (AttributeType) registry.get(typeName);
        assertTrue(type instanceof ComplexType);
        assertFalse(type instanceof FeatureType);
        assertTrue(type.getUserData(XSDTypeDefinition.class) instanceof XSDComplexTypeDefinition);

        ComplexType measurementType = (ComplexType) type;
        assertEquals(typeName, measurementType.getName());
        assertTrue(measurementType.isIdentified());
        assertFalse(measurementType.isAbstract());
        assertEquals(2, measurementType.getProperties().size());

        name = Types.attributeName(NS_URI, "measurement");
        AttributeDescriptor descriptor;
        descriptor = (AttributeDescriptor) Types.descriptor(wq_plus_Type, name);
        assertNotNull(descriptor);
        assertEquals(name, descriptor.getName());
        assertNotNull(descriptor.getType());
        assertSame(measurementType, descriptor.getType());
        assertEquals(0, descriptor.getMinOccurs());
        assertEquals(Integer.MAX_VALUE, descriptor.getMaxOccurs());
        assertTrue(descriptor.getUserData(XSDElementDeclaration.class) instanceof XSDElementDeclaration);

        name = Types.attributeName(NS_URI, "result");
        descriptor = (AttributeDescriptor) Types.descriptor(measurementType, name);
        typeName = new TypeName(XS.NAMESPACE, XS.FLOAT.getLocalPart());
        assertSimpleAttribute(descriptor, name, typeName, Float.class, 1, 1);

        name = Types.attributeName(NS_URI, "determinand_description");
        descriptor = (AttributeDescriptor) Types.descriptor(measurementType, name);
        typeName = new TypeName(XS.NAMESPACE, XS.STRING.getLocalPart());
        assertSimpleAttribute(descriptor, name, typeName, String.class, 1, 1);

        name = Types.attributeName(NS_URI, "the_geom");
        descriptor = (AttributeDescriptor) Types.descriptor(wq_plus_Type, name);
        typeName = new TypeName(GML.NAMESPACE, GML.PointPropertyType.getLocalPart());
        assertSimpleAttribute(descriptor, name, typeName, Point.class, 1, 1);

        name = Types.attributeName(NS_URI, "sitename");
        descriptor = (AttributeDescriptor) Types.descriptor(wq_plus_Type, name);
        typeName = new TypeName(XS.NAMESPACE, XS.STRING.getLocalPart());
        assertSimpleAttribute(descriptor, name, typeName, String.class, 1, Integer.MAX_VALUE);

    }

    public void testSimpleAttributeFromComplexDeclaration() throws Exception {
        String res = "/test-data/complexFeature.xsd";
        URL resource = getClass().getResource(res);
        schemaLoader.parse(resource);

        Map registry = schemaLoader.getTypeRegistry();

        org.opengis.feature.type.TypeName tcl = Types.typeName(NS_URI, "TypedCategoryListType");
        AttributeType typedCategoryListType = (AttributeType) registry.get(tcl);
        assertNotNull(typedCategoryListType);
        assertFalse(typedCategoryListType instanceof ComplexType);
        
        AttributeType superType = typedCategoryListType.getSuper();
        assertNotNull(superType);
        org.opengis.feature.type.TypeName superName = superType.getName();
        assertEquals(XS.STRING.getNamespaceURI(), superName.getNamespaceURI());
        assertEquals(XS.STRING.getLocalPart(), superName.getLocalPart());
        
        assertNotNull(typedCategoryListType.getUserData(XSDTypeDefinition.class));
    }
}

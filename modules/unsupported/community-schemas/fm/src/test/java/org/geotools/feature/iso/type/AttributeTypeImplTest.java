/*
 * AttributeTypeTest.java
 * JUnit based test
 *
 * Created on July 18, 2003, 12:56 PM
 */

package org.geotools.feature.iso.type;

import java.util.Collections;

import junit.framework.TestCase;

import org.geotools.feature.iso.Types;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeName;

public class AttributeTypeImplTest extends TestCase {
    
	AttributeType type(TypeName name, Class binding) {
    	return new AttributeTypeImpl(
		  name, binding, false, false, Collections.EMPTY_SET, null, null
	   );
    }
    
   public void testGetName(){
	   AttributeType type = type(Types.typeName("testType"), Object.class);
	   
        assertEquals("testType", type.getName().getLocalPart());
        assertEquals(Types.typeName("testType"), type.getName());
        
        type = type(Types.typeName("testNamespace","testType"), Object.class);
 		 
 	     assertEquals("testType", type.getName().getLocalPart());
         assertEquals("testNamespace", type.getName().getNamespaceURI());
         assertEquals(Types.typeName("testNamespace","testType"), type.getName());
    }

    public void testGetBinding(){
    	AttributeType type = type(Types.typeName("testType"), String.class);
		 
    	assertEquals(String.class, type.getBinding());
    }
    
    
    public void testEquals(){
        AttributeType typeA = type(Types.typeName("testType"), Double.class);
        AttributeType typeB = type(Types.typeName("testType"), Double.class);
        
        AttributeType typeC = type(Types.typeName("differnetName"), Double.class);
        AttributeType typeD = type(Types.typeName("testType"), Integer.class);
        AttributeType typeE = type(Types.typeName("secondDifferentName"), Integer.class);
        AttributeType typeF = type(Types.typeName("secondDifferentName"), Integer.class);

        assertTrue(typeA.equals(typeA));
        assertTrue(typeA.equals(typeB));
        assertTrue(typeE.equals(typeF));
        
        assertFalse(typeA.equals(typeC));
        assertFalse(typeA.equals(typeD));
        assertFalse(typeA.equals(null));
        assertFalse(typeA.equals(typeE));
    }
    
    
}

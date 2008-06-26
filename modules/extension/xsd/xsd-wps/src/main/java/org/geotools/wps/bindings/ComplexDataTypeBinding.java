/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.wps.bindings;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.wps.ComplexDataType;
import net.opengis.wps.WpsFactory;

import org.eclipse.emf.ecore.util.FeatureMap;
import org.geotools.geometry.jts.JTS;
import org.geotools.gml2.GML;
import org.geotools.wps.WPS;
import org.geotools.xml.ComplexEMFBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class ComplexDataTypeBinding extends ComplexEMFBinding
{
    private WpsFactory factory;

    public ComplexDataTypeBinding(WpsFactory factory)
    {
        super(factory, WPS.ComplexDataType);
        this.factory = factory;
    }

    /*
    	Would need to look at the contained values to detect the correct
    	order.
    */
    public List<List<Object>> getProperties(Object obj)
    {
    	ComplexDataType data = (ComplexDataType)obj;

    	List/*<List<Object>>*/ properties = new ArrayList/*<List<Object>>*/();

    	List<?> features = data.getData();

    	for(Object obj0 : features)	// XXX TEST
    	{
            if ( obj0 instanceof Point ) {
                properties.add( new Object[]{ GML.Point, obj0 } );
            }
            else if ( obj0 instanceof Polygon ) {
                properties.add( new Object[]{ GML.Polygon, obj0 } );
            }
    	}
    	return properties;
    }

    public QName getTarget()
    {
        return WPS.ComplexDataType;
    }

    public Class<?> getType()
    {
        return ComplexDataType.class;
    }

    /*
    	NodeImpl -> JTS.Polygon
    */
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception
    {
        Object parsed = super.parse(instance, node, value);

        // XXX DEBUG +
        String d = parsed.getClass().toString();
        List<Object> b = node.getChildren();
        Object c = b.get(1);
        String e = c.getClass().toString();
        // XXX -

    	return parsed;
    }
}

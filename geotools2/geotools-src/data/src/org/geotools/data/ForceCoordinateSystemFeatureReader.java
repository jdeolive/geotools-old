/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003, Geotools Project Managment Committee (PMC)
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
package org.geotools.data;

import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.opengis.sc.CoordinateReferenceSystem;
import java.io.IOException;
import java.util.NoSuchElementException;


/**
 * ForceCoordinateSystemFeatureReader provides a CoordinateReferenceSystem for
 * FeatureTypes.
 * 
 * <p>
 * ForceCoordinateSystemFeatureReader is a wrapper used to force
 * GeometryAttributes to a user supplied CoordinateReferenceSystem rather then
 * the default supplied by the DataStore.
 * </p>
 * 
 * <p>
 * Example Use:
 * <pre><code>
 * ForceCoordinateSystemFeatureReader reader =
 *     new ForceCoordinateSystemFeatureReader( origionalReader, forceCS );
 * 
 * CoordinateReferenceSystem orgionalCS =
 *     origionalReader.getFeatureType().getDefaultGeometry().getCoordianteSystem();
 * 
 * CoordinateReferenceSystem newCS =
 *     reader.getFeatureType().getDefaultGeometry().getCoordianteSystem();
 * 
 * assertEquals( forceCS, newCS );
 * </code></pre>
 * </p>
 *
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: ForceCoordinateSystemFeatureReader.java,v 1.1 2003/12/19 01:05:08 jive Exp $
 */
public class ForceCoordinateSystemFeatureReader implements FeatureReader {
    private FeatureReader reader;
    private FeatureType schema;
    private CoordinateReferenceSystem coordianteSystem;

    public ForceCoordinateSystemFeatureReader(FeatureReader reader,
        CoordinateReferenceSystem cs) throws SchemaException {
        if (cs == null) {
            throw new NullPointerException("CoordinateSystem required");
        }

        FeatureType type = reader.getFeatureType();
        CoordinateReferenceSystem origional = type.getDefaultGeometry()
                                                  .getCoordinateSystem();

        if (cs.equals(origional)) {
            throw new IllegalArgumentException("CoordinateSystem " + cs
                + " already used (check before using wrapper)");
        }

        coordianteSystem = cs;

        FeatureTypeFactory typeFactory = FeatureTypeFactory.newInstance(type
                .getTypeName());
                        
        typeFactory.setNamespace(type.getNamespace());
        typeFactory.setName(type.getTypeName());
        
        GeometryAttributeType defaultGeometryType = null;
        for( int i=0; i<type.getAttributeCount(); i++ ){
            AttributeType attributeType = type.getAttributeType( i );
            if( attributeType instanceof GeometryAttributeType ){
                GeometryAttributeType geometryType =
                    (GeometryAttributeType) attributeType;
                GeometryAttributeType forcedGeometry;
                    
                ;
                forcedGeometry = (GeometryAttributeType)
                    AttributeTypeFactory.newAttributeType(
                        geometryType.getName(),
                        geometryType.getClass(),
                        geometryType.isNillable(),
                        geometryType.getFieldLength(),
                        geometryType.createDefaultValue(),
                        cs
                    );
                if( defaultGeometryType == null ||
                    geometryType == type.getDefaultGeometry() ){
                    defaultGeometryType = forcedGeometry;                        
                }
                typeFactory.addType( forcedGeometry );                
            }
            else {
                typeFactory.addType( attributeType );
            }            
        }
        typeFactory.setDefaultGeometry( defaultGeometryType );
        schema = typeFactory.getFeatureType();
        this.reader = reader;
    }

    /**
     * Implement getFeatureType.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @return
     *
     * @see org.geotools.data.FeatureReader#getFeatureType()
     */
    public FeatureType getFeatureType() {
        if( schema == null ){
            throw new IllegalStateException("Reader has already been closed");
        }
        return schema;
    }

    /**
     * Implement next.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @return
     *
     * @throws IOException
     * @throws IllegalAttributeException
     * @throws NoSuchElementException
     *
     * @see org.geotools.data.FeatureReader#next()
     */
    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        if( reader == null ){
            throw new IllegalStateException("Reader has already been closed");
        }            
        Feature next = reader.next();            
        return schema.create( next.getAttributes( null ), next.getID() );
    }

    /**
     * Implement hasNext.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @return
     *
     * @throws IOException
     *
     * @see org.geotools.data.FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        if( reader == null ){
            throw new IllegalStateException("Reader has already been closed");
        }        
        return reader.hasNext();
    }

    /**
     * Implement close.
     * 
     * <p>
     * Description ...
     * </p>
     *
     * @throws IOException
     *
     * @see org.geotools.data.FeatureReader#close()
     */
    public void close() throws IOException {
        if( reader == null ){
            throw new IllegalStateException("Reader has already been closed");
        }
        reader.close();
        reader = null;
        schema = null;
    }
}

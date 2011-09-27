/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008-2011 TOPP - www.openplans.org.
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
package org.geotools.process.feature.gs;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.gs.GSProcess;
import org.geotools.process.gs.WrappingIterator;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.collection.DecoratingSimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.util.Converters;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * Buffers a feature collection using a certain distance
 * 
 * @author Gianni Barrotta - Sinergis
 * @author Andrea Di Nora - Sinergis
 * @author Pietro Arena - Sinergis
 * @author Andrea Aime - GeoSolutions
 *
 * @source $URL$
 */
@DescribeProcess(title = "buffer", description = "Buffers each feature in a collection by a fixed amount or by a value coming from a feature attribute. Works in pure cartesian mode.")
public class BufferFeatureCollection implements GSProcess {
    @DescribeResult(description = "The buffered feature collection")
    public SimpleFeatureCollection execute(
            @DescribeParameter(name = "feature collection", description = "Feature collection") SimpleFeatureCollection features,
            @DescribeParameter(name = "width of the buffer", description = "The width of the buffer") Double distance,
            @DescribeParameter(name = "name of the layer attribute containing the width of the buffer", description = "Name of the layer attribute",min=0) String attribute) {

        if (distance == null && (attribute == null || attribute == "")) {
            throw new IllegalArgumentException("Buffer distance was not specified");
        } 

        if(attribute != null && !"".equals(attribute)) {
            if(features.getSchema().getDescriptor(attribute) == null) {
                boolean found = false;
                // case insensitive search
                for (AttributeDescriptor ad : features.getSchema().getAttributeDescriptors()) {
                    if(ad.getLocalName().equals(attribute)) {
                        attribute = ad.getLocalName();
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    throw new IllegalArgumentException("Attribute not found among the source collection ones: " + attribute);
                }
            }
        } else {
            attribute = null;
        }
        return new BufferedFeatureCollection(features, attribute, distance);
    }

    /**
     * Wrapper that will trigger the buffer computation as features are requested
     */
    static class BufferedFeatureCollection extends DecoratingSimpleFeatureCollection {

        Double distance;

        String attribute;

        SimpleFeatureType schema;

        public BufferedFeatureCollection(SimpleFeatureCollection delegate, String attribute,
                Double distance) {
            super(delegate);
            this.distance = distance;
            this.attribute = attribute;

            // create schema
            SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
            for (AttributeDescriptor descriptor : delegate.getSchema().getAttributeDescriptors()) {
                if (!(descriptor.getType() instanceof GeometryTypeImpl)
                        || (!delegate.getSchema().getGeometryDescriptor().equals(descriptor))) {
                    tb.add(descriptor);
                } else {
                    AttributeTypeBuilder builder = new AttributeTypeBuilder();
                    builder.setBinding(MultiPolygon.class);
                    AttributeDescriptor attributeDescriptor = builder.buildDescriptor(descriptor
                            .getLocalName(), builder.buildType());
                    tb.add(attributeDescriptor);
                    if(tb.getDefaultGeometry() == null) {
                        tb.setDefaultGeometry(descriptor.getLocalName());
                    }
                }
            }
            tb.setDescription(delegate.getSchema().getDescription());
            tb.setCRS(delegate.getSchema().getCoordinateReferenceSystem());
            tb.setName(delegate.getSchema().getName());
            this.schema = tb.buildFeatureType();
        }

        @Override
        public SimpleFeatureIterator features() {
            return new BufferedFeatureIterator(delegate, this.attribute, this.distance, getSchema());
        }

        @Override
        public Iterator<SimpleFeature> iterator() {
            return new WrappingIterator(features());
        }

        @Override
        public void close(Iterator<SimpleFeature> close) {
            if (close instanceof WrappingIterator) {
                ((WrappingIterator) close).close();
            }
        }

        @Override
        public SimpleFeatureType getSchema() {
            return this.schema;
        }
    }

    /**
     * Buffers each feature as we scroll over the collection
     */
    static class BufferedFeatureIterator implements SimpleFeatureIterator {
        SimpleFeatureIterator delegate;

        SimpleFeatureCollection collection;

        Double distance;

        String attribute;

        int count;

        SimpleFeatureBuilder fb;

        SimpleFeature next;

        public BufferedFeatureIterator(SimpleFeatureCollection delegate, String attribute,
                Double distance, SimpleFeatureType schema) {
            this.delegate = delegate.features();
            this.distance = distance;
            this.collection = delegate;
            this.attribute = attribute;
            fb = new SimpleFeatureBuilder(schema);
        }

        public void close() {
            delegate.close();
        }

        public boolean hasNext() {
            while (next == null && delegate.hasNext()) {
                SimpleFeature f = delegate.next();
                for (Object value : f.getAttributes()) {
                    if (value instanceof Geometry) {
                        Double fDistance = distance;
                        if(this.attribute != null) {
                            fDistance = Converters.convert(f.getAttribute(this.attribute), Double.class);
                        }
                        if(fDistance != null && fDistance != 0.0) {
                            value = ((Geometry) value).buffer(fDistance);
                        } 
                    }
                    fb.add(value);
                }
                next = fb.buildFeature("" + count);
                count++;
                fb.reset();
            }
            return next != null;
        }

        public SimpleFeature next() throws NoSuchElementException {
            if (!hasNext()) {
                throw new NoSuchElementException("hasNext() returned false!");
            }
            SimpleFeature result = next;
            next = null;
            return result;
        }

    }
}

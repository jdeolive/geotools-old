/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data.feature.adapter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.collection.AbstractSimpleFeatureCollection;
import org.geotools.feature.iso.type.AttributeDescriptorImpl;
import org.opengis.feature.simple.SimpleFeatureFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

/**
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class FeatureCollectionAdapter extends AbstractSimpleFeatureCollection {

	private FeatureCollection gtFeatures;

	private SimpleFeatureType isoType;
    
    private SimpleFeatureFactory attributeFactory;
	
    private int maxFeatures = Integer.MAX_VALUE;
    
    private AttributeDescriptor featureDescriptor;
    
    public FeatureCollectionAdapter(SimpleFeatureType isoType, FeatureCollection features, SimpleFeatureFactory attributeFactory) {
        super(null, null);
		this.isoType = isoType;
		this.gtFeatures = features;
		
        Name typeName = isoType.getName();
        Name name = Types.typeName(typeName.getNamespaceURI(), typeName.getLocalPart());
        featureDescriptor = new AttributeDescriptorImpl(isoType, name, 0, Integer.MAX_VALUE, true, null);
    }

//	public Iterator iterator() {
//		final Iterator gtFeatureIterator = gtFeatures.iterator();
//
//		Iterator isoFeatures = new Iterator() {
//            int featureCount = 0;
//			public boolean hasNext() {
//				return featureCount <= maxFeatures && gtFeatureIterator.hasNext();
//			}
//
//			public Object next() {
//                featureCount++;
//				Feature gtFeature = (Feature) gtFeatureIterator.next();
//				org.opengis.feature.Feature isoFeature;
//				isoFeature = new ISOFeatureAdapter(gtFeature, isoType, attributeFactory, featureDescriptor);
//				return isoFeature;
//			}
//
//			public void remove() {
//				gtFeatureIterator.remove();
//			}
//
//		};
//		return isoFeatures;
//	}

	public int size() {
		return gtFeatures.size();
	}

    public int getMaxFeatures() {
        return maxFeatures;
    }

    public void setMaxFeatures(int maxFeatures) {
        this.maxFeatures = maxFeatures;
    }

    protected void closeIterator(Iterator close) throws IOException {
        FeatureIteratorWrapper wrapper = (FeatureIteratorWrapper) close;
        wrapper.close();
    }

    protected Iterator openIterator() throws IOException {
        final FeatureIterator gtFeatureIterator = gtFeatures.features();
        final FeatureIteratorWrapper wrapper = new FeatureIteratorWrapper(gtFeatureIterator);
        return wrapper;
    }

    public Object operation(Name arg0, List arg1) {
        return null;
    }

    private class FeatureIteratorWrapper implements Iterator{
        final FeatureIterator gtFeatureIterator;
        int featureCount = 0;
    
        public FeatureIteratorWrapper(FeatureIterator gtFeatureIterator){
            this.gtFeatureIterator = gtFeatureIterator;
        }

        public boolean hasNext() {
            return featureCount <= maxFeatures && gtFeatureIterator.hasNext();
        }

        public Object next() {
            featureCount++;
            Feature gtFeature = (Feature) gtFeatureIterator.next();
            org.opengis.feature.Feature isoFeature;
            isoFeature = new ISOFeatureAdapter(gtFeature, isoType, attributeFactory,
                    featureDescriptor);
            return isoFeature;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        public void close(){
            this.gtFeatureIterator.close();
        }

    }

    public List get(Name name) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}

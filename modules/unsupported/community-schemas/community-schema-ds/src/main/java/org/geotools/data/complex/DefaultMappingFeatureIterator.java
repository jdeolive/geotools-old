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

package org.geotools.data.complex;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.data.Query;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.feature.AttributeBuilder;
import org.geotools.feature.Types;
import org.geotools.feature.ValidatingFeatureFactoryImpl;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;

/**
 * A Feature iterator that operates over the FeatureSource of a
 * {@linkplain org.geotools.data.complex.FeatureTypeMapping} and produces Features of the output
 * schema by applying the mapping rules to the Features of the source schema.
 * <p>
 * This iterator acts like a one-to-one mapping, producing a Feature of the target type for each
 * feature of the source type. For a one-to-many iterator see
 * {@linkplain org.geotools.data.complex.GroupingFeatureIterator}
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL:
 *         http://svn.geotools.org/branches/2.4.x/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/DefaultMappingFeatureIterator.java $
 * @since 2.4
 */
class DefaultMappingFeatureIterator extends AbstractMappingFeatureIterator {

    /**
     * maxFeatures restriction value as provided by query
     */
    private final int maxFeatures;

    /** counter to ensure maxFeatures is not exceeded */
    private int featureCounter;

    public DefaultMappingFeatureIterator(ComplexDataStore store, FeatureTypeMapping mapping,
            Query query) throws IOException {
        super(store, mapping, query, new ValidatingFeatureFactoryImpl());
        maxFeatures = query.getMaxFeatures();
    }

    public Feature next() {
        try {
            return computeNext();
        } catch (IOException e) {
            close();
            throw new RuntimeException(e);
        }
    }

    public boolean hasNext() {
        return featureCounter < maxFeatures && sourceFeatures != null && sourceFeatures.hasNext();
    }

    protected Query getUnrolledQuery(Query query) {
        return store.unrollQuery(query, mapping);
    }

    private Feature computeNext() throws IOException {
        ComplexAttribute sourceInstance = (ComplexAttribute) sourceFeatures.next();
        final AttributeDescriptor targetNode = mapping.getTargetFeature();
        final Name targetNodeName = targetNode.getName();
        final List mappings = mapping.getAttributeMappings();
        String id = super.extractIdForFeature(sourceInstance);
        AttributeBuilder builder = new AttributeBuilder(attf);
        builder.setDescriptor(targetNode);
        Feature target = (Feature) builder.build(id);
        for (Iterator itr = mappings.iterator(); itr.hasNext();) {
            AttributeMapping attMapping = (AttributeMapping) itr.next();
            StepList targetXpathProperty = attMapping.getTargetXPath();
            if (targetXpathProperty.size() == 1) {
                Step rootStep = (Step) targetXpathProperty.get(0);
                QName stepName = rootStep.getName();
                if (Types.equals(targetNodeName, stepName)) {
                    // ignore the top level mapping for the Feature itself
                    // as it was already set
                    continue;
                }
            }
            setSingleValuedAttribute(target, sourceInstance, attMapping);
        }
        featureCounter++;
        if (!hasNext()) {
            close();
        }
        return target;
    }

}

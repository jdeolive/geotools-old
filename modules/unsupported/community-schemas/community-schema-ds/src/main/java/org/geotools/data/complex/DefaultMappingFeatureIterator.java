/*
 *    GeoTools - OpenSource mapping toolkit
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
 */

package org.geotools.data.complex;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.geotools.data.Query;
import org.geotools.data.complex.filter.XPath;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.feature.iso.AttributeBuilder;
import org.geotools.feature.iso.Types;
import org.geotools.filter.FilterFactoryImplNamespaceAware;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * A Feature iterator that operates over the FeatureSource of a
 * {@linkplain org.geotools.data.complex.FeatureTypeMapping} and produces
 * Features of the output schema by applying the mapping rules to the Features
 * of the source schema.
 * <p>
 * This iterator acts like a one-to-one mapping, producing a Feature of the
 * target type for each feature of the source type. For a one-to-many iterator
 * see {@linkplain org.geotools.data.complex.GroupingFeatureIterator}
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
class DefaultMappingFeatureIterator extends AbstractMappingFeatureIterator {

    private XPath xpathAttributeBuilder;

    public DefaultMappingFeatureIterator(ComplexDataStore store, FeatureTypeMapping mapping,
            Query query) throws IOException {
        super(store, mapping, query);
        xpathAttributeBuilder = new XPath();
        xpathAttributeBuilder.setFeatureFactory(super.attf);

        NamespaceSupport namespaces = mapping.getNamespaces();
        // FilterFactory namespaceAwareFilterFactory =
        // CommonFactoryFinder.getFilterFactory(hints);
        FilterFactory namespaceAwareFilterFactory = new FilterFactoryImplNamespaceAware(namespaces);
        xpathAttributeBuilder.setFilterFactory(namespaceAwareFilterFactory);
    }

    public Object/* Feature */next() {
        Feature currentFeature = computeNext();
        return currentFeature;
    }

    public boolean hasNext() {
        return sourceFeatures.hasNext();
    }

    protected Query getUnrolledQuery(Query query) {
        return store.unrollQuery(query, mapping);
    }

    private Feature computeNext() {
        ComplexAttribute sourceInstance = (ComplexAttribute) this.sourceFeatures.next();
        final AttributeDescriptor targetNode = mapping.getTargetFeature();
        final Name targetNodeName = targetNode.getName();
        final List mappings = mapping.getAttributeMappings();
        final FeatureType targetType = (FeatureType) targetNode.getType();

        String id = super.extractIdForFeature(sourceInstance);

        AttributeBuilder builder = new AttributeBuilder(attf);
        builder.setDescriptor(targetNode);

        Feature mapped = (Feature) builder.build(id);

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

            Expression sourceExp = attMapping.getSourceExpression();
            AttributeType targetNodeType = attMapping.getTargetNodeInstance();

            Object value = super.getValue(sourceExp, sourceInstance);
            id = extractIdForAttribute(attMapping, sourceInstance);

            xpathAttributeBuilder.set(mapped, targetXpathProperty, value, id, targetNodeType);
        }

        return mapped;
    }

}

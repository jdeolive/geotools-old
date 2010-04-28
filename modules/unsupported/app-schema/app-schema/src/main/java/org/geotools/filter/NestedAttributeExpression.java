/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.geotools.data.complex.AppSchemaDataAccessRegistry;
import org.geotools.data.complex.AttributeMapping;
import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.data.complex.NestedAttributeMapping;
import org.geotools.data.complex.filter.XPath;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.factory.Hints;
import org.geotools.feature.FeatureImpl;
import org.geotools.feature.Types;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.expression.FeaturePropertyAccessorFactory;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.type.Name;
import org.opengis.filter.expression.Expression;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * This class represents a list of expressions broken up from a single XPath expression that is
 * nested in more than one feature. The purpose is to allow filtering these attributes on the parent
 * feature.
 * 
 * @author Rini Angreani, CSIRO Earth Science and Resource Engineering
 * 
 * @source $URL:
 *         http://svn.osgeo.org/geotools/trunk/modules/unsupported/app-schema/app-schema/src/main
 *         /java/org/geotools/filter/NestedAttributeExpression.java $
 */
public class NestedAttributeExpression extends AttributeExpressionImpl {
    private FeatureTypeMapping mappings;

    private NamespaceSupport namespaces;

    /**
     * First constructor
     * 
     * @param xpath
     *            Attribute XPath
     * @param expressions
     *            List of broken up expressions
     */
    public NestedAttributeExpression(String xpath, FeatureTypeMapping mappings) {
        super(xpath);
        this.mappings = mappings;
        this.namespaces = mappings.getNamespaces();
    }

    /**
     * see {@link org.geotools.filter.AttributeExpressionImpl#evaluate(Object)}
     */
    @Override
    public Object evaluate(Object object) {
        if (object == null) {
            return null;
        }

        // only simple/complex features are supported
        if (!(object instanceof Feature)) {
            throw new UnsupportedOperationException(
                    "Expecting a feature to apply filter, but found: " + object);
        }
        Object value = null;
        FeatureTypeMapping fMapping = mappings;
        Feature root = (Feature) object;
        StepList fullSteps = XPath.steps(fMapping.getTargetFeature(), this.attPath.toString(),
                namespaces);
        AttributeMapping attMapping = null;
        AttributeMapping prevMapping = null;
        int startIndex = 0;
        int endIndex = startIndex;
        int lastIndex = fullSteps.size() - 1;

        while (startIndex <= fullSteps.size() - 1 && endIndex <= lastIndex) {
            boolean isElementName = false;
            StepList steps = null;
            prevMapping = attMapping;
            attMapping = null;

            while (!isElementName && attMapping == null && endIndex <= lastIndex) {
                endIndex++;
                steps = fullSteps.subList(startIndex, endIndex);
                if (steps.size() == 1) {
                    Step step = steps.get(0);
                    if (step.isXmlAttribute()) {
                        // use previous mapping to get client properties
                        attMapping = prevMapping;
                        break;
                    }
                    if (Types.equals(fMapping.getTargetFeature().getName(), step.getName())) {
                        // skip element name
                        isElementName = true;
                        break;
                    }
                }
                attMapping = fMapping.getAttributeMapping(steps);
                if (attMapping == null) {
                    if (prevMapping instanceof NestedAttributeMapping) {
                        if (((NestedAttributeMapping) prevMapping).isConditional()) {
                            // polymorphic type, that doesn't match the filter attributes
                            return null;
                        }
                    }
                    continue;
                }
                if (!(attMapping instanceof NestedAttributeMapping)
                        && attMapping.getSourceExpression().equals(Expression.NIL)) {
                    // might be an inline element name inside the feature type mapping
                    attMapping = null;
                }
            }

            startIndex++;
            if (isElementName) {
                // get the next one
                continue;
            }
            if (attMapping == null) {
                throw new IllegalArgumentException("Can't find source expression for: " + attPath);
            } else if (steps.size() == 1 && steps.get(0).isXmlAttribute()) {
                // a client properties
                value = getClientPropertyExpression(steps.get(0), attMapping, this.attPath)
                        .evaluate(root);
            } else if (attMapping instanceof NestedAttributeMapping) {
                // feature chaining mapping
                NestedAttributeMapping nestedMapping = ((NestedAttributeMapping) attMapping);
                try {
                    fMapping = nestedMapping.getFeatureTypeMapping(root);
                } catch (IOException e) {
                    fMapping = null;
                }
                if (fMapping != null && nestedMapping.isSameSource()) {
                    // same root/database row, different mappings, used in polymorphism
                    continue;
                }
                namespaces = nestedMapping.getNamespaces();
                try {
                    int index = steps.get(steps.size() - 1).getIndex() - 1;
                    List<Feature> nestedFeatures = getNestedFeatures(root, nestedMapping, fMapping);
                    if (nestedFeatures == null || nestedFeatures.isEmpty()) {
                        return null;
                    }
                    root = nestedFeatures.get(index);
                } catch (IOException e) {
                    throw new RuntimeException("Failed evaluating filter expression: '" + attPath
                            + "'. Caused by: " + e.getMessage());
                }
                if (fMapping == null) {
                    if (root instanceof FeatureImpl) {
                        // has a complex features backend, therefore doesn't necessarily have a
                        // FeatureTypeMapping, because it's not an app-schema data access
                        return getComplexFeatureValue(root, fullSteps.subList(startIndex, fullSteps
                                .size()));
                    } else {
                        throw new UnsupportedOperationException(
                                "FeatureTypeMapping not found for "
                                        + attPath
                                        + ". This shouldn't happen if it's set in AppSchemaDataAccess mapping file!");
                    }
                }
            } else {
                // normal attribute mapping
                Object val = attMapping.getSourceExpression().evaluate(root);
                if (val == null) {
                    return null;
                }
                if (val instanceof Feature) {
                    root = ((Feature) val);
                } else {
                    value = extractAttributeValue(val);
                }
            }
        }
        return value;
    }

    /**
     * Get nested features from a feature chaining attribute mapping
     * 
     * @param root
     *            Root feature being evaluated
     * @param nestedMapping
     *            Attribute mapping for nested features
     * @param fMapping
     *            The root feature type mapping
     * @return list of nested features
     * @throws IOException
     */
    private List<Feature> getNestedFeatures(Feature root, NestedAttributeMapping nestedMapping,
            FeatureTypeMapping fMapping) throws IOException {
        Object fTypeName = nestedMapping.getNestedFeatureType(root);
        if (fTypeName == null || !(fTypeName instanceof Name)) {
            return null;
        }
        boolean hasSimpleFeatures = AppSchemaDataAccessRegistry.hasName((Name) fTypeName);
        // get foreign key
        Object val = getValue(nestedMapping.getSourceExpression(), root);
        if (val == null) {
            return null;
        }
        if (hasSimpleFeatures) {
            // normal app-schema mapping
            return nestedMapping.getInputFeatures(val, null, fMapping);
        } else {
            // app-schema with a complex feature source
            return nestedMapping.getFeatures(val, null, root);
        }
    }

    /**
     * Extract leaf attribute value from an xpath in a feature.
     * 
     * @param root
     *            the feature
     * @param subList
     *            xpath steps
     * @return leaf attribute value
     */
    private Object getComplexFeatureValue(Feature root, StepList subList) {
        AttributeExpressionImpl att = new AttributeExpressionImpl(subList.toString(), new Hints(
                FeaturePropertyAccessorFactory.NAMESPACE_CONTEXT, namespaces));
        return getValue(att, root);
    }

    private Object getValue(Expression expression, Feature feature) {
        Object value = expression.evaluate(feature);

        return extractAttributeValue(value);
    }

    /**
     * Extract the value that might be wrapped in an attribute. If the value is a collection, gets
     * the first value.
     * 
     * @param value
     * @return
     */
    private Object extractAttributeValue(Object value) {
        if (value == null) {
            return null;
        }

        while (value instanceof Attribute) {
            // get real value
            value = ((Attribute) value).getValue();
        }
        if (value == null) {
            return null;
        }
        if (value instanceof Collection) {
            if (((Collection) value).isEmpty()) {
                return null;
            }
            value = ((Collection) value).iterator().next();
            while (value instanceof Attribute) {
                value = ((Attribute) value).getValue();
            }
        }
        return value;
    }

    /**
     * Find the expression of a client property if the step is one.
     * 
     * @param nextRootStep
     *            the step
     * @param fMapping
     *            feature type mapping to get namespaces from
     * @param mapping
     *            attribute mapping
     * @param targetXPath
     *            the full target xpath
     * @return
     */
    private Expression getClientPropertyExpression(Step nextRootStep, AttributeMapping mapping,
            String targetXPath) {
        if (nextRootStep.isXmlAttribute()) {
            Map<Name, Expression> clientProperties = mapping.getClientProperties();
            QName lastStepName = nextRootStep.getName();
            Name lastStep;
            if (lastStepName.getPrefix() != null
                    && lastStepName.getPrefix().length() > 0
                    && (lastStepName.getNamespaceURI() == null || lastStepName.getNamespaceURI()
                            .length() == 0)) {
                String prefix = lastStepName.getPrefix();
                String uri = namespaces.getURI(prefix);
                lastStep = Types.typeName(uri, lastStepName.getLocalPart());
            } else {
                lastStep = Types.toTypeName(lastStepName);
            }
            if (clientProperties.containsKey(lastStep)) {
                return (Expression) clientProperties.get(lastStep);
            } else {
                throw new IllegalArgumentException("Client property mapping is missing for: "
                        + targetXPath);
            }
        }
        return null;
    }
}

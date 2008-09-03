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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.commons.collections.map.LinkedMap;
import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.complex.filter.XPath;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.AttributeImpl;
import org.geotools.feature.iso.ComplexAttributeImpl;
import org.geotools.feature.iso.FeatureImpl;
import org.geotools.feature.iso.Types;
import org.geotools.feature.iso.UserData;
import org.geotools.feature.iso.attribute.GeometricAttribute;
import org.geotools.filter.FilterAttributeExtractor;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * An alternative strategy to fetch grouped multivalued content.
 * 
 * @author Gabriel Roldan
 * @version $Id: GroupingFeatureIterator2.java 27773 2007-11-06 23:06:54Z
 *          groldan $
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.4.x/modules/unsupported/community-schemas/community-schema-ds/src/main/java/org/geotools/data/complex/GroupingFeatureIterator.java $
 * @since 2.4
 */
class GroupingFeatureIterator3 extends AbstractMappingFeatureIterator {
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(GroupingFeatureIterator3.class.getPackage().getName());

    /**
     * maxFeatures restriction value as provided by query
     */
    private int maxFeatures;

    /** counter to ensure maxFeatures is not exceeded */
    private int featureCounter;

    /**
     * List of attribute mappings marked as multivalued, and any attribute which
     * maps to a child attribute of any multivalued mapping. The keys in the map
     * are attribute mappings explicitly set as multivalued. The values in the
     * map are the list of mappings whose target xpath expressions correspond to
     * child properties of the target xpath of the mapping used as key.
     */
    private LinkedMap multivaluedMappings;

    /**
     * List of source property names referenced by multivalued mappings and its
     * childs.
     */
    private Set /* String */multiValuedSourcePropNames;

    /**
     * List of attribute mappings not marked as multivalued and whose target
     * xpath is not a child of any multivalued attribute.
     */
    private List /* AttributeMapping */singleValuedMappings;

    /**
     * flag to avoid fetching multiple source feature in repeated calld to
     * hasNext() without calls to next() in the middle
     */
    private boolean hasNextCalled;

    private Feature curSrcFeature;
    
    public GroupingFeatureIterator3(final ComplexDataStore store,
            final FeatureTypeMapping mappings, final Query query) throws IOException {
        super(store, mappings, query, new MutableFeatureFactory());
        splitMappings();
    }

    protected Query getUnrolledQuery(Query query) {
        maxFeatures = query.getMaxFeatures();
        Query unmappedQuery = store.unrollQuery(query, mapping);
        ((DefaultQuery) unmappedQuery).setMaxFeatures(Integer.MAX_VALUE);

        unmappedQuery = ensureGroupingAttsPresent(unmappedQuery);

        return unmappedQuery;
    }

    public boolean hasNext() {
        if (hasNextCalled) {
            return curSrcFeature != null;
        }

        boolean exists = false;

        if (sourceFeatures != null && featureCounter < maxFeatures) {

            exists = this.sourceFeatures.hasNext();

            if (exists && this.curSrcFeature == null) {
                this.curSrcFeature = (Feature) this.sourceFeatures.next();
            }
        }

        if (!exists) {
            LOGGER.finest("no more features, produced " + featureCounter);
        }

        hasNextCalled = true;
        if (!exists) {
            close();
        }
        return exists;
    }

    public Object next() {
        if (!hasNext()) {
            throw new IllegalStateException("there are no more features in this iterator");
        }
        Feature next;
        try {
            next = computeNext();
        } catch (IOException e) {
            close();
            throw new RuntimeException(e);
        }
        hasNextCalled = false;
        ++featureCounter;
        return next;
    }

    // ///////////////////

    /**
     * 
     */
    private Feature computeNext() throws IOException {
        assert this.curSrcFeature != null : "hastNext not called?";

        // get the mapping set of a feature attribute
        final AttributeDescriptor targetNode = mapping.getTargetFeature();

        // create the target feature and iterate in the source ones to set its
        // values.
        final String fid = extractIdForFeature(curSrcFeature);
        final Feature targetFeature = attf.createFeature(null, targetNode, fid);

        setNonMultivaluedAttributes(targetFeature);

        setMultiValuedAttributes(targetFeature);

        if (!sourceFeatures.hasNext()) {
            close();
        }
        return targetFeature;
    }

    private void setNonMultivaluedAttributes(final Feature targetFeature) throws IOException {
        AttributeMapping mapping;
        for (Iterator it = this.singleValuedMappings.iterator(); it.hasNext();) {
            mapping = (AttributeMapping) it.next();
            StepList targetXPath = mapping.getTargetXPath();
            if (targetXPath.size() == 1) {
                Step rootStep = (Step) targetXPath.get(0);
                QName stepName = rootStep.getName();
                if (Types.equals(targetFeature.getDescriptor().getName(), stepName)) {
                    // ignore the top level mapping for the Feature
                    // itself
                    // as it was already set
                    continue;
                }
            }
            setSingleValuedAttribute(targetFeature, curSrcFeature, mapping);
        }
    }

    private void setMultiValuedAttributes(final Feature targetFeature) {
        // Map sourcePropName/List<values> with the contents
        // of the source attributes needed to map multivalued
        // properties and its children
        final Map /* String/List<Object> */mvaluedSourceProps = new HashMap();
        final NamespaceSupport namespaces = mapping.getNamespaces();
        final String targetNamespaceUri = targetFeature.getDescriptor()
                .getName().getNamespaceURI();
        final String targetNamespacePrefix = targetNamespaceUri == null ? null
                : namespaces.getPrefix(targetNamespaceUri);
        // populate mvaluedSourceProps with keys and empty lists
        for (Iterator it = multiValuedSourcePropNames.iterator(); it.hasNext();) {
            // this is just an ugly way of getting rid of index in expressions
            // like "attName[2]"
            String attName = (String) it.next();
            if (targetNamespacePrefix != null && attName.indexOf(":") == -1) {
                // if we have a namespace prefix, prepend names with it to
                // silence bogus warning in XPath.deglose()
                attName = targetNamespacePrefix + ":" + attName;
            }
            StepList steps = XPath.steps(targetFeature.getDescriptor(), attName, namespaces);
            String string = ((XPath.Step) steps.get(0)).getName().getLocalPart();
            mvaluedSourceProps.put(string, new ArrayList());
        }
        // // build the group of properties comprising the mvalued properties
        // and its children ///
        final List /* Name */groupByAttNames = toTypeNames(mapping.getGroupByAttNames());
        // the grouping values to check for equality
        final List baseGroupingAttributes = extractGroupingAttributes(curSrcFeature,
                groupByAttNames);
        extractSourceProperties(mvaluedSourceProps, (SimpleFeature) curSrcFeature);
        List currFeatureGroupingAtts;
        int index = 0;
        while (sourceFeatures.hasNext()) {
            curSrcFeature = (Feature) sourceFeatures.next();
            currFeatureGroupingAtts = extractGroupingAttributes(curSrcFeature, groupByAttNames);
            if (baseGroupingAttributes.equals(currFeatureGroupingAtts)) {
                extractSourceProperties(mvaluedSourceProps, (SimpleFeature) curSrcFeature);
                index++;
            } else {
                break;
            }
        }

        // /now set the mapping properties explicitly marked as multivalued as a
        // lazy
        // list of properties for its enclosing property in the target feature
        for (Iterator it = multivaluedMappings.entrySet().iterator(); it.hasNext();) {
            final Map.Entry entry = (Entry) it.next();
            final AttributeMapping mvaluedProp = (AttributeMapping) entry.getKey();
            final List childPropMappings = (List) entry.getValue();

            final LazyProperties mvaluedInstances;
            mvaluedInstances = new LazyProperties(targetFeature, mvaluedProp, childPropMappings,
                    mvaluedSourceProps);
            final StepList targetXPath = mvaluedProp.getTargetXPath();

            MutableAttribute parent = getParent(targetFeature, targetXPath);
            Collection newParentContent = mvaluedInstances;
            // may the parent have pre-existing non multivalued properties?
            Collection previousProperties = (Collection) parent.getValue();
            if (previousProperties.size() > 0) {
                newParentContent = new DeferredList();
                newParentContent.addAll(new ArrayList(previousProperties));
                newParentContent.addAll(mvaluedInstances);
            }
            parent.setValue(newParentContent);
        }
    }

    private static class DeferredList extends AbstractList {
        private List deferredContent = new ArrayList(2);

        public boolean addAll(Collection c) {
            deferredContent.add(c);
            return true;
        }

        public Object get(int index) {
            if (index < 0 || index >= size()) {
                throw new IndexOutOfBoundsException(index + ": size=" + size());
            }
            List deferred;
            int cumulativeIndex = 0;
            Object value = null;
            for (Iterator it = deferredContent.iterator(); it.hasNext();) {
                deferred = (List) it.next();
                int size = cumulativeIndex + deferred.size();
                if (index < size) {
                    value = deferred.get(index - cumulativeIndex);
                    break;
                }
                cumulativeIndex += deferred.size();
            }
            return value;
        }

        public int size() {
            int size = 0;
            for (Iterator it = deferredContent.iterator(); it.hasNext();) {
                List deferred = (List) it.next();
                size += deferred.size();
            }
            return size;
        }
    }

    private MutableAttribute getParent(final Feature targetFeature, final StepList childXPath) {
        StepList parentPath = new StepList(childXPath);
        parentPath.remove(childXPath.size() - 1);
        MutableAttribute parent = null;

        if (parentPath.size() > 0) {
            PropertyName property = namespaceAwareFilterFactory.property(parentPath.toString());
            parent = (MutableAttribute) property.evaluate(targetFeature);
            if (parent == null) {
                parent = (MutableAttribute) xpathAttributeBuilder.set(targetFeature, parentPath,
                        null, null, null);
            }
        } else /* set direct child descendant of feature */
        {
            parent = (MutableAttribute) targetFeature;
        }

        return parent;
    }

    private static class MutableFeatureFactory extends AttributeFactoryImpl {
        public ComplexAttribute createComplexAttribute(Collection value, AttributeDescriptor desc,
                String id) {
            return new MutableComplexAttribute(value, desc, id);
        }

        public ComplexAttribute createComplexAttribute(Collection value, ComplexType type, String id) {
            return new MutableComplexAttribute(value, type, id);
        }

        public Feature createFeature(Collection value, AttributeDescriptor desc, String id) {
            return new MutableFeature(value, desc, id);
        }

        public Feature createFeature(Collection value, FeatureType type, String id) {
            return new MutableFeature(value, type, id);
        }

        public Attribute createAttribute(Object value, AttributeDescriptor descriptor, String id) {
            return new MutableAttributeImpl(value, descriptor, id);
        }

        public GeometryAttribute createGeometryAttribute(Object value, AttributeDescriptor desc,
                String id, CoordinateReferenceSystem crs) {
            return new MutableGeometryAttribute(value, desc, id, crs);
        }
    }

    private interface MutableAttribute extends Attribute {
        /**
         * Overrides the semantics of setValue with the following: if
         * <code>newValue instanceof LazyProperties</code> or
         * <code>newValue instanceof DeferredList</code>, the content of this
         * attribute is <code>newValue</code> itself, no safe copy is made.
         * Otherwise it behaves as usual.
         * 
         */
        public void setValue(Object newValue);

        public void setIdExpression(Expression identifierExpression);

        public void setValueExpression(Expression sourceExpression);

        public void setSourceProperties(Map mvaluedSourceProps);

        public void setIndex(int index);

        /**
         * Call this last, after index and source properties set.
         * 
         * @param clientProperties
         */
        public void setClientProperties(Map clientProperties);
    }

    private static class MutableAdapter {

        public List get(final Name name, final List properties) {
            LazyProperties lazyProperties = findLazyProperties(name, properties);
            if (lazyProperties != null) {
                return lazyProperties;
            }
            // JD: this is a farily lenient check, should we be stricter about
            // matching up the namespace
            List/* <Property> */childs = new LinkedList/* <Property> */();

            for (Iterator itr = properties.iterator(); itr.hasNext();) {
                Property prop = (Property) itr.next();
                PropertyDescriptor node = prop.descriptor();
                Name propName = node.getName();
                if (name.getNamespaceURI() != null) {
                    if (propName.equals(name)) {
                        childs.add(prop);
                    }
                } else {
                    // just do a local part compare
                    String localName = propName.getLocalPart();
                    if (localName.equals(name.getLocalPart())) {
                        childs.add(prop);
                    }
                }
            }
            return childs;
        }

        private LazyProperties findLazyProperties(Name name, List properties) {
            LazyProperties propsForName = null;
            if (properties instanceof LazyProperties) {
                LazyProperties lazyProperties = (LazyProperties) properties;
                if (lazyPropertiesElementTargetsName(name, lazyProperties)) {
                    propsForName = lazyProperties;
                }
            } else if (properties instanceof DeferredList) {
                DeferredList dl = (DeferredList) properties;
                for (Iterator it = dl.deferredContent.iterator(); it.hasNext();) {
                    List list = (List) it.next();
                    if (list instanceof LazyProperties) {
                        if (lazyPropertiesElementTargetsName(name, (LazyProperties) list)) {
                            propsForName = (LazyProperties) list;
                            break;
                        }
                    }
                }
            }
            return propsForName;
        }

        private boolean lazyPropertiesElementTargetsName(Name name, LazyProperties lazyProperties) {
            StepList targetXPath = lazyProperties.multivaluedMapping.getTargetXPath();
            Step lastStep = (Step) targetXPath.get(targetXPath.size() - 1);
            QName qname = lastStep.getName();
            String nameURI = name.getNamespaceURI();
            String nameLocal = name.getLocalPart();
            String qnameURI = qname.getNamespaceURI();
            String qnameLocal = qname.getLocalPart();
            if (nameURI == null && nameLocal.equals(qnameLocal)) {
                return true;
            } else if (nameURI.equals(qnameURI) && nameLocal.equals(qnameLocal)) {
                return true;
            }
            return false;
        }

        public Object getValue(final MutableAttribute mutableAttributeImpl,
                final Expression valueExpression, final Map mvaluedSourceProps, int index) {
            final Map indexValues = getIndexProperties(mvaluedSourceProps, index);
            // now let the MapPropertyAccessorFactory to evaluate the attribute
            // expressions
            Object evaluatedValue = valueExpression.evaluate(indexValues);
            return evaluatedValue;
        }

        private Map getIndexProperties(Map mvaluedSourceProps, int index) {
            Map indexValues = new HashMap();
            Map.Entry entry;
            String attName;
            List values;
            Object valueAtIndex;
            for (Iterator it = mvaluedSourceProps.entrySet().iterator(); it.hasNext();) {
                entry = (Entry) it.next();
                attName = (String) entry.getKey();
                values = (List) entry.getValue();
                valueAtIndex = values.get(index);
                indexValues.put(attName, valueAtIndex);
            }
            return indexValues;
        }

        public String getId(MutableAttribute mutableAttributeImpl, Expression identifierExpression,
                Map mvaluedSourceProps, int index) {
            String id = (String) identifierExpression.evaluate(mvaluedSourceProps, String.class);
            return id;
        }

        public void setChildrenIndex(final List properties, final int index) {
            MutableAttribute child;
            for (Iterator it = properties.iterator(); it.hasNext();) {
                child = (MutableAttribute) it.next();
                child.setIndex(index);
            }
        }

        public void setClientProperties(final Attribute target,
                final Map clientProperties, final Map mvaluedSourceProps,
                int index) {
            if (clientProperties != null && clientProperties.size() > 0
                    && mvaluedSourceProps != null) {
                final Map indexValues = getIndexProperties(mvaluedSourceProps,
                        index);
                final Map targetAttributes = new HashMap();
                for (Iterator it = clientProperties.entrySet().iterator(); it
                        .hasNext();) {
                    Map.Entry entry = (Map.Entry) it.next();
                    org.opengis.feature.type.Name propName = (org.opengis.feature.type.Name) entry
                            .getKey();
                    Expression propExpr = (Expression) entry.getValue();
                    Object propValue = propExpr.evaluate(indexValues);
                    targetAttributes.put(propName, propValue);
                }
                ((UserData) target).putUserData(Attributes.class,
                        targetAttributes);
            }
        }

    }

    private static class MutableFeature extends FeatureImpl implements MutableAttribute {
        private static MutableAdapter mutableAtapter = new MutableAdapter();

        private Expression identifierExpression = Expression.NIL;

        private Map mvaluedSourceProps;

        private int index;

        public MutableFeature(Collection values, AttributeDescriptor desc, String id) {
            super(values, desc, id);
        }

        public MutableFeature(Collection values, FeatureType type, String id) {
            super(values, type, id);
        }

        public void setValue(Object newValue) {
            if (newValue instanceof LazyProperties || newValue instanceof DeferredList) {
                super.setValue(Collections.EMPTY_LIST);
                properties = (List) newValue;
            } else {
                super.setValue(newValue);
            }
        }

        public List/* <Property> */get(Name name) {
            return mutableAtapter.get(name, properties);
        }

        public String getID() {
            if (mvaluedSourceProps != null) {
                return mutableAtapter.getId(this, identifierExpression, mvaluedSourceProps, index);
            }
            return super.getID();
        }

        public void setIdExpression(Expression identifierExpression) {
            this.identifierExpression = identifierExpression;
        }

        public void setSourceProperties(Map mvaluedSourceProps) {
            this.mvaluedSourceProps = mvaluedSourceProps;
        }

        public void setValueExpression(Expression sourceExpression) {
            if (sourceExpression != null && Expression.NIL.equals(sourceExpression)) {
                throw new UnsupportedOperationException(
                        "not applicable to complex. Source expression: " + sourceExpression);
            }
        }

        public void setIndex(int index) {
            this.index = index;
            mutableAtapter.setChildrenIndex(properties, index);
        }

        public void setClientProperties(final Map clientProperties) {
            mutableAtapter.setClientProperties(this, clientProperties,
                    mvaluedSourceProps, index);
        }
        
    }

    private static class MutableAttributeImpl extends AttributeImpl implements MutableAttribute {
        private static MutableAdapter mutableAtapter = new MutableAdapter();

        private Expression identifierExpression = Expression.NIL;

        private Map mvaluedSourceProps;

        private Expression valueExpression;

        private int index;

        public MutableAttributeImpl(Object content, AttributeDescriptor descriptor, String id) {
            super(content, descriptor, id);
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Object getValue() {
            if (mvaluedSourceProps != null) {
                return mutableAtapter.getValue(this, valueExpression, mvaluedSourceProps, index);
            }
            return super.getValue();
        }

        public String getID() {
            if (mvaluedSourceProps != null) {
                return mutableAtapter.getId(this, identifierExpression, mvaluedSourceProps, index);
            }
            return super.getID();
        }

        public void setIdExpression(Expression identifierExpression) {
            this.identifierExpression = identifierExpression;
        }

        public void setSourceProperties(Map mvaluedSourceProps) {
            this.mvaluedSourceProps = mvaluedSourceProps;
        }

        public void setValueExpression(Expression sourceExpression) {
            this.valueExpression = sourceExpression;
        }
 
        public void setClientProperties(final Map clientProperties) {
            mutableAtapter.setClientProperties(this, clientProperties,
                    mvaluedSourceProps, index);
        }

    }

    private static class MutableGeometryAttribute extends GeometricAttribute implements
            MutableAttribute {
        private static MutableAdapter mutableAtapter = new MutableAdapter();

        private Expression identifierExpression = Expression.NIL;

        private Map mvaluedSourceProps;

        private Expression valueExpression;

        private int index;

        public MutableGeometryAttribute(Object content, AttributeDescriptor descriptor, String id,
                CoordinateReferenceSystem cs) {
            super(content, descriptor, id, cs);
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Object getValue() {
            if (mvaluedSourceProps != null) {
                return mutableAtapter.getValue(this, valueExpression, mvaluedSourceProps, index);
            }
            return super.getValue();
        }

        public String getID() {
            if (mvaluedSourceProps != null) {
                return mutableAtapter.getId(this, identifierExpression, mvaluedSourceProps, index);
            }
            return super.getID();
        }

        public void setIdExpression(Expression identifierExpression) {
            this.identifierExpression = identifierExpression;
        }

        public void setSourceProperties(Map mvaluedSourceProps) {
            this.mvaluedSourceProps = mvaluedSourceProps;
        }

        public void setValueExpression(Expression sourceExpression) {
            this.valueExpression = sourceExpression;
        }

        public void setClientProperties(final Map clientProperties) {
            mutableAtapter.setClientProperties(this, clientProperties,
                    mvaluedSourceProps, index);
        }

    }

    private static class MutableComplexAttribute extends ComplexAttributeImpl implements
            MutableAttribute {
        private static MutableAdapter mutableAtapter = new MutableAdapter();

        private Expression identifierExpression = Expression.NIL;

        private Map mvaluedSourceProps;

        private int index;

        public MutableComplexAttribute(Collection values, ComplexType type, String id) {
            super(values, type, id);
        }

        public MutableComplexAttribute(Collection values, AttributeDescriptor desc, String id) {
            super(values, desc, id);
        }

        public void setIndex(int index) {
            this.index = index;
            mutableAtapter.setChildrenIndex(properties, index);
        }

        public void setValue(Object newValue) {
            if (newValue instanceof LazyProperties || newValue instanceof DeferredList) {
                super.setValue(Collections.EMPTY_LIST);
                properties = (List) newValue;
            } else {
                super.setValue(newValue);
            }
        }

        public List/* <Property> */get(Name name) {
            return mutableAtapter.get(name, properties);
        }

        public String getID() {
            if (mvaluedSourceProps != null) {
                return mutableAtapter.getId(this, identifierExpression, mvaluedSourceProps, index);
            }
            return super.getID();
        }

        public void setIdExpression(Expression identifierExpression) {
            this.identifierExpression = identifierExpression;
        }

        public void setSourceProperties(Map mvaluedSourceProps) {
            this.mvaluedSourceProps = mvaluedSourceProps;
        }

        public void setValueExpression(Expression sourceExpression) {
            if (sourceExpression != null && !Expression.NIL.equals(sourceExpression)) {
                throw new UnsupportedOperationException(
                        "not applicable to complex. Source expression: " + sourceExpression);
            }
        }

        public void setClientProperties(final Map clientProperties) {
            mutableAtapter.setClientProperties(this, clientProperties,
                    mvaluedSourceProps, index);
        }

    }

    /**
     * 
     * @param mvaluedSourceProps
     * @param srcFeature
     */
    private void extractSourceProperties(Map mvaluedSourceProps, final SimpleFeature srcFeature) {
        Map.Entry entry;
        String propName;
        List sourceValues;
        Object value;
        for (Iterator it = mvaluedSourceProps.entrySet().iterator(); it.hasNext();) {
            entry = (Entry) it.next();
            propName = (String) entry.getKey();
            sourceValues = (List) entry.getValue();
            value = srcFeature.getValue(propName);
            sourceValues.add(value);
        }
    }

    private class LazyProperties extends AbstractList {
        /** The multivalued attribute this list holds */
        private AttributeMapping multivaluedMapping;

        /** the mapping of the child properties of the multivalued attribute */
        private List/* AttributeMapping */childMappings;

        /**
         * Map of source properties values comprising the whole current group
         * for the target feature instance multivalued properties. The keys are
         * Strings representing the source property names and the values Lists
         * of values for the source properties in the current group.
         */
        private Map mvaluedSourceProps;

        /** cached group size */
        private int size;

        private LazyFeatureFactory ffac;

        private Feature targetFeature;

        public LazyProperties(Feature targetFeature, final AttributeMapping multivaluedMapping,
                final List childMappings, Map mvaluedSourceProps) {
            this.targetFeature = targetFeature;
            this.multivaluedMapping = multivaluedMapping;
            this.childMappings = childMappings;
            this.mvaluedSourceProps = mvaluedSourceProps;
            this.size = ((List) mvaluedSourceProps.values().iterator().next()).size();
            this.ffac = new LazyFeatureFactory();
        }

        /**
         * returns the attribute targeted by the list multivalued property at
         * index <code>index/code>
         */
        public Object get(int index) {
            MutableAttribute attribute = ffac.create(multivaluedMapping, childMappings, index);
            attribute.setIndex(index);
            return attribute;
        }

        public int size() {
            return size;
        }

        private class LazyFeatureFactory extends AttributeFactoryImpl {
            Map properties = new HashMap();

            public MutableAttribute create(final AttributeMapping mapping,
                    final List childMappings, final int index) {
                StepList targetXPath = mapping.getTargetXPath();
                if (!properties.containsKey(targetXPath)) {
                    buildLazyMultivaluedAttribute(mapping, childMappings);
                }

                MutableAttribute attribute = (MutableAttribute) properties.get(targetXPath);
                attribute.setSourceProperties(mvaluedSourceProps);
                attribute.setClientProperties(mapping.getClientProperties());
                return attribute;
            }

            private void buildLazyMultivaluedAttribute(final AttributeMapping mapping,
                    final List childMappings) {
                StepList targetXPath = mapping.getTargetXPath();
                AttributeType targetNodeType = mapping.getTargetNodeInstance();
                MutableAttribute parent = getParent(targetFeature, targetXPath);
                Step lastStep = (Step) targetXPath.get(targetXPath.size() - 1);
                Name name = Types.toTypeName(lastStep.getName());
                AttributeDescriptor descriptor;
                ComplexType parentType = (ComplexType) parent.getType();
                if (targetNodeType == null) {
                    descriptor = (AttributeDescriptor) Types.descriptor(parentType, name);
                } else {
                    descriptor = (AttributeDescriptor) Types.descriptor(parentType, name,
                            targetNodeType);
                }
                MutableAttribute attribute = create(descriptor);
                attribute.setIdExpression(mapping.getIdentifierExpression());

                properties.put(targetXPath, attribute);

                for (Iterator it = childMappings.iterator(); it.hasNext();) {
                    AttributeMapping childMapping = (AttributeMapping) it.next();
                    StepList childXpath = new XPath.StepList(childMapping.getTargetXPath());
                    int parentPathStepCount = targetXPath.size();
                    for (int i = 0; i < parentPathStepCount; i++) {
                        childXpath.remove(0);
                    }

                    AttributeType childNodeInstanceType = childMapping.getTargetNodeInstance();
                    MutableAttribute child;
                    child = (MutableAttribute) xpathAttributeBuilder.set(attribute, childXpath,
                            null, null, childNodeInstanceType);
                    child.setIdExpression(childMapping.getIdentifierExpression());
                    child.setValueExpression(childMapping.getSourceExpression());
                    child.setSourceProperties(mvaluedSourceProps);
                    child.setClientProperties(childMapping.getClientProperties());
                }
            }

            private MutableAttribute create(final AttributeDescriptor descriptor) {
                AttributeType type = (AttributeType) descriptor.type();
                MutableAttribute att;
                if (type instanceof FeatureType) {
                    att = new MutableFeature(null, descriptor, "fake");
                } else if (type instanceof ComplexType) {
                    att = new MutableComplexAttribute(null, descriptor, null);
                } else if (type instanceof GeometryType) {
                    CoordinateReferenceSystem crs = ((GeometryType) type).getCRS();
                    att = new MutableGeometryAttribute(null, descriptor, null, crs);
                } else {
                    att = new MutableAttributeImpl(null, descriptor, null);
                }
                return att;
            }
        }

    }

    private List toTypeNames(final List groupByAttNames) {
        List typeNames = new ArrayList(groupByAttNames.size());
        String sourceAttName;
        Name attributeName;
        for (Iterator it = groupByAttNames.iterator(); it.hasNext();) {
            sourceAttName = (String) it.next();
            attributeName = Types.typeName(sourceAttName);
            typeNames.add(attributeName);
        }
        return typeNames;
    }

    /**
     * Extract the attributes from grouping attributes.
     * 
     * @param groupByAttNames
     * 
     * @param Feature
     *            a source feature
     * @return List<List<Attribute>> the the contened list has the attributes
     *         required
     */
    private final List/* <List<Attribute>> */extractGroupingAttributes(
            final ComplexAttribute srcFeature, final List /* Name */groupByAttNames) {

        List/* <List<Attribute>> */attrGroup = new ArrayList/* <List<Attribute>> */(
                groupByAttNames.size());

        for (Iterator it = groupByAttNames.iterator(); it.hasNext();) {
            Name name = (Name) it.next();
            List/* <Attribute> */listAttrForName = srcFeature.get(name);
            attrGroup.add(listAttrForName);
        }

        return attrGroup;
    }

    /**
     * Split the attribute mappings in two sets, the ones that belong to a
     * multivalued property {@link #multivaluedMappings} and the ones that not
     * {@link #singleValuedMappings}.
     */
    private void splitMappings() {
        this.multivaluedMappings = new LinkedMap();
        this.singleValuedMappings = new ArrayList(mapping.getAttributeMappings());
        this.multiValuedSourcePropNames = new HashSet();

        for (Iterator it = mapping.getAttributeMappings().iterator(); it.hasNext();) {
            AttributeMapping am = (AttributeMapping) it.next();
            if (am.isMultiValued()) {
                singleValuedMappings.remove(am);
                multivaluedMappings.put(am, new ArrayList(2));
                multiValuedSourcePropNames.addAll(getSourcePropertyNames(am));
            }
        }

        for (Iterator mvaluedPaths = multivaluedMappings.entrySet().iterator(); mvaluedPaths
                .hasNext();) {
            final Map.Entry entry = (Entry) mvaluedPaths.next();
            final AttributeMapping parentMapping = (AttributeMapping) entry.getKey();
            final List childMappings = (List) entry.getValue();
            final StepList mvaluedPath = parentMapping.getTargetXPath();

            for (Iterator mappings = mapping.getAttributeMappings().iterator(); mappings.hasNext();) {
                final AttributeMapping am = (AttributeMapping) mappings.next();
                if (am == parentMapping) {
                    continue;
                }

                final StepList targetXPath = am.getTargetXPath();
                boolean isChild = true;
                if (targetXPath.size() >= mvaluedPath.size()) {
                    for (int currStepIdx = 0; currStepIdx < mvaluedPath.size(); currStepIdx++) {
                        XPath.Step parentStep = (XPath.Step) mvaluedPath.get(currStepIdx);
                        XPath.Step childStep = (XPath.Step) targetXPath.get(currStepIdx);
                        QName parentStepName = parentStep.getName();
                        QName childStepName = childStep.getName();
                        if (!parentStepName.equals(childStepName)) {
                            isChild = false;
                            break;
                        }
                    }
                } else {
                    isChild = false;
                }
                if (isChild) {
                    singleValuedMappings.remove(am);
                    childMappings.add(am);
                    multiValuedSourcePropNames.addAll(getSourcePropertyNames(am));
                }
            }
        }
    }

    /**
     * Looks up and returns the property names from the source feature type
     * referenced by a given mapping
     * 
     * @param am
     * @return
     */
    private Collection getSourcePropertyNames(AttributeMapping am) {
        Set sourceAttNames = new HashSet();
        FilterAttributeExtractor attExtractor = new FilterAttributeExtractor();

        am.getIdentifierExpression().accept(attExtractor, null);
        sourceAttNames.addAll(attExtractor.getAttributeNameSet());

        am.getSourceExpression().accept(attExtractor, null);
        sourceAttNames.addAll(attExtractor.getAttributeNameSet());
  
        // Rob A: add attribute (clientProperty) values here too!
        Map cp = am.getClientProperties();

        for (Iterator it = cp.entrySet().iterator(); it.hasNext();) {
            Entry entry = (Entry) it.next();
 //           Name attName = (Name) entry.getKey();
            Object expr =  entry.getValue();
            if( expr instanceof List) 
            	expr =  (Object) ((List) expr).get(0);
            
            if ( expr instanceof Expression )
            {
              ((Expression) expr).accept(attExtractor, null);
              // now should parse it as an expression and extract any property names 
              sourceAttNames.addAll(attExtractor.getAttributeNameSet());
            }
        }

        return sourceAttNames;
    }

    /**
     * Takes a Query and returns another one ensuring that all the grouping
     * attributes are requested, in order to be able of producing the correct
     * number of output features, for example, from a joined set of tables.
     * 
     * @param query
     * @return
     */
    private Query ensureGroupingAttsPresent(Query query) {
        if (query.retrieveAllProperties()) {
            return query;
        }

        List groupByAttributeNames = super.mapping.getGroupByAttNames();
        DefaultQuery neededQuery = new DefaultQuery(query);
        List requestedAtts = Arrays.asList(query.getPropertyNames());
        if (!requestedAtts.containsAll(groupByAttributeNames)) {
            List remaining = new ArrayList(groupByAttributeNames);
            remaining.removeAll(requestedAtts);
            LOGGER.fine("Adding missing grouping atts: " + remaining);

            List queryAtts = new ArrayList(remaining);
            queryAtts.addAll(requestedAtts);

            neededQuery.setPropertyNames(queryAtts);
        }
        return neededQuery;
    }
}

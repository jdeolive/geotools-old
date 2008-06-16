/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data.complex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.geotools.data.DefaultQuery;
import org.geotools.data.Query;
import org.geotools.data.complex.filter.XPath;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.feature.iso.AttributeFactoryImpl;
import org.geotools.feature.iso.Types;
import org.geotools.filter.FilterAttributeExtractor;
import org.geotools.filter.FilterFactoryImplNamespaceAware;
import org.opengis.feature.Attribute;
import org.opengis.feature.ComplexAttribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * 
 * A Feature can be stored in many tables of database. Restoring that feature
 * may be did by join, the resulting table can be explored using this iterator
 * class. This iterator return Features with simple or complex attributes.
 * <p>
 * In the FeatureTypeMapping must be defined the grouping attributes, this value
 * will be used to distinguish different features in the data sotore.
 * </p>
 * <p>
 * The next can be a posible data store. <table>
 * <tr>
 * <td> station_no (string) </td>
 * <td> sitename (string)</td>
 * <td> anzlic_no (string)</td>
 * <td> project_no (string)</td>
 * <td> id (string)</td>
 * <td> sample_collection_date (string)</td>
 * <td> determinand_description (string)</td>
 * <td> results_value (float)</td>
 * <td> location (Point)</td>
 * </tr>
 * <tr>
 * <td> station_1 </td>
 * <td> sitename_1 </td>
 * <td> anzlic_no_1 </td>
 * <td> project_no_1 </td>
 * <td> id_1_1 </td>
 * <td> sample_collection_date_1_1 </td>
 * <td> determinand_description_1_1 </td>
 * <td> 1.1 </td>
 * <td> POINT(1, 1) </td>
 * </tr>
 * <tr>
 * <td> station_2 </td>
 * <td> sitename_2 </td>
 * <td> anzlic_no_2 </td>
 * <td> project_no_2 </td>
 * <td> id_2_1 </td>
 * <td> sample_collection_date_2_1 </td>
 * <td> determinand_description_2_1 </td>
 * <td> 2.1 </td>
 * <td> POINT(2, 2) </td>
 * </tr>
 * <tr>
 * <td> station_2 </td>
 * <td> sitename_2 </td>
 * <td> anzlic_no_2 </td>
 * <td> project_no_2 </td>
 * <td> id_2_2 </td>
 * <td> sample_collection_date_2_2 </td>
 * <td> determinand_description_2_2 </td>
 * <td> 2.2 </td>
 * <td> POINT(2, 2) </td>
 * </tr>
 * <tr>
 * <td colspan="9">...</td>
 * </tr>
 * <tr>
 * <td> station_10 </td>
 * <td> sitename_10 </td>
 * <td> anzlic_no_10 </td>
 * <td> project_no_10 </td>
 * <td> id_10_10 </td>
 * <td> sample_collection_date_10_9 </td>
 * <td> determinand_description_10_9 </td>
 * <td> 10.10 </td>
 * <td> POINT(10, 10) </td>
 * </tr>
 * <tr>
 * <td> station_10 </td>
 * <td> sitename_10 </td>
 * <td> anzlic_no_10 </td>
 * <td> project_no_10 </td>
 * <td> id_10_10 </td>
 * <td> sample_collection_date_10_10 </td>
 * <td> determinand_description_10_10 </td>
 * <td> 10.10 </td>
 * <td> POINT(10, 10) </td>
 * </tr>
 * </table>
 * </p>
 * 
 * @author Mauricio Pazos, Axios Engineering
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
class GroupingFeatureIterator extends AbstractMappingFeatureIterator {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(GroupingFeatureIterator.class
            .getPackage().getName());

    /**
     * 
     */
    private Feature curSrcFeature;

    /**
     * maxFeatures restriction value as provided by query
     */
    private int maxFeatures;

    /** counter to ensure maxFeatures is not exceeded */
    private int featureCounter;

    /**
     * flag to avoid fetching multiple source feature in repeated calld to
     * hasNext() without calls to nex() in the middle
     */
    private boolean hasNextCalled;

    private List/* <String> */groupByAttributeNames;

    private List/* <AttributeMapping> */groupingMappings = new ArrayList();

    private List/* <AttributeMapping> */nonGroupingMappings = new ArrayList();

    private XPath xpathAttributeBuilder;

    /**
     * Map of xpath target attributes/parent multivalued property. Used to
     * recognize child properties of a multivalued and complex attributes and
     * set their values for each member of the group.
     */
    private Map /* <StepList, XPath.Step> */multivaluedAttributePaths = new HashMap();

    private static final FeatureFactory attf = new AttributeFactoryImpl();

    /**
     * New instance of GroupingFeatureIterator
     * 
     * @param mapping
     * @param query
     * @param groupByAttributeNames
     *            list of attribute identifies for grouping the source feature
     * @throws IOException
     */
    public GroupingFeatureIterator(ComplexDataStore store, FeatureTypeMapping mappings, Query query)
            throws IOException {
        super(store, mappings, query);
        if (mappings.getGroupByAttNames().size() == 0) {
            throw new IllegalArgumentException("no grouping attributes defined");
        }
        groupByAttributeNames = mappings.getGroupByAttNames();

        xpathAttributeBuilder = new XPath();
        xpathAttributeBuilder.setFeatureFactory(super.attf);
        NamespaceSupport namespaces = mappings.getNamespaces();
        // FilterFactory namespaceAwareFilterFactory =
        // CommonFactoryFinder.getFilterFactory(hints);
        FilterFactory namespaceAwareFilterFactory = new FilterFactoryImplNamespaceAware(namespaces);
        xpathAttributeBuilder.setFilterFactory(namespaceAwareFilterFactory);

        // Extracts simple and complex mappings
        List attrMappings = this.mapping.getAttributeMappings();

        splitMappings(attrMappings);
    }

    protected Query getUnrolledQuery(Query query) {
        maxFeatures = query.getMaxFeatures();
        Query unmappedQuery = store.unrollQuery(query, mapping);
        ((DefaultQuery) unmappedQuery).setMaxFeatures(Integer.MAX_VALUE);

        unmappedQuery = ensureGroupingAttsPresent(unmappedQuery);

        return unmappedQuery;
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

        groupByAttributeNames = super.mapping.getGroupByAttNames();
        DefaultQuery neededQuery = new DefaultQuery(query);
        List requestedAtts = Arrays.asList(query.getPropertyNames());
        if (!requestedAtts.containsAll(this.groupByAttributeNames)) {
            List remaining = new ArrayList(groupByAttributeNames);
            remaining.removeAll(requestedAtts);
            GroupingFeatureIterator.LOGGER.fine("Adding missing grouping atts: " + remaining);

            List queryAtts = new ArrayList(remaining);
            queryAtts.addAll(requestedAtts);

            neededQuery.setPropertyNames(queryAtts);
        }
        return neededQuery;
    }

    /**
     * @return boolean true if exist next feature, false in other case.
     */
    public final boolean hasNext() {
        if (hasNextCalled) {
            return curSrcFeature != null;
        }

        boolean exists = false;

        if (featureCounter < maxFeatures) {
            
            exists = this.sourceFeatures.hasNext();

            if (exists && this.curSrcFeature == null) {
                this.curSrcFeature = (Feature) this.sourceFeatures.next();
            }
        }

        if (!exists) {
            GroupingFeatureIterator.LOGGER.finest("no more features, produced " + featureCounter);
        }
        
        hasNextCalled = true;
        
        return exists;
    }

    /**
     * @return Feature the next feature.
     */
    public final Object /* Feature */next() {
        if (!hasNext()) {
            throw new IllegalStateException("there are no more features in this iterator");
        }
        //note hasNextCalled is going to be updated in createCurrentGroup()
        //as it might be called directly to improve performance in
        //calculating the resultset size
        Feature next = computeNext();
        ++featureCounter;
        return next;
    }

    /**
     * Makes a Complex Feature using the data source and grouping the field of
     * mapped features. The source Features can be a Simple or Complex,
     * furthermore an attribute of target Feature can be in one or more Source
     * Features.
     * <p>
     * The target (mapped) attributes are created in the order defined in the
     * {@linkplain FeatureTypeMapping}'s attribute mappings.
     * </p>
     * 
     * @return a Feature of the target FeatureType composed up of applying the
     *         mappings and groupings defined in the
     *         {@linkplain FeatureTypeMapping}
     */
    private Feature computeNext() {
        // get the mapping set of a feature attribute
        final AttributeDescriptor targetNode = mapping.getTargetFeature();
        final Name targetNodeName = targetNode.getName();
        final List attMappings = mapping.getAttributeMappings();

        String fid = extractIdForFeature(curSrcFeature);

        // create the target feature and iterate in the source for set its
        // values.
        final Feature targetFeature = GroupingFeatureIterator.attf.createFeature(null, targetNode,
                fid);

        final Feature featureForGroupingAtts = this.curSrcFeature;

        final List/* <Feature> */currentGroup = createCurrentGroup();

        /**
         * We need to set attributes in the attribute mapping's declared order
         */
        for (Iterator itr = attMappings.iterator(); itr.hasNext();) {
            AttributeMapping mapping = (AttributeMapping) itr.next();
            StepList targetXPath = mapping.getTargetXPath();
            if (targetXPath.size() == 1) {
                Step rootStep = (Step) targetXPath.get(0);
                QName stepName = rootStep.getName();
                if (Types.equals(targetNodeName, stepName)) {
                    // ignore the top level mapping for the Feature
                    // itself
                    // as it was already set
                    continue;
                }
            }

            if (mapping.isMultiValued()) {
                setMultivaluedAttribute(targetFeature, mapping, currentGroup);
            } else {
                setSingleValuedAttribute(targetFeature, featureForGroupingAtts, mapping,
                        currentGroup);
            }
        }

        return targetFeature;
    }

    /**
     * Iterate over the source features while they belong to the same set of
     * grouping attributes.
     * 
     * <p>
     * Each time this method is called {@link #hasNextCalled} is set to false
     * </p>
     * 
     * @return the set of Features from the source resultset that belongs to the
     *         same group.
     */
    List/* <Feature> */createCurrentGroup() {
        List/* <List<Attribute>> */curGroupingAttrList = extractGroupingAttributes(this.curSrcFeature);
        List/* <Feature> */currentGroup = new LinkedList/* <Feature> */();
        currentGroup.add(this.curSrcFeature);

        // loop control: break, if has not next or grouping attributes of
        // next feature are diferent
        while (this.sourceFeatures.hasNext()) {
            this.curSrcFeature = (Feature) this.sourceFeatures.next();
            List/* <List<Attribute>> */newGroupingAttrList = extractGroupingAttributes(this.curSrcFeature);
            if (!curGroupingAttrList.equals(newGroupingAttrList)) {
                break;
            }
            currentGroup.add(this.curSrcFeature);
        }
        hasNextCalled = false;
        return currentGroup;
        // [[Attribute[station_no:station_no:@:station_no.2]],
        // [Attribute[sitename:sitename:@:sitename2]],
        // [Attribute[anzlic_no:anzlic_no:@:anzlic_no2]],
        // [Attribute[project_no:project_no:@:project_no2]],
        // [Attribute[location:location:@:POINT (2 2)]]]
        // [[Attribute[station_no:station_no:@:station_no.2]],
        // [Attribute[sitename:sitename:@:sitename2]],
        // [Attribute[anzlic_no:anzlic_no:@:anzlic_no2]],
        // [Attribute[project_no:project_no:@:project_no2]],
        // [Attribute[location:location:@:POINT (2 2)]]]
    }

    /**
     * Sets the values of grouping attributes.
     * 
     * @param sourceFeature
     * @param groupingMappings
     * @param targetFeature
     * 
     * @return Feature. Target feature sets with simple attributes
     */
    private void setSingleValuedAttribute(Feature target, final Feature source,
            final AttributeMapping attMapping, final List /* <Feature> */currentGroup) {

        Expression expression = attMapping.getSourceExpression();
        final AttributeType targetNodeType = attMapping.getTargetNodeInstance();
        final StepList xpath = attMapping.getTargetXPath();

        if (expression == null) {
            expression = Expression.NIL;
        }

        List parentMultivaluedAttributePath = (List) multivaluedAttributePaths.get(xpath);
        Object value;
        if (parentMultivaluedAttributePath != null) {
            final int parentIndex = parentMultivaluedAttributePath.size() - 1;
            int index = 0;

            for (Iterator it = currentGroup.iterator(); it.hasNext();) {
                index++;
                Feature sourceFeature = (Feature) it.next();
                try {
                    value = getValue(expression, sourceFeature);
                } catch (Exception e) {
                    // HACK: what we actually need to resolve is dealing
                    // with queries that restricts the attributes returned
                    // by the source featurestore
                    continue;
                }

                String id = extractIdForAttribute(attMapping, sourceFeature);

                StepList childSteps = setIndexAtStep(xpath, index, parentIndex);

                Attribute instance = xpathAttributeBuilder.set(target, childSteps, value, id,
                        targetNodeType);
                Map clientPropsMappings = attMapping.getClientProperties();
                setClientProperties(instance, sourceFeature, clientPropsMappings);
            }
        } else {

            try {
                value = getValue(expression, source);

                // if target has id construct the id value from source
                String id = extractIdForAttribute(attMapping, this.curSrcFeature);
                Attribute instance = xpathAttributeBuilder.set(target, xpath, value, id,
                        targetNodeType);
                Map clientPropsMappings = attMapping.getClientProperties();
                setClientProperties(instance, curSrcFeature, clientPropsMappings);

            } catch (Exception e) {
                // HACK: what we actually need to resolve is dealing
                // with queries that restricts the attributes returned
                // by the source featurestore
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw (RuntimeException) new RuntimeException().initCause(e);
                }
            }
        }

    }

    /**
     * Sets values of compex attributes, meaning those that are marked by the
     * {@link AttributeMapping#isMultiValued() multivalued flag} on the
     * attribute mapping.
     * 
     * @param sourceFeature
     * @param complexAttrMappings
     * @param targetFeature
     * 
     * @return Feature target feature sets with complex attributes
     */
    private void setMultivaluedAttribute(Feature target, AttributeMapping attMapping,
            List/* <Feature> */group) {

        Expression sourceExpression = attMapping.getSourceExpression();
        final AttributeType targetNodeType = attMapping.getTargetNodeInstance();
        final StepList targetXpath = attMapping.getTargetXPath();

        int index = 0;
        Object value;
        for (Iterator itr = group.iterator(); itr.hasNext();) {
            Feature sourceFeature = (Feature) itr.next();
            try {
                value = getValue(sourceExpression, sourceFeature);
            } catch (Exception e) {
                // HACK: what we actually need to resolve is dealing
                // with queries that restricts the attributes returned
                // by the source featurestore
                continue;
            }
            index++;
            // if target has id construct the id value from source
            String id = extractIdForAttribute(attMapping, sourceFeature);

            // if complex or leaf of complex attribute then insert index in
            // xpath.
            StepList targetXpathAttr = null;

            targetXpathAttr = setIndexOfLastStep(target, targetXpath, index);
            // boolean isComplexType =
            // xpathAttributeBuilder.isComplexType(targetXpath, target
            // .getDescriptor());
            // if (isComplexType) {
            // targetXpathAttr = setIndexOfLastStep(target, targetXpath, index);
            //
            // } else if (isLeafOfNestedComplexType(targetXpath,
            // target.getDescriptor())) {
            // targetXpathAttr = setLeafInexInXPathExpression(target,
            // targetXpath, index);
            // } else {
            // throw new IllegalArgumentException(
            // "Attribute must be complex type or belong to the grouping
            // attributes");
            // }

            Attribute instance = xpathAttributeBuilder.set(target, targetXpathAttr, value, id,
                    targetNodeType);
            Map clientPropsMappings = attMapping.getClientProperties();
            setClientProperties(instance, sourceFeature, clientPropsMappings);
        }
    }

    private void setClientProperties(Attribute target, Feature source, Map clientProperties) {
        if (clientProperties.size() == 0) {
            return;
        }

        final Map nodeAttributes = new HashMap();
        final AttributeDescriptor node = target.getDescriptor();

        for (Iterator it = clientProperties.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            org.opengis.feature.type.Name propName = (org.opengis.feature.type.Name) entry.getKey();
            Expression propExpr = (Expression) entry.getValue();

            Object propValue = getValue(propExpr, source);

            nodeAttributes.put(propName, propValue);
        }

        node.putUserData(Attributes.class, nodeAttributes);
    }

    /**
     * Splits the attribute mappings in two sets, one for grouping attributes
     * and another for non grouping (aka, multiple values) ones.
     * 
     * @param groupByAttributeNames
     *            names of grouping attributes (input)
     * @param allAttrMappings
     *            mappings of attributes feature (input)
     * @param groupMappings
     *            output mappings for single attributes
     * @param nonGroupingMappings
     *            output mappings for complex attributes
     */
    private final void splitMappings(final List allAttrMappings) {

        FilterAttributeExtractor attExtractor = new FilterAttributeExtractor();

        final List multivaluedAttributes = new ArrayList();

        for (Iterator itr = allAttrMappings.iterator(); itr.hasNext();) {
            AttributeMapping attMapping = (AttributeMapping) itr.next();
            Expression source = attMapping.getSourceExpression();

            if (attMapping.isMultiValued()) {
                StepList targetXPath = attMapping.getTargetXPath();
                multivaluedAttributes.add(targetXPath);
            }

            source.accept(attExtractor, null);
            Set sourceAttNames = new HashSet(attExtractor.getAttributeNameSet());
            // if at least one of the attributes used by the expression is not
            // a grouping attribute, the expression addresses a multivalued
            // target property
            if (groupByAttributeNames.containsAll(sourceAttNames)) {
                groupingMappings.add(attMapping);
            } else {
                sourceAttNames.removeAll(groupByAttributeNames);
                GroupingFeatureIterator.LOGGER.fine("attributes of multivalued property: "
                        + sourceAttNames);
                nonGroupingMappings.add(attMapping);
            }
        }

        for (Iterator itr = allAttrMappings.iterator(); itr.hasNext();) {
            AttributeMapping attMapping = (AttributeMapping) itr.next();
            if (attMapping.isMultiValued()) {
                continue;
            }

            StepList targetSteps = attMapping.getTargetXPath();

            for (Iterator mvalues = multivaluedAttributes.iterator(); mvalues.hasNext();) {
                StepList parentSteps = (StepList) mvalues.next();

                if (targetSteps.size() <= parentSteps.size()) {
                    // shortcut. Couldn't be a parent path since it has
                    // less steps than child
                    continue;
                }

                int equalCount = 0;
                for (int i = 0; i < parentSteps.size(); i++) {
                    XPath.Step targetStep = (XPath.Step) targetSteps.get(i);
                    XPath.Step parentStep = (XPath.Step) parentSteps.get(i);
                    if (!targetStep.equals(parentStep)) {
                        break;
                    }
                    equalCount++;
                }
                int parentStepCount = parentSteps.size();
                if (equalCount == parentStepCount) {
                    GroupingFeatureIterator.LOGGER.fine("scheduling " + targetSteps
                            + " as property of multivalued attribute " + parentSteps);
                    multivaluedAttributePaths.put(targetSteps, parentSteps);
                }
            }
        }
    }

    private final StepList setIndexOfLastStep(final Attribute root,
            final StepList xpathAttrDefinition, int index) {

        int insertPosition = xpathAttrDefinition.size();

        StepList indexXpath = insertIndexInXpath(root, xpathAttrDefinition, index, insertPosition);

        return indexXpath;
    }

    /**
     * 
     * @param xpath
     *            XPath expression for which to set the <code>stepIndex</code>'th
     *            attribute to index <code>newInde</code>
     * @param newIndex
     *            index to set for step number <code>stepIndex</code>.
     *            <code>newIndex</code> minimun value is <code>1</code>, as
     *            in XPath spec.
     * @param stepIndex
     *            index of the step in the list of steps for <code>xpath</code>
     *            for which to set the step index to <code>newIndex</code>.
     *            The minimun value is <code>0</code>, as in Java collections
     *            and arrays.
     * @return
     */
    private final StepList setIndexAtStep(StepList xpath, int newIndex, int stepIndex) {

        StepList steps = (StepList) xpath.clone();

        XPath.Step step = (XPath.Step) steps.get(stepIndex);
        step = new XPath.Step(step.getName(), newIndex);
        steps.set(stepIndex, step);

        return steps;
    }

    /**
     * Insert index into step of xpath; Position indicate the step.
     * 
     * @param featureType
     * @param attrXpath
     * @param index
     * @param insertPositon
     * 
     * @return String
     */
    private final StepList insertIndexInXpath(final Attribute root, final StepList attrXpath,
            final int index, final int insertPositon) {

        // Constructs an Xpath adding index in the step corresponding to complex
        // attribute
        StepList stepList = (StepList) attrXpath.clone();

        Step step = (Step) stepList.get(insertPositon - 1);
        Step newStep = new XPath.Step(step.getName(), index);

        stepList.set(insertPositon - 1, newStep);

        return stepList;
    }

    /**
     * Extract the attributes from grouping attributes.
     * 
     * @param Feature
     *            a source feature
     * @return List<List<Attribute>> the the contened list has the attributes
     *         required
     */
    private final List/* <List<Attribute>> */extractGroupingAttributes(ComplexAttribute srcFeature) {

        List/* <List<Attribute>> */attrGroup = new LinkedList/* <List<Attribute>> */();

        for (Iterator itr = this.groupByAttributeNames.iterator(); itr.hasNext();) {
            String attrName = (String) itr.next();
            Name name = Types.attributeName(attrName);
            List/* <Attribute> */listAttrForName = srcFeature.get(name);
            attrGroup.add(listAttrForName);
        }

        return attrGroup;
    }

}

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2009-2011, Open Source Geospatial Foundation (OSGeo)
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.data.complex.PathAttributeList.Pair;
import org.geotools.data.complex.filter.XPath.Step;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.data.complex.xml.XmlFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.LiteralExpressionImpl;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.filter.expression.Expression;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * @author Russell Petty (GeoScience Victoria)
 * @version $Id$
 *
 *
 * @source $URL$
 *         http://svn.osgeo.org/geotools/trunk/modules/unsupported/app-schema/app-schema/src/main
 *         /java/org/geotools/data/complex/FeatureTypeMapping.java $
 */
public class XmlFeatureTypeMapping extends FeatureTypeMapping {

    /**
     * Constants for manipulating XPath Expressions
     */
    private static final String XPATH_SEPARATOR = "/";

    private static final String XPATH_PROPERTY_SEPARATOR = "/@";

    private static final String XPATH_LEFT_INDEX_BRACKET = "[";

    private static final String XPATH_RIGHT_INDEX_BRACKET = "]";

    private Map<String, String> mapping = new HashMap<String, String>();

    private AttributeCreateOrderList attOrderedTypeList = null;

    private Map<String, AttributeMapping> indexAttributeList;

    AttributeMapping rootAttribute;

    private int index = 1;

    /**
     * Attributes that don't have their own label, therefore are children of another node.
     */
    List<AttributeMapping> setterAttributes = new ArrayList<AttributeMapping>();

    PathAttributeList elements;

    protected String itemXpath;

    /**
     * No parameters constructor for use by the digester configuration engine as a JavaBean
     */
    public XmlFeatureTypeMapping() {
        super(null, null, new LinkedList<AttributeMapping>(), new NamespaceSupport());
    }

    public XmlFeatureTypeMapping(FeatureSource source, AttributeDescriptor target,
            List<AttributeMapping> mappings, NamespaceSupport namespaces, String itemXpath) {
        super(source, target, mappings, namespaces);
        this.itemXpath = itemXpath;
        ((XmlFeatureSource) source).setItemXpath(itemXpath);
        try {
            populateFeatureData();
        } catch (IOException ex) {
            throw new RuntimeException("Error occured when trying to create attribute mappings", ex);
        }
    }
    
    public List<String> getStringMappingsIgnoreIndex(final StepList targetPath) {
        List<String> mappings = new ArrayList<String>();
        String path = targetPath.toString();

        Collection<String> c = mapping.keySet();

        // obtain an Iterator for Collection
        Iterator<String> itr = c.iterator();

        // iterate through HashMap values iterator
        while (itr.hasNext()) {

            String listPath = itr.next();
            String unindexedListPath = removeIndexFromPath(listPath);
            if (path.equals(unindexedListPath)) {
                mappings.add(mapping.get(listPath)); 
            }
        }
        
        if (mappings.isEmpty()) {
            // look in the setter attributes
            Iterator<AttributeMapping> leafAtts = setterAttributes.iterator();
            while (leafAtts.hasNext()) {
                AttributeMapping att = leafAtts.next();
                String listPath = att.getTargetXPath().toString();
                String unindexedListPath = removeIndexFromPath(listPath);
                if (path.equals(unindexedListPath)) {
                    mappings.add(att.getSourceExpression().toString());
                }
            }

        }
        
        return mappings;
    }

    private String removeIndexFromPath(String inputPath) {
        String tempPath = inputPath;
        while (tempPath.contains(XPATH_LEFT_INDEX_BRACKET)) {
            int leftIndex = tempPath.indexOf(XPATH_LEFT_INDEX_BRACKET);
            int rightIndex = tempPath.indexOf(XPATH_RIGHT_INDEX_BRACKET, leftIndex);
            String leftTempPath = tempPath.substring(0, leftIndex);
            String rightTempPath = tempPath.substring(rightIndex + 1);
            tempPath = leftTempPath + rightTempPath;
        }

        return tempPath;
    }

    /**
     * Finds the attribute mappings for the given source expression.
     * 
     * @param sourceExpression
     * @return list of matching attribute mappings
     */
    public List<AttributeMapping> getAttributeMappingsByExpression(final Expression sourceExpression) {
        AttributeMapping attMapping;
        List<AttributeMapping> mappings = Collections.emptyList();
        for (Iterator<AttributeMapping> it = attributeMappings.iterator(); it.hasNext();) {
            attMapping = (AttributeMapping) it.next();
            if (sourceExpression.equals(attMapping.getSourceExpression())) {
                if (mappings.size() == 0) {
                    mappings = new ArrayList<AttributeMapping>(2);
                }
                mappings.add(attMapping);
            }
        }
        return mappings;
    }

    /**
     * Finds the attribute mapping for the target expression <code>exactPath</code>
     * 
     * @param exactPath
     *            the xpath expression on the target schema to find the mapping for
     * @return the attribute mapping that match 1:1 with <code>exactPath</code> or <code>null</code>
     *         if
     */
    public AttributeMapping getStringMapping(final StepList exactPath) {
        AttributeMapping attMapping;
        for (Iterator<AttributeMapping> it = attributeMappings.iterator(); it.hasNext();) {
            attMapping = (AttributeMapping) it.next();
            if (exactPath.equals(attMapping.getTargetXPath())) {
                return attMapping;
            }
        }
        return null;
    }

    public void populateFeatureData() throws IOException {

        List<AttributeMapping> attMap = getAttributeMappings();
        if (attOrderedTypeList == null) {
            initialiseAttributeLists(attMap);
        }
        // create required elements
        PathAttributeList elements = new PathAttributeList();
        String xpath = rootAttribute.getInstanceXpath() == null ? itemXpath : itemXpath
                + XPATH_SEPARATOR + rootAttribute.getInstanceXpath();
        
        elements.put(rootAttribute.getLabel(), xpath, null);
        if (!rootAttribute.getIdentifierExpression().equals(Expression.NIL)) {
            String id;
            if (rootAttribute.getInstanceXpath() != null) {
                id = rootAttribute.getInstanceXpath() + XPATH_SEPARATOR + rootAttribute.getIdentifierExpression();
            } else {
                id = rootAttribute.getIdentifierExpression().toString();
            }                    
            mapping.put("@gml:id", id);    
        }        

        // iterator returns the attribute mappings starting from the root of the tree.
        // parents are always returned before children elements.
        Iterator<AttributeMapping> it = attOrderedTypeList.iterator();
        addComplexAttributes(elements, it);
        addSetterAttributes(elements);

        this.elements = elements;
        
        index++;
        removeAllRelativePaths();
    }

    private void addComplexAttributes(PathAttributeList elements, Iterator<AttributeMapping> it) {
        while (it.hasNext()) {
            AttributeMapping attMapping = it.next();
            final Expression sourceExpression = attMapping.getIdentifierExpression();

            List<Pair> ls = elements.get(attMapping.getParentLabel());
            if (ls != null) {
                for (int i = 0; i < ls.size(); i++) {
                    Pair parentAttribute = ls.get(i);
                    String instancePath = attMapping.getInstanceXpath();
                    int count = 1;
                    String countXpath = null;
                    // if instance path not set, then element exists, with one instance
                    if (instancePath != null) {
                        countXpath = parentAttribute.getXpath() + XPATH_SEPARATOR
                                + attMapping.getInstanceXpath();
                    }

                    for (int j = 0; j < count; j++) {
                        final String bracketIndex = "";
                        String xpath = null;
                        if (instancePath == null) {
                            xpath = parentAttribute.getXpath() + XPATH_SEPARATOR
                                    + sourceExpression.toString();
                        } else {
                            xpath = parentAttribute.getXpath() + XPATH_SEPARATOR
                                    + attMapping.getInstanceXpath() + bracketIndex
                                    + XPATH_SEPARATOR + sourceExpression.toString();
                        }
                        String label = getFullQueryPath(attMapping, attMapping
                                .getSourceExpression().toString());
                        
                        mapping.put(label + XPATH_PROPERTY_SEPARATOR + "gml:id", xpath);
                        
                        StepList sl = attMapping.getTargetXPath();
                        setPathIndex(j, sl);
                        Attribute subFeature = null;
                        elements.put(attMapping.getLabel(), countXpath + bracketIndex, subFeature);
                    }
                }
            }
        }
    }

    private void addSetterAttributes(PathAttributeList elements) {
        for (AttributeMapping attMapping : setterAttributes) {
            List<Pair> ls = elements.get(attMapping.getParentLabel());
            if (ls != null) {
                for (int i = 0; i < ls.size(); i++) {
                    Pair parentPair = ls.get(i);
                    final Expression sourceExpression = attMapping.getSourceExpression();

                    StringBuffer usedXpath = (StringBuffer) getValue(parentPair.getXpath(),
                            sourceExpression);
                    if (usedXpath != null) {
                        String label = getFullQueryPath(attMapping, attMapping.getTargetXPath()
                                .toString());
                        mapping.put(label, usedXpath.toString());
                        addClientProperties(attMapping, usedXpath, label);
                    } else {
                        usedXpath = new StringBuffer(parentPair.getXpath());
                        String label = getFullQueryPath(attMapping, attMapping.getTargetXPath()
                                .toString());
                        addClientProperties(attMapping, usedXpath, label);
                    }

                }
            }

        }
    }

    private void addClientProperties(AttributeMapping attMapping, StringBuffer usedXpath,
            String label) {
        Map<Name, Expression> clientProperties = attMapping.getClientProperties();
        if (clientProperties.size() != 0) {

            for (Map.Entry<Name, Expression> entry : clientProperties.entrySet()) {
                Name propName = entry.getKey();
                Expression propExpr = entry.getValue();
                Object xPath = getValue(usedXpath.toString(), propExpr);
                if (xPath != null) {                                    
                    mapping.put(label + XPATH_PROPERTY_SEPARATOR  + getPropertyNameXpath(propName), 
                            xPath.toString());                                    
                }
            }
        }
    }

    private String getPropertyNameXpath(Name propName) {
        String xpath;
        String namespaceUri = propName.getNamespaceURI();                                    
        if (namespaceUri != null) {
            String namespace = namespaces.getPrefix(namespaceUri);
            xpath = namespace + propName.getSeparator()
                        + propName.getLocalPart();
        } else {
            xpath = propName.getLocalPart();
        }
        return xpath;
    }
    
    private void setPathIndex(int j, StepList sl) {
        if (j > 0) {
            Step st = sl.get(sl.size() - 1);
            Step st2 = new Step(st.getName(), j + 1, st.isXmlAttribute());
            sl.remove(sl.size() - 1);
            sl.add(st2);
        }
    }

    protected Object getValue(String xpathPrefix, Expression node) {
        StringBuffer usedXpath = new StringBuffer();
        String expressionValue = node.toString();
        if (expressionValue.startsWith("'") || node instanceof LiteralExpressionImpl) {
            usedXpath.append(xpathPrefix);
            return null;
        } else {
            expressionValue = xpathPrefix + XPATH_SEPARATOR + expressionValue;
        }
        usedXpath.append(expressionValue);
        return usedXpath;
    }

    private void initialiseAttributeLists(List<AttributeMapping> mappings) {

        for (AttributeMapping attMapping : mappings) {
            if (attMapping.getLabel() != null && attMapping.getParentLabel() == null
                    && attMapping.getTargetNodeInstance() == null) {

                rootAttribute = attMapping;
                break;
            }
        }

        attOrderedTypeList = new AttributeCreateOrderList(rootAttribute.getLabel());
        indexAttributeList = new HashMap<String, AttributeMapping>();
        indexAttributeList.put(rootAttribute.getLabel(), rootAttribute);

        for (AttributeMapping attMapping : mappings) {
            if (attMapping.getLabel() == null) {
                setterAttributes.add(attMapping);
            } else if (attMapping.getParentLabel() != null) {
                attOrderedTypeList.put(attMapping);
                indexAttributeList.put(attMapping.getLabel(), attMapping);
            }
        }
    }

    protected void setClientProperties(final Attribute target, final Object source,
            final Map<Name, Expression> clientProperties) {
        if (clientProperties.size() == 0) {
            return;
        }
        final Map<Name, Object> targetAttributes = new HashMap<Name, Object>();
        for (Map.Entry<Name, Expression> entry : clientProperties.entrySet()) {
            Name propName = entry.getKey();
            Expression propExpr = entry.getValue();
            Object propValue = null; // getValue(propExpr, source);
            if (propValue != null) {
                List<String> ls = (List<String>) propValue;
                if (ls.size() != 0) {
                    propValue = ls.get(0);
                } else {
                    propValue = "";
                }
            }
            targetAttributes.put(propName, propValue);
        }
    }

    private String getFullQueryPath(AttributeMapping attMapping, String initialString) {
        StringBuffer name = new StringBuffer();
        if (!Expression.NIL.toString().equals(initialString)) {
            name.append(initialString);
        }
        AttributeMapping tam = attMapping;
        while (tam.getParentLabel() != null) {
            tam = indexAttributeList.get(tam.getParentLabel());
            if (!rootAttribute.equals(tam) && !tam.getSourceExpression().equals(Expression.NIL)) {
                if (name.length() > 0) {
                    name.insert(0, XPATH_SEPARATOR);
                }
                name.insert(0, tam.getSourceExpression());
            }
        }
        return name.toString();
    }

    private void removeAllRelativePaths() {

        Collection<String> c = mapping.keySet();
        Iterator<String> itr = c.iterator();

        while (itr.hasNext()) {
            String key = itr.next();
            String xPath = mapping.get(key);
            String xPath2 = removeRelativePaths(xPath);
            if (!xPath.equals(xPath2)) {
                mapping.put(key, xPath2);
            }
        }
    }

    private String removeRelativePaths(String xPath) {
        String xPathTemp = xPath;
        final int NOT_FOUND = -1;
        final String RELATIVE_PATH = "/../";

        int i = xPathTemp.indexOf(RELATIVE_PATH);
        while (i != NOT_FOUND) {
            int slashPos = xPathTemp.lastIndexOf(XPATH_SEPARATOR, i - 1);
            if (slashPos != NOT_FOUND) {
                String left = xPathTemp.substring(0, slashPos + 1);
                String right = xPathTemp.substring(i + RELATIVE_PATH.length());
                xPathTemp = left + right;
            } else {
                break;
            }
            i = xPathTemp.indexOf(RELATIVE_PATH);
        }
        return xPathTemp;
    }
    
    /**
     * Looks up for attribute mappings matching the xpath expression <code>propertyName</code>.
     * <p>
     * If any step in <code>propertyName</code> has index greater than 1, any mapping for the same
     * property applies, regardless of the mapping. For example, if there are mappings for
     * <code>gml:name[1]</code>, <code>gml:name[2]</code> and <code>gml:name[3]</code>, but
     * propertyName is just <code>gml:name</code>, all three mappings apply.
     * </p>
     * 
     * @param mappings
     *            Feature type mapping to search for
     * @param simplifiedSteps
     * @return
     */
    @Override
    public List<Expression> findMappingsFor(final StepList propertyName) {
        List<Expression> expressions = null;
        
        // get all matching mappings if index is not specified, otherwise
        // get the specified mapping
        if (!propertyName.toString().contains("[")) {
            // collect all the mappings for the given property
            List<String> candidates = getStringMappingsIgnoreIndex(propertyName);
            expressions = getExpressions(candidates);
        }

        if (expressions == null || expressions.isEmpty()) {
            // get specified mapping if indexed or that expressions is not found because it
            // could be attribute mapping with OCQL containing functions, instead of inputattribute
            // with xpath
            expressions = new ArrayList<Expression>(1);
            AttributeMapping mapping = getStringMapping(propertyName);
            if (mapping != null) {   
//TODO handle instancepath and also consider functions
//                if (mapping.getInstanceXpath() != null) {
//                    // add prefix
//                } else {
                    expressions.add(mapping.getSourceExpression());
//                }
            }
        }
        return expressions;
        
        
    }

    private List<Expression> getExpressions(List<String> candidates) {
        List<Expression> ls = new ArrayList<Expression>(candidates.size());
        Iterator<String> itr = candidates.iterator();
        while (itr.hasNext()) {
            String element = itr.next();
            Expression ex = CommonFactoryFinder.getFilterFactory2(null).property(element);
            ls.add(ex);
        }
        return ls;
    }
}

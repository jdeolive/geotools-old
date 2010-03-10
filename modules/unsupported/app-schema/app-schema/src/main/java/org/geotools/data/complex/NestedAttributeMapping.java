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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.complex.filter.XPath.StepList;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.Types;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.FilterFactoryImplNamespaceAware;
import org.geotools.filter.NestedAttributeExpression;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * This class represents AttributeMapping for attributes that are nested inside another complex
 * attribute. The nested attributes would be features, or fake features, ie. complex attributes
 * which types are wrapped with NonFeatureTypeProxy instances. The purpose of this class is to store
 * nested built features so they can be retrieved when the parent feature is being built. Simple
 * features are also stored for caching if a filter involving these nested features is run.
 * 
 * @author Rini Angreani, Curtin University of Technology
 * 
 * @source $URL:
 *         http://svn.osgeo.org/geotools/trunk/modules/unsupported/app-schema/app-schema/src/main
 *         /java/org/geotools/data/complex/NestedAttributeMapping.java $
 */
public class NestedAttributeMapping extends AttributeMapping {
    /**
     * Input feature source of the nested features
     */
    private FeatureSource<FeatureType, Feature> source;

    /**
     * Mapped feature source of the nested features
     */
    private FeatureSource<FeatureType, Feature> mappingSource;

    /**
     * Name of the nested features element
     */
    private final Expression nestedFeatureType;

    /**
     * Target xpath that links to nested features
     */
    private final StepList nestedTargetXPath;

    /**
     * Source expression of the nested features
     */
    private Expression nestedSourceExpression;

    /**
     * Filter factory
     */
    private FilterFactory filterFac;

    private NamespaceSupport namespaces;

    /**
     * Sole constructor
     * 
     * @param idExpression
     * @param parentExpression
     * @param targetXPath
     * @param targetNodeInstance
     * @param isMultiValued
     * @param clientProperties
     * @param sourceElement
     *            parent feature element type
     * @param sourcePath
     *            XPath link to nested feature
     * @param parentSource
     *            parent feature source
     * @throws IOException
     */
    public NestedAttributeMapping(Expression idExpression, Expression parentExpression,
            StepList targetXPath, boolean isMultiValued, Map<Name, Expression> clientProperties,
            Expression sourceElement, StepList sourcePath, NamespaceSupport namespaces)
            throws IOException {
        super(idExpression, parentExpression, targetXPath, null, isMultiValued, clientProperties);
        this.nestedTargetXPath = sourcePath;
        this.nestedFeatureType = sourceElement;
        this.filterFac = new FilterFactoryImplNamespaceAware(namespaces);
        this.namespaces = namespaces;
    }

    @Override
    /*
     * @see org.geotools.data.complex.AttributeMapping#isNestedAttribute()
     */
    public boolean isNestedAttribute() {
        return true;
    }

    /**
     * Get matching input features that are stored in this mapping using a supplied link value.
     * 
     * @param foreignKeyValue
     * @param reprojection
     *            Reprojected CRS or null
     * @return The matching input feature
     * @throws IOException
     * @throws IOException
     */
    public Collection<Feature> getInputFeatures(Object foreignKeyValue,
            CoordinateReferenceSystem reprojection, Feature feature) throws IOException {
        if (isPolymorphic()) {
            // if linkField is null, this method shouldn't be called because the mapping
            // should use the same table, and handles it differently
            throw new UnsupportedOperationException(
                    "Link field is missing from feature chaining mapping!");
        }
        ArrayList<Feature> matchingFeatures = new ArrayList<Feature>();
        if (source == null || !(nestedFeatureType instanceof AttributeExpressionImpl)) {
            // We can't initiate this in the constructor because the feature type mapping
            // might not be built yet.
            Name featureTypeName = getNestedFeatureType(feature);
            if (featureTypeName == null) {
                // this could be legitimate, for some null values polymorphism use case
                return Collections.EMPTY_LIST;
            }
            FeatureTypeMapping featureTypeMapping = AppSchemaDataAccessRegistry
                    .getMapping(featureTypeName);
            assert featureTypeMapping != null;

            source = featureTypeMapping.getSource();
            assert source != null;

            // find source expression on nested features side
            AttributeMapping mapping = featureTypeMapping
                    .getAttributeMapping(this.nestedTargetXPath);
            assert mapping != null;
            nestedSourceExpression = mapping.getSourceExpression();
        }
        assert nestedSourceExpression != null;

        Filter filter = filterFac.equals(this.nestedSourceExpression, filterFac
                .literal(foreignKeyValue));
        // get all the nested features based on the link values
        DefaultQuery query = new DefaultQuery();
        query.setCoordinateSystemReproject(reprojection);
        query.setFilter(filter);
        FeatureCollection<FeatureType, Feature> fCollection = source.getFeatures(query);
        Iterator<Feature> it = fCollection.iterator();

        while (it.hasNext()) {
            Feature f = it.next();
            Object value = this.nestedSourceExpression.evaluate(f);
            if (value != null && value.equals(foreignKeyValue)) {
                matchingFeatures.add(f);
            }
        }
        fCollection.close(it);

        return matchingFeatures;
    }

    /**
     * Get the maching built features that are stored in this mapping using a supplied link value
     * 
     * @param foreignKeyValue
     * @param reprojection
     *            Reprojected CRS or null
     * @return The matching simple features
     * @throws IOException
     */
    public Collection<Feature> getFeatures(Object foreignKeyValue,
            CoordinateReferenceSystem reprojection, Feature feature) throws IOException {
        if (isPolymorphic()) {
            // if linkField is null, this method shouldn't be called because the mapping
            // should use the same table, and handles it differently
            throw new UnsupportedOperationException(
                    "Link field is missing from feature chaining mapping!");
        }
        if (mappingSource == null || !(nestedFeatureType instanceof AttributeExpressionImpl)) {
            // initiate if null, or evaluate a new one if the targetElement is a function
            // which value depends on the feature
            Name featureTypeName = getNestedFeatureType(feature);
            if (featureTypeName == null) {
                return Collections.EMPTY_LIST;
            }
            // this cannot be set in the constructor since it might not exist yet
            mappingSource = DataAccessRegistry.getFeatureSource(featureTypeName);
        }
        assert mappingSource != null;
        Filter filter;

        ArrayList<Feature> matchingFeatures = new ArrayList<Feature>();
        PropertyName propertyName = null;

        if (!(mappingSource instanceof MappingFeatureSource)) {
            // non app-schema source.. may not have simple feature source behind it
            // so we need to filter straight on the complex features
            propertyName = new NestedAttributeExpression(this.nestedTargetXPath.toString(),
                    reprojection);
        } else {
            propertyName = filterFac.property(this.nestedTargetXPath.toString());
        }
        filter = filterFac.equals(propertyName, filterFac.literal(foreignKeyValue));

        DefaultQuery query = new DefaultQuery();
        query.setCoordinateSystemReproject(reprojection);
        query.setFilter(filter);

        // get all the mapped nested features based on the link values
        FeatureCollection<FeatureType, Feature> fCollection = mappingSource.getFeatures(query);
        Iterator<Feature> iterator = fCollection.iterator();
        while (iterator.hasNext()) {
            matchingFeatures.add(iterator.next());
        }
        fCollection.close(iterator);

        return matchingFeatures;
    }

    /**
     * @return the nested feature type name
     */
    public Name getNestedFeatureType(Feature feature) {
        Object fTypeValue;
        if (nestedFeatureType instanceof AttributeExpressionImpl) {
            fTypeValue = ((AttributeExpressionImpl) nestedFeatureType).getPropertyName();
        } else {
            fTypeValue = nestedFeatureType.evaluate(feature);
        }
        if (fTypeValue == null) {
            // this could be legitimate, i.e. in polymorphism
            // to evaluate a function with a certain column value
            // if null, don't encode this element
            return null;
        }
        return Types.degloseName(fTypeValue.toString(), namespaces);
    }

    public boolean isPolymorphic() {
        // if the linkField is null, we're meant to work out the nestedFeatureType from
        // the linkElement, which should contain a function. So the value could vary
        // feature per feature.
        return this.nestedTargetXPath == null;
    }
}

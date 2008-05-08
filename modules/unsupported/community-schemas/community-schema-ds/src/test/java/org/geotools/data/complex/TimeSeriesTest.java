/*
 *    Geotools2 - OpenSource mapping toolkit
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
 *
 */
package org.geotools.data.complex;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.geotools.data.DataAccessFinder;
import org.geotools.data.complex.config.ComplexDataStoreConfigurator;
import org.geotools.data.complex.config.ComplexDataStoreDTO;
import org.geotools.data.complex.config.EmfAppSchemaReader;
import org.geotools.data.complex.config.XMLConfigDigester;
import org.geotools.data.feature.FeatureAccess;
import org.geotools.data.feature.FeatureSource2;
import org.geotools.feature.iso.Types;
import org.geotools.filter.AttributeExpressionImpl;
import org.geotools.filter.FilterFactoryImplNamespaceAware;
import org.geotools.xlink.XLINK;
import org.opengis.feature.Attribute;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureCollection;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.NamespaceSupport;

/**
 * DOCUMENT ME!
 * 
 * @author Rob Atkinson
 * @version $Id$
 * @source $URL$
 * @since 2.4
 */
public class TimeSeriesTest extends TestCase {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(TimeSeriesTest.class.getPackage()
            .getName());

    private static final String AWNS = "http://brs.gov.au/awdip/0.2";

    private static final String CVNS = "http://www.opengis.net/cv/0.0";

    private static final String SANS = "http://www.opengis.net/sampling/0.0";

    private static final String OMNS = "http://www.opengis.net/om";

    private static final String SWENS = "http://www.opengis.net/swe/0.0";

    private static final String GMLNS = "http://www.opengis.net/gml";

    private static final String GEONS = "http://www.seegrid.csiro.au/xml/geometry";

    final String schemaBase = "/test-data/";

    EmfAppSchemaReader reader;

    private FeatureSource2 source;

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        reader = EmfAppSchemaReader.newInstance();

        // Logging.GEOTOOLS.forceMonolineConsoleOutput(Level.FINEST);
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws Exception
     *             DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param location
     *            schema location path discoverable through
     *            getClass().getResource()
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    private void loadSchema(String location) throws IOException {
        // load needed GML types directly from the gml schemas
        URL schemaLocation = getClass().getResource(location);
        if (schemaLocation == null) {
            schemaLocation = new URL(location);
        }
        assertNotNull(location, schemaLocation);
        reader.parse(schemaLocation);
    }

    /**
     * Tests if the schema-to-FM parsing code developed for complex datastore
     * configuration loading can parse the GeoSciML types
     * 
     * @throws Exception
     */
    public void testParseSchema() throws Exception {
        /*
         * not found types and elements:
         */

        // load geosciml schema
        try {
            String schemaLocation = schemaBase + "SampleSite.xsd";
            // schemaLocation =
            // "file:/home/gabriel/svn/geoserver/trunk/configuration/community-schema-timeseries2/SampleSite.xsd";
            loadSchema(schemaLocation);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        Map typeRegistry = reader.getTypeRegistry();

        TypeName typeName = Types.typeName(AWNS, "SiteSinglePhenomTimeSeriesType");
        ComplexType testType = (ComplexType) typeRegistry.get(typeName);
        assertNotNull(testType);
        assertTrue(testType instanceof FeatureType);

        AttributeType superType = testType.getSuper();
        assertNotNull(superType);

        TypeName superTypeName = Types.typeName(AWNS, "SamplingSiteType");
        assertEquals(superTypeName, superType.getName());
        assertTrue(superType instanceof FeatureType);

        // ensure all needed types were parsed and aren't just empty proxies
        Map samplingProperties = new HashMap();

        // from gml:AbstractFeatureType
        samplingProperties.put(name(GMLNS, "metaDataProperty"), typeName(GMLNS,
                "MetaDataPropertyType"));
        samplingProperties.put(name(GMLNS, "description"), typeName(GMLNS, "StringOrRefType"));
        samplingProperties.put(name(GMLNS, "name"), typeName(GMLNS, "CodeType"));
        samplingProperties.put(name(GMLNS, "boundedBy"), typeName(GMLNS, "BoundingShapeType"));
        samplingProperties.put(name(GMLNS, "location"), typeName(GMLNS, "LocationPropertyType"));

        // aw:SamplingSiteType
        samplingProperties.put(name(AWNS, "samplingRegimeType"), typeName(AWNS, "NRSamplingTypeCodeType"));
        samplingProperties.put(name(AWNS, "waterBodyType"), typeName(GMLNS, "CodeType"));
        samplingProperties.put(name(AWNS, "accessTypeCode"), typeName(GMLNS, "CodeType"));
        
        // sa:SamplingPointType
        samplingProperties.put(name(SANS, "position"), typeName(GMLNS, "PointPropertyType"));
        samplingProperties.put(name(SANS, "elevation"), typeName(GMLNS, "DirectPositionType"));
        samplingProperties.put(name(SANS, "timeVaryingProperty"), typeName(SANS, "CV_Coverage_PropertyType"));
        
        // sa:SamplingFeatureType
        samplingProperties.put(name(SANS, "relatedObservation"), typeName(OMNS, "AbstractObservationPropertyType"));
        samplingProperties.put(name(SANS, "relatedSamplingFeature"), typeName(SANS, "SamplingFeatureRelationPropertyType"));
        samplingProperties.put(name(SANS, "sampledFeature"), typeName(GMLNS, "FeaturePropertyType"));
        samplingProperties.put(name(SANS, "surveyDetails"), typeName(SANS, "SurveyProcedurePropertyType"));
        
        // sa:SiteSinglePhenomTimeSeriesType
        samplingProperties.put(name(AWNS, "relatedObservation"), typeName(AWNS,
                "PhenomenonTimeSeriesPropertyType"));

        assertPropertyNamesAndTypeNames(testType, samplingProperties);

        AttributeDescriptor relatedObservation = (AttributeDescriptor) Types.descriptor(testType,
                name(AWNS, "relatedObservation"));
        Map relatedObsProps = new HashMap();
        relatedObsProps.put(name(AWNS, "PhenomenonTimeSeries"), typeName(AWNS,
                "PhenomenonTimeSeriesType"));
        ComplexType phenomenonTimeSeriesPropertyType = (ComplexType) relatedObservation.getType();

        assertPropertyNamesAndTypeNames(phenomenonTimeSeriesPropertyType, relatedObsProps);

        AttributeDescriptor phenomenonTimeSeries = (AttributeDescriptor) Types.descriptor(
                phenomenonTimeSeriesPropertyType, name(AWNS, "PhenomenonTimeSeries"));
        ComplexType phenomenonTimeSeriesType = (ComplexType) phenomenonTimeSeries.getType();
        Map phenomenonTimeSeriesProps = new HashMap();
        // from
        // aw:WaterObservationType/om:TimeSeriesObsType/om:AbstractObservationType
        phenomenonTimeSeriesProps.put(name(OMNS, "procedure"), typeName(OMNS,
                "ObservationProcedurePropertyType"));
        phenomenonTimeSeriesProps.put(name(OMNS, "countParameter"), typeName(SWENS,
                "TypedCountType"));
        phenomenonTimeSeriesProps.put(name(OMNS, "measureParameter"), typeName(SWENS,
                "TypedMeasureType"));
        phenomenonTimeSeriesProps.put(name(OMNS, "termParameter"), typeName(SWENS,
                "TypedCategoryType"));
        phenomenonTimeSeriesProps.put(name(OMNS, "observedProperty"), typeName(SWENS,
                "PhenomenonPropertyType"));

        // from PhenomenonTimeSeriesType
        phenomenonTimeSeriesProps.put(name(AWNS, "result"), typeName(CVNS,
                "CompactDiscreteTimeCoveragePropertyType"));

        assertPropertyNamesAndTypeNames(phenomenonTimeSeriesType, phenomenonTimeSeriesProps);

        AttributeDescriptor observedProperty = (AttributeDescriptor) Types.descriptor(
                phenomenonTimeSeriesType, name(OMNS, "observedProperty"));

        ComplexType phenomenonPropertyType = (ComplexType) observedProperty.getType();

        assertPropertyNamesAndTypeNames(phenomenonPropertyType, Collections.singletonMap(name(
                SWENS, "Phenomenon"), typeName(SWENS, "PhenomenonType")));

        AttributeDescriptor phenomenon = (AttributeDescriptor) Types.descriptor(
                phenomenonPropertyType, name(SWENS, "Phenomenon"));
        ComplexType phenomenonType = (ComplexType) phenomenon.getType();
        assertNotNull(phenomenonType.getSuper());
        assertEquals(typeName(GMLNS, "DefinitionType"), phenomenonType.getSuper().getName());

        Map phenomenonProps = new HashMap();
        // from gml:DefinitionType
        phenomenonProps.put(name(GMLNS, "metaDataProperty"), null);
        phenomenonProps.put(name(GMLNS, "description"), null);
        phenomenonProps.put(name(GMLNS, "name"), null);

        assertPropertyNamesAndTypeNames(phenomenonType, phenomenonProps);
    }

    private void assertPropertyNamesAndTypeNames(ComplexType parentType,
            Map expectedPropertiesAndTypes) throws Exception {

        for (Iterator it = expectedPropertiesAndTypes.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Entry) it.next();
            Name dName = (Name) entry.getKey();
            TypeName tName = (TypeName) entry.getValue();

            AttributeDescriptor d = (AttributeDescriptor) Types.descriptor(parentType, dName);
            assertNotNull("Descriptor not found: " + dName, d);
            AttributeType type;
            try {
                type = d.getType();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "type not parsed for "
                        + ((AttributeDescriptor) d).getName(), e);
                throw e;
            }
            assertNotNull(type);
            assertNotNull(type.getName());
            assertNotNull(type.getBinding());
            if (tName != null) {
                assertEquals(tName, type.getName());
            }
        }
    }

    private TypeName typeName(String ns, String localName) {
        return Types.typeName(ns, localName);
    }

    private Name name(String ns, String localName) {
        return Types.attributeName(ns, localName);
    }

    public void testLoadMappingsConfig() throws Exception {
        XMLConfigDigester reader = new XMLConfigDigester();
        String configLocation = schemaBase + "TimeSeriesTest_properties.xml";
        URL url = getClass().getResource(configLocation);

        // configLocation =
        // "file:/home/gabriel/svn/geoserver/trunk/configuration/community-schema-timeseries2/TimeSeriesTest_properties.xml";
        // URL url = new URL(configLocation);

        ComplexDataStoreDTO config = reader.parse(url);

        Set mappings = ComplexDataStoreConfigurator.buildMappings(config);

        assertNotNull(mappings);
        assertEquals(1, mappings.size());

        FeatureTypeMapping mapping = (FeatureTypeMapping) mappings.iterator().next();

        AttributeDescriptor targetFeature = mapping.getTargetFeature();
        assertNotNull(targetFeature);
        assertNotNull(targetFeature.getType());
        assertEquals(AWNS, targetFeature.getName().getNamespaceURI());
        assertEquals("SiteSinglePhenomTimeSeries", targetFeature.getName().getLocalPart());

        List attributeMappings = mapping.getAttributeMappings();
        AttributeMapping attMapping = (AttributeMapping) attributeMappings.get(0);
        assertNotNull(attMapping);
        assertEquals("aw:SiteSinglePhenomTimeSeries", attMapping.getTargetXPath().toString());

        attMapping = (AttributeMapping) attributeMappings.get(1);
        assertNotNull(attMapping);
        //note the mapping says SiteSinglePhenomTimeSeries/gml:name[1] but
        //attMapping.getTargetXPath().toString() results in a simplyfied form
        assertEquals("gml:name", attMapping.getTargetXPath().toString());
        
        attMapping = (AttributeMapping) attributeMappings.get(2);
        assertNotNull(attMapping);
        assertEquals("sa:sampledFeature", attMapping.getTargetXPath().toString());
        //this mapping has no source expression, just client properties
        assertSame(Expression.NIL, attMapping.getSourceExpression());
        assertSame(Expression.NIL, attMapping.getIdentifierExpression());
        Map clientProperties = attMapping.getClientProperties();
        assertEquals(2, clientProperties.size());

        Name clientPropName = name(XLINK.NAMESPACE, "title");
        assertTrue("client property " + clientPropName + " not found", clientProperties
                .containsKey(clientPropName));
        clientPropName = name(XLINK.NAMESPACE, "href");
        assertTrue("client property " + clientPropName + " not found", clientProperties
                .containsKey(clientPropName));

        
        // now test the use of specific subtype overriding a general node type
        attMapping = (AttributeMapping) attributeMappings.get(3);
        assertNotNull(attMapping);
        String expected = "aw:relatedObservation/aw:PhenomenonTimeSeries/om:observedProperty/swe:Phenomenon/gml:name";
        String actual = attMapping.getTargetXPath().toString();
        assertEquals(expected, actual);
    }

    public void testDataStore() throws Exception {
        final Map dsParams = new HashMap();
        String configLocation = schemaBase + "TimeSeriesTest_properties.xml";
        final URL url = getClass().getResource(configLocation);
        // configLocation =
        // "file:/home/gabriel/svn/geoserver/trunk/configuration/community-schema-timeseries2/TimeSeriesTest_properties.xml";
        // URL url = new URL(configLocation);

        dsParams.put("dbtype", "complex");
        dsParams.put("url", url.toExternalForm());

        Map propsParams = new HashMap();

        final Name typeName = new org.geotools.feature.Name(AWNS, "SiteSinglePhenomTimeSeries");

        FeatureAccess mappingDataStore = (FeatureAccess) DataAccessFinder.createAccess(dsParams);
        assertNotNull(mappingDataStore);

        AttributeDescriptor attDesc = (AttributeDescriptor) mappingDataStore.describe(typeName);
        assertNotNull(attDesc);
        assertTrue(attDesc.getType() instanceof FeatureType);

        FeatureType fType = (FeatureType) attDesc.getType();

        FeatureSource2 fSource = (FeatureSource2) mappingDataStore.access(typeName);

        // make a getFeatures request with a nested properties filter.
        // note that the expected result count is 6 - 3 sites x 2 phenomena
        final int EXPECTED_RESULT_COUNT = 6;

        FeatureCollection features = (FeatureCollection) fSource.content();

        int resultCount = features.size();
        String msg = "be sure difference in result count is not due to different dataset.";
        assertEquals(msg, EXPECTED_RESULT_COUNT, resultCount);

        Feature feature;
        int count = 0;

        NamespaceSupport namespaces = new NamespaceSupport();
        namespaces.declarePrefix("aw", AWNS);
        namespaces.declarePrefix("om", OMNS);
        namespaces.declarePrefix("swe", SWENS);
        namespaces.declarePrefix("gml", GMLNS);
        namespaces.declarePrefix("sa", SANS);
        // TODO: use commonfactoryfinder or the mechanism choosed
        // to pass namespace context to filter factory
        FilterFactory ffac = new FilterFactoryImplNamespaceAware(namespaces);

        final String phenomNamePath = "aw:relatedObservation/aw:PhenomenonTimeSeries/om:observedProperty/swe:Phenomenon/gml:name";
        Iterator it = features.iterator();
        for (; it.hasNext();) {
            feature = (Feature) it.next();
            count++;

            PropertyName gmlName = ffac.property("gml:name");
            PropertyName phenomName = ffac.property(phenomNamePath);

            Object nameVal = gmlName.evaluate(feature, String.class);
            assertNotNull("gml:name evaluated to null", nameVal);

            Object phenomNameVal = phenomName.evaluate(feature, String.class);
            assertNotNull(phenomNamePath + " evaluated to null", phenomNameVal);

            PropertyName sampledFeatureName = ffac.property("sa:sampledFeature");
            Attribute sampledFeatureVal = (Attribute) sampledFeatureName.evaluate(feature);
            assertNotNull("sa:sampledFeature evaluated to null", sampledFeatureVal);
            assertNull(sampledFeatureVal.get());
            Map attributes = (Map) sampledFeatureVal.getDescriptor().getUserData(Attributes.class);
            assertNotNull(attributes);
            Name xlinkTitle = name(XLINK.NAMESPACE, "title");
            assertTrue(attributes.containsKey(xlinkTitle));
            assertNotNull(attributes.get(xlinkTitle));

            Name xlinkHref = name(XLINK.NAMESPACE, "href");
            assertTrue(attributes.containsKey(xlinkHref));
            assertNotNull(attributes.get(xlinkHref));
        }
        features.close(it);

        assertEquals(EXPECTED_RESULT_COUNT, count);
    }
}

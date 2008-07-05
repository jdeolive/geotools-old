/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.geotools.data.DataSourceException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.gml3.GML;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.util.Converters;
import org.geotools.wfs.WFS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * A {@link GetFeatureParser} implementation that uses plain xml pull to parse a
 * GetFeature response.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5.x
 * @source $URL$
 * @deprecated should be removed as long as {@link StreamingParserFeatureReader} works well
 */
public class XmlSimpleFeatureParser implements GetFeatureParser {

    private static final GeometryFactory geomFac = new GeometryFactory();

    private InputStream inputStream;

    private XmlPullParser parser;

    private SimpleFeatureType targetType;

    private SimpleFeatureBuilder builder;

    private QName name;

    final String featureNamespace;

    final String featureName;

    private final Set<String> expectedProperties;

    public XmlSimpleFeatureParser(final InputStream getFeatureResponseStream,
            final QName featureName, final SimpleFeatureType targetType) throws IOException {
        this.inputStream = getFeatureResponseStream;
        this.name = featureName;
        this.featureNamespace = this.name.getNamespaceURI();
        this.featureName = this.name.getLocalPart();
        this.targetType = targetType;
        this.builder = new SimpleFeatureBuilder(targetType);

        XmlPullParserFactory factory;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);

            // parse root element
            parser = factory.newPullParser();
            parser.setInput(inputStream, "UTF-8");
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, WFS.NAMESPACE, WFS.FeatureCollection
                    .getLocalPart());
        } catch (XmlPullParserException e) {
            throw new DataSourceException(e);
        }

        expectedProperties = new HashSet<String>();
        for (AttributeDescriptor desc : targetType.getAttributeDescriptors()) {
            expectedProperties.add(desc.getLocalName());
        }
    }

    public void close() throws IOException {
        if (this.inputStream != null) {
            try {
                this.parser.setInput(null);
                this.parser = null;
                this.inputStream.close();
                this.inputStream = null;
            } catch (XmlPullParserException e) {
                throw new DataSourceException(e);
            }
        }
    }

    public SimpleFeature parse() throws IOException {
        final String fid;
        try {
            fid = seekFeature();
            if (fid == null) {
                return null;
            }
            int tagType;
            String tagNs;
            String tagName;
            Object attributeValue;
            while (true) {
                tagType = parser.next();
                if (XmlPullParser.END_DOCUMENT == tagType) {
                    close();
                    return null;
                }
                tagNs = parser.getNamespace();
                tagName = parser.getName();
                if (XmlPullParser.END_TAG == tagType && featureNamespace.equals(tagNs)
                        && featureName.equals(tagName)) {
                    // found end of current feature
                    break;
                }
                if (XmlPullParser.START_TAG == tagType) {
                    if (expectedProperties.contains(tagName)) {
                        attributeValue = parseAttributeValue();
                        builder.set(tagName, attributeValue);
                    }
                }
            }
        } catch (XmlPullParserException e) {
            throw new DataSourceException(e);
        }
        SimpleFeature feature = builder.buildFeature(fid);
        return feature;
    }

    /**
     * Parses the value of the current attribute, parser cursor shall be on a
     * feature attribute START_TAG event.
     * 
     * @return
     * @throws IOException
     * @throws XmlPullParserException
     * @throws FactoryException
     * @throws NoSuchAuthorityCodeException
     */
    @SuppressWarnings("unchecked")
    private Object parseAttributeValue() throws XmlPullParserException, IOException {
        final String name = parser.getName();
        final AttributeDescriptor attribute = this.targetType.getDescriptor(name);
        final AttributeType type = attribute.getType();
        Object parsedValue;
        if (type instanceof GeometryType) {
            parser.nextTag();
            try {
                parsedValue = parseGeom();
            } catch (NoSuchAuthorityCodeException e) {
                throw new DataSourceException(e);
            } catch (FactoryException e) {
                throw new DataSourceException(e);
            }
        } else {
            String rawTextValue = parser.nextText();
            Class binding = type.getBinding();
            parsedValue = Converters.convert(rawTextValue, binding);
        }
        return parsedValue;
    }

    /**
     * Prerequisite: parser cursor positioned on a geometry property (ej,
     * {@code gml:Point}, etc)
     * 
     * @return
     * @throws FactoryException
     * @throws NoSuchAuthorityCodeException
     * @throws IOException
     * @throws XmlPullParserException
     */
    private Geometry parseGeom() throws NoSuchAuthorityCodeException, FactoryException,
            XmlPullParserException, IOException {
        final QName tag = new QName(parser.getNamespace(), parser.getName());
        int dimension = crsDimension(2);
        CoordinateReferenceSystem crs = crs(DefaultGeographicCRS.WGS84);

        Geometry geom;
        if (GML.Point.equals(tag)) {
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, GML.NAMESPACE, GML.pos.getLocalPart());
            crs = crs(crs);
            Coordinate[] coords = parseCoordList(dimension);
            geom = geomFac.createPoint(coords[0]);
            geom.setUserData(crs);
        } else if (GML.LineString.equals(tag)) {
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, GML.NAMESPACE, GML.posList.getLocalPart());
            crs = crs(crs);
            Coordinate[] coords = parseCoordList(dimension);
            geom = geomFac.createLineString(coords);
            geom.setUserData(crs);
        } else if (GML.Polygon.equals(tag)) {
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, GML.NAMESPACE, GML.exterior.getLocalPart());
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, GML.NAMESPACE, GML.LinearRing.getLocalPart());
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, GML.NAMESPACE, GML.posList.getLocalPart());
            crs = crs(crs);
            Coordinate[] shellCoords = parseCoordList(dimension);
            LinearRing shell;
            LinearRing[] holes = null;
            shell = geomFac.createLinearRing(shellCoords);
            geom = geomFac.createPolygon(shell, holes);
            geom.setUserData(crs);
        } else if (GML.MultiPoint.equals(tag)) {
            throw new UnsupportedOperationException("MultiPoint parsing not yet implemented");
        } else if (GML.MultiLineString.equals(tag)) {
            throw new UnsupportedOperationException("MultiLineString parsing not yet implemented");
        } else if (GML.MultiPolygon.equals(tag)) {
            throw new UnsupportedOperationException("MultiPolygon parsing not yet implemented");
        } else {
            throw new IllegalStateException("Unrecognized geometry element " + tag);
        }
        return geom;
    }

    private CoordinateReferenceSystem crs(CoordinateReferenceSystem defaultValue)
            throws NoSuchAuthorityCodeException, FactoryException {
        String srsName = parser.getAttributeValue(null, "srsName");
        if (srsName == null) {
            return defaultValue;
        }
        CoordinateReferenceSystem crs = CRS.decode(srsName);
        return crs;
    }

    private int crsDimension(final int defaultValue) {
        String srsDimension = parser.getAttributeValue(null, "srsDimension");
        if (srsDimension == null) {
            return defaultValue;
        }
        int dimension = Integer.valueOf(srsDimension);
        return dimension;
    }

    private Coordinate[] parseCoordList(int dimension) throws XmlPullParserException, IOException {
        // we might be on a posList tag with srsDimension defined
        dimension = crsDimension(dimension);
        String rawTextValue = parser.nextText();
        Coordinate[] coords = toCoordList(rawTextValue, dimension);
        return coords;
    }

    private Coordinate[] toCoordList(String rawTextValue, final int dimension) {
        rawTextValue = rawTextValue.trim();
        rawTextValue = rawTextValue.replaceAll("\n", " ");
        rawTextValue = rawTextValue.replaceAll("\r", " ");
        String[] split = rawTextValue.trim().split(" +");
        final int ordinatesLength = split.length;
        if (ordinatesLength % dimension != 0) {
            throw new IllegalArgumentException("Number of ordinates (" + ordinatesLength
                    + ") does not match crs dimension: " + dimension);
        }
        final int nCoords = ordinatesLength / dimension;
        Coordinate[] coords = new Coordinate[nCoords];
        Coordinate coord;
        int currCoordIdx = 0;
        double x, y, z;
        for (int i = 0; i < ordinatesLength; i += dimension) {
            x = Double.valueOf(split[i]);
            y = Double.valueOf(split[i + 1]);
            if (dimension > 2) {
                z = Double.valueOf(split[i + 2]);
                coord = new Coordinate(x, y, z);
            } else {
                coord = new Coordinate(x, y);
            }
            coords[currCoordIdx] = coord;
            currCoordIdx++;
        }
        return coords;
    }

    private String seekFeature() throws IOException, XmlPullParserException {
        int tagType;

        while (true) {
            tagType = parser.next();
            if (tagType == XmlPullParser.END_DOCUMENT) {
                close();
                return null;
            }
            if (XmlPullParser.START_TAG != tagType) {
                continue;
            }
            if (XmlPullParser.START_TAG == tagType) {
                String namespace = parser.getNamespace();
                String name = parser.getName();
                if (featureNamespace.equals(namespace) && featureName.equals(name)) {
                    String featureId = parser.getAttributeValue(GML.id.getNamespaceURI(), GML.id
                            .getLocalPart());
                    return featureId;
                }
            }
        }
    }

}

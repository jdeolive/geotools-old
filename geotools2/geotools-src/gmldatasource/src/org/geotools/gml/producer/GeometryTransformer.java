/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
/*
 * GeometryTransformer.java
 *
 * Created on October 24, 2003, 1:08 PM
 */
package org.geotools.gml.producer;

import com.vividsolutions.jts.geom.*;
import org.geotools.xml.transform.TransformerBase;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * DOCUMENT ME!
 *
 * @author Ian Schneider
 */
public class GeometryTransformer extends TransformerBase {
    public org.geotools.xml.transform.Translator createTranslator(
        ContentHandler handler) {
        return new GeometryTranslator(handler);
    }

    public static class GeometryTranslator extends TranslatorSupport {
        CoordinateWriter coordWriter = new CoordinateWriter();

        public GeometryTranslator(ContentHandler handler) {
            super(handler, "gml", GMLUtils.GML_URL);
        }

        public GeometryTranslator(ContentHandler handler, int numDecimals) {
            this(handler);
            coordWriter = new CoordinateWriter(numDecimals);
        }

        public void encode(Object o, String srsName)
            throws IllegalArgumentException {
            if (o instanceof Geometry) {
                encode((Geometry) o, srsName);
            } else {
                throw new IllegalArgumentException("Unable to encode " + o);
            }
        }

        public void encode(Object o) throws IllegalArgumentException {
            encode(o, null);
        }

        public void encode(Envelope bounds) {
            encode(bounds, null);
        }

        public void encode(Envelope bounds, String srsName) {
            String boxName = "Box";

            if ((srsName == null) || srsName.equals("")) {
                start(boxName);
            } else {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "srsName", "srsName", "", srsName);
                start(boxName, atts);
            }

            try {
                Coordinate[] coords = new Coordinate[2];
                coords[0] = new Coordinate(bounds.getMinX(), bounds.getMinY());
                //coords[1] = new Coordinate(bounds.getMinX(), bounds.getMaxY());
                coords[1] = new Coordinate(bounds.getMaxX(), bounds.getMaxY());
                //coords[3] = new Coordinate(bounds.getMaxX(), bounds.getMinY());
                coordWriter.writeCoordinates(coords, contentHandler);
            } catch (SAXException se) {
                throw new RuntimeException(se);
            }

            end(boxName);
        }

        public void encode(Geometry geometry) {
            encode(geometry, null);
        }

        public void encode(Geometry geometry, String srsName) {
            String geomName = GMLUtils.getGeometryName(geometry);

            if ((srsName == null) || srsName.equals("")) {
                start(geomName);
            } else {
                AttributesImpl atts = new AttributesImpl();
                atts.addAttribute("", "srsName", "srsName", "", srsName);
                start(geomName, atts);
            }

            int geometryType = GMLUtils.getGeometryType(geometry);

            switch (geometryType) {
            case GMLUtils.POINT:
            case GMLUtils.LINESTRING:

                try {
                    coordWriter.writeCoordinates(geometry.getCoordinates(),
                        contentHandler);
                } catch (SAXException s) {
                    throw new RuntimeException(s);
                }

                break;

            case GMLUtils.POLYGON:
                writePolygon((Polygon) geometry);

                break;

            case GMLUtils.MULTIPOINT:
            case GMLUtils.MULTILINESTRING:
            case GMLUtils.MULTIPOLYGON:
            case GMLUtils.MULTIGEOMETRY:
                writeMulti((GeometryCollection) geometry,
                    GMLUtils.getMemberName(geometryType));

                break;
            }

            end(geomName);
        }

        private void writePolygon(Polygon geometry) {
            String outBound = "outerBoundaryIs";
            String lineRing = "LinearRing";
            String inBound = "innerBoundaryIs";
            start(outBound);
            start(lineRing);

            try {
                coordWriter.writeCoordinates(geometry.getExteriorRing()
                                                     .getCoordinates(),
                    contentHandler);
            } catch (SAXException s) {
                throw new RuntimeException(s);
            }

            end(lineRing);
            end(outBound);

            for (int i = 0, ii = geometry.getNumInteriorRing(); i < ii; i++) {
                start(inBound);
                start(lineRing);

                try {
                    coordWriter.writeCoordinates(geometry.getInteriorRingN(i)
                                                         .getCoordinates(),
                        contentHandler);
                } catch (SAXException s) {
                    throw new RuntimeException(s);
                }

                end(lineRing);
                end(inBound);
            }
        }

        private void writeMulti(GeometryCollection geometry, String member) {
            for (int i = 0, n = geometry.getNumGeometries(); i < n; i++) {
                start(member);

                encode(geometry.getGeometryN(i));

                end(member);
            }
        }
    }
}

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
package org.geotools.wms.gtserver;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalFeatureException;
import java.io.OutputStream;
import java.io.PrintWriter;


/**
 * DOCUMENT ME!
 *
 * @author jamesm
 */
public class TextFeatureFormatter
    implements org.geotools.wms.WMSFeatureFormatter {
    /**
     * Creates a new instance of TextFeatureFormatter
     */
    public TextFeatureFormatter() {
    }

    /**
     * Formats the given array of Features as this Formatter's mime-type and
     * writes it to the given OutputStream
     *
     * @param features DOCUMENT ME!
     * @param out DOCUMENT ME!
     *
     * @task HACK: Exception handleling is not elegant
     */
    public void formatFeatures(Feature[] features, OutputStream out) {
        PrintWriter writer = new PrintWriter(out);

        try {
            for (int i = 0; i < features.length; i++) {
                FeatureType schema = features[i].getSchema();
                AttributeType[] types = schema.getAllAttributeTypes();
                writer.println("------");

                for (int j = 0; j < types.length; j++) {
                    if (Geometry.class.isAssignableFrom(types[j].getType())) {
                        writer.println(types[j].getName() + " = [GEOMETRY]");
                    } else {
                        writer.println(types[j].getName() + " = " +
                            features[i].getAttribute(types[j].getName()));
                    }
                }
            }
        } catch (IllegalFeatureException ife) {
            writer.println("Unable to generate information " + ife);
        }

        writer.flush();
    }

    /**
     * Gets the mime-type of the stream written to by formatFeatures()
     *
     * @return DOCUMENT ME!
     */
    public String getMimeType() {
        return ("text/plain");
    }
}

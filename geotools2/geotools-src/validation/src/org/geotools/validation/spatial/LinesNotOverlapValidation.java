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
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.spatial;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.validation.ValidationResults;
import java.util.Map;


/**
 * LinesNotOverlapValidation purpose.
 * 
 * <p>
 * Ensures Lines do not overlap.
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: LinesNotOverlapValidation.java,v 1.2 2004/02/17 17:19:13 dmzwiers Exp $
 */
public class LinesNotOverlapValidation extends LineLineAbstractValidation {
    /**
     * LinesNotOverlapValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public LinesNotOverlapValidation() {
        super();

        // TODO Auto-generated constructor stub
    }

    /**
     * Ensure Lines do not overlap.
     *
     * @param layers a HashMap of key="TypeName" value="FeatureSource"
     * @param envelope The bounding box of modified features
     * @param results Storage for the error and warning messages
     *
     * @return True if no features intersect. If they do then the validation
     *         failed.
     *
     * @throws Exception DOCUMENT ME!
     *
     * @see org.geotools.validation.IntegrityValidation#validate(java.util.Map,
     *      com.vividsolutions.jts.geom.Envelope,
     *      org.geotools.validation.ValidationResults)
     */
    public boolean validate(Map layers, Envelope envelope,
        ValidationResults results) throws Exception {
        FeatureSource lineSource1 = (FeatureSource) layers.get(getLineTypeRef());
        FeatureSource lineSource2 = (FeatureSource) layers.get(getRestrictedLineTypeRef());

        Object[] lines1 = lineSource1.getFeatures().collection().toArray();
        Object[] lines2 = lineSource2.getFeatures().collection().toArray();

        if (!envelope.contains(lineSource1.getBounds())) {
            results.error((Feature) lines1[0],
                "Point Feature Source is not contained within the Envelope provided.");

            return false;
        }

        if (!envelope.contains(lineSource2.getBounds())) {
            results.error((Feature) lines2[0],
                "Line Feature Source is not contained within the Envelope provided.");

            return false;
        }

        boolean r = true;

        for (int i = 0; i < lines2.length; i++) {
            Feature tmp = (Feature) lines2[i];
            Geometry gt = tmp.getDefaultGeometry();

            for (int j = 0; j < lines1.length; j++) {
                Feature tmp2 = (Feature) lines1[j];
                Geometry gt2 = tmp2.getDefaultGeometry();

                if (gt.overlaps(gt2)) {
                    results.error(tmp,
                        "Overlaps with another line specified. Id="
                        + tmp2.getID());
                    r = false;
                }
            }
        }

        return r;
    }
}

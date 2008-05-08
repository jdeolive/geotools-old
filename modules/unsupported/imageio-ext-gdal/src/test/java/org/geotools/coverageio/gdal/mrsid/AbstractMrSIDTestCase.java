/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, GeoTools Project Management Committee (PMC)
 *    (C) 2007, GeoSolutions S.A.S.
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
package org.geotools.coverageio.gdal.mrsid;

import java.util.logging.Logger;

import org.geotools.coverageio.gdal.AbstractGDALBasedTestCase;


/**
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini (simboss), GeoSolutions
 *
 * Base MrSID testing class.
 */
public abstract class AbstractMrSIDTestCase extends AbstractGDALBasedTestCase {
    protected final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(
            "org.geotools.gce.mrsid");

    public AbstractMrSIDTestCase(String name) {
        super(name, "MrSID", new MrSIDFormatFactory());
    }
}

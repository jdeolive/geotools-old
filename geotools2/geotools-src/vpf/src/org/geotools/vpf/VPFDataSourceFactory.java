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

package org.geotools.vpf;

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSourceFactorySpi;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;


/**
 * Class VPFDataSourceFactory.java is responsible for
 * 
 * <p>
 * Created: Fri Mar 28 15:54:32 2003
 * </p>
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 */
public class VPFDataSourceFactory implements DataSourceFactorySpi {
    /**
     * Creates a new VPFDataSourceFactory object.
     */
    public VPFDataSourceFactory() {}

    // Implementation of org.geotools.data.DataSourceFactorySpi

    /**
     * Describe <code><code>createDataSource</code></code> method here.
     *
     * @param hashMap a <code><code>HashMap</code></code> value
     *
     * @return a <code><code>DataSource</code></code> value
     *
     * @exception DataSourceException if an error occurs
     */
    public DataSource createDataSource(HashMap hashMap)
        throws DataSourceException {
        if (!canProcess(hashMap)) {
            return null;
        }

        try {
            File file = new File(new URI((String) hashMap.get("url")));

            try {
                return new VPFDataSource(file);
            } catch (IOException e) {
                throw
                    new DataSourceException("Unable to open VPF data base "+
                                            file, e);
            }
        } catch (URISyntaxException e) {
            return null;
        }

        // end of try-catch
    }

    /**
     * Describe <code><code>getDescription</code></code> method here.
     *
     * @return a <code><code>String</code></code> value
     */
    public String getDescription() {
        return "Vector Product Format data source implementation.";
    }

    /**
     * Describe <code><code>canProcess</code></code> method here.
     *
     * @param hashMap a <code><code>HashMap</code></code> value
     *
     * @return a <code><code>boolean</code></code> value
     */
    public boolean canProcess(HashMap hashMap) {
        if (!hashMap.containsKey("url")) {
            return false;
        }

        String url = (String) hashMap.get("url");

        try {
            File file = new File(new File(new URI(url)), "dht");

            if (file.exists() && !file.isDirectory()) {
                return true;
            } else {
                return false;
            }

            // end of else
        } catch (URISyntaxException e) {
            return false;
        }

        // end of try-catch
    }
}


// VPFDataSourceFactory

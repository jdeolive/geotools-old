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
package org.geotools.data;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import java.io.IOException;
import java.util.NoSuchElementException;


/**
 * Basic support for a FeatureReader that does filtering.  I think that
 * filtering should perhaps be done in the AttributeReader.  I'm still having
 * a bit of trouble with the split between attributeReader and featureReader
 * as to where the hooks for advanced processing like filtering should take
 * place.  See my note on hasNext(), as the method is currently broken and
 * there are more optimizations that could take place if we had a
 * FilteringAttributeReader.  So this class may go, but I thought I'd put the
 * ideas into code.
 * 
 * <p>
 * Jody here - changed hasNext() to peek as required.
 * </p>
 *
 * @author Chris Holmes
 * @version $Id: FilteringFeatureReader.java,v 1.2 2003/11/04 00:28:49 cholmesny Exp $
 */
public class FilteringFeatureReader implements FeatureReader {
    protected final FeatureReader featureReader;
    protected final Filter filter;
    protected Feature next;

    /**
     * Creates a new instance of AbstractFeatureReader
     * 
     * <p>
     * Please don't call this method with Filter.NONE or Filter.ALL (consider
     * not filtering and EmptyFeatureReader instead)
     * </p>
     *
     * @param featureReader FeatureReader being filtered
     * @param filter Filter used to limit the results of featureReader
     */
    public FilteringFeatureReader(FeatureReader featureReader, Filter filter) {
        this.featureReader = featureReader;
        this.filter = filter;
        next = null;
    }

    public Feature next()
        throws IOException, IllegalAttributeException, NoSuchElementException {
        Feature f = null;

        if (hasNext()) {
            // hasNext() ensures that next != null
            f = next;
            next = null;

            return f;
        } else {
            throw new NoSuchElementException("No such Feature exsists");
        }
    }

    public void close() throws IOException {
        featureReader.close();
    }

    public FeatureType getFeatureType() {
        return featureReader.getFeatureType();
    }

    /**
     * Query for additional content.
     * 
     * <p>
     * This class will peek ahead to see if there is additional content.
     * </p>
     * 
     * <p>
     * Chris has pointed out that we could make use of AttributeReader based filtering:<br>
     * <i>"Also doing things in the Attribute Reader would allow us to do the
     * smart filtering, only looking at the attributes needed for comparison,
     * whereas doing filtering here means we have to create an entire feature
     * each time."</i>
     * </p>
     *
     * @return <code>true</code> if we have additional content
     *
     * @throws IOException If the reader we are filtering encounters a problem
     * @throws DataSourceException See IOException
     */
    public boolean hasNext() throws IOException {
        if (next != null) {
            return true;
        }
        try {
            Feature peek;

            while (featureReader.hasNext()) {
                peek = featureReader.next();

                if (filter.contains(peek)) {
                    next = peek;
                    return true;
                }                                
            }
        } catch (IllegalAttributeException e) {
            throw new DataSourceException("Could not peek ahead", e);
        }
        return next != null;
    }
}

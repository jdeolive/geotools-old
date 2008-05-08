package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;

/**
 * Adapts a {@link GetFeatureParser} to the geotools {@link FeatureReader}
 * interface, being the base for all the data content related implementations in
 * the WFS module.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5.x
 * @source $URL$
 * @see WFS110ProtocolHandler#getFeatureReader(org.geotools.data.Query,
 *      org.geotools.data.Transaction)
 */
class WFSFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    private SimpleFeature next;

    private GetFeatureParser parser;

    private SimpleFeatureType featureType;

    public WFSFeatureReader(final GetFeatureParser parser) throws IOException {
        this.parser = parser;
        this.next = parser.parse();
        if (this.next != null) {
            //this is the FeatureType as parsed by the StreamingParser, we need a simple view
            FeatureType parsedType = next.getFeatureType();
            this.featureType = EmfAppSchemaParser.toSimpleFeatureType(parsedType);
        }
    }

    /**
     * @see FeatureReader#close()
     */
    public void close() throws IOException {
        final GetFeatureParser parser = this.parser;
        this.parser = null;
        this.next = null;
        if (parser != null) {
            parser.close();
        }
    }

    /**
     * @see FeatureReader#getFeatureType()
     */
    public SimpleFeatureType getFeatureType() {
        if (featureType == null) {
            throw new IllegalStateException(
                    "No features were retrieved, shouldn't be calling getFeatureType()");
        }
        return featureType;
    }

    /**
     * @see FeatureReader#hasNext()
     */
    public boolean hasNext() throws IOException {
        return next != null;
    }

    /**
     * @see FeatureReader#next()
     */
    public SimpleFeature next() throws IOException, NoSuchElementException {
        if (this.next == null) {
            throw new NoSuchElementException();
        }
        SimpleFeature current = this.next;
        this.next = parser.parse();
        return current;
    }

}

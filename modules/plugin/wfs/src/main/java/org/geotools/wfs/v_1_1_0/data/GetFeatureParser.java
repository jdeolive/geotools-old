package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;

import org.opengis.feature.simple.SimpleFeature;

/**
 * Interface to abstract out the parsing of features coming from a GetFeature
 * request from the underlying xml parsing technology in use.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5.x
 * @source $URL$
 * @see WFSFeatureReader
 */
interface GetFeatureParser {

    /**
     * @return the next feature in the stream or {@code null} if there are no
     *         more features to parse.
     */
    SimpleFeature parse() throws IOException;

    void close() throws IOException;
}

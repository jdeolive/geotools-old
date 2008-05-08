package org.geotools.wfs.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Interface to abstract out the plain connection and stream set up against the
 * target WFS
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5.x
 * @source $URL$
 */
public interface ConnectionFactory {

    String getAuthUsername();

    String getAuthPassword();

    boolean isTryGzip();

    /**
     * Returns the preferred character encoding name to encode requests in
     * 
     * @return
     */
    Charset getEncoding();

    /**
     * Creates and returns a connection object for the supplied URL, settled up
     * for the given HTTP method (GET or POST) and this connection factory
     * tryGzip and authentication settings.
     * 
     * @param query
     * @param method
     * @return
     * @throws IOException
     */
    HttpURLConnection getConnection(URL query, HttpMethod method) throws IOException;

    InputStream getInputStream(HttpURLConnection hc) throws IOException;

    /**
     * Shortcut for
     * {@code conn = getConnection(url, method); getInputStream(conn);}
     * 
     * @param query
     * @param method
     * @return
     * @throws IOException
     */
    InputStream getInputStream(URL query, HttpMethod method) throws IOException;
}
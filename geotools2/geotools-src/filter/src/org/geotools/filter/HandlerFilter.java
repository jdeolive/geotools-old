/*
 * This code is released under the Apache license, availible at the root GML4j directory.
 */
package org.geotools.filter;

import java.util.*;
import java.math.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;


/**
 * Creates an OGC filter using a SAX filter.
 *
 * <p>Possibly the worst-named class of all time, <code>FilterFilter</code>
 * extracts an OGC filter object from an XML stream and passes it to its parent
 * as a fully instantiated OGC filter object.</p>
 * 
 * @author Rob Hranac, Vision for New York
 * @version alpha, 12/01/01
 */
public interface HandlerFilter {

    /**
     * Constructor with parent, which must implement GMLHandlerJTS.
     *
     * @param filter The parent of this filter.
     */
    public void filter(Filter filter);

}

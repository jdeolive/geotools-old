package org.geotools.feature.visitor;

import org.geotools.filter.FidFilter;
import org.geotools.filter.visitor.AbstractFinderFilterVisitor;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.geotools.filter.visitor.NullFilterVisitor;
import org.opengis.filter.Id;

/**
 * Quick check to see if an ID filter is found.
 * <p>
 * Example:<code>found = (Boolean) filter.accept( new IdFinderFilter(), null )</code>
 */
public class IdFinderFilterVisitor extends AbstractFinderFilterVisitor {

    public Object visit( Id filter, Object data ) {
        found = true;
        return found;
    }
        
}

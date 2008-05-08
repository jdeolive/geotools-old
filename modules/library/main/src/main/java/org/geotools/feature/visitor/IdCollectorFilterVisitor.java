package org.geotools.feature.visitor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.geotools.filter.FidFilter;
import org.geotools.filter.visitor.DefaultFilterVisitor;
import org.opengis.filter.Id;

/**
 * Gather up all FeatureId strings into a provided HashSet.
 * <p>
 * Example:<code>Set<String> fids = (Set<String>) filter.accept( IdCollectorFilterVisitor.ID_COLLECTOR, new HashSet() );</code>
 */
public class IdCollectorFilterVisitor extends DefaultFilterVisitor {
    public static final IdCollectorFilterVisitor ID_COLLECTOR = new IdCollectorFilterVisitor();
    
    protected IdCollectorFilterVisitor(){        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object visit( Id filter, Object data ) {
        Set set = (Set) data;
        set.addAll( filter.getIDs() );        
        return set;
    }
}

/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Management Committee (PMC)
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
 */

package org.geotools.filter.text.txt;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.cql2.CQLFilterBuilder;
import org.geotools.filter.text.cql2.IToken;
import org.geotools.filter.text.cql2.Result;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.Id;
import org.opengis.filter.identity.FeatureId;

/**
 * Builds the filters required by the {@link TXTCompiler}.
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5
 */
final class TXTFilterBuilder extends CQLFilterBuilder {

    public TXTFilterBuilder(String cqlSource, FilterFactory filterFactory) {
        super(cqlSource, filterFactory);
    }

    /**
     * build the filter id
     * 
     * @param token  #<character>
     * @return String without the #
     */
    public FeatureId buildFeatureID(IToken token) {
       
        // remove the #
        String txtId = token.toString();
        txtId = txtId.trim();
        txtId = txtId.substring(1);
        
        FeatureId id = getFilterFactory().featureId( txtId);
        return id;
    }

    /**
     * builds the filter id
     * @param jjtfeature_id_separator_node 
     * @return Id
     * @throws CQLException
     */
    public Id buildFilterId(final int nodeFeatureId) throws CQLException {
        
        //retrieves the id from stack
        List<FeatureId> idList = new LinkedList<FeatureId>();
        while (!getResultStack().empty()) {

            Result result = getResultStack().peek();

            int node = result.getNodeType();
            if (node != nodeFeatureId) {
                break;
            }
            FeatureId id = (FeatureId) result.getBuilt();
            idList.add(id);
            getResultStack().popResult();
        }
        assert idList.size() >= 1: "must have one or more FeatureIds";
        
        // shorts the id list and builds the filter Id
        Collections.reverse(idList);
        Set<FeatureId> idSet = new LinkedHashSet<FeatureId>(idList); 
        Id filter = getFilterFactory().id(idSet);

        return filter;
    }

}

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
import org.opengis.filter.expression.Literal;
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
     * builds the filter id
     * 
     * @param token  <character>
     * @return String without the quotes
     */
    public FeatureId buildFeatureID(IToken token) {
       
        String strId = removeQuotes(token.toString());

        FeatureId id = getFilterFactory().featureId( strId);
        
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

    /**
     * Builds a negative Number
     * @return Negative number
     * @throws CQLException
     */
    public Literal bulidNegativeNumber() throws CQLException {
        
        // retrieves the number value from stack and adds the (-) minus
        Literal literal = getResultStack().popLiteral();
        String strNumber = "-" + literal.getValue();
        Object value = literal.getValue();
        
        //builds the negative number
        @SuppressWarnings("unused")
        Number number = null;
        if(value instanceof Double){
            number = Double.parseDouble(strNumber);
        }else if (value instanceof Float){
            number = Float.parseFloat(strNumber);
        }else if(value instanceof Integer) {
            number = Integer.parseInt(strNumber);
        }else if(value instanceof Long) {
            number = Long.parseLong(strNumber);
        }else{
            assert false: "Number instnce is expected";
        }
        Literal signedNumber = getFilterFactory().literal(strNumber);
        
        return signedNumber;
    }

}

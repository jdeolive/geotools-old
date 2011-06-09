package org.geotools.filter.visitor;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.Join;
import org.opengis.filter.And;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.temporal.BinaryTemporalOperator;

public class JoinExtractingVisitor extends FilterVisitorSupport {

    List<Filter> filters = new ArrayList<Filter>();
    List<Filter> joins = new ArrayList<Filter>();

    public Object visitNullFilter(Object extraData) {
        return null;
    }

    public Object visit(ExcludeFilter filter, Object extraData) {
        handle(filter);
        return extraData;
    }

    public Object visit(IncludeFilter filter, Object extraData) {
        handle(filter);
        return extraData;
    }

    public Object visit(Id filter, Object extraData) {
        handle(filter);
        return extraData;
    }

    public Object visit(Not filter, Object extraData) {
        handle(filter);
        return extraData;
    }

    @Override
    protected Object visit(BinaryLogicOperator op, Object extraData) {
        handle(op);
        return extraData;
    }

    @Override
    protected Object visit(BinaryComparisonOperator op, Object extraData) {
        handle(op);
        return extraData;
    }

    @Override
    protected Object visit(BinarySpatialOperator op, Object extraData) {
        handle(op);
        return extraData;
    }

    @Override
    protected Object visit(BinaryTemporalOperator op, Object extraData) {
        handle(op);
        return extraData;
    }

    void handle(Filter f) {
        if (f instanceof And) {
            for (Filter g : ((And)f).getChildren()) {
                handle(g);
            }
        }
        else {
            //check if this is a join filter
            boolean join = false;
            if (f instanceof BinaryComparisonOperator) {
                join = isJoinFilter(((BinaryComparisonOperator)f).getExpression1(), 
                    ((BinaryComparisonOperator)f).getExpression2());
            }
            else if (f instanceof BinarySpatialOperator) {
                join = isJoinFilter(((BinarySpatialOperator)f).getExpression1(), 
                        ((BinarySpatialOperator)f).getExpression2());
            }
            else if (f instanceof BinaryTemporalOperator) {
                join = isJoinFilter(((BinaryTemporalOperator)f).getExpression1(), 
                        ((BinaryTemporalOperator)f).getExpression2());
            }
            
            if (join) {
                joins.add(f);
            }
            else {
                filters.add(f);
            }
        }
    }
    
    boolean isJoinFilter(Expression e1, Expression e2) {
        return e1 instanceof PropertyName && e2 instanceof PropertyName;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public List<Filter> getJoins() {
        return joins;
    }
}

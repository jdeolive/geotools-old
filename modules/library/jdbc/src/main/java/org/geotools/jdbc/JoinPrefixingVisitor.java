package org.geotools.jdbc;

import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.Intersects;


public class JoinPrefixingVisitor extends DuplicatingFilterVisitor {

    String p1;
    String p2;
    
    public JoinPrefixingVisitor(String prefix1) {
        this(prefix1, prefix1);
    }
    
    public JoinPrefixingVisitor(String prefix1, String prefix2) {
        this.p1 = prefix1;
        this.p2 = prefix2;
    }

    @Override
    public Object visit(PropertyIsEqualTo filter, Object extraData) {
        return ff.equal(prefix(filter.getExpression1(), p1), prefix(filter.getExpression2(), p2),
            filter.isMatchingCase());
    }

    @Override
    public Object visit(Intersects filter, Object extraData) {
        return ff.intersects(prefix(filter.getExpression1(), p1), prefix(filter.getExpression2(), p2));
    }
    
    Expression prefix(Expression e, String prefix) {
        if (e instanceof PropertyName) {
            return ff.property(prefix + "." + ((PropertyName)e).getPropertyName());
        }
        return e;
    }
}

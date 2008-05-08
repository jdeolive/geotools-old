package org.geotools.filter.pojo;

import java.util.Iterator;

import org.opengis.filter.And;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.Not;
import org.opengis.filter.Or;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.PropertyIsGreaterThan;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNotEqualTo;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.NilExpression;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.expression.Subtract;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

public class JDOQLEncoder implements FilterVisitor, ExpressionVisitor {
	
	private int srid;
	
	public JDOQLEncoder(int srid) {
		this.srid = srid;
	}

	public Object visit(ExcludeFilter arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(IncludeFilter arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * a&&b&&c
	 */
	public Object visit(And and, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		Iterator it = and.getChildren().iterator();
		while ( it.hasNext() ) {
			((Filter) it.next()).accept( this, buf );
			if ( it.hasNext() ) {
				buf.append( "&&" );
			}
		}
		return buf;
	}

	public Object visit(Id arg0, Object arg1) {
		
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * !a
	 */
	public Object visit(Not not, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		buf.append( "!" );
		not.accept(this, buf);
		return buf;
	}

	/**
	 * a||b||c
	 */
	public Object visit(Or or, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		Iterator it = or.getChildren().iterator();
		while ( it.hasNext() ) {
			((Filter) it.next()).accept( this, buf );
			if ( it.hasNext() ) {
				buf.append( "||" );
			}
		}
		return buf;
	}

	/**
	 * a>=b&&a<=c
	 */
	public Object visit(PropertyIsBetween between, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		StringBuffer bufExpr = new StringBuffer();
		between.getExpression().accept( this, bufExpr );
		buf.append( bufExpr ).append( ">=" );		
		between.getLowerBoundary().accept( this, buf );
		buf.append( "&&").append( bufExpr ).append( "<=" );		
		between.getUpperBoundary().accept( this, buf );
		return buf;
	}

	/**
	 * a==b
	 */
	public Object visit(PropertyIsEqualTo eq, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		eq.getExpression1().accept( this, buf );
		buf.append( "==" );
		eq.getExpression2().accept( this, buf );
		return buf;
	}

	/**
	 * a!=b
	 */
	public Object visit(PropertyIsNotEqualTo neq, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		neq.getExpression1().accept( this, buf );
		buf.append("!=");
		neq.getExpression2().accept( this, buf );
		return buf;
	}

	/**
	 * a>b
	 */
	public Object visit(PropertyIsGreaterThan gt, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		gt.getExpression1().accept( this, buf );
		buf.append(">");
		gt.getExpression2().accept( this, buf );
		return buf;
	}

	/**
	 * a>=b
	 */
	public Object visit(PropertyIsGreaterThanOrEqualTo gte, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		gte.getExpression1().accept( this, buf );
		buf.append(">=");
		gte.getExpression2().accept( this, buf );
		return buf;
	}

	/**
	 * a<b
	 */
	public Object visit(PropertyIsLessThan lt, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		lt.getExpression1().accept( this, buf );
		buf.append("<");
		lt.getExpression2().accept( this, buf );
		return buf;
	}

	/**
	 * a<=b
	 */
	public Object visit(PropertyIsLessThanOrEqualTo lte, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		lte.getExpression1().accept( this, buf );
		buf.append("<=");
		lte.getExpression2().accept( this, buf );
		return buf;
	}

	/**
	 * a.like('b')
	 */
	public Object visit(PropertyIsLike like, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		like.getExpression().accept( this, buf );
		buf.append( ".like('" ).append( like.getLiteral() ).append( "')" );
		return buf;
	}

	/**
	 * a==null
	 */
	public Object visit(PropertyIsNull isnull, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		isnull.getExpression().accept( this, buf );
		buf.append( "==null" );
		return buf;
	}

	/**
	 * Spatial.bboxIntersects(a, Spatial.geomFromText('POLYGON((minx miny, maxx miny, maxx, maxy, minx maxy, minx miny))', srid))"
	 */
	public Object visit(BBOX bbox, Object obj) {
		// TODO check if bbox uses same coordinate reference system
		StringBuffer buf = (StringBuffer) obj;
		buf.append( "Spatial.bboxIntersects(" ).append( bbox.getPropertyName() ).append( ", " );
		buf.append( "Spatial.geomFromText('POLYGON((" );
		buf.append( bbox.getMinX() ).append(" ").append( bbox.getMinY() ).append(", ");
		buf.append( bbox.getMaxX() ).append(" ").append( bbox.getMinY() ).append(", ");
		buf.append( bbox.getMaxX() ).append(" ").append( bbox.getMaxY() ).append(", ");
		buf.append( bbox.getMinX() ).append(" ").append( bbox.getMaxY() ).append(", ");
		buf.append( bbox.getMinX() ).append(" ").append( bbox.getMinY() ).append("))', ").append(srid).append("))");
		return buf;
	}

	public Object visit(Beyond beyond, Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Spatial.contains(a, b)
	 */
	public Object visit(Contains contains, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		buf.append( "Spatial.contains(" );
		contains.getExpression1().accept( this, buf );
		buf.append( ", " );
		contains.getExpression2().accept( this, buf );
		buf.append( ')' );
		return buf;
	}

	/**
	 * Spatial.crosses(a, b)
	 */
	public Object visit(Crosses crosses, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		buf.append( "Spatial.crosses(" );
		crosses.getExpression1().accept( this, buf );
		buf.append( ", " );
		crosses.getExpression2().accept( this, buf );
		buf.append( ')' );
		return buf;
	}

	/**
	 * Spatial.disjoint(a, b)
	 */
	public Object visit(Disjoint disjoint, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		buf.append( "Spatial.disjoint(" );
		disjoint.getExpression1().accept( this, buf );
		buf.append( ", " );
		disjoint.getExpression2().accept( this, buf );
		buf.append( ')' );
		return buf;
	}

	public Object visit(DWithin dwithin, Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Spatial.equals(a, b)
	 */
	public Object visit(Equals eq, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		buf.append( "Spatial.equals(" );
		eq.getExpression1().accept( this, buf );
		buf.append( ", " );
		eq.getExpression2().accept( this, buf );
		buf.append( ')' );
		return buf;
	}

	/**
	 * Spatial.intersects(a, b)
	 */
	public Object visit(Intersects intersects, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		buf.append( "Spatial.intersects(" );
		intersects.getExpression1().accept( this, buf );
		buf.append( ", " );
		intersects.getExpression2().accept( this, buf );
		buf.append( ')' );
		return buf;
	}

	/**
	 * Spatial.overlaps(a, b)
	 */
	public Object visit(Overlaps overlaps, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		buf.append( "Spatial.overlaps(" );
		overlaps.getExpression1().accept( this, buf );
		buf.append( ", " );
		overlaps.getExpression2().accept( this, buf );
		buf.append( ')' );
		return buf;
	}

	/**
	 * Spatial.touches(a, b)
	 */
	public Object visit(Touches touches, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		buf.append( "Spatial.touches(" );
		touches.getExpression1().accept( this, buf );
		buf.append( ", " );
		touches.getExpression2().accept( this, buf );
		buf.append( ')' );
		return buf;
	}

	/**
	 * Spatial.within(a, b)
	 */
	public Object visit(Within within, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		buf.append( "Spatial.within(" );
		within.getExpression1().accept( this, buf );
		buf.append( ", " );
		within.getExpression2().accept( this, buf );
		buf.append( ')' );
		return buf;
	}

	public Object visitNullFilter(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(NilExpression arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(Add arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(Divide arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(Function arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(Literal literal, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		Object value = literal.getValue();
		if ( value instanceof String ) {
			buf.append( '"' ).append( value ).append( '"' );
		} else {
			buf.append( literal.getValue() );
		}
		return buf;
	}

	public Object visit(Multiply arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object visit(PropertyName property, Object obj) {
		StringBuffer buf = (StringBuffer) obj;
		buf.append( property.getPropertyName() );
		return buf;
	}

	public Object visit(Subtract arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}

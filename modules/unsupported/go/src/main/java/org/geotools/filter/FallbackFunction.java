package org.geotools.filter;

import java.util.Collections;
import java.util.List;

import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;

public class FallbackFunction implements Function {
	private List<Expression> parameters;
	private Literal fallback;
	private String name;
	
	public FallbackFunction( String name, List<Expression> parameters, Literal fallback ){
		this.name = name;
		this.parameters = parameters;
		this.fallback = fallback;
	}
	public String getName() {
		return name;
	}

	public List<Expression> getParameters() {
		return Collections.unmodifiableList( parameters );
	}

	public Object accept(ExpressionVisitor visitor, Object extraData) {
		return visitor.visit( this, extraData );
	}

	public Object evaluate(Object object) {
		return fallback.evaluate(object);
	}

	public <T> T evaluate(Object object, Class<T> context) {
		return fallback.evaluate(object,context);
	}	
	public Literal getFallbackValue() {
		return fallback;
	}

}

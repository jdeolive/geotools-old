package org.geotools.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.ExpressionVisitor;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.Filter;
/**
 * Implementation of "Categorize" as a normal function.
 * <p>
 * This implementation is compatible with the Function
 * interface; the parameter list can be used to set the
 * threshold values etc...
 * <p>
 * This function expects:
 * <ol>
 * <li>PropertyName; use "Rasterdata" to indicate this is a colour map
 * <li>Literal: lower value
 * <li>Literal: threshold 1
 * <li>Literal: value 1
 * <li>Literal: threshold 2
 * <li>Literal: value 2
 * <li>Literal: (Optional) succeeding or preceding
 * </ol>
 * In reality any expression will do.
 * @author Jody
 */
public class CategorizeFunction implements Function {	
	/** Use as a literal value to indicate behaviour of threshold boundary */
	public String SUCCEEDING = "succeeding";
	/** Use as a literal value to indicate behaviour of threshold boundary */	
	public String PRECEDING = "preceding";
	/** Use as a PropertyName when defining a color map */
	public String RASTER_DATA = "Rasterdata";
	
	private List<Expression> parameters;
	private Literal fallback;
	
	public static FunctionName NAME = new FunctionName(){
		public int getArgumentCount() {
			return -2; // indicating unbounded, 2 minimum
		}
		public List<String> getArgumentNames() {
			return Arrays.asList( new String[]{
					"LookupValue",
					"Value",
					"Threshold 1", "Value 1",
					"Threshold 2", "Value 2",
					"succeeding or preceding"
			});
		}
		public String getName() {
			return "Categorize";
		}		
	};
	
	public CategorizeFunction( List<Expression> parameters, Literal fallback ){
		this.parameters = parameters;
		this.fallback = fallback;
	}
	
	public String getName() {
		return "Categorize";
	}
	
	public List<Expression> getParameters() {
		return Collections.unmodifiableList( parameters );
	}

	public Object accept(ExpressionVisitor visitor, Object extraData) {
		return visitor.visit(this, extraData);
	}

	public Object evaluate(Object object) {
		return evaluate( object, null );
	}

	public <T> T evaluate(Object object, Class<T> context) {
		Expression lookupValue = parameters.get(0);
		String belongsTo = queryThreshdoldBelongsTo( object );		
		Expression currentValue = parameters.get(1);
		
		List<Expression> splits; 
		if( parameters.size() % 2 == 0 ){
			splits = parameters.subList(2, parameters.size()-1);
		}
		else {
			splits = parameters.subList(2, parameters.size()-2);
		}
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
		for( int i = 0; i < splits.size(); i+=2){
			Expression threshhold = splits.get(i);
			Expression value = splits.get(i+1);
			Filter isIncludedInThreshold;
			if( PRECEDING.equals( belongsTo ) ){
				isIncludedInThreshold = ff.greater( lookupValue, threshhold );
			}
			else {
				isIncludedInThreshold = ff.greaterOrEqual(lookupValue, threshhold );
			}
			if( isIncludedInThreshold.evaluate( object ) ){
				currentValue = value;
			}
			else {
				break;
			}
		}
		return currentValue.evaluate( object, context );
	}
	
	/**
	 * There is no great way to check if the last parameter
	 * is intended to be the optional ThreshholdBelongsTo
	 * or not (since it is an optional parameter).
	 * <p>
	 * We can figure out if "succeeding" or "preceding" is
	 * being provided by checking the number of parameters.
	 * @param object
	 * @return PRECEDING or SUCCEEDING after checking if any information was provided.
	 */
	private String queryThreshdoldBelongsTo( Object object ){
		if( parameters.size() % 2 == 0 ){
			return SUCCEEDING; // user has not specified anything
		}
		Expression lastParameter = parameters.get( parameters.size()-1 );
		String lastValue = lastParameter.evaluate(object, String.class);
		if( PRECEDING.equalsIgnoreCase( lastValue)){
			return PRECEDING;
		}
		else if( SUCCEEDING.equalsIgnoreCase( lastValue)){
			return SUCCEEDING;
		}
		else {
			// warning?
			return SUCCEEDING; // default
		}		
	}
	
	public Literal getFallbackValue() {
		return fallback;
	}

}

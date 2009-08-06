package org.geotools.styling.builder;

import java.util.ArrayList;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Rule;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.opengis.filter.Filter;

public class RuleBuilder implements Builder<Rule> {
	StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
	List<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
	Builder<? extends Symbolizer> symbolizerBuilder;
	String name;
	String ruleAbstract;
	double minScaleDenominator;
	double maxScaleDenominator;
	Filter filter;
	boolean elseFilter;
	String title;

	public RuleBuilder() {
		reset();
	}
	
	public RuleBuilder name(String name) {
		this.name = name;
		return this;
	}
	
	public RuleBuilder title(String title) {
		this.title = title;
		return this;
	}
	
	public RuleBuilder ruleAbstract(String ruleAbstract) {
		this.ruleAbstract = ruleAbstract;
		return this;
	}
	
	public RuleBuilder minScaleDenominator(double minScaleDenominator) {
		if(minScaleDenominator < 0)
			throw new IllegalArgumentException("Invalid min scale denominator, should be positive or 0");
		this.minScaleDenominator = minScaleDenominator;
		return this;
	}
	
	public RuleBuilder maxScaleDenominator(double maxScaleDenominator) {
		if(maxScaleDenominator < 0)
			throw new IllegalArgumentException("Invalid max scale denominator, should be positive or 0");
		this.maxScaleDenominator = maxScaleDenominator;
		return this;
	}
	
	public RuleBuilder elseFilter() {
		this.elseFilter = true;
		this.filter = null;
		return this;
	}
	
	public RuleBuilder filter(Filter filter) {
		this.elseFilter = false;
		this.filter = filter;
		return this;
	}
	
	public PointSymbolizerBuilder newPoint() {
		if(symbolizerBuilder != null)
			symbolizers.add(symbolizerBuilder.build());
		symbolizerBuilder = new PointSymbolizerBuilder();
		return (PointSymbolizerBuilder) symbolizerBuilder;
	}
	
	public LineSymbolizerBuilder newLine() {
		if(symbolizerBuilder != null)
			symbolizers.add(symbolizerBuilder.build());
		symbolizerBuilder = new LineSymbolizerBuilder();
		return (LineSymbolizerBuilder) symbolizerBuilder;
	}
	
	public PolygonSymbolizerBuilder newPolygon() {
		if(symbolizerBuilder != null)
			symbolizers.add(symbolizerBuilder.build());
		symbolizerBuilder = new PolygonSymbolizerBuilder();
		return (PolygonSymbolizerBuilder) symbolizerBuilder;
	}
	
	public Rule build() {
		if(symbolizerBuilder == null) {
			symbolizerBuilder = new PointSymbolizerBuilder();
		}
		// cascade build operation
		symbolizers.add(symbolizerBuilder.build());
		
		Rule rule = sf.createRule();
		rule.setName(name);
		// TODO: rule's description cannot be set
		rule.setTitle(title);
		rule.setAbstract(ruleAbstract);
		rule.setMinScaleDenominator(minScaleDenominator);
		rule.setMaxScaleDenominator(maxScaleDenominator);
		rule.setFilter(filter);
		rule.setElseFilter(elseFilter);
		rule.symbolizers().addAll(symbolizers);
		
		reset();
		return rule;
	}
	
	public RuleBuilder reset() {
		name = null;
		title = null;
		ruleAbstract = null;
		minScaleDenominator = 0;
		maxScaleDenominator = Double.POSITIVE_INFINITY;
		filter = null;
		elseFilter = false;
		symbolizers.clear();
		return this;
	}
	
}

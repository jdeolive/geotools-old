package org.geotools.styling.builder;

import java.util.ArrayList;
import java.util.List;

import org.geotools.Builder;
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

    private boolean unset = false;

    public RuleBuilder() {
        reset();
    }

    public RuleBuilder name(String name) {
        unset = false;
        this.name = name;
        return this;
    }

    public RuleBuilder title(String title) {
        unset = false;
        this.title = title;
        return this;
    }

    public RuleBuilder ruleAbstract(String ruleAbstract) {
        unset = false;
        this.ruleAbstract = ruleAbstract;
        return this;
    }

    public RuleBuilder minScaleDenominator(double minScaleDenominator) {
        unset = false;
        if (minScaleDenominator < 0)
            throw new IllegalArgumentException(
                    "Invalid min scale denominator, should be positive or 0");
        this.minScaleDenominator = minScaleDenominator;
        return this;
    }

    public RuleBuilder maxScaleDenominator(double maxScaleDenominator) {
        unset = false;
        if (maxScaleDenominator < 0)
            throw new IllegalArgumentException(
                    "Invalid max scale denominator, should be positive or 0");
        this.maxScaleDenominator = maxScaleDenominator;
        return this;
    }

    public RuleBuilder elseFilter() {
        unset = false;
        this.elseFilter = true;
        this.filter = null;
        return this;
    }

    public RuleBuilder filter(Filter filter) {
        unset = false;
        this.elseFilter = false;
        this.filter = filter;
        return this;
    }

    public PointSymbolizerBuilder newPoint() {
        unset = false;
        if (symbolizerBuilder != null)
            symbolizers.add(symbolizerBuilder.build());
        symbolizerBuilder = new PointSymbolizerBuilder();
        return (PointSymbolizerBuilder) symbolizerBuilder;
    }

    public LineSymbolizerBuilder newLine() {
        unset = false;
        if (symbolizerBuilder != null)
            symbolizers.add(symbolizerBuilder.build());
        symbolizerBuilder = new LineSymbolizerBuilder();
        return (LineSymbolizerBuilder) symbolizerBuilder;
    }

    public PolygonSymbolizerBuilder newPolygon() {
        unset = false;
        if (symbolizerBuilder != null)
            symbolizers.add(symbolizerBuilder.build());
        symbolizerBuilder = new PolygonSymbolizerBuilder();
        return (PolygonSymbolizerBuilder) symbolizerBuilder;
    }

    public Rule build() {
        if( unset ){
            return null;
        }
        if (symbolizerBuilder == null) {
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

    public RuleBuilder unset(){
        reset();
        unset = true;
        return this;
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
        unset = false;
        return this;
    }
    public RuleBuilder reset( Rule rule ){
        if( rule == null ){
            return unset();            
        }
        name = rule.getName();
        title = rule.getTitle();
        ruleAbstract = rule.getAbstract();
        minScaleDenominator = rule.getMinScaleDenominator();
        maxScaleDenominator = rule.getMaxScaleDenominator();
        filter = rule.getFilter();
        elseFilter = rule.isElseFilter();
        symbolizers.clear();
        symbolizers.addAll( rule.symbolizers() ); // TODO: unpack into builders in order to "copy"
        unset = false;
        return this;
    }

}

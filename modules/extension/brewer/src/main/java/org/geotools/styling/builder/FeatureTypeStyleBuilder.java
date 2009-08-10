package org.geotools.styling.builder;

import java.util.ArrayList;
import java.util.List;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.NameImpl;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.StyleFactory;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.type.Name;

public class FeatureTypeStyleBuilder implements Builder<FeatureTypeStyle> {
    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    List<Rule> rules = new ArrayList<Rule>();

    RuleBuilder ruleBuilder;

    String name;

    String ftsAbstract;

    String title;

    List<Name> featureTypeNames;

    // TODO : add semantic type identifier, provided it makes any sense to have it

    public FeatureTypeStyleBuilder() {
        reset();
    }

    public RuleBuilder newRule() {
        if (ruleBuilder != null)
            rules.add(ruleBuilder.build());
        else
            ruleBuilder = new RuleBuilder();
        return ruleBuilder;
    }

    public FeatureTypeStyleBuilder name(String name) {
        this.name = name;
        return this;
    }

    public FeatureTypeStyleBuilder title(String title) {
        this.title = title;
        return this;
    }

    public FeatureTypeStyleBuilder ftsAbstract(String ftsAbstract) {
        this.ftsAbstract = ftsAbstract;
        return this;
    }

    /**
     * Accumulates another feature type name in the list of the feature type names for this
     * {@link FeatureTypeStyle}
     * 
     * @param featureTypeName
     * @return
     */
    public FeatureTypeStyleBuilder featureTypeName(String featureTypeName) {
        this.featureTypeNames.add(new NameImpl(featureTypeName));
        return this;
    }

    /**
     * Accumulates another feature type name in the list of the feature type names for this
     * {@link FeatureTypeStyle}
     * 
     * @param featureTypeName
     * @return
     */
    public FeatureTypeStyleBuilder featureTypeName(Name featureTypeName) {
        this.featureTypeNames.add(featureTypeName);
        return this;
    }

    public FeatureTypeStyle build() {
        if (ruleBuilder == null)
            ruleBuilder = new RuleBuilder();
        rules.add(ruleBuilder.build());

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().addAll(rules);
        if (ftsAbstract != null)
            fts.getDescription().setAbstract(new SimpleInternationalString(ftsAbstract));
        if (title != null)
            fts.getDescription().setTitle(new SimpleInternationalString(title));
        fts.setName(name);
        if (featureTypeNames != null && featureTypeNames.size() > 0)
            fts.featureTypeNames().addAll(featureTypeNames);

        reset();
        return fts;
    }

    public FeatureTypeStyleBuilder reset() {
        rules.clear();
        ruleBuilder = null;
        name = null;
        ftsAbstract = null;
        title = null;
        featureTypeNames = null;

        return this;
    }

    public Builder<FeatureTypeStyle> reset(FeatureTypeStyle original) {
        return null;
    }

    public Builder<FeatureTypeStyle> unset() {
        return null;
    }

}

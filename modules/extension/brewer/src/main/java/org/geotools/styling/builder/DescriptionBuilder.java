package org.geotools.styling.builder;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.StyleFactory;
import org.geotools.util.SimpleInternationalString;
import org.opengis.style.Description;
import org.opengis.util.InternationalString;

public class DescriptionBuilder implements Builder<Description> {
    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    private boolean unset;
    private InternationalString title;
    private InternationalString description;

    public Description build() {
        if( unset ){
            return null;
        }
        return null;
    }

    public DescriptionBuilder reset() {
        return null;
    }
    public DescriptionBuilder title(InternationalString title) {
        this.title = title;
        return this;
    }
    public DescriptionBuilder title(String title) {
        this.title = new SimpleInternationalString(title);
        return this;
    }
    public InternationalString title() {
        return title;
    }
    public DescriptionBuilder description(InternationalString description) {
        this.description = description;
        return this;
    }
    public DescriptionBuilder description(String description) {
        this.description = new SimpleInternationalString( description );
        return this;
    }
    public InternationalString description() {
        return description;
    }
    
    public DescriptionBuilder reset(Description original) {
        unset = false;
        title = original.getTitle();
        description = original.getAbstract();
        return this;
    }

    public DescriptionBuilder unset() {
        unset = true;
        title = null;
        description = null;
        return this;
    }

}

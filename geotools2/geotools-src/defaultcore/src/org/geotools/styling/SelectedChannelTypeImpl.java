/*
 * SelectedChannelImpl.java
 *
 * Created on 13 November 2002, 14:03
 */
package org.geotools.styling;

import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;


/**
 *
 * @author  iant
 */
public class SelectedChannelTypeImpl implements SelectedChannelType {
    private static  FilterFactory filterFactory = FilterFactory.createFilterFactory();
    private Expression contrastEnhancement;
    private String name = "channel";

    /** Creates a new instance of SelectedChannelImpl */
    public SelectedChannelTypeImpl() {
        contrastEnhancement = filterFactory.createLiteralExpression(1.0);
    }

    public String getChannelName() {
        return name;
    }

    public Expression getContrastEnhancement() {
        return contrastEnhancement;
    }

    public void setChannelName(String name) {
        this.name = name;
    }

    public void setContrastEnhancement(Expression enhancement) {
        this.contrastEnhancement = enhancement;
    }
}
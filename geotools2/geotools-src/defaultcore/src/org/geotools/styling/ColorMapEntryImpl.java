package org.geotools.styling;

import org.geotools.filter.Expression;

/**
 * Default color map entry implementation
 * @author aaime
 */
public class ColorMapEntryImpl implements ColorMapEntry {

    private Expression quantity;
    private Expression opacity;
    private static final java.util.logging.Logger LOGGER =
        java.util.logging.Logger.getLogger("org.geotools.core");
    private static final org.geotools.filter.FilterFactory filterFactory =
        org.geotools.filter.FilterFactory.createFilterFactory();

    private Expression color;

    private String label;

    /**
     * @see org.geotools.styling.ColorMapEntry#getLabel()
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#setLabel(java.lang.String)
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#setColor(org.geotools.filter.Expression)
     */
    public void setColor(Expression color) {
        this.color = color;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#getColor()
     */
    public Expression getColor() {
        return this.color;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#setOpacity(org.geotools.filter.Expression)
     */
    public void setOpacity(Expression opacity) {
        this.opacity = opacity;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#getOpacity()
     */
    public Expression getOpacity() {
        return this.opacity;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#setQuantity(org.geotools.filter.Expression)
     */
    public void setQuantity(Expression quantity) {
        this.quantity = quantity;
    }

    /**
     * @see org.geotools.styling.ColorMapEntry#getQuantity()
     */
    public Expression getQuantity() {
        return quantity;
    }

}

package org.geotools.styling;

import java.awt.Color;

import org.geotools.filter.Expression;


/**
 * A basic interface for objects which can hold color map entries.
 * <pre> 
 * &lt;xs:element name="ColorMapEntry"&gt;
 *   &lt;xs:complexType&gt;
 *     &lt;xs:attribute name="color" type="xs:string" use="required"/&gt;
 *     &lt;xs:attribute name="opacity" type="xs:double"/&gt;
 *     &lt;xs:attribute name="quantity" type="xs:double"/&gt;
 *     &lt;xs:attribute name="label" type="xs:string"/&gt;
 *   &lt;/xs:complexType&gt;
 * &lt;/xs:element&gt;
 * </pre>
 */
public interface ColorMapEntry {
    String getLabel();

    void setLabel(String label);

    void setColor(Expression color);

    Expression getColor();
    
    void setOpacity(Expression opacity);

    Expression getOpacity();

    void setQuantity(Expression quantity);

    Expression getQuantity();
}
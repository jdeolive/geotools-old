/*
 * Font.java
 *
 * Created on 03 July 2002, 12:02
 */

package org.geotools.styling;

import org.geotools.filter.Expression;
/**
 * A system independent object for holding SLD font infomation.
 * This holds information on the text font to use in text processing.
 * Font-family, font-style, font-weight and font-size.
 * $Id: Font.java,v 1.1 2002/07/03 13:35:21 ianturton Exp $
 * @author  iant
 */
public interface Font {
    
    public Expression getFontFamily();
    public Expression getFontStyle();
    public Expression getFontWeight();
    public Expression getFontSize();
    
}

/*
 * Halo.java
 *
 * Created on 03 July 2002, 12:36
 */

package org.geotools.styling;

import org.geotools.filter.Expression;
/**
 * A Halo fills an extended area outside the glyphs of a rendered textlabel
 * to make it easier to read over a background
 *
 * $Id: Halo.java,v 1.1 2002/07/03 13:35:21 ianturton Exp $ 
 * @author  iant
 */
public interface Halo {
    public Expression getRadius();
    public Fill getFill();
}

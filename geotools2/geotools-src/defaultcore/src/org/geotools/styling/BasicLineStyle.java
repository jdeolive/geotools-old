/**
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.styling;

/**
 * A style object is quite hard to set up, involving fills, strokes,
 * symbolizers and rules.
 * @version $Id: BasicLineStyle.java,v 1.2 2003/07/22 15:52:29 ianturton Exp $
 * @author James Macgill, CCG
 */
public class BasicLineStyle extends StyleImpl
    implements org.geotools.styling.Style {
    FeatureTypeStyle[] featureTypeStyleList;

    /** Creates a new instance of BasicPolygonStyle */
    public BasicLineStyle() {
        this(new StrokeImpl());
    }

    public BasicLineStyle(Stroke stroke) {
        LineSymbolizerImpl linesym = new LineSymbolizerImpl();
        linesym.setStroke(stroke);

        RuleImpl rule = new RuleImpl();
        rule.setSymbolizers(new Symbolizer[] { linesym });

        FeatureTypeStyleImpl fts = new FeatureTypeStyleImpl();
        fts.setRules(new Rule[] { rule });
        this.setFeatureTypeStyles(new FeatureTypeStyle[] { fts });
    }

    public String getAbstract() {
        return "A simple line style";
    }

    public String getName() {
        return "default line style";
    }

    public String getTitle() {
        return "default line style";
    }
}
/*
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Center for Computational Geography
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
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */

package org.geotools.styling;

/**
 * A Graphic is a "graphical symbol" with an inherent shape, color(s), and
 * possibly size.<p>
 * A "graphic" can very informally be defined as "a little picture" and can be
 * of either a raster or vector graphic source type.  The term graphic is used
 * since the term "symbol" is similar to "symbolizer" which is used in a
 * difference context in SLD.
 *
 * The graphical symbol to display can be provided either as an external
 * graphical resource or as a Mark.<br>
 * Multiple external URLs and marks can be referenced with the proviso that
 * they all provide equivalent graphics in different formats.
 * The 'hot spot' to use for positioning the rendering at a point must either
 * be inherent from the external format or be defined to be the 
 * "central point" of the graphic.
 * 
 * The details of this object are taken from the OGC Styled-Layer Descriptor
 * Report (OGC 01-077) version 0.7.2.
 * Renderers can use this information when displaying styled features,
 * though it must be remembered that not all renderers will be able to
 * fully represent strokes as set out by this interface.  For example, opacity
 * may not be supported.
 *
 * The graphical parameters and their values are derived from SVG/CSS2 
 * standards with names and semantics which are as close as possible.<p>
 *
 * @task REVISIT: There are no setter methods in this interface, is this a problem?
 *
 * @version $Id: Graphic.java,v 1.8 2002/10/14 17:07:58 ianturton Exp $
 * @author James Macgill, CCG
 */

import org.geotools.filter.Expression;

public interface Graphic {


    /**
     * Provides a list of external graphics which can be used to represent
     * this graphic.
     * Each one should be an equivalent representation but in a 
     * different format.
     * If none are provided, or if none of the formats are supported, then the
     * list of Marks should be used instead.
     *
     * @task REVISIT: The following may be a handy extra to have in this interface.
     *                public ExternalGraphic getExternalGraphic(String formats);
     *                return the first external graphic to match one of the given formats
     *
     * @return An array of ExternalGraphics objects which should be equivalents
     *         but in different formats.  If null is returned, use
     *         getMarks instead.
     */
    ExternalGraphic[] getExternalGraphics();
    void setExternalGraphics(ExternalGraphic[] externalGraphics);

    
    /**
     * Provides a list of suitable marks which can be used to represent this
     * graphic.
     * These should only be used if no ExternalGraphic is provided, or if none
     * of the external graphics formats are supported.
     *
     * @return An array of marks to use when displaying this Graphic. 
     * By default, a "square" with 50% gray fill and black outline with a size
     * of 6 pixels (unless a size is specified) is provided.
     */    
    Mark[] getMarks();
    void setMarks(Mark[] marks);
    /**
     * Provides a list of all the symbols which can be used to represent this
     * graphic. A symbol is an ExternalGraphic, Mark or any other object which
     * implements the Symbol interface.
     * These are returned in the order they were set. 
     *
     * @return An array of symbols to use when displaying this Graphic. 
     * By default, a "square" with 50% gray fill and black outline with a size
     * of 6 pixels (unless a size is specified) is provided.
     */    
    Symbol[] getSymbols();
    void setSymbols(Symbol[] symbols); 
    /**
     * This specifies the level of translucency to use when rendering the 
     * graphic.<br>
     * The value is encoded as a floating-point value between 0.0 and
     * 1.0 with 0.0 representing totally transparent and 1.0 representing
     * totally opaque, with a linear scale of translucency for intermediate
     * values.<br>
     * For example, "0.65" would represent 65% opacity.  
     * The default value is 1.0 (opaque).
     *
     * @return The opacity of the Graphic, where 0.0 is completely 
     *         transparent and 1.0 is completely opaque.
     */
    Expression getOpacity();
    void setOpacity(Expression opacity);
    /**
     * This paramteter gives the absolute size of the graphic in pixels
     * encoded as a floating point number.<p>
     * The default size of an image format (such as GIFD) is the inherent size
     * of the image.  The default size of a format without an inherent size
     * (such as SVG) is defined to be 16 pixels in height and the 
     * corresponding aspect in width.
     * If a size is specified, the height of the graphic will be scaled to that
     * size and the corresponding aspect will be used for the width.
     *
     * @return The size of the graphic.  The default is context specific.
     *         Negative values are not possible.
     */
    Expression getSize();
    void setSize(Expression size);
    /**
     * This parameter defines the rotation of a graphic in the clockwise
     * direction about its centre point in decimal degrees.  
     * The value encoded as a floating point number.
     * 
     * @return The angle of rotation in decimal degrees.  Negative values
     *         represent counter-clockwise rotation.
     *         The default is 0.0 (no rotation).
     */
    Expression getRotation();
    void setRotation(Expression rotation);
}


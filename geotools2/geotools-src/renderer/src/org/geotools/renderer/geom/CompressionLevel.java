/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
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
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.renderer.geom;

// J2SE and JAI dependencies
import java.util.Locale;
import java.io.ObjectStreamException;
import java.util.NoSuchElementException;
import javax.media.jai.EnumeratedParameter;

// JTS dependencies (for JavaDoc only)
import com.vividsolutions.jts.geom.Coordinate;


/**
 * The compression level for coordinate points in a {@link Geometry} object. Compressions
 * are trigged by the {@link Geometry#compress Geometry.compress(...)} method and consist
 * in a change of the storage type for (<var>x</var>,<var>y</var>) coordinates. Note that
 * the compression may be destructive, i.e. it may sacrifice data and/or precision.   For
 * example,  <A HREF="http://www.vividsolutions.com/JTS/jts_frame.htm">JTS</A> 1.3 stores
 * points as {@link Coordinate} objects with (<var>x</var>,<var>y</var>,<var>z</var>)
 * <code>double</code> values, which consume a lot of memory. The compression level
 * {@link #DIRECT_AS_FLOATS} recopies the (<var>x</var>,<var>y</var>) ordinates in a
 * <code>float[]</code> array, loosing the <var>z</var> value and some precision due to the
 * conversion of <code>double</code> to <code>float</code> values. The compression level
 * {@link #RELATIVE_AS_BYTES} goes further with a two steps process:
 *
 * <ul>
 *   <li><P>First, it invokes
 *       <code>{@link Geometry#setResolution setResolution}(dx&nbsp;+&nbsp;0.5*std)</code> where
 *       <var>dx</var> is the geometry {@linkplain Geometry#getResolution mean resolution} and
 *       <var>std</var> is the resolution's standard deviation. This call ensure that distance
 *       between each consecutive points is approximatively constant and long enough for 2/3 of
 *       line segments (i.e. this resampling may <strong>add</strong> new points for less than
 *       1/3 of line segments, assuming a normal (gaussian) distribution). If a different resampling
 *       is wanted, just invoke {@link Geometry#setResolution setResolution(...)} explicitely before
 *       {@link Geometry#compress compress(...)}.</P></li>
 *
 *   <li><P>Second, it replaces absolute positions (left handed image) by relative positions
 *       (right handed image), i.e. distances relative to the previous point. Since all
 *       distances are of similar magnitude, distances can be coded in <code>byte</code>
 *       primitive type instead of <code>float</code>.
 *
 *       <table cellspacing='12'><tr>
 *       <td><p align="center"><img src="doc-files/uncompressed.png"></p></td>
 *       <td><p align="center"><img src="doc-files/compressed.png"></p></td>
 *       </tr></table>
 *   </li>
 * </ul>
 *
 * @version $Id: CompressionLevel.java,v 1.1 2003/05/27 18:22:43 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see Geometry#compress
 * @see Geometry#getMemoryUsage
 */
public final class CompressionLevel extends EnumeratedParameter {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1436049145789008139L;
    
    /**
     * Transform coordinate points into direct positions stored as <code>float</code> values.
     * This compression level has no effect if data are already stored as <code>float</code>,
     * or if a more agressive compression is already in use (e.g. {@link #RELATIVE_AS_BYTES}).
     */
    public static final CompressionLevel DIRECT_AS_FLOATS =
                        new CompressionLevel("DIRECT_AS_FLOATS", 0);
    
    /**
     * Transform coordinate points into relative positions stored as <code>byte</code> values.
     * Before the compression, the coordinates are resampled (if needed) in order to obtain line
     * segments of equal length. This compression level has no effect if the data are already
     * compressed as relative positions.
     */
    public static final CompressionLevel RELATIVE_AS_BYTES =
                        new CompressionLevel("RELATIVE_AS_BYTES", 1);
    
    /**
     * Interior type by value. Used to canonicalize after deserialization.
     */
    private static final CompressionLevel[] ENUMS = {
        DIRECT_AS_FLOATS, RELATIVE_AS_BYTES
    };
    static {
        for (int i=0; i<ENUMS.length; i++) {
            if (ENUMS[i].getValue() != i) {
                throw new AssertionError(ENUMS[i]);
            }
        }
    }
    
    /**
     * Construct a new enum with the specified value.
     */
    private CompressionLevel(final String name, final int value) {
        super(name, value);
    }
    
    /**
     * Return the enum for the specified value.
     *
     * @param  value The enum value.
     * @return The enum for the specified value.
     * @throws NoSuchElementException if there is no enum for the specified value.
     */
    static CompressionLevel getEnum(final int value) throws NoSuchElementException {
        if (value>=0 && value<ENUMS.length) return ENUMS[value];
        throw new NoSuchElementException(String.valueOf(value));
    }
    
    /**
     * Use a single instance of {@link CompressionLevel} after deserialization.
     * It allow client code to test <code>enum1==enum2</code> instead of
     * <code>enum1.equals(enum2)</code>.
     *
     * @return A single instance of this enum.
     * @throws ObjectStreamException is deserialization failed.
     */
    private Object readResolve() throws ObjectStreamException {
        int value = getValue();
        if (value<0 || value>=ENUMS.length) {
            // Collapse unknow value to a single canonical one
            value = 0;
        }
        return ENUMS[value]; // Canonicalize
    }
}

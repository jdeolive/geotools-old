/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.ct;

// J2SE dependencies
import java.awt.Point;
import java.util.Arrays;

// JAI dependencies
import javax.media.jai.util.Range;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.EnumeratedParameter;

// Geotools dependencies and resources
import org.geotools.pt.Matrix;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

/**
 * A matrix editable as a {@link ParameterList} object. Changes to the {@link Matrix} are
 * reflected in the {@link ParameterList}/{@link ParameterListDescriptor} and vis-versa.
 * This custom implementation is different than the default {@link ParameterListDescriptor}
 * implementation in that it is "extensible", i.e. the number of parameters depends of the
 * number of row and column in the matrix. 
 *
 * @version $Id: MatrixParameters.java,v 1.1 2002/10/10 14:44:21 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class MatrixParameters extends Matrix implements ParameterList, ParameterListDescriptor {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5150306783193080487L;

    /**
     * Range of positives values. Range
     * goes from 1 to the maximum value.
     */
    static final Range POSITIVE_RANGE = new Range(Integer.class, new Integer(1), null);

    /**
     * The default matrix size.
     */
    static final Integer DEFAULT_SIZE = new Integer(4);

    /**
     * Construct a default parameter list.
     * The default matrix size is 4&times;4.
     */
    public MatrixParameters() {
        super(DEFAULT_SIZE.intValue());
    }

    /**
     * Returns the associated parameter list descriptor.
     * This <code>MatrixParameters</code> is its own descriptor.
     */
    public ParameterListDescriptor getParameterListDescriptor() {
        return this;
    }

    /**
     * Returns the total number of parameters. This is equals to the
     * number of matrix elements plus 2 (the <code>"num_row"</code>
     * and <code>"num_col"</code> parameters).
     */
    public int getNumParameters() {
        return getNumRow() * getNumCol() + 2;
    }

    /**
     * Construct a matrix from a parameter block. This method is used by
     * {@link MathTransformFactory#createParameterizedTransform}.
     */
    public static Matrix getMatrix(final ParameterList parameters) {
        if (parameters instanceof Matrix) {
            return (Matrix) parameters;
        }
        final int numRow = parameters.getIntParameter("num_row");
        final int numCol = parameters.getIntParameter("num_col");
        final Matrix  matrix = new Matrix(numRow, numCol);
        final String[] names = parameters.getParameterListDescriptor().getParamNames();
        if (names!=null) {
            for (int i=0; i<names.length; i++) {
                final String name = names[i];
                if (name.regionMatches(true, 0, "elt_", 0, 4)) {
                    final int separator = name.lastIndexOf('_');
                    final int row = Integer.parseInt(name.substring(4, separator));
                    final int col = Integer.parseInt(name.substring(separator+1));
                    matrix.setElement(row, col, parameters.getDoubleParameter(name));
                }
            }
        }
        return matrix;
    }

    /**
     * Returns the matrix index for the specified name, or <code>null</code>
     * if the name is <code>"num_row"</code> or <code>"num_col"</code>.
     *
     * @param  name The parameter name.
     * @return The matrix index of the parameter name, or <code>null</code>.
     * @throws IllegalArgumentException if the name is not valid.
     */
    private Point getIndex(final String name) throws IllegalArgumentException {
        NumberFormatException cause = null;
        if (name != null) try {
            if (name.equalsIgnoreCase("num_row")) return null;
            if (name.equalsIgnoreCase("num_col")) return null;
            if (name.regionMatches(true, 0, "elt_", 0, 4)) {
                final int separator = name.lastIndexOf('_');
                final int row = Short.parseShort(name.substring(4, separator));
                final int col = Short.parseShort(name.substring(separator+1));
                return new Point(col, row);
            }
        } catch (NumberFormatException exception) {
            cause = exception;
        }
        IllegalArgumentException exception = new IllegalArgumentException(name);
        exception.initCause(cause);
        throw exception;
    }

    /**
     * Returns an array of names of the parameters
     * associated with this descriptor.
     */
    public String[] getParamNames() {
        final String[] names = new String[getNumParameters()];
        final int     numRow = getNumRow();
        final int     numCol = getNumCol();
        int index = 0;
        names[index++] = "num_row";
        names[index++] = "num_col";
        final StringBuffer buffer=new StringBuffer("elt_");
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                buffer.setLength(4);
                buffer.append(j);
                buffer.append('_');
                buffer.append(i);
                names[index++] = buffer.toString();
            }
        }
        assert index == names.length;
        return names;
    }

    /**
     * Returns an array of parameters classes.
     */
    public Class[] getParamClasses() {
        final Class[] classes = new Class[getNumParameters()];
        Arrays.fill(classes, 0, 2,             Integer.class);
        Arrays.fill(classes, 2, classes.length, Double.class);
        return classes;
    }

    /**
     * Returns an array of objects that define
     * the default values of the parameters.
     */
    public Object[] getParamDefaults() {
        final Number[] values = new Number[getNumParameters()];
        final int     numRow = getNumRow();
        final int     numCol = getNumCol();
        int index = 0;
        values[index++] = new Integer(numRow);
        values[index++] = new Integer(numCol);
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                values[index++] = ((i==j) ? MathTransformProvider.ONE : MathTransformProvider.ZERO);
            }
        }
        assert index == values.length;
        return values;
    }

    /**
     * Returns the default value of a specified parameter. Default value for
     * a matrix element is 1 for any element on the diagonal, or 0 otherwise.
     */
    public Object getParamDefaultValue(final String name) {
        if (name.equalsIgnoreCase("num_row")) return DEFAULT_SIZE;
        if (name.equalsIgnoreCase("num_col")) return DEFAULT_SIZE;
        final Point index = getIndex(name);
        return (index.x==index.y) ? MathTransformProvider.ONE : MathTransformProvider.ZERO;
    }

    /**
     * Gets a named parameter as an {@link Object}.
     */
    public Object getObjectParameter(final String name) {
        if (name.equalsIgnoreCase("num_row")) return new Integer(getNumRow());
        if (name.equalsIgnoreCase("num_col")) return new Integer(getNumCol());
        final Point index = getIndex(name);
        try {
            return new Double(getElement(index.y, index.x));
        } catch (IndexOutOfBoundsException cause) {
            IllegalArgumentException exception = new IllegalArgumentException(name);
            exception.initCause(cause);
            throw exception;
        }
    }

    /**
     * Gets a named parameter as an integer.
     * The parameter must be the matrix height or width.
     */
    public int getIntParameter(final String name) {
        if (name.equalsIgnoreCase("num_row")) return getNumRow();
        if (name.equalsIgnoreCase("num_col")) return getNumCol();
        throw new IllegalArgumentException(name);
    }

    /**
     * Gets a named parameter as a floating-point number.
     * The parameter can be the matrix height or width, or
     * a matrix element.
     */
    public double getDoubleParameter(final String name) {
        final Point index = getIndex(name);
        if (index == null) {
            return getIntParameter(name);
        } else try {
            return getElement(index.y, index.x);
        } catch (IndexOutOfBoundsException cause) {
            IllegalArgumentException exception = new IllegalArgumentException(name);
            exception.initCause(cause);
            throw exception;
        }
    }

    /**
     * Return an array of the names of all parameters
     * the type of which is {@link EnumeratedParameter}.
     */
    public String[] getEnumeratedParameterNames() {
        return new String[0];
    }

    /**
     * Return an array of {@link EnumeratedParameter} objects
     * corresponding to the parameter with the specified name
     */
    public EnumeratedParameter[] getEnumeratedParameterValues(final String name) {
        return new EnumeratedParameter[0];
    }

    /**
     * Returns the range of valid values for the specified parameter.
     */
    public Range getParamValueRange(final String name) {
        return getIndex(name)==null ? POSITIVE_RANGE : null;
    }

    /**
     * Checks to see whether the specified parameter can take on the specified value.
     */
    public boolean isParameterValueValid(final String name, final Object value) {
        if (getIndex(name) == null) {
            if (value instanceof Integer) {
                return ((Number) value).intValue() > 0;
            }
        } else {
            if (value instanceof Number) {
                return !Double.isNaN(((Number) value).doubleValue());
            }
        }
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, name, value));
    }

    /**
     * Ensure that the specified value is a non-null positive integer.
     */
    private static void ensurePositive(final String name, final int value) {
        if (value <=0) {
            throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, name, new Integer(value)));
        }
    }

    /**
     * Set a parameter to the specified integer value.
     */
    public ParameterList setParameter(final String name, final int value) {
        if (name.equalsIgnoreCase("num_row")) {
            ensurePositive("num_row", value);
            setSize(value, getNumCol());
            return this;
        }
        if (name.equalsIgnoreCase("num_col")) {
            ensurePositive("num_col", value);
            setSize(getNumRow(), value);
            return this;
        }
        return setParameter(name, (double)value);
    }

    /**
     * Set a parameter to the specified floating-point value.
     */
    public ParameterList setParameter(final String name, final double value) {
        IndexOutOfBoundsException cause = null;
        final Point index = getIndex(name);
        if (index != null) try {
            setElement(index.y, index.x, value);
            return this;
        } catch (IndexOutOfBoundsException exception) {
            cause = exception;
        }
        IllegalArgumentException exception = new IllegalArgumentException(name);
        exception.initCause(cause);
        throw exception;
    }

    /**
     * Set a parameter to the specified object.
     */
    public ParameterList setParameter(final String name, final Object value) {
        if (isParameterValueValid(name, value)) {
            if (value instanceof Integer) {
                return setParameter(name, ((Integer) value).intValue());
            }
            return setParameter(name, ((Number) value).doubleValue());
        }
        throw new IllegalArgumentException(Resources.format(
                ResourceKeys.ERROR_ILLEGAL_ARGUMENT_$2, name, value));
    }

    public boolean getBooleanParameter(String n) {throw new IllegalArgumentException(n);}
    public byte    getByteParameter   (String n) {throw new IllegalArgumentException(n);}
    public char    getCharParameter   (String n) {throw new IllegalArgumentException(n);}
    public short   getShortParameter  (String n) {throw new IllegalArgumentException(n);}
    public long    getLongParameter   (String n) {throw new IllegalArgumentException(n);}
    public float   getFloatParameter  (String n) {return   (float)getDoubleParameter(n);}

    public ParameterList setParameter(String n, boolean v) {return setParameter(n, v ? 1 : 0);}
    public ParameterList setParameter(String n, byte    v) {return setParameter(n,    (int)v);}
    public ParameterList setParameter(String n, char    v) {return setParameter(n,    (int)v);}
    public ParameterList setParameter(String n, short   v) {return setParameter(n,    (int)v);}
    public ParameterList setParameter(String n, long    v) {return setParameter(n, (double)v);}
    public ParameterList setParameter(String n, float   v) {return setParameter(n, (double)v);}
}

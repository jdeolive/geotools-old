/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Institut de Recherche pour le Développement
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
package org.geotools.gc;

// J2SE dependencies
import java.util.Map;
import java.util.Arrays;
import java.awt.image.DataBuffer;

// JAI dependencies
import javax.media.jai.LookupTableJAI;

// Geotools dependencies
import org.geotools.ct.MathTransform1D;
import org.geotools.ct.TransformException;
import org.geotools.util.WeakValueHashMap;


/**
 * A factory for {@link LookupTableJAI} objects built from an array of {@link MathTransform1D}.
 * This factory is used internally by {@link GridCoverage#createGeophysics}.
 *
 * @version $Id: LookupTableFactory.java,v 1.1 2003/05/05 10:50:20 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class LookupTableFactory {
    /**
     * The pool of {@link LookupTableJAI} objects already created.
     */
    private static final Map pool = new WeakValueHashMap();

    /**
     * The source data type. Should be one of {@link DataBuffer} constants.
     */
    private final int sourceType;

    /**
     * The target data type. Should be one of {@link DataBuffer} constants.
     */
    private final int targetType;

    /**
     * The math transforms for this key.
     */
    private final MathTransform1D[] transforms;

    /**
     * Create a new objet to use as a key in the {@link #pool}.
     *
     * @param sourceType The source data type. Should be one of {@link DataBuffer} constants.
     * @param targetType The target data type. Should be one of {@link DataBuffer} constants.
     * @param transforms The math transforms to apply.
     */
    private LookupTableFactory(final int sourceType,
                               final int targetType,
                               final MathTransform1D[] transforms)
    {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.transforms = transforms;
    }

    /**
     * Gets a lookup factory
     *
     * @param  sourceType The source data type. Should be one of {@link DataBuffer} constants.
     * @param  targetType The target data type. Should be one of {@link DataBuffer} constants.
     * @param  transforms The math transforms to apply.
     * @return The lookup table, or <code>null</code> if this method can't build a lookup
     *         table for the supplied parameters.
     * @throws TransformException if a transformation failed.
     */
    public static LookupTableJAI create(final int sourceType,
                                        final int targetType,
                                        final MathTransform1D[] transforms)
            throws TransformException
    {
        /*
         * Argument check. Null values are legal but can't be processed by this method.
         */
        for (int i=0; i<transforms.length; i++) {
            if (transforms[i] == null) {
                return null;
            }
        }
        synchronized (pool) {
            /*
             * Check if a table is already available in the cache. Since tables may be 64 ko big,
             * sharing tables may save a significant amount of memory if there is many images.
             */
            final LookupTableFactory key=new LookupTableFactory(sourceType, targetType, transforms);
            LookupTableJAI table = (LookupTableJAI) pool.get(key);
            if (table != null) {
                return table;
            }
            /*
             * Compute the table's size according the source datatype.  For datatype 'short' (signed
             * or unsigned), we will create the table only if the target datatype is 'byte' in order
             * to avoid to use too much memory for the table. The memory consumed for a table from
             * source datatype 'short' to target datatype 'byte' is 64 ko.
             */
            final int length;
            final int offset;
            switch (sourceType) {
                default: {
                    return null;
                }
                case DataBuffer.TYPE_BYTE: {
                    length = 0x100;
                    offset = 0;
                    break;
                }
                case DataBuffer.TYPE_SHORT:
                case DataBuffer.TYPE_USHORT: {
                    if (targetType != DataBuffer.TYPE_BYTE) {
                        // Avoid to use too much memory for the table.
                        return null;
                    }
                    length = 0x10000;
                    offset = (sourceType==DataBuffer.TYPE_SHORT) ? Short.MIN_VALUE : 0;
                    break;
                }
            }
            /*
             * Build the table according the target datatype.
             */
            switch (targetType) {
                default: {
                    return null;
                }

                case DataBuffer.TYPE_DOUBLE: {
                    final double[][]  data = new double[transforms.length][];
                    final double[]  buffer = new double[length];
                    for (int i=buffer.length; --i>=0;) {
                        buffer[i] = i;
                    }
                    for (int i=transforms.length; --i>=0;) {
                        final double[] array = (i==0) ? buffer : (double[])buffer.clone();
                        transforms[i].transform(array, 0, array, 0, array.length);
                        data[i] = array;
                    }
                    table = new LookupTableJAI(data, offset);
                    break;
                }

                case DataBuffer.TYPE_FLOAT: {
                    final float[][]  data = new float[transforms.length][];
                    final float[]  buffer = new float[length];
                    for (int i=buffer.length; --i>=0;) {
                        buffer[i] = i;
                    }
                    for (int i=transforms.length; --i>=0;) {
                        final float[] array = (i==0) ? buffer : (float[])buffer.clone();
                        transforms[i].transform(array, 0, array, 0, array.length);
                        data[i] = array;
                    }
                    table = new LookupTableJAI(data, offset);
                    break;
                }

                case DataBuffer.TYPE_INT: {
                    final int[][] data = new int[transforms.length][];
                    for (int i=transforms.length; --i>=0;) {
                        final MathTransform1D tr = transforms[i];
                        final int[] array = new int[length];
                        for (int j=array.length; --j>=0;) {
                            array[j] = (int)Math.min(Math.max(Math.round(tr.transform(j+offset)),
                                                             Integer.MIN_VALUE), Integer.MAX_VALUE);
                        }
                        data[i] = array;
                    }
                    table = new LookupTableJAI(data, offset);
                    break;
                }

                case DataBuffer.TYPE_SHORT:
                case DataBuffer.TYPE_USHORT: {
                    final int minimum, maximum;
                    if (targetType == DataBuffer.TYPE_SHORT) {
                        minimum = Short.MIN_VALUE;
                        maximum = Short.MAX_VALUE;
                    } else {
                        minimum = 0;
                        maximum = 0xFFFF;
                    }
                    final short[][] data = new short[transforms.length][];
                    for (int i=transforms.length; --i>=0;) {
                        final MathTransform1D tr = transforms[i];
                        final short[] array = new short[length];
                        for (int j=array.length; --j>=0;) {
                            array[j] = (short)Math.min(Math.max(Math.round(tr.transform(j+offset)),
                                                                minimum), maximum);
                        }
                        data[i] = array;
                    }
                    table = new LookupTableJAI(data, offset, minimum!=0);
                    break;
                }

                case DataBuffer.TYPE_BYTE: {
                    final byte[][] data = new byte[transforms.length][];
                    for (int i=transforms.length; --i>=0;) {
                        final MathTransform1D tr = transforms[i];
                        final byte[] array = new byte[length];
                        for (int j=array.length; --j>=0;) {
                            array[j] = (byte)Math.min(Math.max(Math.round(tr.transform(j+offset)),
                                                               0), 0xFF);
                        }
                        data[i] = array;
                    }
                    table = new LookupTableJAI(data, offset);
                    break;
                }
            }
            pool.put(key, table);
            return table;
        }
    }

    /**
     * Returns a hash code value for this key. This is for internal use by
     * <code>LookupTableFactory</code> and is public only as an implementation side effect.
     */
    public int hashCode() {
        int code = sourceType + 37*targetType;
        for (int i=0; i<transforms.length; i++) {
            code = code*37 + transforms[i].hashCode();
        }
        return code;
    }

    /**
     * Compare the specified object with this key for equality. This is for internal use by
     * <code>LookupTableFactory</code> and is public only as an implementation side effect.
     */
    public boolean equals(final Object other) {
        if (other instanceof LookupTableFactory) {
            final LookupTableFactory that = (LookupTableFactory) other;
            return this.sourceType == that.sourceType &&
                   this.targetType == that.targetType &&
                   Arrays.equals(this.transforms, that.transforms);
        }
        return false;
    }
}

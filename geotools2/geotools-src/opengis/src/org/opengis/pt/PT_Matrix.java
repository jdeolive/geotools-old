package org.opengis.pt;

/** A two dimensional array of numbers.
 */
public class PT_Matrix implements java.io.Serializable
{
    /** Elements of the matrix.
     * The elements should be stored in a rectangular two dimensional array.
     * So in Java, all double[] elements of the outer array must have the
     * same size.  In COM, this is represented as a 2D SAFEARRAY.
     */
    public double[][] elt;
}


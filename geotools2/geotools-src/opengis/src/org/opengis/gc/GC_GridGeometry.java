/*
 * OpenGIS® Grid Coverage Services Implementation Specification
 * Copyright (2001) OpenGIS consortium
 *
 * THIS COPYRIGHT NOTICE IS A TEMPORARY PATCH.   Version 1.00 of official
 * OpenGIS's interface files doesn't contain a copyright notice yet. This
 * file is a slightly modified version of official OpenGIS's interface.
 * Changes have been done in order to fix RMI problems and are documented
 * on the SEAGIS web site (seagis.sourceforge.net). THIS FILE WILL LIKELY
 * BE REPLACED BY NEXT VERSION OF OPENGIS SPECIFICATIONS.
 */
package org.opengis.gc;

// Input/output
import java.io.Serializable;

// CSS dependencies
import org.opengis.ct.CT_MathTransform;


/**
 * Describes the geometry and georeferencing information of the grid coverage.
 * The grid range attribute determines the valid grid coordinates and allows
 * for calculation of grid size. A grid coverage may or may not have georeferencing.
 *
 * @version 1.00
 * @since   1.00
 */
public class GC_GridGeometry implements Serializable
{
    /**
     * Use <code>serialVersionUID</code> from first
     * draft for interoperability with GCS 1.00.
     */
    private static final long serialVersionUID = 6688149642370334819L;

    /**
     * The valid coordinate range of a grid coverage.
     * The lowest valid grid coordinate is zero.
     * A grid with 512 cells can have a minimum coordinate of 0 and maximum of 512,
     * with 511 as the highest valid index.
     */
    public GC_GridRange gridRange;

    /**
     * The math transform allows for the transformations from grid coordinates to real
     * world earth coordinates. The transform is often an affine transformation. The
     * coordinate system of the real world coordinates is given by the
     * {@link org.opengis.cv.CV_Coverage#getCoordinateSystem} method.
     * If no math transform is given, this attribute will be <code>null</code>.
     */
    public CT_MathTransform gridToCoordinateSystem;

    /**
     * Construct an empty Data Type object. Caller
     * must initialize {@link #gridRange} and {@link #gridToCoordinateSystem}.
     */
    public GC_GridGeometry()
    {}

    /**
     * Construct a new Data Type object.
     */
    public GC_GridGeometry(final GC_GridRange gridRange, final CT_MathTransform gridToCoordinateSystem)
    {
        this.gridRange              = gridRange;
        this.gridToCoordinateSystem = gridToCoordinateSystem;
    }

    /**
     * Returns a hash value for this <code>GridGeometry</code>.
     * This value need not remain consistent between
     * different implementations of the same class.
     */
    public int hashCode()
    {
        int code = 0;
        if (gridRange              != null) code = gridRange.hashCode();
        if (gridToCoordinateSystem != null) code = 37*code + gridToCoordinateSystem.hashCode();
        return code;
    }

    /**
     * Compares the specified object with
     * this grid geometry for equality.
     */
    public boolean equals(final Object object)
    {
        if (object!=null && getClass().equals(object.getClass()))
        {
            final GC_GridGeometry that = (GC_GridGeometry) object;
            return GC_ParameterInfo.equals(gridRange,              that.gridRange) &&
                   GC_ParameterInfo.equals(gridToCoordinateSystem, that.gridToCoordinateSystem);
        }
        else return false;
    }

    /**
     * Returns a string représentation of this enum.
     * The returned string is implementation dependent.
     * It is usually provided for debugging purposes only.
     */
    public String toString()
    {
        final StringBuffer buffer=new StringBuffer("GC_GridGeometry");
        buffer.append('[');
        buffer.append(gridRange);
        buffer.append(',');
        buffer.append(gridToCoordinateSystem);
        buffer.append(']');
        return buffer.toString();
    }
}

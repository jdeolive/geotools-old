package org.opengis.ct;
import org.opengis.cs.*;

// interface CS_CoordinateSystem;

/** Creates coordinate transformations. */
public interface CT_CoordinateTransformationFactory extends java.rmi.Remote
{
    /** Creates a transformation between two coordinate systems.
     *  This method will examine the coordinate systems in order to
     *  construct a transformation between them. This method may fail if no
     *  path between the coordinate systems is found, using the normal failing
     *  behavior of the DCP (e.g. throwing an exception).
     *  @param sourceCS Input coordinate system.
     *  @param targetCS Output coordinate system.
     */
    CT_CoordinateTransformation createFromCoordinateSystems(CS_CoordinateSystem sourceCS,CS_CoordinateSystem targetCS);
}

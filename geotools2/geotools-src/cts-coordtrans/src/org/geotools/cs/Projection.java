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
package org.geotools.cs;

// OpenGIS dependencies
import org.opengis.cs.CS_Projection;
import org.opengis.cs.CS_ProjectionParameter;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.Utilities;
import org.geotools.resources.DescriptorNaming;
import org.geotools.ct.MissingParameterException;
//import org.geotools.ct.MathTransformProvider;

// Parameters
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListImpl;
import javax.media.jai.ParameterListDescriptor;

// J2SE dependencies
import java.awt.geom.Point2D;
import java.rmi.RemoteException;


/**
 * A projection from geographic coordinates to projected coordinates.
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_Projection
 */
public class Projection extends Info {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 2153398430020498215L;
    
    /**
     * Classification string for projection (e.g. "Transverse_Mercator").
     */
    private final String classification;
    
    /**
     * Parameters to use for projection, in metres or degrees.
     */
    private final ParameterList parameters;
    
    /**
     * Convenience constructor for a projection using the specified ellipsoid.
     *
     * @param name           Name to give new object.
     * @param classification Classification string for projection (e.g. "Transverse_Mercator").
     * @param ellipsoid      Ellipsoid parameter. If non-null, then <code>"semi_major"</code>
     *                       and <code>"semi_minor"</code> parameters will be set according.
     * @param centre         Central meridian and latitude of origin, in degrees. If non-null, then
     *                       <code>"central_meridian"</code> and <code>"latitude_of_origin"</code>
     *                       will be set according.
     * @param translation    False easting and northing, in metres. If non-null, then
     *                       <code>"false_easting"</code> and <code>"false_northing"</code>
     *                       will be set according.
     */
    public Projection(final CharSequence name, final String classification,
                      final Ellipsoid ellipsoid, final Point2D centre, final Point2D translation) {
        super(name);
        ensureNonNull("classification", classification);
        this.classification = classification;
        this.parameters = init(getParameterList(classification), ellipsoid, centre, translation);
    }
    
    /**
     * Creates a projection. The set of parameters (<code>parameters</code>) may be queried
     * with the <code>{@link CoordinateSystemFactory#createProjectionParameterList}</code>
     * method.
     *
     * @param name           Name to give new object.
     * @param classification Classification string for projection (e.g. "Transverse_Mercator").
     * @param parameters     Parameters to use for projection, in metres or degrees.
     *
     * @see org.opengis.cs.CS_CoordinateSystemFactory#createProjection
     */
    public Projection(final CharSequence name, final String classification, final ParameterList parameters) {
        super(name);
        ensureNonNull("classification", classification);
        ensureNonNull("parameters",     parameters);
        this.classification = classification;
        this.parameters = clone(parameters);
    }
    
    /**
     * Returns a parameter list for the specified classification.
     * If there is no special parameter descriptor for the specified
     * classification, then a default descriptor is used.
     */
    static ParameterList getParameterList(final String classification) {
        return null; // TODO
//      return Naming.PROJECTIONS.getParameterList(classification,
//              MathTransformProvider.DEFAULT_PROJECTION_DESCRIPTOR);
    }
    
    /**
     * Initialize a list of parameter from the specified ellipsoid and points.
     *
     * @param parameters     The parameters to initialize.
     * @param ellipsoid      Ellipsoid parameter. If non-null, then <code>"semi_major"</code>
     *                       and <code>"semi_minor"</code> parameters will be set according.
     * @param centre         Central meridian and latitude of origin, in degrees.
     *                       If non-null, then <code>"central_meridian"</code> and
     *                       <code>"latitude_of_origin"</code> will be set according.
     * @param translation    False easting and northing, in metres. If non-null, then
     *                       <code>"false_easting"</code> and <code>"false_northing"</code>
     *                       will be set according.
     * @return               <code>parameters</code> for convenience.
     */
    static ParameterList init(final ParameterList parameters, final Ellipsoid ellipsoid, final Point2D centre, final Point2D translation) {
        if (ellipsoid!=null) {
            final Unit axisUnit = ellipsoid.getAxisUnit();
            parameters.setParameter("semi_major", Unit.METRE.convert(ellipsoid.getSemiMajorAxis(), axisUnit));
            parameters.setParameter("semi_minor", Unit.METRE.convert(ellipsoid.getSemiMinorAxis(), axisUnit));
        }
        if (centre!=null) {
            parameters.setParameter("central_meridian",   centre.getX());
            parameters.setParameter("latitude_of_origin", centre.getY());
        }
        if (translation!=null) {
            parameters.setParameter("false_easting",  translation.getX());
            parameters.setParameter("false_northing", translation.getY());
        }
        return parameters;
    }
    
    /**
     * Returns a clone of a parameter list.
     */
    private static ParameterList clone(final ParameterList list) {
        if (list==null) {
            return null;
        }
        final ParameterListDescriptor descriptor = list.getParameterListDescriptor();
        final ParameterList copy = new ParameterListImpl(descriptor);
        final String[] names = descriptor.getParamNames();
        if (names!=null) for (int i=0; i<names.length; i++) {
            final String name = names[i];
            copy.setParameter(name, list.getObjectParameter(name));
        }
        return copy;
    }
    
    /**
     * Gets the projection classification name (e.g. "Transverse_Mercator").
     *
     * @see org.opengis.cs.CS_Projection#getClassName()
     */
    public String getClassName() {
        return classification;
    }
    
    /**
     * Returns all parameters.
     *
     * @see org.opengis.cs.CS_Projection#getNumParameters()
     * @see org.opengis.cs.CS_Projection#getParameter(int)
     */
    public ParameterList getParameters() {
        return clone(parameters);
    }
    
    /**
     * Convenience method for fetching a parameter value.
     * Search is case-insensitive and ignore leading and trailing blanks.
     *
     * @param  name Parameter to look for.
     * @return The parameter value.
     * @throws MissingParameterException if parameter <code>name</code> is not found.
     */
    public double getValue(final String name) throws MissingParameterException {
        return getValue(parameters, name, Double.NaN, true);
    }
    
    /**
     * Convenience method for fetching a parameter value.
     * Search is case-insensitive and ignore leading and trailing blanks.
     *
     * @param  name Parameter to look for.
     * @param  defaultValue Default value to return if
     *         parameter <code>name</code> is not found.
     * @return The parameter value, or <code>defaultValue</code>
     *         if the parameter <code>name</code> is not found.
     */
    public double getValue(final String name, final double defaultValue) {
        return getValue(parameters, name, defaultValue, false);
    }
    
    /**
     * Convenience method for fetching a parameter value.
     * Search is case-insensitive and ignore leading and
     * trailing blanks.
     *
     * @param  parameters User-suplied parameters.
     * @param  name Parameter to look for.
     * @param  defaultValue Default value to return if
     *         parameter <code>name</code> is not found.
     * @param  required <code>true</code> if the parameter is required (in which case
     *         <code>defaultValue</code> is ignored), or <code>false</code> otherwise.
     * @return The parameter value, or <code>defaultValue</code> if the parameter is
     *         not found and <code>required</code> is <code>false</code>.
     * @throws MissingParameterException if <code>required</code> is <code>true</code>
     *         and parameter <code>name</code> is not found.
     */
    private static double getValue(final ParameterList parameters, String name, final double defaultValue, final boolean required) throws MissingParameterException {
        name = name.trim();
        RuntimeException cause=null;
        if (parameters!=null) {
            try {
                final Object value = parameters.getObjectParameter(name);
                if (value instanceof Number) {
                    // Do not require an instance of Double.
                    return ((Number) value).doubleValue();
                } else {
                    // May require an instance of Double. Will
                    // probably throw ClassCastException since
                    // the last try didn't worked.
                    return parameters.getDoubleParameter(name);
                }
            } catch (IllegalArgumentException exception) {
                // There is no parameter with the specified name.
                cause = exception;
            } catch (IllegalStateException exception) {
                // the parameter value is still NO_PARAMETER_DEFAULT
                cause = exception;
            }
        }
        if (!required) {
            return defaultValue;
        }
        final MissingParameterException exception = new MissingParameterException(null, name);
        if (cause!=null) {
            exception.initCause(cause);
        }
        throw exception;
    }
    
    /**
     * Returns a hash value for this projection.
     */
    public int hashCode() {
        int code = 45896321;
        if (classification!=null) code = code*37 + classification.hashCode();
        if (parameters    !=null) code = code*37 + parameters.hashCode();
        return code;
    }
    
    /**
     * Compares the specified object with
     * this projection for equality.
     */
    public boolean equals(final Object object) {
        if (super.equals(object)) {
            final Projection that = (Projection) object;
            return Utilities.equals(this.classification, that.classification) &&
                   Utilities.equals(this.parameters,     that.parameters);
        }
        return false;
    }
    
    /**
     * Fill the part inside "[...]".
     * Used for formatting Well Know Text (WKT).
     */
    String addString(final StringBuffer buffer, Object context) {
        return "PROJECTION";
    }
    
    /**
     * Returns an OpenGIS interface for this projection.
     * The returned object is suitable for RMI use.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid too early class loading of OpenGIS interface.
     */
    final Object toOpenGIS(final Object adapters) {
        return new Export(adapters);
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wrap a {@link Projection} object for use with OpenGIS.
     * This class is suitable for RMI use.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    private final class Export extends Info.Export implements CS_Projection {
        /**
         * The set of parameters. This array is constructed
         * only the first time it is needed.
         */
        private transient CS_ProjectionParameter[] parameters;
        
        /**
         * Construct a remote object.
         */
        protected Export(final Object adapters) {
            super(adapters);
        }
        
        /**
         * Gets number of parameters of the projection.
         */
        public int getNumParameters() throws RemoteException {
            final CS_ProjectionParameter[] parameters = getParameters();
            return (parameters!=null) ? parameters.length : 0;
        }
        
        /**
         * Gets an indexed parameter of the projection.
         */
        public CS_ProjectionParameter getParameter(final int index) throws RemoteException {
            final CS_ProjectionParameter[] parameters = getParameters();
            return (CS_ProjectionParameter) parameters[index].clone();
        }
        
        /**
         * Gets the projection classification name (e.g. 'Transverse_Mercator').
         */
        public String getClassName() throws RemoteException {
            return Projection.this.getClassName();
        }
        
        /**
         * Returns the set of parameters.
         */
        private synchronized CS_ProjectionParameter[] getParameters() {
            if (parameters==null) {
                parameters = adapters.export(Projection.this.getParameters());
            }
            return parameters;
        }
    }
}

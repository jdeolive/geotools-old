/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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

// J2SE dependencies
import java.rmi.server.UnicastRemoteObject;
import java.rmi.ServerException;
import java.rmi.RemoteException;
import java.sql.SQLException; // For JavaDoc
import java.io.IOException;   // For JavaDoc
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

// OpenGIS dependencies
import org.opengis.cs.CS_Ellipsoid;
import org.opengis.cs.CS_LinearUnit;
import org.opengis.cs.CS_AngularUnit;
import org.opengis.cs.CS_PrimeMeridian;
import org.opengis.cs.CS_VerticalDatum;
import org.opengis.cs.CS_HorizontalDatum;
import org.opengis.cs.CS_VerticalCoordinateSystem;
import org.opengis.cs.CS_HorizontalCoordinateSystem;
import org.opengis.cs.CS_GeographicCoordinateSystem;
import org.opengis.cs.CS_ProjectedCoordinateSystem;
import org.opengis.cs.CS_CompoundCoordinateSystem;
import org.opengis.cs.CS_CoordinateSystemAuthorityFactory;

// Geotools dependencies
import org.geotools.units.Unit;


/**
 * Creates spatial reference objects using codes.
 * The codes are maintained by an external authority.
 * A commonly used authority is EPSG, which is also
 * used in the GeoTIFF standard.
 *
 * @version $Id: CoordinateSystemAuthorityFactory.java,v 1.10 2003/05/13 10:58:47 desruisseaux Exp $
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory
 */
public abstract class CoordinateSystemAuthorityFactory {
    /**
     * The underlying factory used for objects creation.
     */
    protected final CoordinateSystemFactory factory;

    /**
     * OpenGIS object returned by {@link #toOpenGIS}.
     * It may be a hard or a weak reference.
     */
    private transient Object proxy;
    
    /**
     * Constructs an authority factory using the
     * specified coordinate system factory.
     *
     * @param factory The underlying factory used for objects creation.
     */
    public CoordinateSystemAuthorityFactory(final CoordinateSystemFactory factory) {
        Info.ensureNonNull("factory", factory);
        this.factory = factory;
    }
    
    /**
     * Returns the authority name.
     */
    public abstract String getAuthority();
    
    /**
     * Returns an arbitrary {@link Object} from a code. Subclasses can override
     * this method if they are capable to automatically detect the object type
     * from its code. The default implementation always throw an exception.
     *
     * @param  code Value allocated by authority.
     * @return The object.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see #createCoordinateSystem
     * @see #createDatum
     * @see #createEllipsoid
     * @see #createUnit
     */
    public Object createObject(String code) throws FactoryException {
        throw new NoSuchAuthorityCodeException("Object", code);
    }
    
    /**
     * Returns a {@link Unit} object from a code.
     *
     * @param  code Value allocated by authority.
     * @return The unit.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory#createLinearUnit
     * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory#createAngularUnit
     */
    public abstract Unit createUnit(String code) throws FactoryException;
    
    /**
     * Returns an {@link Ellipsoid} object from a code.
     *
     * @param  code Value allocated by authority.
     * @return The ellipsoid.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory#createEllipsoid
     */
    public abstract Ellipsoid createEllipsoid(String code) throws FactoryException;
    
    /**
     * Returns a {@link PrimeMeridian} object from a code.
     *
     * @param  code Value allocated by authority.
     * @return The prime meridian.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory#createPrimeMeridian
     */
    public abstract PrimeMeridian createPrimeMeridian(String code) throws FactoryException;
    
    /**
     * Returns a {@link Datum} object from a code.
     *
     * @param  code Value allocated by authority.
     * @return The datum.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see #createHorizontalDatum
     * @see #createVerticalDatum
     */
    public abstract Datum createDatum(String code) throws FactoryException;
    
    /**
     * Returns a {@link HorizontalDatum} object from a code.
     * The default implementation invokes {@link #createDatum}.
     *
     * @param  code Value allocated by authority.
     * @return The horizontal datum.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory#createHorizontalDatum
     */
    public HorizontalDatum createHorizontalDatum(String code) throws FactoryException {
        final Datum datum = createDatum(code);
        if (datum instanceof HorizontalDatum) {
            return (HorizontalDatum) datum;
        }
        throw new NoSuchAuthorityCodeException("HorizontalDatum", code);
    }
    
    /**
     * Returns a {@link VerticalDatum} object from a code.
     * The default implementation invokes {@link #createDatum}.
     *
     * @param  code Value allocated by authority.
     * @return The vertical datum.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory#createVerticalDatum
     */
    public VerticalDatum createVerticalDatum(String code) throws FactoryException {
        final Datum datum = createDatum(code);
        if (datum instanceof VerticalDatum) {
            return (VerticalDatum) datum;
        }
        throw new NoSuchAuthorityCodeException("VerticalDatum", code);
    }
    
    /**
     * Returns a {@link CoordinateSystem} object from a code.
     *
     * @param  code Value allocated by authority.
     * @return The coordinate system.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see #createHorizontalCoordinateSystem
     * @see #createGeographicCoordinateSystem
     * @see #createProjectedCoordinateSystem
     * @see #createVerticalCoordinateSystem
     * @see #createCompoundCoordinateSystem
     */
    public abstract CoordinateSystem createCoordinateSystem(String code) throws FactoryException;
    
    /**
     * Returns a {@link HorizontalCoordinateSystem} object from a code.
     * The default implementation invokes {@link #createCoordinateSystem}.
     *
     * @param  code Value allocated by authority.
     * @return The horizontal coordinate system.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory#createHorizontalCoordinateSystem
     */
    public HorizontalCoordinateSystem createHorizontalCoordinateSystem(String code) throws FactoryException {
        final CoordinateSystem cs = createCoordinateSystem(code);
        if (cs instanceof HorizontalCoordinateSystem) {
            return (HorizontalCoordinateSystem) cs;
        }
        throw new NoSuchAuthorityCodeException("HorizontalCoordinateSystem", code);
    }
    
    /**
     * Returns a {@link GeographicCoordinateSystem} object from a code.
     * The default implementation invokes {@link #createHorizontalCoordinateSystem}.
     *
     * @param  code Value allocated by authority.
     * @return The geographic coordinate system.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory#createGeographicCoordinateSystem
     */
    public GeographicCoordinateSystem createGeographicCoordinateSystem(String code) throws FactoryException {
        final HorizontalCoordinateSystem cs = createHorizontalCoordinateSystem(code);
        if (cs instanceof GeographicCoordinateSystem) {
            return (GeographicCoordinateSystem) cs;
        }
        throw new NoSuchAuthorityCodeException("GeographicCoordinateSystem", code);
    }
    
    /**
     * Returns a {@link ProjectedCoordinateSystem} object from a code.
     * The default implementation invokes {@link #createHorizontalCoordinateSystem}.
     *
     * @param  code Value allocated by authority.
     * @return The projected coordinate system.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory#createProjectedCoordinateSystem
     */
    public ProjectedCoordinateSystem createProjectedCoordinateSystem(String code) throws FactoryException {
        final HorizontalCoordinateSystem cs = createHorizontalCoordinateSystem(code);
        if (cs instanceof ProjectedCoordinateSystem) {
            return (ProjectedCoordinateSystem) cs;
        }
        throw new NoSuchAuthorityCodeException("ProjectedCoordinateSystem", code);
    }
    
    /**
     * Returns a {@link VerticalCoordinateSystem} object from a code.
     * The default implementation invokes {@link #createCoordinateSystem}.
     *
     * @param  code Value allocated by authority.
     * @return The vertical coordinate system.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory#createVerticalCoordinateSystem
     */
    public VerticalCoordinateSystem createVerticalCoordinateSystem(String code) throws FactoryException {
        final CoordinateSystem cs = createCoordinateSystem(code);
        if (cs instanceof VerticalCoordinateSystem) {
            return (VerticalCoordinateSystem) cs;
        }
        throw new NoSuchAuthorityCodeException("VerticalCoordinateSystem", code);
    }
    
    /**
     * Returns a {@link CompoundCoordinateSystem} object from a code.
     * The default implementation invokes {@link #createCoordinateSystem}.
     *
     * @param  code Value allocated by authority.
     * @return The compound coordinate system.
     * @throws NoSuchAuthorityCodeException if this method can't find the requested code.
     * @throws FactoryException if some other kind of failure occurred in the backing store.
     *         Most common failure causes include {@link SQLException} or {@link IOException}.
     *
     * @see org.opengis.cs.CS_CoordinateSystemAuthorityFactory#createCompoundCoordinateSystem
     */
    public CompoundCoordinateSystem createCompoundCoordinateSystem(String code) throws FactoryException {
        final CoordinateSystem cs = createCoordinateSystem(code);
        if (cs instanceof CompoundCoordinateSystem) {
            return (CompoundCoordinateSystem) cs;
        }
        throw new NoSuchAuthorityCodeException("CompoundCoordinateSystem", code);
    }

    /**
     * Releases resources immediately instead of waiting for the garbage collector.
     * Once a factory has been disposed, further <code>create(...)</code> invocations
     * may throw a {@link FactoryException}. Disposing a previously-disposed factory,
     * however, has no effect.
     *
     * @throws FactoryException if an error occured while disposing the factory.
     */
    public void dispose() throws FactoryException {
    }

    /**
     * Returns an OpenGIS interface for this info. This method first looks in the cache.
     * If no interface was previously cached, then this method creates a new adapter and
     * caches the result.
     *
     * Note: The returned type is a generic {@link Object} in order
     *       to avoid premature class loading of OpenGIS interface.
     *
     * @param  adapters The originating {@link Adapters}.
     * @return The OpenGIS interface for this info.
     * @throws RemoteException if the object can't be exported.
     */
    final synchronized Object toOpenGIS(final Object adapters) throws RemoteException {
        if (proxy != null) {
            if (proxy instanceof Reference) {
                final Object ref = ((Reference) proxy).get();
                if (ref != null) {
                    return ref;
                }
            } else {
                return proxy;
            }
        }
        final Object opengis = new Export(adapters);
        proxy = new WeakReference(opengis);
        return opengis;
    }
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wrap an {@link Info} object for use with OpenGIS. This wrapper is a
     * good place to check for non-implemented OpenGIS methods (just check
     * for methods throwing {@link UnsupportedOperationException}). This
     * class is suitable for RMI use.
     */
    private final class Export extends UnicastRemoteObject implements CS_CoordinateSystemAuthorityFactory {
        /**
         * The originating adapter.
         */
        protected final Adapters adapters;
        
        /**
         * Constructs a remote object.
         */
        protected Export(final Object adapters) throws RemoteException {
            super(); // TODO: Fetch the port number from the adapter.
            this.adapters = (Adapters)adapters;
        }
        
        /**
         * Returns the underlying implementation.
         */
        public CoordinateSystemAuthorityFactory getImplementation() {
            return CoordinateSystemAuthorityFactory.this;
        }
        
        /** Returns the authority name.
         */
        public String getAuthority() throws RemoteException {
            return CoordinateSystemAuthorityFactory.this.getAuthority();
        }
        
        /**
         * Returns an angular unit from a code.
         */
        public CS_AngularUnit createAngularUnit(String code) throws RemoteException {
            try {
                return (CS_AngularUnit) adapters.export(CoordinateSystemAuthorityFactory.this.createUnit(code));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Returns a linear unit from a code.
         */
        public CS_LinearUnit createLinearUnit(String code) throws RemoteException {
            try {
                return (CS_LinearUnit) adapters.export(CoordinateSystemAuthorityFactory.this.createUnit(code));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Returns an ellipsoid from a code.
         */
        public CS_Ellipsoid createEllipsoid(String code) throws RemoteException {
            try {
                return adapters.export(CoordinateSystemAuthorityFactory.this.createEllipsoid(code));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Returns a prime meridian object from a code.
         */
        public CS_PrimeMeridian createPrimeMeridian(String code) throws RemoteException {
            try {
                return adapters.export(CoordinateSystemAuthorityFactory.this.createPrimeMeridian(code));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Returns a horizontal datum from a code.
         */
        public CS_HorizontalDatum createHorizontalDatum(String code) throws RemoteException {
            try {
                return adapters.export(CoordinateSystemAuthorityFactory.this.createHorizontalDatum(code));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Creates a vertical datum from a code.
         */
        public CS_VerticalDatum createVerticalDatum(String code) throws RemoteException {
            try {
                return adapters.export(CoordinateSystemAuthorityFactory.this.createVerticalDatum(code));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }

        /**
         * Creates a horizontal coordinate system from a code.
         */
        public CS_HorizontalCoordinateSystem createHorizontalCoordinateSystem(String code) throws RemoteException {
            try {
                return adapters.export(CoordinateSystemAuthorityFactory.this.createHorizontalCoordinateSystem(code));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Returns a geographic coordinate system object from a code.
         */
        public CS_GeographicCoordinateSystem createGeographicCoordinateSystem(String code) throws RemoteException {
            try {
                return adapters.export(CoordinateSystemAuthorityFactory.this.createGeographicCoordinateSystem(code));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Returns a projected coordinate system object from a code.
         */
        public CS_ProjectedCoordinateSystem createProjectedCoordinateSystem(String code) throws RemoteException {
            try {
                return adapters.export(CoordinateSystemAuthorityFactory.this.createProjectedCoordinateSystem(code));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Create a vertical coordinate system from a code.
         */
        public CS_VerticalCoordinateSystem createVerticalCoordinateSystem(String code) throws RemoteException {
            try {
                return adapters.export(CoordinateSystemAuthorityFactory.this.createVerticalCoordinateSystem(code));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Creates a 3D coordinate system from a code.
         */
        public CS_CompoundCoordinateSystem createCompoundCoordinateSystem(String code) throws RemoteException {
            try {
                return adapters.export(CoordinateSystemAuthorityFactory.this.createCompoundCoordinateSystem(code));
            } catch (FactoryException exception) {
                throw Adapters.serverException(exception);
            }
        }
        
        /**
         * Gets a description of the object corresponding to a code.
         */
        public String descriptionText(String code) throws RemoteException {
            throw new UnsupportedOperationException("Description text not yet implemented");
        }
        
        /**
         * Gets the Geoid code from a WKT name.
         */
        public String geoidFromWKTName(String wkt) throws RemoteException {
            throw new UnsupportedOperationException("Geoid from WKT code not yet implemented");
        }
        
        /**
         * Gets the WKT name of a Geoid.
         */
        public String wktGeoidName(String geoid) throws RemoteException {
            throw new UnsupportedOperationException("WKT formatting not yet implemented");
        }
    }
}

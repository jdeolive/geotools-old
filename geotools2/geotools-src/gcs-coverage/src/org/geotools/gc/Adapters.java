/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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
import java.rmi.RemoteException;

// OpenGIS dependencies
import org.opengis.gc.GC_GridRange;
import org.opengis.gc.GC_GridGeometry;
import org.opengis.gc.GC_GridCoverage;
import org.opengis.cv.CV_Coverage;

// Geotools dependencies
import org.geotools.cv.Coverage;


/**
 * <FONT COLOR="#FF6633">Provide methods for interoperability
 * with <code>org.opengis.gc</code> package.</FONT>
 * All methods accept null argument.
 *
 * @version $Id: Adapters.java,v 1.2 2002/09/16 10:34:10 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see org.geotools.gp.Adapters#getDefault()
 */
public class Adapters extends org.geotools.cv.Adapters {
    /**
     * Default constructor. A shared instance of <code>Adapters</code> can
     * be obtained with {@link org.geotools.gp.Adapters#getDefault()}.
     *
     * @param CS The underlying adapters from the <code>org.geotools.ct</code> package.
     */
    protected Adapters(final org.geotools.ct.Adapters CT) {
        super(CT);
    }

    /**
     * Returns an OpenGIS interface for a grid range.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public GC_GridRange export(final GridRange range) {
        if (range == null) {
            return null;
        }
        return (GC_GridRange) range.toOpenGIS(this);
    }

    /**
     * Returns an OpenGIS interface for a grid geometry.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public GC_GridGeometry export(final GridGeometry geometry) {
        if (geometry == null) {
            return null;
        }
        return (GC_GridGeometry) geometry.toOpenGIS(this);
    }

    /**
     * Returns an OpenGIS interface for a grid coverage.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public GC_GridCoverage export(final GridCoverage coverage) {
        if (coverage == null) {
            return null;
        }
        final CV_Coverage candidate = getExported(coverage);
        if (candidate instanceof GC_GridCoverage) {
            return (GC_GridCoverage) candidate;
        }
        return coverage.new Export(this);
    }

    /**
     * Returns an OpenGIS interface for a coverage.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public CV_Coverage export(final Coverage coverage) {
        if (coverage instanceof GridCoverage) {
            return export((GridCoverage) coverage);
        } else {
            return super.export(coverage);
        }
    }

    /**
     * Returns a grid range from an OpenGIS's interface.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    public GridRange wrap(final GC_GridRange range) throws RemoteException {
        if (range == null) {
            return null;
        }
        if (range instanceof GridRange.Export) {
            return ((GridRange.Export) range).unwrap();
        }
        final GridRange result = new GridRange(range.getLo(), range.getHi());
        result.proxy = range;
        return result;
    }

    /**
     * Returns a grid geometry from an OpenGIS's interface.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    public GridGeometry wrap(final GC_GridGeometry geometry) throws RemoteException {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof GridGeometry.Export) {
            return ((GridGeometry.Export) geometry).unwrap();
        }
        final GridGeometry result = new GridGeometry(wrap(geometry.getGridRange()),
                                        CT.wrap(geometry.getGridToCoordinateSystem()));
        result.proxy = geometry;
        return result;
    }

    /**
     * Returns a grid coverage from an OpenGIS's interface.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    public GridCoverage wrap(final GC_GridCoverage coverage) throws RemoteException {
        if (coverage == null) {
            return null;
        }
        final Coverage candidate = getWrapped(coverage);
        if (candidate instanceof GridCoverage) {
            return (GridCoverage) candidate;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns a coverage from an OpenGIS's interface.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    public Coverage wrap(final CV_Coverage coverage) throws RemoteException {
        if (coverage instanceof GC_GridCoverage) {
            return wrap((GC_GridCoverage) coverage);
        } else {
            return super.wrap(coverage);
        }
    }
}

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
package org.geotools.cv;

// J2SE dependencies
import java.lang.ref.Reference;
import java.rmi.RemoteException;

// OpenGIS dependencies
import org.opengis.cv.CV_Coverage;
import org.opengis.cv.CV_SampleDimension;
import org.opengis.cv.CV_ColorInterpretation;


/**
 * <FONT COLOR="#FF6633">Provide methods for interoperability
 * with <code>org.opengis.cv</code> package.</FONT>
 * All methods accept null argument.
 *
 * @version $Id: Adapters.java,v 1.2 2002/09/16 10:34:10 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see org.geotools.gp.Adapters#getDefault()
 */
public class Adapters {
    /**
     * The underlying adapters from the <code>org.geotools.cs</code> package.
     */
    public final org.geotools.cs.Adapters CS;

    /**
     * The underlying adapters from the <code>org.geotools.ct</code> package.
     */
    public final org.geotools.ct.Adapters CT;

    /**
     * The underlying adapters from the <code>org.geotools.pt</code> package.
     */
    public final org.geotools.pt.Adapters PT;
    
    /**
     * Default constructor. A shared instance of <code>Adapters</code> can
     * be obtained with {@link org.geotools.gp.Adapters#getDefault()}.
     *
     * @param CS The underlying adapters from the <code>org.geotools.ct</code> package.
     */
    protected Adapters(final org.geotools.ct.Adapters CT) {
        this.CT = CT;
        this.CS = CT.CS;
        this.PT = CT.PT;
    }

    /**
     * Returns an OpenGIS enumeration for a color interpretation.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public CV_ColorInterpretation export(final ColorInterpretation colors) {
        if (colors == null) {
            return null;
        }
        return new CV_ColorInterpretation(colors.getValue());
    }

    /**
     * Returns an OpenGIS interface for a sample dimension.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public CV_SampleDimension export(final SampleDimension dimension) {
        if (dimension == null) {
            return null;
        }
        return (CV_SampleDimension) dimension.toOpenGIS(this);
    }

    /**
     * Returns an OpenGIS interface for a coverage.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public CV_Coverage export(final Coverage coverage) {
        if (coverage == null) {
            return null;
        }
        final CV_Coverage candidate = getExported(coverage);
        if (candidate != null) {
            return candidate;
        }
        return coverage.new Export(this);
    }

    /**
     * Returns a color interpretation from an OpenGIS's enumeration.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     */
    public ColorInterpretation wrap(final CV_ColorInterpretation colors) {
        return (colors!=null) ? ColorInterpretation.getEnum(colors.value) : null;
    }
    
    /**
     * Returns a sample dimension from an OpenGIS's interface.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    public SampleDimension wrap(final CV_SampleDimension dimension) throws RemoteException {
        if (dimension == null) {
            return null;
        }
        if (dimension instanceof SampleDimension.Export) {
            return ((SampleDimension.Export) dimension).unwrap();
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
        if (coverage == null) {
            return null;
        }
        final Coverage candidate = getWrapped(coverage);
        if (candidate != null) {
            return candidate;
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * If the specified {@link Coverage} has already been exported to an OpenGIS
     * {@link CV_Coverage} object, returns it. Otherwise, returns <code>null</code>.
     * This method may also returns <code>null</code> if the {@link CV_Coverage} object
     * has been garbage-collected.
     *
     * This method is usually invoked by {@link #export(Coverage)} before
     * to create a new OpenGIS interface, in order to reuse existing one.
     *
     * @param  coverage The Geotools's object.
     * @return The OpenGIS's interface, or <code>null</code> if none.
     */
    protected static CV_Coverage getExported(final Coverage coverage) {
        if (coverage != null) {
            Object proxy = coverage.proxy;
            if (proxy instanceof Reference) {
                proxy = ((Reference) proxy).get();
            }
            return (CV_Coverage) proxy;
        } else {
            return null;
        }
    }

    /**
     * If the specified {@link CV_Coverage} has already been wrapped into a Geotools
     * {@link Coverage} object, returns it. Otherwise, returns <code>null</code>.
     *
     * This method is usually invoked by {@link #wrap(CV_Coverage)} before
     * to create a new Geotools object, in order to reuse existing one.
     *
     * @param  coverage The OpenGIS's interface.
     * @return The Geotools's object, or <code>null</code> if none.
     *
     * @task TODO: We should use a WeakHashMap<CV_Coverage,Coverage> here.
     */
    protected static Coverage getWrapped(final CV_Coverage coverage) {
        if (coverage instanceof Coverage.Export) {
            return ((Coverage.Export) coverage).unwrap();
        } else {
            return null;
        }
    }
}

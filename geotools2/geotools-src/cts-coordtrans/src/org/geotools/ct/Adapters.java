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
package org.geotools.ct;

// OpenGIS dependencies
import org.opengis.ct.CT_Parameter;
import org.opengis.ct.CT_DomainFlags;
import org.opengis.ct.CT_TransformType;
import org.opengis.ct.CT_MathTransform;
import org.opengis.ct.CT_MathTransformFactory;
import org.opengis.ct.CT_CoordinateTransformation;
import org.opengis.ct.CT_CoordinateTransformationFactory;

// Geotools dependencies
import org.geotools.resources.XArray;
import org.geotools.cs.FactoryException;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemFactory;

// J2SE and JAI dependencies
import java.rmi.RemoteException;
import java.rmi.ServerException;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListImpl;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;


/**
 * <FONT COLOR="#FF6633">Provide methods for interoperability with
 * <code>org.opengis.ct</code> package.</FONT>  All methods accept
 * null argument. All OpenGIS objects are suitable for RMI use.
 *
 * @version $Id: Adapters.java,v 1.6 2003/05/13 10:58:48 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Adapters extends org.geotools.cs.Adapters {
    /**
     * Default adapters. Will be constructed
     * only when first requested.
     */
    private static Adapters DEFAULT;

    /**
     * Construct an adapter with default factories.
     */
    protected Adapters() {
        super();
    }

    /**
     * Construct an adapter with the specified factories.
     *
     * @param csFactory The factory to use for creating {@link CoordinateSystem} objects.
     * @param mtFactory The factory to use for creating {@link MathTransform} objects.
     */
//  protected Adapters(final CoordinateSystemFactory csFactory,
//                     final MathTransformFactory    mtFactory)
//  {
//      super(csFactory, mtFactory);
//  }
    
    /**
     * Gets the default adapters.
     */
    public static synchronized Adapters getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new Adapters();
        }
        return DEFAULT;
    }
    
    /**
     * Returns an OpenGIS interface for a math transform.
     * @throws RemoteException if the object can't be exported.
     */
    public CT_MathTransform export(final MathTransform transform) throws RemoteException {
        if (transform == null) {
            return null;
        }
        if (transform instanceof AbstractMathTransform) {
            final AbstractMathTransform atr = (AbstractMathTransform) transform;
            return (CT_MathTransform) atr.cachedOpenGIS(this);
        }
        // TODO: We don't have any cache mechanism for math transforms that are
        //       not AbstractMathTransform subclass (e.g. AffineTransform2D). A
        //       better solution would be to do the cache inside this Adapters,
        //       but we need something like a WeakHashMap with WeakReference on
        //       values rather than keys...
        return new MathTransformExport(this, transform);
    }
    
    /**
     * Returns an OpenGIS interface for a math transform.
     * @throws RemoteException if the object can't be exported.
     */
    public CT_CoordinateTransformation export(final CoordinateTransformation transform)
            throws RemoteException 
    {
        return (transform!=null) ? (CT_CoordinateTransformation)transform.cachedOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a math transform factory.
     * @throws RemoteException if the object can't be exported.
     */
    public CT_MathTransformFactory export(final MathTransformFactory factory)
            throws RemoteException
    {
        return (factory!=null) ? (CT_MathTransformFactory)factory.toOpenGIS(this) : null;
    }
    
    /**
     * Returns an OpenGIS interface for a coordinate transformation factory.
     * @throws RemoteException if the object can't be exported.
     */
    public CT_CoordinateTransformationFactory export(final CoordinateTransformationFactory factory)
            throws RemoteException 
    {
        return (factory!=null) ? (CT_CoordinateTransformationFactory)factory.toOpenGIS(this) : null;
    }
    
    /**
     * Construct an array of OpenGIS structure from a parameter list.
     */
    public CT_Parameter[] export(final ParameterList parameters) {
        if (parameters==null) {
            return null;
        }
        final String[] names = parameters.getParameterListDescriptor().getParamNames();
        final CT_Parameter[] param = new CT_Parameter[names!=null ? names.length : 0];
        int count=0;
        for (int i=0; i<param.length; i++) {
            final String name = names[i];
            final Object value;
            try {
                value = parameters.getObjectParameter(name);
            } catch (IllegalStateException exception) {
                // No value and no default. Ignore...
                continue;
            }
            if (value instanceof Number) {
                param[count++] = new CT_Parameter(name, ((Number)value).doubleValue());
            }
        }
        return (CT_Parameter[]) XArray.resize(param, count);
    }
    
    /**
     * Construct an OpenGIS enum from a transform type.
     */
    public CT_TransformType export(final TransformType type) {
        return (type!=null) ? new CT_TransformType(type.getValue()) : null;
    }
    
    /**
     * Construct an OpenGIS enum from a domain flag.
     */
    public CT_DomainFlags export(final DomainFlags flags) {
        return (flags!=null) ? new CT_DomainFlags(flags.getValue()) : null;
    }
    
    /**
     * Returns a math transform for an OpenGIS interface.
     * @throws RemoteException if a remote call failed.
     */
    public MathTransform wrap(final CT_MathTransform transform)
            throws RemoteException
    {
        if (transform==null) {
            return null;
        }
        if (transform instanceof MathTransformExport) {
            return ((MathTransformExport)transform).transform;
        }
        if (transform.getDimSource()==2 && transform.getDimTarget()==2) {
            return new MathTransformAdapter2D(transform);
        } else {
            return new MathTransformAdapter(transform);
        }
    }
    
    /**
     * Returns a coordinate transform for an OpenGIS interface.
     * @throws RemoteException if a remote call failed.
     */
    public CoordinateTransformation wrap(final CT_CoordinateTransformation transform)
            throws RemoteException
    {
        if (transform==null) {
            return null;
        }
        if (transform instanceof CoordinateTransformation.Export) {
            return (CoordinateTransformation) ((CoordinateTransformation.Export)transform).unwrap();
        }
        return new CoordinateTransformation(null,
                                            wrap(transform.getSourceCS()),
                                            wrap(transform.getTargetCS()),
                                            wrap(transform.getTransformType()),
                                            wrap(transform.getMathTransform()));
    }
    
    /**
     * Returns a parameter list for an array of OpenGIS structure.
     */
    public ParameterList wrap(final CT_Parameter[] parameters) {
        if (parameters==null) {
            return null;
        }
        int count=0;
        String[] paramNames   = new String[parameters.length];
        Class [] paramClasses = new Class [parameters.length];
        for (int i=0; i<parameters.length; i++) {
            final CT_Parameter param = parameters[i];
            if (param!=null) {
                paramNames  [count] = param.name;
                paramClasses[count] = Double.class;
                count++;
            }
        }
        paramNames   = (String[]) XArray.resize(paramNames,   count);
        paramClasses = (Class []) XArray.resize(paramClasses, count);
        final ParameterList list = new ParameterListImpl(new ParameterListDescriptorImpl(
                                            null, paramNames, paramClasses, null, null));
        for (int i=0; i<paramNames.length; i++) {
            list.setParameter(paramNames[i], parameters[i].value);
        }
        return list;
    }
    
    /**
     * Construct a transform type from an OpenGIS enum.
     */
    public TransformType wrap(final CT_TransformType type) {
        return (type!=null) ? TransformType.getEnum(type.value) : null;
    }
    
    /**
     * Construct a domain flag from an OpenGIS enum.
     */
    public DomainFlags wrap(final CT_DomainFlags flags) {
        return (flags!=null) ? DomainFlags.getEnum(flags.value) : null;
    }

    /**
     * Wrap a {@link FactoryException} into a {@link RemoteException}.
     */
    static RemoteException serverException(final FactoryException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof RemoteException) {
            return (RemoteException) cause;
        }
        return new ServerException("Can't create object", exception);
    }
}

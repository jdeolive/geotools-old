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

// OpenGIS dependencies
import org.opengis.ct.CT_TransformType;
import org.opengis.ct.CT_MathTransform;
import org.opengis.ct.CT_CoordinateTransformation;
import org.opengis.cs.CS_CoordinateSystem;

// Geotools dependencies
import org.geotools.cs.Info;
import org.geotools.cs.CoordinateSystem;
import org.geotools.resources.Utilities;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;

// J2SE dependencies
import java.util.Locale;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.lang.ref.WeakReference;
import java.lang.ref.Reference;


/**
 * Describes a coordinate transformation. A coordinate transformation class establishes
 * an association between a source and a target coordinate reference system, and provides
 * a {@link MathTransform} for transforming coordinates in the source coordinate reference
 * system to coordinates in the target coordinate reference system. These coordinate
 * systems can be ground or image coordinates. In general mathematics, "transformation"
 * is the general term for mappings between coordinate systems (see tensor analysis).
 * <br><br>
 * For a ground coordinate point, if the transformation depends only on mathematically
 * derived parameters (as in a cartographic projection), then this is an ISO conversion.
 * If the transformation depends on empirically derived parameters (as in datum
 * transformations), then this is an ISO transformation.
 *
 * @version 1.00
 * @author OpenGIS (www.opengis.org)
 * @author Martin Desruisseaux
 *
 * @see org.opengis.ct.CT_CoordinateTransformation
 */
public class CoordinateTransformation extends Info {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1850470924499685544L;
    
    /**
     * OpenGIS object returned by {@link #cachedOpenGIS}.
     * It may be a hard or a weak reference.
     */
    private transient Object proxy;
    
    /**
     * The source coordinate system.
     */
    private final CoordinateSystem sourceCS;
    
    /**
     * The destination coordinate system.
     */
    private final CoordinateSystem targetCS;
    
    /**
     * The transform type.
     */
    private final TransformType type;
    
    /**
     * The underlying math transform, or <code>null</code> if it
     * doesn't has been constructed yet.   If <code>null</code>,
     * then subclass <strong>must</strong> initialize this field
     * the first time {@link #getMathTransform} is invoked.
     */
    protected MathTransform transform;
    
    /**
     * The inverse transform. This field
     * will be computed only when needed.
     */
    transient CoordinateTransformation inverse;
    
    /**
     * Construct a coordinate transformation.
     *
     * @param name      The coordinate transformation name, or <code>null</code>
     *                  for an automatically generated name.
     * @param sourceCS  The source coordinate system.
     * @param targetCS  The destination coordinate system.
     * @param type      The transform type.
     * @param transform The math transform.  This argument is allowed to
     *                  be <code>null</code> only if this constructor is
     *                  invoked from within a subclass constructor. In
     *                  this case, the subclass <strong>must</strong>
     *                  construct a math transform no later than the first
     *                  time {@link #getMathTransform} is invoked.
     */
    public CoordinateTransformation(final String           name,
                                    final CoordinateSystem sourceCS,
                                    final CoordinateSystem targetCS,
                                    final TransformType    type,
                                    final MathTransform    transform)
    {
        super((name!=null) ? name : "");
        this.sourceCS  = sourceCS;
        this.targetCS  = targetCS;
        this.type      = type;
        this.transform = transform;
        ensureNonNull("sourceCS",  sourceCS);
        ensureNonNull("targetCS",  targetCS);
        ensureNonNull("type",      type);
        if (getClass().equals(CoordinateTransformation.class)) {
            ensureNonNull("transform", transform);
        }
        if (transform.getDimSource() != sourceCS.getDimension()) {
            throw new IllegalArgumentException("sourceCS");
        }
        if (transform.getDimTarget() != targetCS.getDimension()) {
            throw new IllegalArgumentException("targetCS");
        }
    }
    
    /**
     * Gets the name of this coordinate transformation.
     *
     * @param locale The desired locale, or <code>null</code> for the default locale.
     */
    public String getName(final Locale locale) {
        final String name = super.getName(locale);
        if (name.length()!=0) {
            return name;
        } else if (transform instanceof AbstractMathTransform) {
            return ((AbstractMathTransform) transform).getName(locale);
        } else {
            return sourceCS.getName(locale)+"\u00A0\u21E8\u00A0"+targetCS.getName(locale);
        }
    }
    
    /**
     * Gets the source coordinate system.
     *
     * @see org.opengis.ct.CT_CoordinateTransformation#getSourceCS()
     */
    public CoordinateSystem getSourceCS() {
        return sourceCS;
    }
    
    /**
     * Gets the target coordinate system.
     *
     * @see org.opengis.ct.CT_CoordinateTransformation#getTargetCS()
     */
    public CoordinateSystem getTargetCS() {
        return targetCS;
    }
    
    /**
     * Gets the semantic type of transform.
     * For example, a datum transformation or a coordinate conversion.
     *
     * @see org.opengis.ct.CT_CoordinateTransformation#getTransformType()
     */
    public TransformType getTransformType() {
        return type;
    }
    
    /**
     * Gets the math transform. The math transform will transform positions in
     * the source coordinate system into positions in the target coordinate system.
     *
     * @see org.opengis.ct.CT_CoordinateTransformation#getMathTransform()
     */
    public MathTransform getMathTransform() {
        if (transform!=null) {
            return transform;
        } else {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Returns the inverse transform of this object.
     */
    public synchronized CoordinateTransformation inverse()
            throws NoninvertibleTransformException
    {
        if (inverse==null) {
            inverse = new Inverse(this);
        }
        return inverse;
    }
    
    /**
     * The inverse coordinate transformation. This class override
     * {@link #getName} in order to delegate part of the call to
     * the underlying direct transformation.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    private static final class Inverse extends CoordinateTransformation {
        /**
         * Construct a coordinate transformation.
         */
        public Inverse(final CoordinateTransformation transform)
                throws NoninvertibleTransformException
        {
            super(null, transform.getTargetCS(), transform.getSourceCS(),
                  transform.getTransformType(), transform.getMathTransform().inverse());
            this.inverse = transform;
        }
        
        /**
         * Gets the name of this coordinate transformation.
         */
        public String getName(final Locale locale) {
            return Resources.getResources(locale).getString(
                    ResourceKeys.INVERSE_$1, this.inverse.getName(locale));
        }
    }
    
    /**
     * Returns a hash value for this
     * coordinate transformation.
     */
    public int hashCode() {
        int code = 7851236;
        CoordinateSystem cs;
        if ((cs=getSourceCS()) != null) code = code*37 + cs.hashCode();
        if ((cs=getTargetCS()) != null) code = code*37 + cs.hashCode();
        return code;
    }
    
    /**
     * Compares the specified object with this coordinate transformation
     * for equality.  The default implementation compare name, transform
     * type, source and target coordinate systems. It doesn't compare the
     * math transform, since it should be equivalents if the above mentionned
     * parameters are equal.
     */
    public boolean equals(final Object object) {
        if (object==this) {
            return true;
        }
        if (super.equals(object)) {
            final CoordinateTransformation that = (CoordinateTransformation) object;
            return Utilities.equals(this.getTransformType(), that.getTransformType()) &&
                   Utilities.equals(this.getSourceCS(),      that.getSourceCS()     ) &&
                   Utilities.equals(this.getTargetCS(),      that.getTargetCS()     );
        }
        return false;
    }
    
    /**
     * Returns an OpenGIS interface for this math transform.
     * The returned object is suitable for RMI use.
     *
     * Note 1: The returned type is a generic {@link Object} in order
     *         to avoid too early class loading of OpenGIS interface.
     *
     * Note 2: We do NOT want this method to override Info.toOpenGIS(),
     *         since the returned object do not implements CS_Info. The
     *         package-private access do the trick.
     */
    Object toOpenGIS(final Object adapters) {
        return new Export(adapters);
    }
    
    /**
     * Returns an OpenGIS interface for this info.
     * This method first look in the cache. If no
     * interface was previously cached, then this
     * method create a new adapter  and cache the
     * result.
     *
     * @param adapters The originating {@link Adapters}.
     */
    final synchronized Object cachedOpenGIS(final Object adapters) {
        if (proxy!=null) {
            if (proxy instanceof Reference) {
                final Object ref = ((Reference) proxy).get();
                if (ref!=null) {
                    return ref;
                }
            } else {
                return proxy;
            }
        }
        final Object opengis = toOpenGIS(adapters);
        proxy = new WeakReference(opengis);
        return opengis;
    }
    
    
    
    
    
    /////////////////////////////////////////////////////////////////////////
    ////////////////                                         ////////////////
    ////////////////             OPENGIS ADAPTER             ////////////////
    ////////////////                                         ////////////////
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Wrap a {@link CoordinateTransformation} for use with OpenGIS.
     * This wrapper is a good place to check for non-implemented
     * OpenGIS methods (just check for methods throwing
     * {@link UnsupportedOperationException}). This class
     * is suitable for RMI use.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    final class Export extends RemoteObject implements CT_CoordinateTransformation {
        /**
         * The originating adapter.
         */
        protected final Adapters adapters;
        
        /**
         * Construct a remote object.
         */
        protected Export(final Object adapters) {
            this.adapters = (Adapters)adapters;
        }
        
        /**
         * Returns the underlying math transform.
         */
        public final CoordinateTransformation unwrap() {
            return CoordinateTransformation.this;
        }
        
        /**
         * Name of transformation.
         */
        public String getName() throws RemoteException {
            return CoordinateTransformation.this.getName(null);
        }
        
        /**
         * Authority which defined transformation and parameter values.
         */
        public String getAuthority() throws RemoteException {
            return CoordinateTransformation.this.getAuthority(null);
        }
        
        /**
         * Code used by authority to identify transformation.
         */
        public String getAuthorityCode() throws RemoteException {
            return CoordinateTransformation.this.getAuthorityCode(null);
        }
        
        /**
         * Gets the provider-supplied remarks.
         */
        public String getRemarks() throws RemoteException {
            return CoordinateTransformation.this.getRemarks(null);
        }
        
        /**
         * Human readable description of domain in source coordinate system.
         */
        public String getAreaOfUse() throws RemoteException {
            throw new UnsupportedOperationException("Area of use not yet implemented");
        }
        
        /**
         * Semantic type of transform.
         */
        public CT_TransformType getTransformType() throws RemoteException {
            return adapters.export(CoordinateTransformation.this.getTransformType());
        }
        
        /**
         * Source coordinate system.
         */
        public CS_CoordinateSystem getSourceCS() throws RemoteException {
            return adapters.CS.export(CoordinateTransformation.this.getSourceCS());
        }
        
        /**
         * Target coordinate system.
         */
        public CS_CoordinateSystem getTargetCS() throws RemoteException {
            return adapters.CS.export(CoordinateTransformation.this.getTargetCS());
        }
        
        /**
         * Gets math transform.
         */
        public CT_MathTransform getMathTransform() throws RemoteException {
            return adapters.export(CoordinateTransformation.this.getMathTransform());
        }
    }
}

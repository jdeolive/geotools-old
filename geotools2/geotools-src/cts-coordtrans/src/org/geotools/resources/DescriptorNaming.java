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
 */
package org.geotools.resources;

// Collections
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;

// Reflection
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

// Parameters and JAI utilities
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListImpl;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.util.CaselessStringKey;

// Resources
import org.geotools.units.Unit;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;


/**
 * Methods for binding names to {@link ParameterListDescriptor}s. For example,
 * {@link org.geotools.cs.Projection} using this class for binding classification
 * name to parameter list descriptors.
 *
 * @version $Id: DescriptorNaming.java,v 1.7 2003/08/04 17:11:18 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class DescriptorNaming {
    /**
     * The naming to use for mapping projection's
     * classification name to parameter descriptor.
     */
    public static final DescriptorNaming PROJECTIONS = new DescriptorNaming(
                "org.geotools.ct.proj.Provider", "org.geotools.ct");

    /**
     * The parameters using linear units.
     * @see #getParameterUnit
     */
    private static final String[] METRES = {
        "semi_major",
        "semi_minor",
        "false_easting",
        "false_northing"
    };

    /**
     * The parameters using angular units.
     * @see #getParameterUnit
     */
    private static final String[] DEGREES = {
        "central_meridian",
        "latitude_of_origin"
    };

    /**
     * The parameters using dimensionless units.
     * @see #getParameterUnit
     */
    private static final String[] DIMENSIONLESS = {
        "scale_factor"
    };

    /**
     * Map classification name to {@link ParameterListDescriptor}
     * objects. Keys are {@link CaselessStringKey} object, while
     * values are {@link ParameterListDescriptor} objects.
     */
    private Map descriptors;

    /**
     * The fully qualified name of the class to load for initializing binding,
     * or <code>null</code> if none. If non-null, then the static initializer
     * of this class should invokes {@link #bind} for binding a default set of
     * descriptors.
     */
    private final String initializer;

    /**
     * The logger to use if initialization failed.
     */
    private final String logger;

    /**
     * Construct a <code>DescriptorNaming</code> object.
     *
     * @param initializer The fully qualified name of the class
     *                    to load for initializing binding.
     * @param logger The logger to use if initialization failed.
     */
    private DescriptorNaming(final String initializer, final String logger) {
        this.initializer = initializer;
        this.logger      = logger;
    }

    /**
     * Try to bind a set of default projections. Those default projections are binded
     * during the static initialization of {@link org.geotools.ct.proj.Provider} class.
     * If the operation fail, a warning is logged but the process continue.
     */
    private void bindDefaults(final String method) {
        try {
            final  Class c = Class.forName(initializer);
            final Method m = c.getMethod("getDefault", null);
            m.invoke(null, null);
        } catch (ClassNotFoundException exception) {
            Utilities.unexpectedException(logger, "DescriptorNaming", method, exception);
        } catch (NoSuchMethodException exception) {
            // No "getDefault()" static method. Ignore...
        } catch (IllegalAccessException exception) {
            // The method is not public. Treat it as if they were no "getDefault()" method.
        } catch (InvocationTargetException exception) {
            Utilities.unexpectedException(logger, "DescriptorNaming", method, exception);
        }
    }

    /**
     * Binds a classification name to a parameter list descriptor.
     *
     * @param  classification The classification name.
     * @param  descriptor the parameter list descriptor.
     * @throws IllegalArgumentException if a descriptor is already
     *         bounds for the specified classification name.
     */
    public synchronized void bind(final String classification,
                                  final ParameterListDescriptor descriptor)
        throws IllegalArgumentException
    {
        if (descriptors == null) {
            descriptors = new HashMap();
            bindDefaults("bind");
        }
        final CaselessStringKey key = new CaselessStringKey(classification);
        if (descriptors.containsKey(key)) {
            throw new IllegalArgumentException(Resources.format(
                      ResourceKeys.ERROR_OPERATION_ALREADY_BOUNDS_$1, classification));
        }
        descriptors.put(key, descriptor);
    }

    /**
     * Returns a default parameter descriptor
     * for the specified classification name,
     * or <code>null</code> if none is found.
     *
     * @param  classification The classification to look for.
     * @return The descriptor for the specified classification,
     *         or <code>null</code> if none.
     */
    public synchronized ParameterListDescriptor lookup(final String classification) {
        if (descriptors == null) {
            descriptors = new HashMap();
            bindDefaults("lookup");
        }
        return (ParameterListDescriptor) descriptors.get(new CaselessStringKey(classification));
    }

    /**
     * Returns a parameter list for the specified classification. If
     * there is no explicit parameter descriptor for the specified
     * classification, then a default descriptor is used.
     *
     * @param  classification The classification to look for.
     * @param  fallback The default parameter list descriptor to use if no
     *         descriptor has been found for the specified classification.
     * @return A parameter list to use for the specified classification
     */
    public ParameterList getParameterList(final String classification,
                                          final ParameterListDescriptor fallback)
    {
        ParameterListDescriptor descriptor = lookup(classification);
        if (descriptor == null) {
            descriptor = fallback;
        }
        return new ParameterListImpl(descriptor);
    }

    /**
     * Returns the list of classification names.
     */
    public synchronized String[] list() {
        if (descriptors == null) {
            descriptors = new HashMap();
            bindDefaults("list");
        }
        int count = 0;
        final String[] names = new String[descriptors.size()];
        for (final Iterator it=descriptors.keySet().iterator(); it.hasNext();) {
            names[count++] = it.next().toString();
        }
        assert count == names.length;
        return names;
    }

    /**
     * Checks if two {@link ParameterListDescriptor} are equal.  This method is
     * a workaround, because the default {@link ParameterListDescriptor} do not
     * overrides {@link Object#equals}.
     */
    public static boolean equals(final ParameterListDescriptor d1,
                                 final ParameterListDescriptor d2)
    {
        if (d1 == d2) {
            return true;
        }
        if (d1==null || d2==null || !Utilities.equals(d1.getClass(), d2.getClass())) {
            return false;
        }
        final String[] names = d1.getParamNames();
        if (!Arrays.equals(names,                 d2.getParamNames   ()) &&
             Arrays.equals(d1.getParamClasses(),  d2.getParamClasses ()) &&
             Arrays.equals(d1.getParamDefaults(), d2.getParamDefaults()))
        {
            return false;
        }
        for (int i=0; i<names.length; i++) {
            final String name = names[i];
            if (!Utilities.equals(d1.getParamValueRange(name), d2.getParamValueRange(name))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if two {@link ParameterList} are equal.  This method is
     * a workaround, because the default {@link ParameterList} do not
     * overrides {@link Object#equals}.
     */
    public static boolean equals(final ParameterList p1,
                                 final ParameterList p2)
    {
        if (p1 == p2) {
            return true;
        }
        if (p1==null || p2==null || !Utilities.equals(p1.getClass(), p2.getClass())) {
            return false;
        }
        final ParameterListDescriptor desc = p1.getParameterListDescriptor();
        if (!equals(desc, p2.getParameterListDescriptor())) {
            return false;
        }
        final String[] names = desc.getParamNames();
        for (int i=0; i<names.length; i++) {
            Object o1;
            try {
                o1 = p1.getObjectParameter(names[i]);
            } catch (IllegalStateException e) {
                o1 = ParameterListDescriptor.NO_PARAMETER_DEFAULT;
            }
            Object o2;
            try {
                o2 = p2.getObjectParameter(names[i]);
            } catch (IllegalStateException e) {
                o2 = ParameterListDescriptor.NO_PARAMETER_DEFAULT;
            }
            if (!Utilities.equals(o1, o2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the specified array contains
     * the specified name. Comparaisons are case-insensitive.
     */
    private static boolean contains(final String[] array, final String name) {
        for (int i=0; i<array.length; i++) {
            if (name.equalsIgnoreCase(array[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the unit for the specified parameter.
     * This method returns one of the following:
     * <ul>
     *   <li>If the specified parameter is a linear measure, then this method returns
     *       {@link Unit#METRE}. Other linear units are not authorized.</li>
     *   <li>If the specified parameter is an angular measure, then this method returns
     *       {@link Unit#DEGREE}. Other angular units are not authorized.</li>
     *   <li>Otherwise, this method may returns {@link Unit#DIMENSIONLESS} or
     *       <code>null</code>.</li>
     * </ul>
     *
     * @param  name The parameter name.
     * @return The parameter unit, or <code>null</code>.
     */
    public static Unit getParameterUnit(final String name) {
        if (contains(METRES,        name)) return Unit.METRE;
        if (contains(DEGREES,       name)) return Unit.DEGREE;
        if (contains(DIMENSIONLESS, name)) return Unit.DIMENSIONLESS;
        return null;
    }
}

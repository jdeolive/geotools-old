/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
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
 */
package org.geotools.gp;

// J2SE dependencies
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

// JAI dependencies
import javax.media.jai.util.Range;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListImpl;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.EnumeratedParameter;

// Geotools dependencies
import org.geotools.gc.GridCoverage;


/**
 * A JAI operation which accepts an arbitrary number of sources.
 *
 * @version $Id: PolyadicOperation.java,v 1.4 2003/10/20 14:35:48 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
class PolyadicOperation extends OperationJAI {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8936249190166828917L;

    /**
     * Construct an operation from a JAI operation name.
     *
     * @param operationName JAI operation name.
     */
    public PolyadicOperation(final String operationName) {
        super(operationName);
    }
    
    /**
     * Returns the number of source grid coverages required for the operation.
     * Since this operation accepts an arbitrary amount of sources, this method
     * returns the <cite>minimal</cite> number of source grid coverages required.
     */
    public int getNumSources() {
        return super.getNumSources();
    }
    
    /**
     * Returns source name for the specified parameters.
     */
    String[] getSourceNames(final ParameterList parameters) {
        if (parameters instanceof DynamicParameterList) {
            return ((DynamicParameterList) parameters).getSourceNames();
        }
        return super.getSourceNames(parameters);
    }
    
    /**
     * Returns a default parameter list for this operation. This method returns a special
     * instance of {@link ParameterList} which accept an arbitrary number of sources.
     */
    public ParameterList getParameterList() {
        return new DynamicParameterList(getParameterListDescriptor());
    }

    /**
     * A {@link ParameterList} which accepts an arbitrary number of sources.
     * This parameter list is also its own descriptor.
     *
     * @version $Id: PolyadicOperation.java,v 1.4 2003/10/20 14:35:48 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class DynamicParameterList extends ParameterListImpl
                                                    implements ParameterListDescriptor
    {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 1110818299138716246L;

        /**
         * The prefix for &quot;Source&quot; argument name.
         */
        private static final String SOURCE = "Source";

        /**
         * The sources.
         */
        private final List sources = new ArrayList();

        /**
         * Constructs a parameter list.
         */
        public DynamicParameterList(final ParameterListDescriptor descriptor) {
            super(descriptor);
        }

        /**
         * Returns the source indice for the specified source name, or -1 if the specified
         * name is not a source name.
         */
        private static int getSourceIndex(final String name) {
            if (name.regionMatches(true, 0, SOURCE, 0, SOURCE.length())) try {
                return Integer.parseInt(name.substring(SOURCE.length()));
            } catch (NumberFormatException exception) {
                /*
                 * The source number can't be parsed. Ignore the exception; we will
                 * assume that the parameter name is not a source.   If this is not
                 * true, an IllegalArgumentException will be throws later, which is
                 * a more correct exception type.
                 */
            }
            return -1;
        }

        /**
         * Returns the associated parameter list descriptor. This method returns a special
         * instance with a variable number of sources, defined as the number of sources
         * actually added to this {@link ParameterList}.
         */
        public ParameterListDescriptor getParameterListDescriptor() {
            return this;
        }

        /**
         * Sets a named parameter to the given value. If the parameter name starts with
         * &quot;Source&quot; followed by a number, then it is assumed to be a source and
         * will be set only if the previous source were added. For example &quot;Source3&quot;
         * is valid only if &quot;Source2&quot; was previously set.
         */
        public ParameterList setParameter(final String name, final Object value) {
            final int index = getSourceIndex(name);
            if (index >= 0) {
                final int length = sources.size();
                if (value == null) {
                    /*
                     * A null value remove the source. We allow the removal of the last source
                     * only, because removing previous sources would shift the next sources by
                     * one index (i.e. change some source name, which is probably not the expected
                     * behavior for most users).
                     */
                    if (index == length-1) {
                        sources.remove(index);
                        return this;
                    }
                } else if (index <= length) {
                    if (index == length) {
                        sources.add(value);
                    } else {
                        sources.set(index, value);
                    }
                    return this;
                }
            }
            return super.setParameter(name, value);
        }

        /**
         * Gets a named parameter as an object. 
         */
        public Object getObjectParameter(final String name) {
            final int index = getSourceIndex(name);
            if (index>=0 && index<sources.size()) {
                return sources.get(index);
            }
            return super.getObjectParameter(name);
        }

        /**
         * Returns the total number of parameters, including sources added.
         */
        public int getNumParameters() {
            return super.getParameterListDescriptor().getNumParameters() + sources.size();
        }

        /**
         * Returns an array of classes that describe the types of parameters, including sources.
         * This array length depends on the number of sources added.
         */
        public Class[] getParamClasses() {
            final int    numSources = sources.size();
            final Class[] arguments = super.getParameterListDescriptor().getParamClasses();
            final Class[]     types = new Class[arguments.length + numSources];
            System.arraycopy(arguments, 0, types, numSources, arguments.length);
            Arrays.fill(types, 0, numSources, GridCoverage.class);
            return types;
        }

        /**
         * Returns an array names of the parameters associated with this descriptor,
         * including sources. This array length depends on the number of sources added.
         */
        public String[] getParamNames() {
            return getNames(super.getParameterListDescriptor().getParamNames());
        }

        /**
         * Returns an array of source names associated with this descriptor.
         * This array length depends on the number of sources added.
         */
        public String[] getSourceNames() {
            return getNames(new String[0]);
        }

        /**
         * Returns an array names of the parameters associated with this descriptor,
         * including sources. This method create the array of sources and append the
         * given array of parameters.
         */
        private String[] getNames(final String[] arguments) {
            final int      numSources = sources.size();
            final String[]      names = new String[arguments.length + numSources];
            final StringBuffer buffer = new StringBuffer(SOURCE);
            for (int i=0; i<numSources; i++) {
                buffer.setLength(SOURCE.length());
                buffer.append(i);
                names[i] = buffer.toString();
            }
            System.arraycopy(arguments, 0, names, numSources, arguments.length);
            return names;
        }

        /**
         * Returns an array of objects that define the default values of the parameters,
         * including sources. This array length depends on the number of sources added.
         */
        public Object[] getParamDefaults() {
            final int     numSources = sources.size();
            final Object[] arguments = super.getParameterListDescriptor().getParamClasses();
            final Object[]  defaults = new Object[arguments.length + numSources];
            System.arraycopy(arguments, 0, defaults, numSources, arguments.length);
            Arrays.fill(defaults, 0, numSources, ParameterListDescriptor.NO_PARAMETER_DEFAULT);
            return defaults;
        }

        /**
         * Returns the default value of a specified parameter. 
         */
        public Object getParamDefaultValue(final String name) {
            if (getSourceIndex(name) >= 0) {
                return ParameterListDescriptor.NO_PARAMETER_DEFAULT;
            }
            return super.getParameterListDescriptor().getParamDefaultValue(name);
        }

        /**
         * Returns the Range that represents the range of valid values for the specified parameter. 
         */
        public Range getParamValueRange(final String name) {
            if (getSourceIndex(name) >= 0) {
                return null;
            }
            return super.getParameterListDescriptor().getParamValueRange(name);
        }

        /**
         * {@link inheritDoc}
         */
        public String[] getEnumeratedParameterNames() {
            return super.getParameterListDescriptor().getEnumeratedParameterNames();
        }

        /**
         * {@link inheritDoc}
         */
        public EnumeratedParameter[] getEnumeratedParameterValues(final String name) {
            if (getSourceIndex(name) >= 0) {
                return null;
            }
            return super.getParameterListDescriptor().getEnumeratedParameterValues(name);
        }

        /**
         * Checks to see if the specified parameter can take on the specified value.
         */
        public boolean isParameterValueValid(final String name, final Object value) {
            if (getSourceIndex(name) >= 0) {
                return value instanceof GridCoverage;
            }
            return super.getParameterListDescriptor().isParameterValueValid(name, value);
        }
    }
}

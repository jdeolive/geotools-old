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
package org.geotools.gp;

// Collections
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.AbstractSet;

// Parameters
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.Interpolation;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListImpl;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.util.CaselessStringKey;

// Miscellaneous
import java.io.Writer;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.awt.RenderingHints;

// Geotools dependencies
import org.geotools.gc.GridCoverage;
import org.geotools.ct.CoordinateTransformationFactory;
import org.geotools.resources.Arguments;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Allows for different ways of accessing the grid coverage values.
 * Using one of these operations to change the way the grid is being
 * accessed will not affect the state of the grid coverage controlled
 * by another operations. For example, changing the interpolation method
 * should not affect the number of sample dimensions currently being
 * accessed or value sequence.
 *
 * @version $Id: GridCoverageProcessor.java,v 1.15 2003/03/14 18:27:36 desruisseaux Exp $
 * @author <a href="www.opengis.org">OpenGIS</a>
 * @author Martin Desruisseaux
 */
public class GridCoverageProcessor {
    /**
     * Augment the amout of memory
     * allocated for the tile cache.
     */
    static {
        final long targetCapacity = 0x4000000; // 64 Mo.
        if (Runtime.getRuntime().maxMemory() > 2*targetCapacity) {
            final TileCache cache = JAI.getDefaultInstance().getTileCache();
            if (cache.getMemoryCapacity() < targetCapacity) {
                cache.setMemoryCapacity(targetCapacity);
            }
        }
        Logger.getLogger("org.geotools.gp").config("Java Advanced Imaging: "+JAI.getBuildVersion());
    }
    
    /**
     * The default grid coverage processor. Will
     * be constructed only when first requested.
     */
    private static GridCoverageProcessor DEFAULT;
    
    /**
     * The set of operation for this grid coverage processor.
     * Keys are operation's name. Values are operations and
     * should not contains duplicated values.
     * <br><br>
     * Generic-Type: <CaselessStringKey,Operation>
     */
    private final Map operations = new HashMap();

    /**
     * The rendering hints for JAI operations, or <code>null</code> if none.
     * This field is usually given as argument to {@link OperationJAI} methods.
     */
    private final RenderingHints hints;
    
    /**
     * Construct a grid coverage processor with no operation and using the
     * default {@link JAI} instance. Operations can be added by invoking
     * the {@link #addOperation} method at construction time.
     */
    protected GridCoverageProcessor() {
        hints = null;
    }

    /**
     * Construct a grid coverage processor initialized with the same set of operations than the
     * specified processor. The rendering hints are initialized to the union of the rendering
     * hints of the specified processor, and the specified rendering hints. More operations can
     * be added by invoking the {@link #addOperation} method at construction time.
     *
     * @param processor The processor to inherit from, or <code>null</code> if none.
     * @param hints A set of supplemental rendering hints, or <code>null</code> if none.
     */
    public GridCoverageProcessor(final GridCoverageProcessor processor, RenderingHints hints) {
        if (hints != null) {
            hints = new RenderingHints(hints);
        }
        if (processor != null) {
            operations.putAll(processor.operations);
            if (hints == null) {
                hints = processor.hints;
            } else if (processor.hints != null) {
                hints.add(processor.hints);
            }
        }
        this.hints = hints;
    }
    
    /**
     * Returns the default grid coverage processor.
     */
    public static synchronized GridCoverageProcessor getDefault() {
        if (DEFAULT==null) {
            DEFAULT = new GridCoverageProcessor();
            DEFAULT.addOperation(new Interpolator.Operation());
            DEFAULT.addOperation(new Resampler.Operation());
            DEFAULT.addOperation(new GradientMagnitude());
            DEFAULT.addOperation(new OperationJAI("Rescale"));
            DEFAULT.addOperation(new RecolorOperation());
            DEFAULT.addOperation(new GradualColormapOperation());
            DEFAULT.addOperation(new SelectSampleDimension.Operation());
        }
        return DEFAULT;
    }
    
    /**
     * Add the specified operation to this processor. This method is usually invoked
     * at construction time <strong>before</strong> this processor is made accessible.
     * Once accessible, all <code>GridCoverageProcessor</code> instances should be
     * immutable.
     *
     * @param operation The operation to add.
     * @param IllegalStateException if an operation already exists
     *        with the same name than <code>operation</code>.
     */
    protected synchronized void addOperation(final Operation operation) throws IllegalStateException {
        final CaselessStringKey name = new CaselessStringKey(operation.getName());
        if (!operations.containsKey(name)) {
            assert !operations.containsValue(operation);
            operations.put(name, operation);
        }
        else throw new IllegalStateException(Resources.format(ResourceKeys.ERROR_OPERATION_ALREADY_BOUND_$1, operation.getName()));
    }
    
    /**
     * Retrieve grid processing operation informations. The operation information
     * will contain the name of the operation as well as a list of its parameters.
     */
    public synchronized Operation[] getOperations() {
        return (Operation[]) operations.values().toArray(new Operation[operations.size()]);
    }
    
    /**
     * Returns the operation for the specified name.
     *
     * @param  operationName Name of the operation.
     * @return The operation for the given name.
     * @throws OperationNotFoundException if there is no operation for the specified name.
     */
    public Operation getOperation(final String name) throws OperationNotFoundException {
        final Operation operation = (Operation) operations.get(new CaselessStringKey(name));
        if (operation!=null) {
            return operation;
        }
        throw new OperationNotFoundException(Resources.format(
                ResourceKeys.ERROR_OPERATION_NOT_FOUND_$1, name));
    }
    
    /**
     * Convenience method applying a process operation with default parameters.
     *
     * @param  operationName Name of the operation to be applied to the grid coverage..
     * @param  source The source grid coverage.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     *
     * @see #doOperation(Operation,ParameterList)
     */
    public GridCoverage doOperation(final String operationName, final GridCoverage source)
        throws OperationNotFoundException
    {
        final Operation operation = getOperation(operationName);
        return doOperation(operation, operation.getParameterList()
                .setParameter("Source", source));
    }
    
    /**
     * Convenience method applying a process operation with one parameter.
     *
     * @param  operationName  Name of the operation to be applied to the grid coverage..
     * @param  source         The source grid coverage.
     * @param  argumentName1  The name of the first parameter to set.
     * @param  argumentValue1 The value for the first parameter.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     * @throws IllegalArgumentException if there is no parameter with the specified name.
     *
     * @see #doOperation(Operation,ParameterList)
     */
    public GridCoverage doOperation(final String operationName, final GridCoverage source,
                                    final String argumentName1, final Object argumentValue1)
        throws OperationNotFoundException, IllegalArgumentException
    {
        final Operation operation = getOperation(operationName);
        return doOperation(operation, operation.getParameterList()
                .setParameter("Source", source)
                .setParameter(argumentName1, argumentValue1));
    }
    
    /**
     * Convenience method applying a process operation with two parameters.
     *
     * @param  operationName  Name of the operation to be applied to the grid coverage..
     * @param  source         The source grid coverage.
     * @param  argumentName1  The name of the first parameter to set.
     * @param  argumentValue1 The value for the first parameter.
     * @param  argumentName2  The name of the second parameter to set.
     * @param  argumentValue2 The value for the second parameter.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     * @throws IllegalArgumentException if there is no parameter with the specified name.
     *
     * @see #doOperation(Operation,ParameterList)
     */
    public GridCoverage doOperation(final String operationName, final GridCoverage source,
                                    final String argumentName1, final Object argumentValue1,
                                    final String argumentName2, final Object argumentValue2)
    throws OperationNotFoundException, IllegalArgumentException
    {
        final Operation operation = getOperation(operationName);
        return doOperation(operation, operation.getParameterList()
                .setParameter("Source", source)
                .setParameter(argumentName1, argumentValue1)
                .setParameter(argumentName2, argumentValue2));
    }
    
    /**
     * Convenience method applying a process operation with three parameters.
     *
     * @param  operationName  Name of the operation to be applied to the grid coverage..
     * @param  source         The source grid coverage.
     * @param  argumentName1  The name of the first parameter to set.
     * @param  argumentValue1 The value for the first parameter.
     * @param  argumentName2  The name of the second parameter to set.
     * @param  argumentValue2 The value for the second parameter.
     * @param  argumentName3  The name of the third parameter to set.
     * @param  argumentValu32 The value for the third parameter.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     * @throws IllegalArgumentException if there is no parameter with the specified name.
     *
     * @see #doOperation(Operation,ParameterList)
     */
    public GridCoverage doOperation(final String operationName, final GridCoverage source,
                                    final String argumentName1, final Object argumentValue1,
                                    final String argumentName2, final Object argumentValue2,
                                    final String argumentName3, final Object argumentValue3)
    throws OperationNotFoundException, IllegalArgumentException
    {
        final Operation operation = getOperation(operationName);
        return doOperation(operation, operation.getParameterList()
                .setParameter("Source", source)
                .setParameter(argumentName1, argumentValue1)
                .setParameter(argumentName2, argumentValue2)
                .setParameter(argumentName3, argumentValue3));
    }
    
    /**
     * Apply a process operation to a grid coverage.
     *
     * @param  operationName Name of the operation to be applied to the grid coverage.
     * @param  parameters List of name value pairs for the parameters required for the operation.
     *         The easiest way to construct this list is to invoke <code>{@link #getOperation
     *         getOperation}(name).{@link Operation#getParameterList getParameterList}()</code>
     *         and to modify the returned list.
     * @return The result as a grid coverage.
     * @throws OperationNotFoundException if there is no operation named <code>operationName</code>.
     */
    public GridCoverage doOperation(final String operationName, final ParameterList parameters)
        throws OperationNotFoundException
    {
        return doOperation(getOperation(operationName), parameters);
    }
    
    /**
     * Apply a process operation to a grid coverage. Default implementation
     * checks if source coverages use an interpolation,    and then invokes
     * {@link Operation#doOperation}. If all source coverages used the same
     * interpolation, the same interpolation is applied to the resulting
     * coverage (except if the resulting coverage has already an interpolation).
     *
     * @param  operation The operation to be applied to the grid coverage.
     * @param  parameters List of name value pairs for the parameters required for
     *         the operation.  The easiest way to construct this list is to invoke
     *         <code>operation.{@link Operation#getParameterList getParameterList}()</code>
     *         and to modify the returned list.
     * @return The result as a grid coverage.
     */
    public GridCoverage doOperation(final Operation operation, final ParameterList parameters) {
        Interpolation[] interpolations = null;
        final String operationName = operation.getName();
        if (!operationName.equalsIgnoreCase("Interpolate")) {
            final String[] paramNames = parameters.getParameterListDescriptor().getParamNames();
            for (int i=0; i<paramNames.length; i++) {
                final Object param = parameters.getObjectParameter(paramNames[i]);
                if (param instanceof Interpolator) {
                    // If all sources use the same interpolation,  preserve the
                    // interpolation for the resulting coverage. Otherwise, use
                    // the default interpolation (nearest neighbor).
                    final Interpolation[] interp = ((Interpolator) param).getInterpolations();
                    if (interpolations == null) {
                        interpolations = interp;
                    } else if (!Arrays.equals(interpolations, interp)) {
                        // Set to no interpolation.
                        interpolations = null;
                        break;
                    }
                }
            }
        }
        GridCoverage coverage = operation.doOperation(parameters, hints);
        if (interpolations!=null && coverage!=null && !(coverage instanceof Interpolator)) {
            coverage = Interpolator.create(coverage, interpolations);
        }
        // Check if the coverage has changed (i.e. if the operation
        // really did something). If the coverage has changed, then
        // an information message will be logger.
        GridCoverage source;
        try {
            source = (GridCoverage) parameters.getObjectParameter("Source");
        } catch (RuntimeException exception) {
            // "Source" parameter may not exists. Conservatively
            // assume that the operation did some usefull work.
            source = null;
        }
        if (coverage != source) {
            String interp = "Nearest";
            if (coverage instanceof Interpolator) {
                interp = ((Interpolator)coverage).getInterpolationName();
            }
            final Locale locale = null; // Set locale here (if any).
            final LogRecord record = Resources.getResources(locale).getLogRecord(
                                     Level.FINE, ResourceKeys.OPERATION_APPLIED_$3,
                                     ((source!=null) ? source : coverage).getName(locale),
                                     operationName, interp);
            record.setSourceClassName("GridCoverageProcessor");
            record.setSourceMethodName("doOperation");
            Logger.getLogger("org.geotools.gp").log(record);
        }
        return coverage;
    }
    
    /**
     * Print a description of all operations to the specified stream.
     * The description include operation names and lists of parameters.
     *
     * @param  out The destination stream.
     * @throws IOException if an error occured will writing to the stream.
     */
    public void print(final Writer out) throws IOException {
        final String  lineSeparator = System.getProperty("line.separator", "\n");
        final Operation[] operations = getOperations();
        for (int i=0; i<operations.length; i++) {
            out.write(lineSeparator);
            operations[i].print(out, null);
        }
    }

    /**
     * Dumps to the standard output stream a list of operations for the default
     * {@link GridCoverageProcessor}. This method can been invoked from the
     * command line. For example:
     *
     * <blockquote><pre>
     * java org.geotools.gc.GridCoverageProcessor
     * </pre></blockquote>
     *
     * <strong>Note to Windows users:</strong> If the output contains strange
     * symbols, try to supply an "<code>-encoding</code>" arguments. Example:
     *
     * <blockquote><pre>
     * java org.geotools.gc.GridCoverageProcessor -encoding Cp850
     * </pre></blockquote>
     *
     * The codepage number (850 in the previous example) can be obtained from the DOS
     * commande line by entering the "<code>chcp</code>" command with no arguments.
     */
    public static void main(final String[] args) {
        final Arguments arguments = new Arguments(args);
        arguments.getRemainingArguments(0);
        try {
            getDefault().print(arguments.out);
        } catch (IOException exception) {
            // Should not occurs
            exception.printStackTrace(arguments.out);
        }
        arguments.out.close();
    }
}

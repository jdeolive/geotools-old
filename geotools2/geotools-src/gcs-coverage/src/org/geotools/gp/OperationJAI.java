/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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

// J2SE dependencies
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.MissingResourceException;
import java.util.Locale;
import java.util.Arrays;

// Java Advanced Imaging
import javax.media.jai.JAI;
import javax.media.jai.util.Range;
import javax.media.jai.ImageLayout;
import javax.media.jai.ParameterList;
import javax.media.jai.IntegerSequence;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

// Geotools dependencies
import org.geotools.pt.Envelope;
import org.geotools.cs.FactoryException;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CompoundCoordinateSystem;
import org.geotools.ct.CoordinateTransformationFactory;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.MathTransform;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridRange;
import org.geotools.gc.GridGeometry;
import org.geotools.gc.GridCoverage;
import org.geotools.gc.InvalidGridGeometryException;

// Resources
import org.geotools.units.Unit;
import org.geotools.resources.Utilities;
import org.geotools.resources.JAIUtilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.GCSUtilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;
import org.geotools.resources.ImageUtilities;


/**
 * Wrap an {@link OperationDescriptor} for interoperability with
 * <A HREF="http://java.sun.com/products/java-media/jai/">Java Advanced Imaging</A>.
 * This class help to leverage the rich set of JAI operators in an OpenGIS framework.
 * <code>OperationJAI</code> inherits operation name and argument types from
 * {@link OperationDescriptor}, except source argument type which is set to
 * <code>GridCoverage.class</code>. If there is only one source argument, il will be
 * renamed <code>&quot;Source&quot;</code> for better compliance to OpenGIS usage.
 * <br><br>
 * The entry point for applying operation is the usual {@link #doOperation doOperation}
 * method. The default implementation forward the call to other methods for
 * different bits of tasks, resulting in the following chain of calls:
 *
 * <ol>
 *   <li>{@link #doOperation doOperation} (the entry point)</li>
 *   <li>{@link #resampleToCommonGeometry resampleToCommonGeometry}
 *       (reproject all source to the same coordinate system)</li>
 *   <li>{@link #deriveGridCoverage deriveGridCoverage} (gets the destination properties)</li>
 *   <li>{@link #deriveSampleDimension} (gets the destination sample dimensions)</li>
 *   <li>{@link #deriveCategory} (gets the destination categories)</li>
 *   <li>{@link #deriveUnit} (gets the destination units)</li>
 *   <li>{@link #createRenderedImage} (the actual call to {@link JAI#createNS JAI.createNS})</li>
 * </ol>
 *
 * @version $Id: OperationJAI.java,v 1.26 2003/08/03 20:15:03 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class OperationJAI extends Operation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5974520239347639965L;

    /**
     * The rendered mode for JAI operation.
     */
    private static final String RENDERED_MODE = RenderedRegistryMode.MODE_NAME;
    
    /**
     * Index of the source {@link GridCoverage} to use as a model. The
     * destination grid coverage will reuse the same coordinate system,
     * envelope and qualitative categories than this "master" source.
     * <br><br>
     * For operations expecting only one source, there is no ambiguity.
     * But for operations expecting more than one source, the choice of
     * a "master" source is somewhat arbitrary.   This constant is used
     * merely as a flag for spotting those places in the code.
     */
    private static final int MASTER_SOURCE_INDEX = 0;
    
    /**
     * The operation descriptor.
     */
    protected final OperationDescriptor descriptor;
    
    /**
     * Construct an OpenGIS operation from a JAI operation name. This convenience constructor
     * fetch the {@link OperationDescriptor} from the specified operation name using the
     * default {@link JAI} instance.
     *
     * @param operationName JAI operation name (e.g. &quot;GradientMagnitude&quot;).
     */
    public OperationJAI(final String operationName) {
        this(getOperationDescriptor(operationName));
    }
    
    /**
     * Construct an OpenGIS operation from a JAI operation descriptor.
     * The OpenGIS operation will have the same name than the JAI operation.
     *
     * @param descriptor The operation descriptor. This descriptor
     *        must supports the "rendered" mode (which is the case
     *        for most JAI operations).
     */
    public OperationJAI(final OperationDescriptor descriptor) {
        this(null, descriptor, null);
    }

    /**
     * Construct an OpenGIS operation backed by a JAI operation.
     *
     * @param name The operation name for {@link GridCoverageProcessor} registration.
     *        May or may not be the same than the JAI operation name. If <code>null</code>,
     *        then the JAI operation name is used.
     * @param operationDescriptor The operation descriptor. This descriptor must supports
     *        supports the &quot;rendered&quot; mode (which is the case for most JAI operations).
     * @param paramDescriptor The parameters descriptor. If <code>null</code>,
     *        then it will be infered from the JAI's parameter descriptor.
     *
     * @throws NullPointerException if <code>operationDescriptor</code> is null.
     */
    protected OperationJAI(final String name,
                           final OperationDescriptor operationDescriptor,
                           final ParameterListDescriptor paramDescriptor)
    {
        super((name!=null) ? name : getName(operationDescriptor),
              (paramDescriptor!=null) ? paramDescriptor
                                      : getParameterListDescriptor(operationDescriptor));
        this.descriptor = operationDescriptor;
    }

    /**
     * Returns a name from the specified operation descriptor. If the
     * name begin with "org.geotools" prefix, the prefix will be ignored.
     *
     * @task TODO: Should be inlined in the constructor if only Sun was to fix RFE #4093999
     *             ("Relax constraint on placement of this()/super() call in constructors").
     */
    private static String getName(final OperationDescriptor descriptor) {
        final String prefix = "org.geotools.";
        String name = descriptor.getName();
        if (name.startsWith(prefix)) {
            name = name.substring(prefix.length());
        }
        return name;
    }

    /**
     * Returns the operation descriptor for the specified JAI operation name.
     * This method uses the default {@link JAI} instance and looks for the
     * <code>&quot;rendered&quot;</code> mode.
     */
    protected static OperationDescriptor getOperationDescriptor(final String name) {
        return (OperationDescriptor) JAI.getDefaultInstance().
                getOperationRegistry().getDescriptor(RENDERED_MODE, name);
    }
    
    /**
     * Gets the parameter list descriptor for an operation descriptor.
     * {@link OperationDescriptor} parameter list do not include sources.
     * This method will add them in front of the parameter list.
     */
    private static ParameterListDescriptor getParameterListDescriptor
                                (final OperationDescriptor descriptor)
    {
        ensureValid(descriptor.getDestClass(RENDERED_MODE));
        final Class[] sourceClasses = descriptor.getSourceClasses(RENDERED_MODE);
        if (sourceClasses != null) {
            for (int i=0; i<sourceClasses.length; i++) {
                ensureValid(sourceClasses[i]);
            }
        }
        final ParameterListDescriptor parent = descriptor.getParameterListDescriptor(RENDERED_MODE);
        final String[] sourceNames    = getSourceNames(descriptor);
        final String[] parentNames    = parent.getParamNames();
        final Class [] parentClasses  = parent.getParamClasses();
        final Object[] parentDefaults = parent.getParamDefaults();
        
        final int    numSources = descriptor.getNumSources();
        final String[]    names = new String[length(parentNames   ) + numSources];
        final Class []  classes = new Class [length(parentClasses ) + numSources];
        final Object[] defaults = new Object[length(parentDefaults) + numSources];
        final Range[]    ranges = new Range [defaults.length];
        for (int i=0; i<ranges.length; i++) {
            if (i<numSources) {
                names   [i] = sourceNames[i];
                classes [i] = GridCoverage.class;
                defaults[i] = ParameterListDescriptor.NO_PARAMETER_DEFAULT;
            } else {
                names   [i] = parentNames   [i-numSources];
                classes [i] = parentClasses [i-numSources];
                defaults[i] = parentDefaults[i-numSources];
                ranges  [i] = parent.getParamValueRange(names[i]);
            }
            // Convert the first letter to upper-case, for consistency with OGC convention.
            String name = names[i];
            if (name.length()!=0) {
                final char c = name.charAt(0);
                if (!Character.isUpperCase(c)) {
                    names[i] = Character.toUpperCase(c) + name.substring(1);
                }
            }
        }
        return new ParameterListDescriptorImpl(descriptor, names, classes, defaults, ranges);
    }
    
    /**
     * Vérifie que la classe spécifiée implémente l'interface {@link RenderedImage}.
     * Cette méthode est utilisée pour vérifier les classes des images sources et
     * destinations.
     */
    private static final void ensureValid(final Class classe) throws IllegalArgumentException {
        if (!RenderedImage.class.isAssignableFrom(classe)) {
            throw new IllegalArgumentException(classe.getName());
        }
    }

    /**
     * Returns the array length, of 0 if the array is <code>null</code>.
     */
    private static int length(final Object[] array) {
        return (array!=null) ? array.length : 0;
    }

    /**
     * Returns the coordinate system for the specified coverage. Since the default implementation
     * of {@link GridCoverage} ignores all dimensions after the 2 first, this method returns only
     * the coordinate system for the 2 first dimensions.
     *
     * @param  coverage The grid coverage to get the coordinate system.
     * @return The coordinate system for the 2 first dimensions.
     * @throws InvalidGridGeometryException if this method can't get the CS for the two first
     *         dimensions.
     */
    private static CoordinateSystem getCoordinateSystem(final GridCoverage coverage)
            throws InvalidGridGeometryException
    {
        CoordinateSystem cs = coverage.getCoordinateSystem();
        cs = CTSUtilities.getSubCoordinateSystem(cs, 0, 2);
        if (cs != null) {
            return cs;
        }
        // TODO: provides a localized message.
        throw new InvalidGridGeometryException();
    }
    
    /**
     * Check if array <code>names</code> contains the element <code>name</code>.
     * Search is done in case-insensitive manner. This method is efficient enough
     * if <code>names</code> is very short (less than 10 entries).
     */
    private static boolean contains(final String[] names, final String name) {
        for (int i=0; i<names.length; i++) {
            if (name.equalsIgnoreCase(names[i])) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns source name for the specified descriptor. If the descriptor has
     * only one source,  it will be renamed "Source" for better conformance to
     * to OpenGIS usage.
     */
    private static String[] getSourceNames(final OperationDescriptor descriptor) {
        if (descriptor.getNumSources() == 1) {
            return new String[] {"Source"};
        } else {
            return descriptor.getSourceNames();
        }
    }
    
    /**
     * Returns source name for the specified parameters. The default implementation ignores
     * the parameters and fetch the sources from the {@link #descriptor} only.  This method
     * is overrided by operations which accept an arbitrary number of sources, like
     * {@link PolyadicOperation}
     */
    String[] getSourceNames(final ParameterList parameters) {
        return getSourceNames(descriptor);
    }
    
    /**
     * Returns the description of the processing operation. If there is no description,
     * returns <code>null</code>. The default implementation fetch the description from
     * the {@linkplain #descriptor}.
     *
     * @param locale The desired locale, or <code>null</code> for the default locale.
     */
    public String getDescription(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        try {
            return descriptor.getResourceBundle(locale).getString("Description");
        } catch (MissingResourceException exception) {
            // No description available. Returns 'null', which is
            // a legal value according this method specification.
            return null;
        }
    }
    
    /**
     * Returns the number of source grid coverages required for the operation. The
     * default implementation fetch the information from the {@linkplain #descriptor}.
     */
    public int getNumSources() {
        return descriptor.getNumSources();
    }

    /**
     * Set a parameter. This method can be overriden in order to apply some conversions
     * from OpenGIS to JAI parameters.
     *
     * @param block The parameter block in which to set a parameter.
     * @param name  The parameter OpenGIS name.
     * @param value The parameter OpenGIS value.
     */
    void setParameter(final ParameterBlockJAI block, final String name, final Object value) {
        block.setParameter(name, value);
    }

    /**
     * Returns <code>true</code> if grid coverage should be transformed from sample values
     * to geophysics value before to apply an operation.
     */
    boolean computeOnGeophysicsValues() {
        return true;
    }

    /**
     * Apply a process operation to a grid coverage.
     * The default implementation performs the following steps:
     *
     * <ul>
     *   <li>Convert source grid coverages to their <cite>geophysics</cite> view, using
     *       <code>{@link GridCoverage#geophysics GridCoverage.geophysics}(true)</code>.
     *       This allow to performs all computation on geophysics values instead of encoded
     *       samples.</li>
     *   <li>Ensure that every source <code>GridCoverage</code>s use the same coordinate
     *       system and have the same envelope.</li>
     *   <li>Invoke {@link #deriveGridCoverage}.
     *       The sources in the <code>ParameterBlock</code> are {@link RenderedImage} objects
     *       obtained from {@link GridCoverage#getRenderedImage()}.</li>
     *   <li>If the source <code>GridCoverage</code>s was not a geophysics view, convert the
     *       result back to the same type with
     *       <code>{@link GridCoverage#geophysics GridCoverage.geophysics}(false)</code>.</li>
     * </ul>
     *
     * @param  parameters List of name value pairs for the parameters required for the operation.
     * @param  hints A set of rendering hints, or <code>null</code> if none.
     * @return The result as a grid coverage.
     *
     * @see #deriveGridCoverage
     */
    protected GridCoverage doOperation(final ParameterList  parameters,
                                       final RenderingHints hints)
    {
        /*
         * Copy parameter values from the ParameterList to the ParameterBlockJAI.
         * The sources GridCoverages are extracted in the process and the source
         * RenderedImage are set in the ParameterBlockJAI. The first array of
         * range specifiers, if any, is treated especialy.
         */
        RangeSpecifier[]        ranges = null;
        Boolean  requireGeophysicsType = null;
        final ParameterBlockJAI  block = new ParameterBlockJAI(descriptor, RENDERED_MODE);
        final String[]      paramNames = parameters.getParameterListDescriptor().getParamNames();
        final String[] blockParamNames = block.getParameterListDescriptor().getParamNames();
        final String[]     sourceNames = getSourceNames(parameters);
        final GridCoverage[]   sources = new GridCoverage[length(sourceNames)];
        for (int srcCount=0,i=0; i<paramNames.length; i++) {
            final String name  = paramNames[i];
            final Object value = parameters.getObjectParameter(name);
            if (contains(sourceNames, name)) {
                GridCoverage source = (GridCoverage) value;
                if (computeOnGeophysicsValues()) {
                    final GridCoverage old = source;
                    source = source.geophysics(true);
                    if (srcCount == MASTER_SOURCE_INDEX) {
                        requireGeophysicsType = Boolean.valueOf(old==source);
                    }
                }
                sources[srcCount++] = source;
                continue;
            }
            if (!contains(blockParamNames, name)) {
                if (value == null) {
                    continue;
                }
                if (value instanceof RangeSpecifier) {
                    ranges = new RangeSpecifier[] {(RangeSpecifier)value};
                    continue;
                }
                if (value instanceof RangeSpecifier[]) {
                    ranges = (RangeSpecifier[]) value;
                    continue;
                }
            }
            setParameter(block, name, value);
        }
        /*
         * Ensures that all coverages use the same coordinate system and has the same envelope.
         * After the projection, the method still checks all CS in case the user overrided the
         * {@link #resampleToCommonGeometry} method.
         */
        resampleToCommonGeometry(sources, null, null, hints);
        GridCoverage          coverage = sources[MASTER_SOURCE_INDEX];
        final CoordinateSystem      cs = getCoordinateSystem(coverage);
        final MathTransform2D gridToCS = coverage.getGridGeometry().getGridToCoordinateSystem2D();
        for (int i=0; i<sources.length; i++) {
            final GridCoverage source = sources[i];
            if (!cs.equals(getCoordinateSystem(source), false) ||
                !gridToCS.equals(source.getGridGeometry().getGridToCoordinateSystem2D()))
            {
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_INCOMPATIBLE_GRID_GEOMETRY));
            }
            block.addSource(source.getRenderedImage());
        }
        /*
         * Apply the operation. This delegates the work to the chain of 'deriveXXX' methods.
         */
        coverage = deriveGridCoverage(sources, new Parameters(cs, gridToCS, block, hints, ranges));
        if (requireGeophysicsType != null) {
            coverage = coverage.geophysics(requireGeophysicsType.booleanValue());
        }
        return coverage;
    }

    /**
     * Resample all sources grid coverages to the same geometry before to apply an operation.
     * Subclasses should overrides this method if they want to specify a target grid geometry
     * and coordinate system different than the default one.
     *
     * @param  sources  The source grid coverages to resample.
     *                  This array is updated in-place as needed.
     * @param  coordinateSystem The target coordinate system to use,
     *                  or <code>null</code> for a default one.
     * @param  gridToCS The target &quot;grid to coordinate system&quot; transform,
     *                  or <code>null</code> for a default one.
     * @param  hints    The rendering hints, or <code>null</code> if none.
     *
     * @throws InvalidGridGeometryException if a source coverage has an unsupported grid geometry.
     * @throws CannotReprojectException if a grid coverage can't be resampled for some other reason.
     */
    protected void resampleToCommonGeometry(final GridCoverage[] sources,
                                            CoordinateSystem     coordinateSystem,
                                            MathTransform        gridToCS,
                                            final RenderingHints hints)
            throws InvalidGridGeometryException, CannotReprojectException
    {
        if (sources.length == 0) {
            return;
        }
        if (coordinateSystem == null) {
            if (gridToCS==null && sources.length==1) {
                return;
            }
            coordinateSystem = getCoordinateSystem(sources[MASTER_SOURCE_INDEX]);
        }
        if (gridToCS == null) {
            gridToCS = sources[MASTER_SOURCE_INDEX].getGridGeometry().getGridToCoordinateSystem2D();
        }
        final int dimension = coordinateSystem.getDimension(); // Usually 2.
        final GridGeometry geometry = new GridGeometry(null, gridToCS);
        final GridCoverageProcessor processor = getGridCoverageProcessor(hints);
        for (int i=0; i<sources.length; i++) {
            final GridCoverage     source   = sources[i];
            final CoordinateSystem sourceCS = source.getCoordinateSystem();
            final CoordinateSystem targetCS;
            final GridGeometry     targetGeom;
            final int sourceDim = sourceCS.getDimension();
            if (sourceDim <= dimension) {
                targetCS   = coordinateSystem;
                targetGeom = geometry;
            } else {
                /*
                 * The source grid coverage has some extra dimensions. Uses the common CS
                 * for the two first dimensions, and try to preserve the extra dimensions.
                 */
                final CoordinateSystem headCS,tailCS;
                headCS = CTSUtilities.getSubCoordinateSystem(sourceCS, 0, dimension);
                tailCS = CTSUtilities.getSubCoordinateSystem(sourceCS, dimension, sourceDim);
                if (headCS==null || tailCS==null) {
                    // TODO: provides a localized message.
                    throw new InvalidGridGeometryException();
                }
                if (coordinateSystem.equals(headCS, false)) {
                    targetCS = sourceCS;
                } else {
                    targetCS = new CompoundCoordinateSystem(coordinateSystem.getName(null),
                                                            coordinateSystem, tailCS);
                }
                /*
                 * Constructs the 'gridToCoordinateSystem' transform in the same way than the CS:
                 * the first dimensions are set to the specified transform, and the extra dimensions
                 * are preserved.
                 */
                MathTransformFactory factory = null;
                final Object ctsFactory = hints.get(Hints.COORDINATE_TRANSFORMATION_FACTORY);
                if (ctsFactory instanceof CoordinateTransformationFactory) {
                    factory = ((CoordinateTransformationFactory)ctsFactory).getMathTransformFactory();
                }
                if (factory == null) {
                    factory = MathTransformFactory.getDefault();
                }
                try {
                    MathTransform transform = source.getGridGeometry().getGridToCoordinateSystem();
                    IntegerSequence headDim = JAIUtilities.createSequence(0, dimension-1);
                    IntegerSequence tailDim = JAIUtilities.createSequence(dimension, sourceDim-1);
                    MathTransform   headTr  = factory.createSubTransform(transform, headDim, null);
                    MathTransform   tailTr  = factory.createSubTransform(transform, tailDim, null);
                    if (!headTr.equals(gridToCS)) {
                        headTr = factory.createPassThroughTransform(0, headTr, sourceDim-dimension);
                        tailTr = factory.createPassThroughTransform(dimension, tailTr, 0);
                        transform = factory.createConcatenatedTransform(headTr, tailTr);
                    }
                    targetGeom = new GridGeometry(null, transform);
                } catch (FactoryException exception) {
                    throw new CannotReprojectException(Resources.format(
                            ResourceKeys.ERROR_CANT_REPROJECT_$1,
                            source.getName(null)), exception);
                }
            }
            sources[i] = processor.doOperation("Resample",         source,
                                               "GridGeometry",     targetGeom,
                                               "CoordinateSystem", targetCS);
        }
    }
    
    /**
     * Apply a JAI operation to a grid coverage.
     * The default implementation performs the following steps:
     *
     * <ul>
     *   <li>Get the {@link SampleDimension}s for the target images by invoking the
     *       {@link #deriveSampleDimension deriveSampleDimension(...)} method.</li>
     *   <li>Apply the operation using the following pseudo-code:
     * <blockquote><pre>
     * {@link JAI#createNS JAI.createNS}({@link #descriptor}.getName(),&nbsp;parameters,&nbsp;hints)
     * </pre></blockquote></li>
     * </ul>
     *
     * @param  sources The source coverages.
     * @param  parameters Parameters, rendering hints and coordinate system to use.
     * @return The result as a grid coverage.
     *
     * @see #doOperation
     * @see #deriveSampleDimension
     * @see JAI#createNS
     */
    protected GridCoverage deriveGridCoverage(final GridCoverage[] sources,
                                              final Parameters  parameters)
    {
        GridCoverage source = sources[MASTER_SOURCE_INDEX];
        /*
         * Get the target SampleDimensions. If they are identical to the SampleDimensions of
         * one of the source GridCoverage, then this GridCoverage will be used at the master
         * source. It will affect the target GridCoverage's name and the visible band. Then,
         * a new color model will be constructed from the new SampleDimensions, taking in
         * account the visible band.
         */
        final SampleDimension[][] list = new SampleDimension[sources.length][];
        for (int i=0; i<list.length; i++) {
            list[i] = sources[i].getSampleDimensions();
        }
        final SampleDimension[] sampleDims = deriveSampleDimension(list, parameters);
        for (int i=0; i<list.length; i++) {
            if (Arrays.equals(sampleDims, list[i])) {
                source = sources[i];
                break;
            }
        }
        /*
         * Set the rendering hints image layout. Only the following properties will be set:
         *
         *     - Color model
         *     - Tile width
         *     - Tile height
         */
        RenderingHints hints = ImageUtilities.getRenderingHints(parameters.getSource());
        ImageLayout   layout = (hints!=null) ? (ImageLayout)hints.get(JAI.KEY_IMAGE_LAYOUT) : null;
        if (layout==null || !layout.isValid(ImageLayout.COLOR_MODEL_MASK)) {
            if (sampleDims!=null && sampleDims.length!=0) {
                if (layout == null) {
                    layout = new ImageLayout();
                }
                int visibleBand = GCSUtilities.getVisibleBand(source.getRenderedImage());
                if (visibleBand >= sampleDims.length) {
                    visibleBand = 0;
                }
                final ColorModel colors;
                colors = sampleDims[visibleBand].getColorModel(visibleBand, sampleDims.length);
                layout = layout.setColorModel(colors);
            }
        }
        if (layout != null) {
            if (hints == null) {
                hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
            } else {
                hints.put(JAI.KEY_IMAGE_LAYOUT, layout);
            }
        }
        if (parameters.hints != null) {
            if (hints != null) {
                hints.add(parameters.hints); // May overwrite the image layout we just set.
            } else {
                hints = parameters.hints;
            }
        }
        /*
         * Performs the operation using JAI and construct the new grid coverage.
         */
        final String        name = deriveName(source, parameters);
        final RenderedImage data = createRenderedImage(parameters.parameters, hints);
        return new GridCoverage(name,                              // The grid coverage name
                                data,                              // The underlying data
                                parameters.coordinateSystem,       // The coordinate system.
                                parameters.gridToCoordinateSystem, // The grid transform.
                                sampleDims,                        // The sample dimensions
                                sources,                           // The source grid coverages.
                                null);                             // Properties
    }
    
    /**
     * Returns the index of the quantitative category, providing that there
     * is one and only one quantitative category. If <code>categories</code>
     * contains 0, 2 or more quantative category, then this method returns
     * <code>-1</code>.
     */
    private static int getQuantitative(final Category[] categories) {
        int index = -1;
        for (int i=0; i<categories.length; i++) {
            if (categories[i].isQuantitative()) {
                if (index >= 0) {
                    return -1;
                }
                index = i;
            }
        }
        return index;
    }
    
    /**
     * Derive the {@link SampleDimension}s for the destination image. The
     * default implementation iterate among all bands and invokes the
     * {@link #deriveCategory deriveCategory} and {@link #deriveUnit deriveUnit}
     * methods for each individual band.
     *
     * @param  bandLists {@link SampleDimension}s for each band in each source
     *         <code>GridCoverage</code>s. For a band (or &quot;sample dimension&quot;)
     *         <code>band</code> in a source coverage <code>source</code>, the
     *         corresponding <code>SampleDimension</code> is
     *
     *                 <code>bandLists[source][band]</code>.
     *
     * @param  parameters Parameters, rendering hints and coordinate system to use.
     * @return The sample dimensions for each band in the destination image. The
     *         length of this array must matches the number of bands in the
     *         destination image. If the <code>SampleDimension</code>s are unknow,
     *         then this method may returns <code>null</code>.
     *
     * @see #deriveCategory
     * @see #deriveUnit
     */
    protected SampleDimension[] deriveSampleDimension(final SampleDimension[][] bandLists,
                                                      final Parameters         parameters)
    {
        /*
         * Compute the number of bands. Sources with only 1 band are treated as
         * a special case:  their unique band is applied to every band in other
         * sources.   If sources don't have the same number of bands, then this
         * method returns  <code>null</code>  since we don't know how to handle
         * those cases.
         */
        int numBands = 1;
        for (int i=0; i<bandLists.length; i++) {
            final int nb = bandLists[i].length;
            if (nb != 1) {
                if (numBands!=1 && nb!=numBands) {
                    return null;
                }
                numBands = nb;
            }
        }
        /*
         * Iterate among all bands. The 'result' array will contains
         * SampleDimensions constructed during the iteration for each
         * individual band. The 'XS' suffix designate temporary arrays
         * of categories and units accross all sources for one particular
         * band.
         */
        final SampleDimension[] result = new SampleDimension[numBands];
        final Category[]    categoryXS = new Category[bandLists.length];
        final Unit[]            unitXS = new Unit[bandLists.length];
        while (--numBands >= 0) {
            SampleDimension sampleDim = null;
            Category[]  categoryArray = null;
            int   indexOfQuantitative = 0;
            assert MASTER_SOURCE_INDEX == 0; // See comment below.
            for (int i=bandLists.length; --i>=0;) {
                /*
                 * Iterate among all sources (i) for the current band. We iterate
                 * sources in reverse order because the master source MUST be the
                 * last one iterated, in order to have proper value for variables
                 * 'sampleDim', 'categoryArray'  and  'indexOfQuantitative' after
                 * the loop.
                 */
                final SampleDimension[] allBands = bandLists[i];
                sampleDim           = allBands[allBands.length==1 ? 0 : numBands];
                categoryArray       = (Category[]) sampleDim.getCategories().toArray();
                indexOfQuantitative = getQuantitative(categoryArray);
                if (indexOfQuantitative < 0) {
                    return null;
                }
                unitXS    [i] = sampleDim.getUnits();
                categoryXS[i] = categoryArray[indexOfQuantitative];
            }
            final Category oldCategory = categoryArray[indexOfQuantitative];
            final Unit     oldUnit     = sampleDim.getUnits();
            final Category newCategory = deriveCategory(categoryXS, parameters);
            final Unit     newUnit     = deriveUnit(unitXS, parameters);
            if (newCategory == null) {
                return null;
            }
            if (!oldCategory.equals(newCategory) || !Utilities.equals(oldUnit, newUnit)) {
                categoryArray[indexOfQuantitative] = newCategory;
                result[numBands] = new SampleDimension(categoryArray, newUnit);
            } else {
                // Reuse the category list from the master source.
                result[numBands] = sampleDim;
            }
        }
        return result;
    }
    
    /**
     * Derive the quantitative category for a {@linkplain SampleDimension sample dimension}
     * in the destination coverage. This method is invoked automatically by the
     * {@link #deriveSampleDimension deriveSampleDimension} method for each band in the
     * destination image. Subclasses should override this method in order to compute the
     * destination {@link Category} from the source categories. For example, the
     * &quot;<code>add</code>&quot; operation may implements this method as below:
     *
     * <blockquote><pre>
     * NumberRange r0 = categories[0].getRange();
     * NumberRange r1 = categories[0].getRange();
     * double min = r0.getMinimum() + r1.getMinimum();
     * double min = r0.getMaximum() + r1.getMaximum();
     * NumberRange newRange = new NumberRange(min, max);
     * return new Category("My category", null, r0, newRange);
     * </pre></blockquote>
     *
     * @param  categories The quantitative categories from every sources. For unary operations
     *         like &quot;GradientMagnitude&quot;, this array has a length of 1. For binary
     *         operations like &quot;add&quot; and &quot;multiply&quot;, this array has a length
     *         of 2.
     * @param  parameters Parameters, rendering hints and coordinate system to use.
     * @return The quantative category to use in the destination image,
     *         or <code>null</code> if unknow.
     */
    protected Category deriveCategory(final Category[] categories, final Parameters parameters) {
        return null;
    }
    
    /**
     * Derive the unit of data for a {@linkplain SampleDimension sample dimension} in the
     * destination coverage. This method is invoked automatically by the
     * {@link #deriveSampleDimension deriveSampleDimension} method for each band in the
     * destination image. Subclasses should override this method in order to compute the
     * destination units from the source units. For example, the &quot;<code>multiply</code>&quot;
     * operation may implement this method as below:
     *
     * <blockquote><pre>
     * if (units[0]!=null && units[1]!=null) {
     *     return units[0].{@link Unit#multiply(Unit) multiply}(units[1]);
     * } else {
     *     return super.deriveUnit(units, cs, parameters);
     * }
     * </pre></blockquote>
     *
     * @param  units The units from every sources. For unary operations like
     *         &quot;GradientMagnitude&quot;, this array has a length of 1.
     *         For binary operations like &quot;add&quot; and &quot;multiply&quot;,
     *         this array has a length of 2.
     * @param  parameters Parameters, rendering hints and coordinate system to use.
     * @return The unit of data in the destination image, or <code>null</code> if unknow.
     */
    protected Unit deriveUnit(final Unit[] units, final Parameters parameters) {
        return null;
    }

    /**
     * Returns a name for the target {@linkplain GridCoverage grid coverage} based on the given
     * source. The default implementation returns the operation name followed by the source name
     * between parenthesis.
     *
     * @param  source The source grid coverage.
     * @param  parameters Parameters, rendering hints and coordinate system to use.
     * @return A name for the target grid coverage.
     */
    protected String deriveName(final GridCoverage source, final Parameters parameters) {
        return getName()+'('+source.getName(null)+')';
    }

    /**
     * Apply the JAI operation. The operation name can be fetch from {@link #descriptor}.
     * The JAI instance to use can be fetch from {@link #getJAI}. The default implementation
     * invokes {@link JAI#createNS JAI.createNS}. Subclasses may overrides this method in order
     * to invokes a different JAI operation according the parameters.
     *
     * @param parameters The parameters to be given to JAI.
     * @param hints The rendering hints to be given to JAI.
     */
    protected RenderedImage createRenderedImage(final ParameterBlockJAI parameters,
                                                final RenderingHints    hints)
    {
        return getJAI(hints).createNS(descriptor.getName(), parameters, hints);
    }

    /**
     * Returns the {@link JAI} instance to use for operations on {@link RenderedImage}.
     * If no JAI instance is defined for the {@link Hints#JAI_INSTANCE} key, then the
     * default instance is returned.
     *
     * @param  hints The rendering hints, or <code>null</code> if none.
     * @return The JAI instance to use (never <code>null</code>).
     */
    protected static JAI getJAI(final RenderingHints hints) {
        if (hints != null) {
            final Object value = hints.get(Hints.JAI_INSTANCE);
            if (value instanceof JAI) {
                return (JAI) value;
            }
        }
        return JAI.getDefaultInstance();
    }
    
    /**
     * Compares the specified object with this operation for equality.
     */
    public boolean equals(final Object object) {
        if (object == this) {
            // Slight optimisation
            return true;
        }
        if (super.equals(object)) {
            final OperationJAI that = (OperationJAI) object;
            return Utilities.equals(this.descriptor, that.descriptor);
        }
        return false;
    }

    /**
     * A block of parameters for a {@link GridCoverage} processed by a {@link OperationJAI}.
     * This parameter is given to the following methods:
     *
     * <ul>
     *   <li>{@link OperationJAI#deriveSampleDimension}</li>
     *   <li>{@link OperationJAI#deriveCategory}</li>
     *   <li>{@link OperationJAI#deriveUnit}</li>
     * </ul>
     *
     * @version $Id: OperationJAI.java,v 1.26 2003/08/03 20:15:03 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    protected static final class Parameters {
        /**
         * The coordinate system for the first 2 dimensions of all sources and the destination
         * {@link GridCoverage}. Sources coverages will be projected in this coordinate system
         * as needed. Extra dimensions after the first 2 will be ignored.
         */
        public final CoordinateSystem coordinateSystem;

        /**
         * The &quot;grid to coordinate system&quot; transform for the first 2 dimensions,
         * which is common to all source grid coverages.
         */
        public final MathTransform2D gridToCoordinateSystem;

        /**
         * The parameters to be given to the {@link JAI#createNS} method.
         */
        public final ParameterBlockJAI parameters;

        /**
         * The rendering hints to be given to the {@link JAI#createNS} method.
         * The {@link JAI} instance to use for the <code>createNS</code> call
         * will be fetch from the {@link Hints#JAI_INSTANCE} key.
         */
        public final RenderingHints hints;

        /**
         * The range, colors and units of the main quantitative {@link Category} to be created.
         * If non-null, then this array length matches the number of sources.
         */
        final RangeSpecifier[] rangeSpecifiers;

        /**
         * Construct a new parameter block with the specified values.
         */
        Parameters(final CoordinateSystem  coordinateSystem,
                   final MathTransform2D   gridToCoordinateSystem,
                   final ParameterBlockJAI parameters,
                   final RenderingHints    hints,
                   final RangeSpecifier[]  rangeSpecifiers)
        {
            this.coordinateSystem       = coordinateSystem;
            this.gridToCoordinateSystem = gridToCoordinateSystem;
            this.parameters             = parameters;
            this.hints                  = hints;
            this.rangeSpecifiers        = rangeSpecifiers;
        }

        /**
         * Returns the range specifier for the first source, or <code>null</code> if none.
         */
        final RangeSpecifier getRangeSpecifier() {
            return (rangeSpecifiers!=null && rangeSpecifiers.length!=0) ? rangeSpecifiers[0] : null;
        }

        /**
         * Returns the first source image, or <code>null</code> if none.
         */
        final RenderedImage getSource() {
            final int n = parameters.getNumSources();
            for (int i=0; i<n; i++) {
                final Object source = parameters.getSource(i);
                if (source instanceof RenderedImage) {
                    return (RenderedImage) source;
                }
            }
            return null;
        }
    }
}

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

// Java Advanced Imaging
import javax.media.jai.JAI;
import javax.media.jai.util.Range;
import javax.media.jai.ImageLayout;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.OperationDescriptor;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;

// Image (Java2D) and collections
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

// Geotools dependencies
import org.geotools.pt.Envelope;
import org.geotools.cv.Category;
import org.geotools.cv.CategoryList;
import org.geotools.gc.GridCoverage;
import org.geotools.cs.CoordinateSystem;

// Resources
import org.geotools.units.Unit;
import org.geotools.resources.Utilities;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Wrap an {@link OperationDescriptor} for interoperability with
 * <A HREF="http://java.sun.com/products/java-media/jai/">Java Advanced
 * Imaging</A>. This class help to leverage the rich set of JAI operators
 * in an OpenGIS framework. <code>OperationJAI</code> inherits operation
 * name and argument types from {@link OperationDescriptor}, except source
 * argument type which is set to <code>{@link GridCoverage}.class</code>.
 * If there is only one source argument, il will be renamed "Source" for
 * better compliance to OpenGIS usage.
 * <br><br>
 * The entry point for applying operation is the usual <code>doOperation</code>
 * method. The default implementation forward the call to other methods for
 * different bits of tasks, resulting in the following chain of calls:
 *
 * <ol>
 *   <li>{@link #doOperation(ParameterList, GridCoverageProcessor)}</li>
 *   <li>{@link #doOperation(GridCoverage[], ParameterBlockJAI, JAI)}</li>
 *   <li>{@link #deriveCategoryList}</li>
 *   <li>{@link #deriveCategory}</li>
 *   <li>{@link #deriveUnit}</li>
 * </ol>
 *
 * Subclasses should override the two last <code>derive</code> methods. The
 * default implementation for other methods should be sufficient in most cases.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class OperationJAI extends Operation {
    /**
     * The rendered mode for JAI operation.
     */
    private static final String RENDERED_MODE = "rendered";
    
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
     * Construct an OpenGIS operation from a JAI operation. This convenience constructor
     * fetch the {@link OperationDescriptor} from the specified operation name using the
     * default {@link JAI} instance.
     *
     * @param operationName JAI operation name (e.g. "GradientMagnitude").
     */
    public OperationJAI(final String operationName) {
        this((OperationDescriptor) JAI.getDefaultInstance().
                getOperationRegistry().getDescriptor(RENDERED_MODE, operationName));
    }
    
    /**
     * Construct an OpenGIS operation from a JAI operation.
     *
     * @param descriptor The operation descriptor. This descriptor
     *        must supports the "rendered" mode (which is the case
     *        for most JAI operations).
     */
    public OperationJAI(final OperationDescriptor descriptor) {
        super(descriptor.getName(), getParameterListDescriptor(descriptor));
        this.descriptor = descriptor;
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
     * Gets the parameter list descriptor for an operation descriptor.
     * {@link OperationDescriptor} parameter list do not include sources.
     * This method will add them in front of the parameter list.
     */
    private static ParameterListDescriptor getParameterListDescriptor(final OperationDescriptor descriptor) {
        ensureValid(descriptor.getDestClass(RENDERED_MODE));
        final Class[] sourceClasses = descriptor.getSourceClasses(RENDERED_MODE);
        for (int i=0; i<sourceClasses.length; i++) {
            ensureValid(sourceClasses[i]);
        }
        
        final ParameterListDescriptor parent = descriptor.getParameterListDescriptor(RENDERED_MODE);
        final String[] sourceNames    = getSourceNames(descriptor);
        final String[] parentNames    = parent.getParamNames();
        final Class [] parentClasses  = parent.getParamClasses();
        final Object[] parentDefaults = parent.getParamDefaults();
        
        final int    numSources = descriptor.getNumSources();
        final String[]    names = new String[parentNames   .length + numSources];
        final Class []  classes = new Class [parentClasses .length + numSources];
        final Object[] defaults = new Object[parentDefaults.length + numSources];
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
        }
        return new ParameterListDescriptorImpl(null, names, classes, defaults, ranges);
    }
    
    /**
     * Returns source name for the specified descriptor. If the descriptor has
     * only one source,  it will be renamed "Source" for better conformance to
     * to OpenGIS usage.
     */
    private static String[] getSourceNames(final OperationDescriptor descriptor) {
        if (descriptor.getNumSources()==1) {
            return new String[] {"Source"};
        } else {
            return descriptor.getSourceNames();
        }
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
     * Apply a process operation to a grid coverage.  The default implementation
     * extract the source <code>GridCoverage</code>s from parameters and invokes
     * {@link #doOperation(GridCoverage[], ParameterBlockJAI, JAI)}.
     *
     * @param  parameters List of name value pairs for the
     *         parameters required for the operation.
     * @param processor The originating {@link GridCoverageProcessor}
     *        (i.e. the instance that invoked this method).
     * @return The result as a grid coverage.
     *
     * @see #doOperation(GridCoverage[], ParameterBlockJAI, JAI)
     */
    protected GridCoverage doOperation(final ParameterList         parameters,
                                       final GridCoverageProcessor processor)
    {
        final ParameterBlockJAI block = new ParameterBlockJAI(descriptor, RENDERED_MODE);
        final String[]     paramNames = parameters.getParameterListDescriptor().getParamNames();
        final String[]    sourceNames = getSourceNames(descriptor);
        final GridCoverage[]  sources = new GridCoverage[descriptor.getNumSources()];
        for (int srcCount=0,i=0; i<paramNames.length; i++) {
            final String name  = paramNames[i];
            final Object param = parameters.getObjectParameter(name);
            if (contains(sourceNames, name)) {
                GridCoverage source = (GridCoverage) param;
                block.addSource(source.getRenderedImage(true));
                sources[srcCount++] = source;
            } else {
                block.setParameter(name, param);
            }
        }
        return doOperation(sources, block, processor.processor);
    }
    
    /**
     * Apply a JAI operation to a grid coverage.  The default implementation
     * ensure that every sources use the same coordinate system and have the
     * same envelope. Then, it construct a new mapping between sample values
     * and geophysics values with new units. This mapping is get by invoking
     * the {@link #deriveCategoryList deriveCategoryList} method. Finally, it
     * apply the operation using the following pseudo-code:
     *
     * <blockquote><pre>
     * {@link JAI#createNS(String,ParameterBlock) processor.createNS}({@link #descriptor}.getName(),&nbsp;parameters)
     * </pre></blockquote>
     *
     * @param  sources The source coverages.
     * @param  processor The {@link JAI} instance to use for instantiating operations.
     *         This argument is usually fetch from a {@link GridCoverageProcessor}.
     * @param  parameters List of name value pairs for the
     *         parameters required for the operation.
     * @return The result as a grid coverage.
     *
     * @see #doOperation(ParameterList, GridCoverageProcessor)
     * @see #deriveCategoryList
     * @see JAI#createNS(String, ParameterBlock, RenderingHints)
     */
    protected GridCoverage doOperation(final GridCoverage[]    sources,
                                       final ParameterBlockJAI parameters,
                                       final JAI               processor)
    {
        final int band = 0; // TODO: The band to examine.
        final GridCoverage source = sources[MASTER_SOURCE_INDEX];
        final CoordinateSystem cs = source.getCoordinateSystem();
        final Envelope   envelope = source.getEnvelope();
        /*
         * Ensure that all coverages use the same
         * coordinate system and has the same envelope.
         */
        for (int i=1; i<sources.length; i++) {
            if (!cs.equivalents(sources[i].getCoordinateSystem())) {
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_INCOMPATIBLE_COORDINATE_SYSTEM));
            }
            if (!envelope.equals(sources[i].getEnvelope())) {
                throw new IllegalArgumentException(Resources.format(
                        ResourceKeys.ERROR_ENVELOPE_MISMATCH));
            }
        }
        /*
         * Get the target category lists. A new color model
         * will be constructed from the new CategoryList.
         */
        final CategoryList[][] list = new CategoryList[sources.length][];
        for (int i=0; i<list.length; i++) {
            list[i] = sources[i].getCategoryLists();
        }
        final CategoryList[] categories = deriveCategoryList(list, cs, parameters);
        ImageLayout layout = new ImageLayout();
        if (categories!=null && categories.length>band) {
            layout = layout.setColorModel(categories[band].getColorModel(true));
        }
        /*
         * Perform the operation using JAI and
         * construct the new grid coverage.
         */
        final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        final RenderedImage   data = processor.createNS(descriptor.getName(), parameters, hints);
        return new GridCoverage(source.getName(null), // The grid coverage name
                                data,                 // The underlying data
                                cs,                   // The coordinate system.
                                envelope,             // The coverage envelope.
                                categories,           // The category lists
                                true,                 // Data are geophysics values.
                                sources,              // The source grid coverages.
                                null);                // Properties
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
     * Derive the {@link CategoryList}s for the destination image. The
     * default implementation iterate among all bands  and invokes the
     * {@link #deriveCategory deriveCategory} and {@link #deriveUnit deriveUnit}
     * methods for each individual band.
     *
     * @param  categoryLists {@link CategoryList}s for each band in each source
     *         <code>GridCoverage</code>s. For a band (or "sample dimension")
     *         <code>band</code> in a source coverage <code>source</code>, the
     *         corresponding <code>CategoryList</code> is
     *
     *                 <code>categoryLists[source][band]</code>.
     *
     * @param  cs The coordinate system of the destination grid coverage.
     * @param  parameters The user-supplied parameters.
     * @return The category lists for each band in the destination image. The
     *         length of this array must matches the number of bands in the
     *         destination image. If the <code>CategoryList</code>s are unknow,
     *         then this method may returns <code>null</code>.
     *
     * @see #deriveCategory
     * @see #deriveUnit
     */
    protected CategoryList[] deriveCategoryList(final CategoryList[][] categoryLists,
                                                final CoordinateSystem cs,
                                                final ParameterList    parameters)
    {
        /*
         * Compute the number of bands. Sources with only 1 band are treated as
         * a special case:  their unique band is applied to every band in other
         * sources.   If sources don't have the same number of bands, then this
         * method returns  <code>null</code>  since we don't know how to handle
         * those cases.
         */
        int numBands = 1;
        for (int i=0; i<categoryLists.length; i++) {
            final int nb = categoryLists[i].length;
            if (nb != 1) {
                if (numBands!=1 && nb!=numBands) {
                    return null;
                }
                numBands = nb;
            }
        }
        /*
         * Iterate among all bands. The 'result' array will contains
         * CategoryLists  constructed during the iteration  for each
         * individual band. The 'XS' suffix designate temporary arrays
         * of categories and units accross all sources for one particular
         * band.
         */
        final CategoryList[] result = new CategoryList[numBands];
        final Category[] categoryXS = new Category[categoryLists.length];
        final Unit[]         unitXS = new Unit[categoryLists.length];
        while (--numBands >= 0) {
            CategoryList categoryList  = null;
            Category[]   categoryArray = null;
            int    indexOfQuantitative = 0;
            assert MASTER_SOURCE_INDEX == 0; // See comment below.
            for (int i=categoryLists.length; --i>=0;) {
                /*
                 * Iterate among all sources (i) for the current band. We iterate
                 * sources in reverse order because the master source MUST be the
                 * last one iterated, in order to have proper value for variables
                 * 'categoryList', 'categoryArray' and 'indexOfQuantitative' after
                 * the loop.
                 */
                final CategoryList[]  allBands = categoryLists[i];
                categoryList        = allBands[allBands.length==1 ? 0 : numBands];
                categoryArray       = categoryList.toArray();
                indexOfQuantitative = getQuantitative(categoryArray);
                if (indexOfQuantitative < 0) {
                    return null;
                }
                unitXS    [i] = categoryList.getUnits();
                categoryXS[i] = categoryArray[indexOfQuantitative];
            }
            final Category oldCategory = categoryArray[indexOfQuantitative];
            final Unit     oldUnit     = categoryList.getUnits();
            final Category newCategory = deriveCategory(categoryXS, cs, parameters);
            final Unit     newUnit     = deriveUnit(unitXS, cs, parameters);
            if (newCategory == null) {
                return null;
            }
            if (!oldCategory.equals(newCategory) || !Utilities.equals(oldUnit, newUnit)) {
                categoryArray[indexOfQuantitative] = newCategory;
                result[numBands] = new CategoryList(categoryArray, newUnit);
            } else {
                // Reuse the category list from the master source.
                result[numBands] = categoryList;
            }
        }
        return result;
    }
    
    /**
     * Derive the quantative category for a band in the destination image.
     * This method is invoked automatically by the {@link #deriveCategoryList
     * deriveCategoryList} method for each band in the destination image. The
     * default implementation always returns <code>null</code>.    Subclasses
     * should override this method in order to compute the destination {@link
     * Category} from the source categories. For example, the "<code>add</code>"
     * operation may implement this method as below:
     *
     * <blockquote><pre>
     * double min = categories[0].{@link Category#minimum minimum} + categories[1].minimum;
     * double max = categories[0].{@link Category#maximum maximum} + categories[1].maximum;
     * return categories[0].rescale(min, max);
     * </pre></blockquote>
     *
     * @param  categories The quantitative categories from every sources.
     *         For unary operations like "GradientMagnitude", this array
     *         as a length of 1. For binary operations like "add" and
     *         "multiply", this array as a length of 2.
     * @param  cs The coordinate system of the destination grid coverage.
     * @param  parameters The user-supplied parameters.
     * @return The quantative category to use in the destination image.
     *         or <code>null</code> if unknow. This category should always
     *         be derived from <code>categories[0]</code>.
     */
    protected Category deriveCategory(final Category[] categories,
                                      final CoordinateSystem cs,
                                      final ParameterList parameters)
    {
        return null;
    }
    
    /**
     * Derive the unit of data for a band in the destination image. This method is
     * invoked automatically by the {@link #deriveCategoryList deriveCategoryList}
     * method for each band in the destination image.   The default implementation
     * always returns <code>null</code>. Subclasses should override this method in
     * order to compute the destination units from the source units.  For example,
     * the "<code>multiply</code>" operation may implement this method as below:
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
     *         "GradientMagnitude", this array as a length of 1.  For binary
     *         operations like "add" and "multiply",  this array as a length
     *         of 2.
     * @param  cs The coordinate system of the destination grid coverage.
     * @param  parameters The user-supplied parameters.
     * @return The unit of data in the destination image,
     *         or <code>null</code> if unknow.
     */
    protected Unit deriveUnit(final Unit[] units,
                              final CoordinateSystem cs,
                              final ParameterList parameters)
    {
        return null;
    }
}

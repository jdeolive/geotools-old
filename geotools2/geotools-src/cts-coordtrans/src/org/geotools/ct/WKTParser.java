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
package org.geotools.ct;

// J2SE dependencies
import java.util.Set;
import java.util.HashSet;

// Parsing
import java.util.Locale;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.ParseException;

// JAI dependencies
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.util.CaselessStringKey;

// Geotools dependencies
import org.geotools.units.Unit;
import org.geotools.resources.WKTFormat;
import org.geotools.resources.WKTElement;
import org.geotools.resources.cts.Resources;
import org.geotools.resources.cts.ResourceKeys;
import org.geotools.resources.DescriptorNaming;


/**
 * Parser for <cite>Well Know Text</cite> (WKT).
 * Instances of this class are thread-safe.
 *
 * @version $Id: WKTParser.java,v 1.1 2002/10/10 14:44:21 desruisseaux Exp $
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
final class WKTParser extends WKTFormat {
    /**
     * The factory to use for creating math transform.
     */                    
    private MathTransformFactory factory;
    
    /**
     * Construct a parser for the specified locale.
     *
     * @param local   The locale for parsing and formatting numbers.
     * @param factory The factory for constructing coordinate systems.
     */
    public WKTParser(final Locale locale, final MathTransformFactory factory) {
        super(locale);
        this.factory = factory;
    }
   
    /**
     * Parses a "PARAM_MT" element. This element has the following pattern:
     *
     * <blockquote><code>
     * PARAM_MT["<classification-name>" {,<parameter>}* ]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "PARAM_MT" element as an {@link MathTransform} object.
     * @throws ParseException if the "PARAM_MT" element can't be parsed.
     */
    private MathTransform parseParamMT(final WKTElement parent) throws ParseException {       
        final WKTElement    element = parent.pullElement("PARAM_MT");
        final String classification = element.pullString("classification");
        ParameterList parameters = factory.getMathTransformProvider(classification).getParameterList();
        /*
         * Gets the list of parameters expecting an integer value.
         * All other parameters will be given a double value.
         */
        final Set integers = new HashSet();
        final ParameterListDescriptor descriptor = parameters.getParameterListDescriptor();
        final Class[] classes = descriptor.getParamClasses();
        if (classes != null) {
            String[] names = null;
            for (int i=0; i<classes.length; i++) {
                if (Integer.class.equals(classes[i])) {
                    if (names == null) {
                        names = descriptor.getParamNames();
                    }
                    integers.add(new CaselessStringKey(names[i]));
                }
            }
        }
        /*
         * Scan over all PARAMETER["name", value] elements and
         * set the corresponding parameter in the ParameterList.
         */
        WKTElement param;
        while ((param=element.pullOptionalElement("PARAMETER")) != null) {
            final String name = param.pullString("name");
            if (integers.contains(new CaselessStringKey(name))) {
                parameters = parameters.setParameter(name, param.pullInteger("value"));
            } else {
                parameters = parameters.setParameter(name, param.pullDouble("value"));
            }
            param.close();
        }
        element.close();
        return factory.createParameterizedTransform(classification, parameters);
    }    
    
    /**
     * Parses a "INVERSE_MT" element. This element has the following pattern:
     *
     * <blockquote><code>
     * INVERSE_MT[<math transform>]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "INVERSE_MT" element as an {@link MathTransform} object.
     * @throws ParseException if the "INVERSE_MT" element can't be parsed.
     */
    private MathTransform parseInverseMT(final WKTElement parent) throws ParseException {       
        final WKTElement element = parent.pullElement("INVERSE_MT");
        try {
            final MathTransform transform = parseMathTransform(element, true).inverse();
            element.close();
            return transform;
        }
        catch (NoninvertibleTransformException exception) {
            throw element.parseFailed(exception, null);
        }
    }
    
    /**
     * Parses a "PASSTHROUGH_MT" element. This element has the following pattern:
     *
     * <blockquote><code>
     * PASSTHROUGH_MT[<integer>, <math transform>]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "PASSTHROUGH_MT" element as an {@link MathTransform} object.
     * @throws ParseException if the "PASSTHROUGH_MT" element can't be parsed.
     */
    private MathTransform parsePassThroughMT(final WKTElement parent) throws ParseException {
        final WKTElement        element = parent.pullElement("PASSTHROUGH_MT");
        final int firstAffectedOrdinate = parent.pullInteger("firstAffectedOrdinate");
        final MathTransform   transform = parseMathTransform(element, true);
        element.close();
        return factory.createPassThroughTransform(firstAffectedOrdinate, transform, 0);
    }    
        
    /**
     * Parses a "CONCAT_MT" element. This element has the following pattern:
     *
     * <blockquote><code>
     * CONCAT_MT[<math transform> {,<math transform>}*]
     * </code></blockquote>
     *
     * @param  parent The parent element.
     * @return The "CONCAT_MT" element as an {@link MathTransform} object.
     * @throws ParseException if the "CONCAT_MT" element can't be parsed.
     */
    private MathTransform parseConcatMT(final WKTElement parent) throws ParseException {       
        final WKTElement element = parent.pullElement("CONCAT_MT");
        MathTransform transform = parseMathTransform(element, true);
        
        MathTransform optionalTransform;
        while ((optionalTransform = parseMathTransform(element, false)) != null) {
            transform = factory.createConcatenatedTransform(transform, optionalTransform);
        }
        element.close();
        return transform;
    }    

    /**
     * Parses a mathTransform element.
     *
     * @param  parent The parent element.
     * @param  required True if parameter is required and false in other case.
     * @return The next element as a {@link MathTransform} object.
     * @throws ParseException if the next element can't be parsed.
     */
    private MathTransform parseMathTransform(final WKTElement element, final boolean required) 
        throws ParseException
    {
        final Object key = element.peek();
        if (key instanceof WKTElement) {
            final String keyword = ((WKTElement) key).keyword.trim().toUpperCase(locale);
            if ("PARAM_MT"      .equals(keyword))  return parseParamMT      (element);
            if ("CONCAT_MT"     .equals(keyword))  return parseConcatMT     (element);
            if ("INVERSE_MT"    .equals(keyword))  return parseInverseMT    (element);
            if ("PASSTHROUGH_MT".equals(keyword))  return parsePassThroughMT(element);
        }
        if (required) {
            throw element.parseFailed(null, Resources.format(ResourceKeys.ERROR_UNKNOW_TYPE_$1, key));
        }
        return null;
    }

    /**
     * Parses a MathTransform element.
     *
     * @param  text The text to be parsed.
     * @return The transformation.
     * @throws ParseException if the string can't be parsed.
     */
    public MathTransform parseMathTransform(final String text) throws ParseException {
        final WKTElement element = getTree(text, new ParsePosition(0));
        final MathTransform mt = parseMathTransform(element, true);
        element.close();
        return mt;
    }
    
    /**
     * Parses the next element in the specified <cite>Well Know Text</cite> (WKT) tree.
     *
     * @param  element The element to be parsed.
     * @return The parsed object.
     * @throws ParseException if the element can't be parsed.
     */
    protected Object parse(final WKTElement element) throws ParseException {
        return parseMathTransform(element, true);
    }    

    /**
     * Format the specified object. Current implementation just append {@link Object#toString},
     * since the <code>toString()</code> implementation for most {@link org.geotools.cs.Info}
     * objects is to returns a WKT.
     *
     * @task TODO: Provides pacakge private <code>Info.toString(WKTFormat)</code> implementations.
     *             It would allows us to invoke <code>((Info)obj).toString(this)</code> here.
     */
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        return toAppendTo.append(obj);
    }    
}

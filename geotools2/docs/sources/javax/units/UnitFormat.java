/*
 * Copyright (c) 2004, JSR-108 group (http://www.jcp.org/en/jsr/detail?id=108)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  - Neither the name of the JSR-108 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package javax.units;

// J2SE direct dependencies
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.HashMap;


/**
 * <p> This is the abstract base class for all unit formats.</p>
 * <p> This class provides the interface for formatting and parsing
 *     units.</p>
 * <p> For all {@link SI} units, the 20 SI prefixes used to form decimal
 *     multiples and sub-multiples of SI units are recognized.
 *     {@link NonSI} units are directly recognized. For example:<pre>
 *        Unit.valueOf("m°C") == SI.MILLI(SI.CELSIUS)
 *        Unit.valueOf("kW")  == SI.KILO(SI.WATT)
 *        Unit.valueOf("ft")  == SI.METER.multiply(0.3048)</pre></p>
 *
 * @author  Jean-Marie Dautelle
 */
public abstract class UnitFormat extends Format {

    ////////////////////////////////////////////////////////////////////////////
    // UNIT DATABASE OPERATIONS.
    //
    /**
     * Holds the system-wide label-unit mapping.
     */
    private static final HashMap LABEL_UNIT = new HashMap();

    /**
     * Holds the system-wide unit-label mapping.
     */
    private static final HashMap UNIT_LABEL = new HashMap();

    /**
     * Holds the system-wide alias-unit mapping.
     */
    private static final HashMap ALIAS_UNIT = new HashMap();

    /**
     * Attaches a system-wide label to the specified unit. This method overrides
     * the previous unit's label (e.g. label from unit database) as units may
     * only have one label (but multiple aliases). For example:
     * <pre><code>
     *     UnitFormat.label(DAY.multiply(365), "year");
     *     Unit FOOT = UnitFormat.label(METER.multiply(0.3048), "ft");
     * </code></pre>
     *
     * @param  unit the unit being associated to the specified label.
     * @param  label the new label for the specified unit or <code>null</code>
     *         to detache the previous label (if any).
     * @return the specified unit.
     * @throws IllegalArgumentException if the specified label is a known symbol
     *         or if the specified label is already attached to a different
     *         unit (must be detached first).
     * @see    #labelFor
     * @see    #alias
     */
    public static Unit label(Unit unit, String label) {
        // Checks label argument.
        if (label != null) {
            if (Unit.searchSymbol(label) != null) {
                throw new IllegalArgumentException(
                    "Label: " + label + " is a known symbol");
            } else {
                Unit u = (Unit) LABEL_UNIT.get(label);
                if ((u != null) && (u != unit)) {
                    throw new IllegalArgumentException(
                        "Label: " + label + " is attached to a different unit" +
                        " (must be detached first)");
                }
            }
        }
        // Updates unit database.
        synchronized (UnitFormat.class) {
            // Removes previous unit mapping.
            String prevLabel = (String) UNIT_LABEL.remove(unit);
            LABEL_UNIT.remove(prevLabel);

            // Removes previous label mapping.
            Unit prevUnit = (Unit) LABEL_UNIT.remove(label);
            UNIT_LABEL.remove(prevUnit);

            // Maps unit and label together.
            LABEL_UNIT.put(label, unit);
            UNIT_LABEL.put(unit, label);
        }
        return unit;
    }

    /**
     * Attaches a system-wide alias to the specified unit. Multiple aliases may
     * be attached to the same unit. Aliases are used during parsing to
     * recognize different variants of the same unit. For example:
     * <pre><code>
     *     UnitFormat.alias(METER.multiply(0.3048), "foot");
     *     UnitFormat.alias(METER.multiply(0.3048), "feet");
     *     UnitFormat.alias(METER, "meter");
     *     UnitFormat.alias(METER, "metre");
     * </code></pre>
     *
     * @param  unit the unit being aliased.
     * @param  alias the alias being attached to the specified unit.
     * @return the specified unit.
     * @see    #unitFor
     */
    public static Unit alias(Unit unit, String alias) {
        synchronized (UnitFormat.class) {
            ALIAS_UNIT.put(alias, unit);
        }
        return unit;
    }
    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the default unit format for the current default locale.
     * For example:
     * <code>centimétre cube par kilogramme fois Ampére carré</code>
     * (French locale).
     *
     * @return the unit format for the current locale.
     */
    public final static UnitFormat getInstance() {
        return getStandardInstance();
    }

    /**
     * Returns the standard unit format. This format is not locale sensitive
     * (international) and uses UTF Latin-1 Supplement
     * (range <code>0080-00FF</code>) supported by the majority of fonts.
     * For example: <code>cm³·A²/kg</code>
     *
     * @return the standard unit format (format used by {@link Unit#toString}).
     */
    public final static UnitFormat getStandardInstance() {
        return Standard.INSTANCE;
    }

    /**
     * Returns the ASCII unit format. This format uses characters range
     * <code>0000-007F</code> exclusively.
     * For example: <code>cm^3 kg^-1 A^2</code>
     *
     * @return the ASCII unit format.
     */
    public final static UnitFormat getAsciiInstance() {
        return Ascii.INSTANCE;
    }

    /**
     * Returns the HTML unit format. This format makes use of HTML tags to
     * represent product units.
     * For example: <code>cm&lt;sup&gt;3&lt;/sup&gt;&amp;#183;kg&lt;sup&gt;-1
     * &lt;/sup&gt;&amp;#183;A&lt;sup&gt;2&lt;/sup&gt;</code>
     * (<code>cm<sup>3</sup>&#183;kg<sup>-1</sup>&#183;A<sup>2</sup></code>)
     *
     * @return the HTML unit format.
     */
    public final static UnitFormat getHtmlInstance() {
        return Html.INSTANCE;
    }

    /**
     * Determines if the specified character may be part of a unit
     * identifier. Any letter or symbol which cannot be mistaken for
     * a separator is allowed.
     *
     * @param  ch the character to be tested.
     * @return  <code>true</code> if the character may be part of a unit
     *          identifier; <code>false</code> otherwise.
     */
    public static boolean isUnitIdentifierPart(char ch) {
        return (ch > '"') && ((ch <= '%')||(ch > '?')) && (ch != '^') &&
            (ch != '¹') && (ch != '²') && (ch != '³') && (ch != '·');
    }

    /**
     * Formats a unit and appends the resulting text to a given string
     * buffer.
     *
     * @param  obj the unit to format.
     * @param  toAppendTo where the text is to be appended
     * @param  pos  a <code>FieldPosition</code> identifying a field
     *         in the formatted text (not used).
     * @return the string buffer passed in as <code>toAppendTo</code>,
     *         with formatted text appended.
     * @throws NullPointerException if <code>toAppendTo</code> is
     *         <code>null</code>
     * @throws IllegalArgumentException if this format cannot format the given
     *         object (e.g. not a <code>Unit</code> instance).
     */
    public abstract StringBuffer format(Object obj, StringBuffer toAppendTo,
                                        FieldPosition pos);

    /**
     * Parses text from a string to produce an object.
     *
     * @param  source a <code>String</code>, part of which should be parsed.
     * @param  pos a <code>ParsePosition</code> object with index and error
     *         index information.
     * @return an <code>Object</code> parsed from the string. In case of
     *         error, returns null.
     * @see    #parseUnit
     */
    public final Object parseObject(String source, ParsePosition pos) {
        try {
            int start = pos.getIndex();
            Unit unit = parseUnit(source.substring(start));
            pos.setIndex(source.length()); // Parsing uses all characters up to
                                           // the end of the string.
            return unit;
        } catch (ParseException e) {
            pos.setErrorIndex(e.getErrorOffset());
            return null;
        }
    }

    /**
     * Parses text from a character sequence to produce a unit.
     * <p> The expected form of the character sequence is:
     *     <code>{&lt;name&gt;{&lt;power&gt;{&lt;root&gt;}}}</code>
     * <p> For examples:
     *     <code><ul>
     *       <li>kg-2/3</li>
     *       <li>rad·s-²</li>
     *       <li>m*s**-2 <i>(for Fortran users)</i></li>
     *       <li>[K+273.15] <i>(equivalent to °C)</i></li>
     *       <li>K^+1:2 cd^-2</li>
     *       <li>mol&lt;sup&gt;-1&lt;/sup&gt;·s&lt;sup&gt;-2&lt;/sup&gt;</li>
     *     </ul></code></p>
     * <p> HTML tags are ignored (e.g. &lt;sup&gt;...&lt;/sup&gt;).</p>
     * <p> Escape sequences are considered as separators (e.g. &amp;#183;).</p>
     * <p> "/" (if not placed between two numbers) indicates ALL the following
     *     unit exponents will be multiplied by <code>-1</code>; multiple
     *     instances of <code>"/"</code> in a line will result in successive
     *     implicit multiplications by <code>-1</code>
     *     of all the exponents that follow (e.g. <code>"s/s/s"</code>
     *     is equivalent to <code>"s"</code>).</p>
     *
     * @param  source the characters sequence to be parsed.
     * @return a <code>Unit</code> parsed from the string.
     * @throws ParseException if a parsing error occurs.
     */
    public Unit parseUnit(CharSequence source) throws ParseException {
        // This method uses local variables only and therefore is thread-safe.
        int state = DEFAULT;
        CharSequence name = null;
        CharSequence power = null;
        CharSequence root = null;
        boolean isInverse = false;
        boolean isSlash = false;
        boolean requestFlush = false;
        int start = 0; // Start index for the relevant state.
        int bracketLevel = 0; // For nested []
        Unit result = Unit.ONE;
        for (int i=0; i <= source.length(); i++) {
            // Adds an extra iteration to flush all buffers when done.
            // Otherwise buffers would be flushed only when a new unit is
            // encountered and the last unit would be ignored.
            char c = (i < source.length()) ? source.charAt(i) : ' ';
            switch (state) {
                case DEFAULT:
                    if (c == '[') {
                        start = i;
                        state = SYSTEM_NAME;
                        requestFlush = true; // Flush previous unit (if any)
                    } else if (isUnitIdentifierPart(c)) {
                        start = i;
                        state = NAME;
                        requestFlush = true; // Flush previous unit (if any)
                    } else if (isExponent(c)) {
                        start = i;
                        state = EXPONENT;
                    } else if (c == '<') {
                        state = MARKUP;
                    } else if (c == '&') {
                        state = ESCAPE;
                    }
                    break;
                case SYSTEM_NAME:
                    if (c == '[') {
                        bracketLevel++;
                    } else if ( (c == ']') && (bracketLevel == 0)) {
                        name = source.subSequence(start, i+1);
                        state = DEFAULT;
                    } else if (c == ']') {
                        bracketLevel--;
                    }
                    break;
                case NAME:
                    if (!isUnitIdentifierPart(c)) {
                        name = source.subSequence(start, i);
                        if (isExponent(c)) {
                            start = i;
                            state = EXPONENT;
                        } else if (c == '<') {
                            state = MARKUP;
                        } else if (c == '&') {
                            state = ESCAPE;
                        } else {
                            state = DEFAULT;
                        }
                    }
                    break;
                case EXPONENT:
                    if (!isExponent(c)) {
                        if (power == null) { // First exponent is power.
                            power = source.subSequence(start, i);
                        } else { // Second is root.
                            isSlash = false; // Reset potential slash (ratio)
                            root = source.subSequence(start, i);
                        }
                        if (c == '[') {
                            start = i;
                            state = SYSTEM_NAME;
                            requestFlush = true; // Flush previous unit (if any)
                        } else if (isUnitIdentifierPart(c)) {
                            start = i;
                            state = NAME;
                            requestFlush = true; // Flush previous unit (if any)
                        } else if (c == '<') {
                            state = MARKUP;
                        } else if (c == '&') {
                            state = ESCAPE;
                        } else {
                            state = DEFAULT;
                        }
                    }
                    break;
                case MARKUP:
                    if (c == '>') {
                        state = DEFAULT;
                    }
                    break;
                case ESCAPE:
                    if (c == ';') {
                        state = DEFAULT;
                    }
                    break;
                default:
                    throw new InternalError("state " + state + " unknown");
            }

            // Check for slash.
            if ( (c == '/') && (state == DEFAULT)) {
                isSlash = true;
            }

            // Flushes buffers when done.
            if (i == source.length()) {
                if (state == NAME) {
                    name = source.subSequence(start, source.length());
                } else if (state == EXPONENT) {
                    if (power == null) {
                        power = source.subSequence(start, source.length());
                    } else {
                        root = source.subSequence(start, source.length());
                    }
                }
                requestFlush = true;
            }
            // Flushes previous unit to the result (multiply).
            if (requestFlush) {
                if (name != null) {
                    Unit unit = unitFor(name);
                    if (unit != null) {
                        int powValue = parseExponent(power);
                        int rootValue = parseExponent(root);
                        powValue = isInverse ? -powValue : powValue;
                        result = result.multiply(
                            unit.pow(powValue).root(rootValue));
                    } else {
                        // Label not recognized.
                        throw new ParseException(
                            "Label: " + name + " not recognized", i);
                    }
                }
                isInverse = (isSlash) ? (!isInverse) : isInverse;
                // Resets all.
                requestFlush = false;
                name = null;
                power = null;
                root = null;
                isSlash = false;
            }
        }
        return result;
    }
    private static boolean isExponent(char c) {
        return  Character.isDigit(c) || (c == '-') ||
            (c == '¹') || (c == '²') || (c == '³');
    }
    private static int parseExponent(CharSequence chars) {
        if (chars != null) {
            boolean isNegative = false;
            int exp = 0;
            for (int i=0; i < chars.length(); i++) {
                char c = chars.charAt(i);
                if (c == '-') {
                    isNegative = true;
                    continue;
                } else if (c == '+') {
                    continue;
                } else if (c == '¹') {
                    c = '1';
                } else if (c == '²') {
                    c = '2';
                } else if (c == '³') {
                    c = '3';
                }
                exp = exp * 10 + (c - '0');
            }
            return isNegative ? - exp : exp;
        } else {
            return 1;
        }
    }
    private static final int DEFAULT = 0;
    private static final int SYSTEM_NAME = 1;
    private static final int NAME = 2;
    private static final int EXPONENT = 3;
    private static final int MARKUP = 4;
    private static final int ESCAPE = 5;

    /**
     * Returns the label for the specified unit. The default behavior of
     * this method (which may be overridden) is first to search the label
     * database.
     *
     * @param  unit the unit to format.
     * @return the database label, the unit's symbol, some other representation
     *         of the specified unit (e.g. <code>[K+273.15], [m*0.01]</code>) or
     *         <code>null</code> for units with custom converters or
     *         {@link ProductUnit} with no label.
     * @see    #unitFor
     */
    public String labelFor(Unit unit) {
        // Label database.
        synchronized (UnitFormat.class) {
            String label = (String) UNIT_LABEL.get(unit);
            if (label != null) {
                return label;
            }
        }
        // Symbol.
        if (unit._symbol != null) {
            return unit._symbol;
        }
        // Transformed unit.
        if (unit instanceof TransformedUnit) {
            TransformedUnit tfmUnit = (TransformedUnit) unit;
            Unit systemUnit = tfmUnit.getSystemUnit();
            Converter cvtr = tfmUnit.getConverterTo(systemUnit);
            if (cvtr instanceof AddConverter) {
                return "[" + systemUnit + "+" +
                       ((AddConverter)cvtr).getOffset() + "]";
            } else if (cvtr instanceof MultiplyConverter) {
                return "[" + systemUnit + "*" +
                       ((MultiplyConverter)cvtr).derivative(0) + "]";
            } else { // Custom converters.
                return null;
            }
        }
        return null;
    }

    /**
     * Returns the unit identified by the specified label. The default behavior
     * of this method (which may be overridden) is first to search the label
     * database, then the alias database and finally the symbols collection.
     *
     * @param  label the label, alias, or symbol identifying the unit.
     * @return the unit identified by the specified label or
     *         <code>null</code> if the identification fails.
     * @see    #labelFor
     */
    public Unit unitFor(CharSequence label) {
        synchronized (UnitFormat.class) {
            // Label database.
            Unit unit = (Unit) LABEL_UNIT.get(label);
            if (unit != null) {
                return unit;
            }
            // Alias database.
            unit = (Unit) ALIAS_UNIT.get(label);
            if (unit != null) {
                return unit;
            }
        }
        // Symbol.
        Unit unit = Unit.searchSymbol(label);
        if (unit != null) {
            return unit;
        }
        return null;
    }

    /**
     * This inner class represents the standard unit format.
     */
    private static final class Standard extends UnitFormat {

        /**
         * Holds the default instance.
         */
        private static final Standard INSTANCE = new Standard();

        // Implements abstract method.
        public StringBuffer format(Object obj, StringBuffer toAppendTo,
                                   FieldPosition pos) {
            if (obj instanceof Unit) {
                String label = labelFor((Unit)obj);
                if (label != null) {
                    toAppendTo.append(label);
                    return toAppendTo; // Done.
                }
            }
            if (obj instanceof ProductUnit) {
                ProductUnit unit = (ProductUnit)obj;
                int invNbr = 0;

                // Write positive exponents first.
                int startIndex = toAppendTo.length();
                for (int i=0; i < unit.size(); i++) {
                    String label = labelFor(unit.get(i).getUnit());
                    int pow = unit.get(i).getPow();
                    int root = unit.get(i).getRoot();
                    if (pow >= 0) {
                        if (startIndex != toAppendTo.length()) {
                            toAppendTo.append('·'); // Separator.
                        }
                        append(toAppendTo, label, pow, root);
                    } else {
                        invNbr++;
                    }
                }

                // Write negative exponents.
                if (invNbr != 0) {
                    if (startIndex == toAppendTo.length()) {
                        toAppendTo.append('1'); // e.g. 1/s
                    }
                    toAppendTo.append('/');
                    if (invNbr > 1) {
                        toAppendTo.append('(');
                    }
                    startIndex = toAppendTo.length();
                    for (int i=0; i < unit.size(); i++) {
                        String label = labelFor(unit.get(i).getUnit());
                        int pow = unit.get(i).getPow();
                        int root = unit.get(i).getRoot();
                        if (pow < 0) {
                            if (startIndex != toAppendTo.length()) {
                                toAppendTo.append('·'); // Separator.
                            }
                            append(toAppendTo, label, -pow, root);
                        }
                    }
                    if (invNbr > 1) {
                        toAppendTo.append(')');
                    }
                }
            } else {
                throw new IllegalArgumentException(
                    "Cannot format given Object as a Unit");
            }
            return toAppendTo;
        }
    }
    private static void append(StringBuffer str, String symbol,
                               int pow, int root) {
        str.append(symbol);
        if ((pow != 1) || (root != 1)) {
            // Write exponent.
            if ((pow == 2) && (root == 1)) {
                str.append('²'); // Square
            } else if ((pow == 3) && (root == 1)) {
                str.append('³'); // Cubic
            } else {
                // Use general exponent form.
                str.append("^" + String.valueOf(pow));
                if (root != 1) {
                    str.append(':' + String.valueOf(root));
                }
            }
        }
    }

    /**
     * This inner class represents the HTML unit format.
     */
    private static final class Html extends UnitFormat {

        /**
         * Holds the default instance.
         */
        private static final Html INSTANCE = new Html();

        // Implements abstract method.
        public StringBuffer format(Object obj, StringBuffer toAppendTo,
                                   FieldPosition pos) {
            if (obj instanceof Unit) {
                String label = labelFor((Unit)obj);
                if (label != null) {
                    toAppendTo.append(label);
                    return toAppendTo; // Done.
                }
            }
            if (obj instanceof ProductUnit) {
                ProductUnit unit = (ProductUnit)obj;
                int startIndex = toAppendTo.length();
                for (int i=0; i < unit.size(); i++) {
                    if (startIndex != toAppendTo.length()) {
                        toAppendTo.append("&#183;"); // Separator.
                    }
                    String label = labelFor(unit.get(i).getUnit());
                    int pow = unit.get(i).getPow();
                    int root = unit.get(i).getRoot();
                    toAppendTo.append(label);
                    if ((pow != 1) || (root != 1)) {
                        // Write exponent.
                        toAppendTo.append("<sup>" + String.valueOf(pow));
                        if (root != 1) {
                            toAppendTo.append(":" + String.valueOf(root));
                        }
                        toAppendTo.append("</sup>");
                    }
                }
            } else {
                throw new IllegalArgumentException(
                    "Cannot format given Object as a Unit");
            }
            return toAppendTo;
        }
    }

    /**
     * This inner class represents the ASCII unit format.
     */
    private static final class Ascii extends UnitFormat {

        /**
         * Holds the default instance.
         */
        private static final Ascii INSTANCE = new Ascii();

        // Implements abstract method.
        public StringBuffer format(Object obj, StringBuffer toAppendTo,
                                   FieldPosition pos) {
            if (obj instanceof Unit) {
                String label = labelFor((Unit)obj);
                if (label != null) {
                    toAppendTo.append(label);
                    return toAppendTo; // Done.
                }
            }
            if (obj instanceof ProductUnit) {
                ProductUnit unit = (ProductUnit)obj;
                int startIndex = toAppendTo.length();
                for (int i=0; i < unit.size(); i++) {
                    if (startIndex != toAppendTo.length()) {
                        toAppendTo.append(" "); // Separator.
                    }
                    String label = labelFor(unit.get(i).getUnit());
                    int pow = unit.get(i).getPow();
                    int root = unit.get(i).getRoot();
                    toAppendTo.append(label);
                    if ((pow != 1) || (root != 1)) {
                        // Write exponent.
                        toAppendTo.append("^" + String.valueOf(pow));
                        if (root != 1) {
                            toAppendTo.append(":" + String.valueOf(root));
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException(
                    "Cannot format given Object as a Unit");
            }
            return toAppendTo;
        }
    }
    
    static { // Force SI/NonSI initialization (load unit database).
        try {
           Class.forName("javax.units.SI");
           Class.forName("javax.units.NonSI");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
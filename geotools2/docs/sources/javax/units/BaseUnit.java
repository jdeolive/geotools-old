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


/**
 * <p> This class represents the building blocks on top of which all others
 *     units are created.</p>
 * <p> Base units are mutually independent; although some implementation may
 *     support conversions between base units by allowing the dimensional 
 *     unit of a base unit to be changed 
 *     <i>(ref. optional operation: {@link #setDimension})</i>.</p>
 * <p> Examples of base units:
 *     <pre><code>
 *         METER = BaseUnit.getInstance("m");
 *         KILOGRAM = BaseUnit.getInstance("kg");
 *         SECOND = BaseUnit.getInstance("s");
 *         AMPERE = BaseUnit.getInstance("A");
 *     </code></pre></p>
 * 
 * @author  Jean-Marie Dautelle
 */
public class BaseUnit extends Unit {

    /**
     * Holds the dimension of this base unit (default: this).
     */
    private Unit _dimension;

    /**
     * Holds the converter to this unit's dimension (default: IDENTITY).
     */
    private Converter _toDimension;

    /**
     * Creates a base unit with the specified symbol.
     *
     * @param  symbol the symbol of this base unit.
     */
    protected BaseUnit(String symbol) {
        super(symbol);
        _dimension = this;
        _toDimension = Converter.IDENTITY;
    }

    /**
     * Returns the base unit with the specified symbol.
     * If the base unit does not already exist, then it is created.
     *
     * @param  symbol the base unit symbol.
     * @return the corresponding base unit.
     * @throws IllegalArgumentException if the specified symbol is currently
     *         associated to a different type of unit.
     */
    public static BaseUnit getInstance(String symbol) {
        BaseUnit newUnit = new BaseUnit(symbol);
        return (BaseUnit) getInstance(newUnit); // Ensures unicity.
    }

    /**
     * Returns the symbol for this base unit.
     *
     * @return this base unit's symbol.
     */
    public final String getSymbol() {
        return _symbol;
    }

    /**
     * <b>Optional operation:</b> Sets the dimensional unit of this base unit.
     * By default a base unit's dimension is itself. It is possible to
     * set the dimension to any other unit. The only constraint being
     * that distinct dimensional units should be independent from each other
     * (e.g. if the dimensional unit for meter is "ns", then the dimensional
     *       unit for second should be "ns" as well). For example:<pre>
     *     SI.METER.setDimension(SI.NANO(SI.SECOND),
     *                           new MultiplyConverter(1e9 / c));
     *     SI.SECOND.setDimension(SI.NANO(SI.SECOND),
     *                            new MultiplyConverter(1e9));
     *     // In this high-energy context, length and time are compatible,
     *     // they have the same "ns" dimensional unit.
     * }</pre>
     * <p> Note: Changing the dimensional units, makes possible conversions
     *           otherwise prohibited (e.g. conversion between meters and 
     *           seconds in a relativistic context).</p>
     *
     * @param  unit the unit identifying the new dimension of this
     *         base unit.
     * @param  toDimension the converter to the specified dimensional unit.
     * @throws UnsupportedOperationException if this operation is not supported
     *         by this implementation.
     */
    public void setDimension(Unit unit, Converter toDimension) {
        _dimension = unit;
        _toDimension = toDimension;
        DIMENSIONS.clear(); // Clears cache values.
        TO_DIMENSIONS.clear(); // Clears cache values.
    }
    
    // Implements abstract method.
    public Unit getSystemUnit() {
        return this; // Base units are system units.
    }

    // Implements abstract method.
    public boolean equals(Object that) {
        return (that instanceof BaseUnit) &&
            ((BaseUnit)that)._symbol.equals(_symbol);
    }

    // Implements abstract method.
    public Unit getBaseUnits() {
        return this;
    }

    // Implements abstract method.
    int calculateHashCode() { // Package private.
        return _symbol.hashCode();
    }

    // Implements abstract method.
    Unit getCtxDimension() {
        return _dimension;
    }

    // Implements abstract method.
    Converter getCtxToDimension() {
        return _toDimension;
    }
}
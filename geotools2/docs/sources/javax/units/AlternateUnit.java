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
 * <p> This class represents an alternate unit. Alternate units are used
 *     in expressions to distinguish between quantities of a different nature
 *     but of the same dimensions (e.g. alternate unit <code>rad</code> 
 *     distinguishes angular acceleration <code>rad/s²</code> from 
 *     scalar acceleration <code>1/s²</code>).</p>
 * <p> Instances of this class are created using the {@link Unit#alternate}
 *     method.</p>
 *
 * @author  Jean-Marie Dautelle
 * @see     Unit#alternate
 */
public final class AlternateUnit extends DerivedUnit {

    /**
     * Holds the base units for this alternate unit (base unit or product of 
     * base units).
     */
    private final Unit _baseUnits;

    /**
     * Creates an alternate unit using the specified symbol.
     *
     * @param  baseUnits the base units for this alternate unit.
     * @param  symbol the symbol for this alternate unit.
     */
    private AlternateUnit(String symbol, Unit baseUnits) {
        super(symbol);
        _baseUnits = baseUnits;
    }

    /**
     * Returns an alternate for this unit. If the alternate unit does not
     * already exist, then it is created.
     *
     * @param  symbol the symbol of the alternate unit.
     * @param  systemUnit the system unit from which to derive the alternate
     *         unit being returned.
     * @return the alternate unit.
     * @throws IllegalArgumentException if the specified symbol is currently
     *         associated to a different unit.
     * @throws UnsupportedOperationException if the specified unit is not
     *         a system unit.
     */
    static AlternateUnit getInstance(String symbol,  // Package private
                                     Unit systemUnit) {
        AlternateUnit newUnit
            = new AlternateUnit(symbol, getBaseUnits(systemUnit));
        return (AlternateUnit) getInstance(newUnit); // Ensures unicity.
    }
    private static Unit getBaseUnits(Unit systemUnit) {
        if (systemUnit instanceof BaseUnit) {
            return systemUnit;
        } else if (systemUnit instanceof AlternateUnit) {
            return ((AlternateUnit)systemUnit)._baseUnits;
        } else if (systemUnit instanceof ProductUnit) {
            ProductUnit product = (ProductUnit) systemUnit;
            Unit baseUnits = ONE;
            for (int i=0; i < product.size(); i++) {
                ProductUnit.Element e = product.get(i);
                Unit unit = getBaseUnits(e.getUnit());
                unit = unit.pow(e.getPow());
                unit = unit.root(e.getRoot());
                baseUnits = baseUnits.multiply(unit);
            }
            return baseUnits;
        } else {
            throw new UnsupportedOperationException(
                "Unit: " + systemUnit + " is not a system unit");
        }
    }

    /**
     * Returns the symbol for this alternate unit.
     *
     * @return this alternate unit's symbol.
     */
    public final String getSymbol() {
        return _symbol;
    }

    // Implements abstract method.
    public Unit getSystemUnit() {
        return this; // Alternate units are system units.
    }

    // Implements abstract method.
    public boolean equals(Object that) {
        return (this == that) || (
            (that instanceof AlternateUnit) &&
            (((AlternateUnit)that)._baseUnits == _baseUnits) &&
            ((AlternateUnit)that)._symbol.equals(_symbol));
    }

    // Implements abstract method.
    int calculateHashCode() { // Package private.
        return _symbol.hashCode();
    }

    // Implements abstract method.
    Unit getCtxDimension() {
        return _baseUnits.getCtxDimension();
    }

    // Implements abstract method.
    Converter getCtxToDimension() {
        return _baseUnits.getCtxToDimension();
    }
}
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
 * <p> This class represents a unit derived from another unit using
 *     a {@link Converter}.</p>
 * <p> Examples of transformed units:
 *     <pre><code>
 *         CELSIUS = KELVIN.add(273.15); // Use label from unit database.
 *         MILLISECOND = SECOND.multiply(1e-3); // Use label from unit database.
 *     </code></pre></p>
 * <p> Transformed units have no intrinsic symbol. But like any other units,
 *     they may have labels attached to them:
 *     <pre><code>
 *         FOOT = UnitFormat.label(METER.multiply(0.3048), "ft");
 *         CENTIMETER = UnitFormat.label(METER.multiply(0.01), "cm");
 *         CALENDAR_YEAR = UnitFormat.label(DAY.multiply(365), "year");
 *     </code></pre>
 *     or aliases:
 *     <pre><code>
 *         UnitFormat.alias(KELVIN.add(273.15), "Celsius");
 *         UnitFormat.alias(METER.multiply(0.01), "centimeter");
 *         UnitFormat.alias(METER.multiply(0.01), "centimetre");
 *     </code></pre>
 *
 * @author  Jean-Marie Dautelle
 * @see     Unit#add(double)
 * @see     Unit#multiply(double)
 * @see     UnitFormat
 */
public class TransformedUnit extends DerivedUnit {

    /**
     * Holds the system unit.
     */
    private final Unit _systemUnit;

    /**
     * Holds the converter to the system unit.
     */
    private final Converter _toSystem;

    /**
     * Creates a transformed unit derived from the specified unit using
     * the specified converter.
     *
     * @param  systemUnit the system unit from which this unit is transformed.
     * @param  toSystem the converter to the system unit.
     */
    private TransformedUnit(Unit systemUnit, Converter toSystem) {
        super(null);
        _systemUnit = systemUnit;
        _toSystem = toSystem;
    }

    /**
     * Returns the unit derived from the specified unit using the specified
     * converter.
     *
     * @param  parent the unit from which this unit is derived.
     * @param  toParent the converter to the parent unit.
     * @return the corresponding derived unit.
     */
    public static Unit getInstance(Unit parent, Converter toParent) {
        Unit systemUnit = parent.getSystemUnit();
        TransformedUnit newUnit = new TransformedUnit(
            systemUnit,
            parent.getConverterTo(systemUnit).concatenate(toParent));
        return (TransformedUnit) getInstance(newUnit); // Ensures unicity.
    }

    // Implements abstract method.
    public Unit getSystemUnit() {
        return _systemUnit;
    }

    // Implements abstract method.
    public boolean equals(Object that) {
        return (this == that) || (
            (that instanceof TransformedUnit) &&
            (((TransformedUnit)that)._systemUnit == _systemUnit) &&
            ((TransformedUnit)that)._toSystem.equals(_toSystem));
    }

    // Implements abstract method.
    int calculateHashCode() { // Package private.
        return _systemUnit.hashCode() + _toSystem.hashCode();
    }

    // Implements abstract method.
    Unit getCtxDimension() {
        return _systemUnit.getCtxDimension();
    }

    // Implements abstract method.
    Converter getCtxToDimension() {
        return _systemUnit.getCtxToDimension().concatenate(_toSystem);
    }
}
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
import java.io.Serializable;


/**
 * <p> This class represents a converter of numeric values.</p>
 * <p> It is not required for sub-classes to be immutable
 *    (e.g. currency converter).</p>
 *
 * @author  Jean-Marie Dautelle
 */
public abstract class Converter implements Serializable {

    /**
     * Holds the identity converter. This converter does nothing
     * (<code>IDENTITY.convert(x) == x</code>).
     */
    public static final Converter IDENTITY = new Identity();

    /**
     * Default constructor.
     */
    protected Converter() {}

    /**
     * Returns the inverse of this converter. If <code>x</code> is a valid
     * value, then <code>x == inverse().convert(convert(x))</code> to within
     * the accuracy of computer arithmetic.
     *
     * @return the inverse of this converter.
     */
    public abstract Converter inverse();

    /**
     * Converts a double value.
     *
     * @param  x the numeric value to convert.
     * @return the converted numeric value.
     * @throws ConversionException if an error occurs during conversion.
     */
    public abstract double convert(double x) throws ConversionException;

    /**
     * Returns this converter derivative for the specified
     * <code>x</code> value. For linear converters, this method returns
     * a constant (the linear factor) for all <code>x</code> values.
     *
     * @param  x the value for which the derivative is calculated.
     * @return the derivative for the specified value.
     */
    public abstract double derivative(double x);

    /**
     * Indicates if this converter is linear. A converter is linear if
     * <code>convert(u + v) == convert(u) + convert(v)</code> and
     * <code>convert(r * u) == r * convert(u)</code>.
     *
     * @return <code>true</code> if this converter is linear;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isLinear();

    /**
     * Indicates whether some other object is "equal to" this converter.
     *
     * @param  obj the reference object with which to compare.
     * @return <code>true</code> if this object is a linear converter and this
     *         object is also a linear converter and both have same
     *         derivatives; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Converter) {
            Converter that = (Converter) obj;
            // Check equality to float precision (allows for some inaccuracies)
            return this.isLinear() && that.isLinear() &&
            (Float.floatToIntBits((float) this.derivative(0)) ==
             Float.floatToIntBits((float) that.derivative(0)));
        } else {
            return false;
        }
    }

    /**
     * Returns a hash code value for this converter. Equals object have equal
     * hash codes.
     *
     * @return this converter hash code value.
     * @see    #equals
     */
    public int hashCode() {
        return Float.floatToIntBits((float)derivative(0));
    }

    /**
     * Concatenates this converter with another converter. The resulting
     * converter is equivalent to first converting by the other converter,
     * and then converting by this converter.
     *
     * @param  converter the other converter.
     * @return the concatenation of this converter with the other converter.
     */
    public Converter concatenate(Converter converter) {
        if (converter == IDENTITY) {
            return this;
        } else {
            return new Compound(converter, this);
        }
    }

    /**
     * This inner class represents the identity converter (singleton).
     */
    private static final class Identity extends Converter {

        // Implements abstract method.
        public Converter inverse() {
            return this;
        }

        // Implements abstract method.
        public double convert(double x) {
            return x;
        }

        // Implements abstract method.
        public double derivative(double x) {
            return 1.0;
        }

        // Implements abstract method.
        public boolean isLinear() {
            return true;
        }

        // Overrides.
        public Converter concatenate(Converter converter) {
            return converter;
        }

    }

    /**
     * This inner class represents a compound converter.
     */
    private static final class Compound extends Converter {

        /**
         * Holds the first converter.
         */
        private final Converter _first;

        /**
         * Holds the second converter.
         */
        private final Converter _second;

        /**
         * Creates a compound converter resulting from the combined
         * transformation of the specified converters.
         *
         * @param  first the first converter.
         * @param  second the second converter.
         */
        private Compound(Converter first, Converter second) {
            _first = first;
            _second = second;
        }

        // Implements abstract method.
        public Converter inverse() {
            return new Compound(_second.inverse(), _first.inverse());
        }

        // Implements abstract method.
        public double convert(double x) {
            return _second.convert(_first.convert(x));
        }

        // Implements abstract method.
        public double derivative(double x) {
            return _first.derivative(x) * _second.derivative(_first.convert(x));
        }

        // Implements abstract method.
        public boolean isLinear() {
            return _first.isLinear() && _second.isLinear();
        }

        // Overrides.
        public boolean equals(Object obj) {
            return super.equals(obj) ||
                (  (obj instanceof Compound) &&
                   ((Compound)obj)._first.equals(_first) &&
                   ((Compound)obj)._second.equals(_second)  );
        }
    }
}
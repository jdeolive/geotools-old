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
 * <p> This class represents a multiply converter. A multiply converter
 *     multiplies numeric values by a constant scale factor.</p>
 * <p> Instances of this class are immutable.</p>
 *
 * @author  Jean-Marie Dautelle
 */
public final class MultiplyConverter extends Converter {

    /**
     * Holds the scale factor.
     */
    private final double _factor;

    /**
     * Creates a multiply converter with the specified scale factor.
     *
     * @param  factor the scale factor.
     */
    public MultiplyConverter(double factor) {
        _factor = factor;
    }

    // Implements abstract method.
    public Converter inverse() {
        return new MultiplyConverter(1.0 / _factor);
    }

    // Implements abstract method.
    public double convert(double amount) {
        return _factor * amount;
    }

    // Implements abstract method.
    public double derivative(double x) {
        return _factor;
    }

    // Implements abstract method.
    public boolean isLinear() {
        return true;
    }

    // Overrides (optimization).
    public Converter concatenate(Converter converter) {
        if (converter instanceof MultiplyConverter) {
            // Optimization (both multiply converters can be merged).
            double factor = _factor * ((MultiplyConverter)converter)._factor;
            return new MultiplyConverter(factor);
        } else {
            return super.concatenate(converter);
        }
    }
}
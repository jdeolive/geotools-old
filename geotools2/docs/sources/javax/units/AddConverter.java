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
 * <p> This class represents an add converter. An add converter adds
 *     a constant offset to numeric values.</p>
 * <p> Instances of this class are immutable.</p>
 */
public final class AddConverter extends Converter {

    /**
     * Holds the offset.
     */
    private final double _offset;

    /**
     * Creates an add converter with the specified offset.
     *
     * @param  offset the offset value.
     */
    public AddConverter(double offset) {
        _offset = offset;
    }

    /**
     * Returns the offset value for this add converter.
     *
     * @return the offset value.
     */
    public double getOffset() {
        return _offset;
    }

    // Implements abstract method.
    public Converter inverse() {
        return new AddConverter(-_offset);
    }

    // Implements abstract method.
    public double convert(double x) {
        return x + _offset;
    }

    // Implements abstract method.
    public double derivative(double x) {
        return 1.0;
    }

    // Implements abstract method.
    public boolean isLinear() {
        return false;
    }

    // Overrides (optimization).
    public Converter concatenate(Converter converter) {
        if (converter instanceof AddConverter) {
            // Optimization (both adding converters can be merged).
            double offset
                = _offset + ((AddConverter)converter).getOffset();
            return new AddConverter(offset);
        } else {
            return super.concatenate(converter);
        }
    }

    // Overrides.
    public boolean equals(Object obj) {
        // Check equality to float precision (allows for some inaccuracies)
	return (obj instanceof AddConverter) &&
            (Float.floatToIntBits((float)((AddConverter)obj)._offset) ==
             Float.floatToIntBits((float)_offset));
    }

    // Overrides.
    public int hashCode() {
	return Float.floatToIntBits((float)_offset);
    }
}
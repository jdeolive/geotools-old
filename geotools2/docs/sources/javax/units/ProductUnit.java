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
 * <p> This class represents a product unit. Product units are formed by
 *     the product of rational powers of existing units.</p>
 * <p> This class maintains the canonical form of this product (simplest
 *     form after factorization). For example:
 *     <code>METER.pow(2).divide(METER)</code> returns
 *     <code>METER</code>.</p>
 *
 * @author  Jean-Marie Dautelle
 * @see     Unit#multiply(Unit)
 * @see     Unit#divide(Unit)
 * @see     Unit#pow(int)
 * @see     Unit#root(int)
 *
 */
public final class ProductUnit extends DerivedUnit {

    /**
     * Holds the units composing this product unit.
     */
    private final Element[] _elements;

    /**
     * Default constructor (used solely to create <code>ONE</code> instance).
     */
    ProductUnit() { // Package private.
        super(null); // No symbol.
        _elements = new Element[0];
    }

    /**
     * Product unit constructor.
     *
     * @param  elements the product elements.
     */
    private ProductUnit(Element[] elements) {
        super(null); // No symbol.
        _elements = elements;
    }

    /**
     * Returns the unit defined from the product of the specifed elements.
     *
     * @param  leftElems left multiplicand elements.
     * @param  rightElems right multiplicand elements.
     * @return the corresponding unit.
     */
    static Unit getInstance(Element[] leftElems, Element[] rightElems) {

        // Merges left elements with right elements.
        Element[] result = new Element[leftElems.length + rightElems.length];
        int resultIndex = 0;
        for (int i=0; i < leftElems.length; i++) {
            Unit unit = leftElems[i]._unit;
            int p1 = leftElems[i]._pow;
            int r1 = leftElems[i]._root;
            int p2 = 0;
            int r2 = 1;
            for (int j = 0; j < rightElems.length; j++) {
                if (unit == rightElems[j]._unit) {
                    p2 = rightElems[j]._pow;
                    r2 = rightElems[j]._root;
                    break; // No duplicate.
                }
            }
            int pow = (p1 * r2) + (p2 * r1);
            int root = r1 * r2;
            if (pow != 0) {
                int gcd = gcd(Math.abs(pow), root);
                result[resultIndex++] = new Element(unit, pow/gcd, root/gcd);
            }
        }

        // Appends remaining right elements not merged.
        for (int i=0; i < rightElems.length; i++) {
            Unit unit = rightElems[i]._unit;
            boolean hasBeenMerged = false;
            for (int j = 0; j < leftElems.length; j++) {
                if (unit == leftElems[j]._unit) {
                    hasBeenMerged = true;
                    break;
                }
            }
            if (!hasBeenMerged) {
                result[resultIndex++] = rightElems[i];
            }
        }

        // Returns or creates instance.
        if (resultIndex == 0) {
            return ONE;
        } else if ( (resultIndex == 1) && (result[0]._pow == result[0]._root) ){
            return result[0]._unit;
        } else {
            Element[] elems = new Element[resultIndex];
            for (int i=0; i < resultIndex; i++) {
                elems[i] = result[i];
            }
            return getInstance(new ProductUnit(elems));
        }
    }

    /**
     * Returns the product of the specified units.
     *
     * @param  left the left unit operand.
     * @param  right the right unit operand.
     * @return <code>left * right</code>
     */
    static Unit getProductInstance(Unit left, Unit right) {
        Element[] leftElems;
        if (left instanceof ProductUnit) {
            leftElems = ((ProductUnit)left)._elements;
        } else {
            leftElems = new Element[] { new Element(left, 1, 1) };
        }
        Element[] rightElems;
        if (right instanceof ProductUnit) {
            rightElems = ((ProductUnit)right)._elements;
        } else {
            rightElems = new Element[] { new Element(right, 1, 1) };
        }
        return getInstance(leftElems, rightElems);
    }

    /**
     * Returns the quotient of the specified units.
     *
     * @param  left the dividend unit operand.
     * @param  right the divisor unit operand.
     * @return <code>dividend / divisor</code>
     */
    static Unit getQuotientInstance(Unit left, Unit right) {
        Element[] leftElems;
        if (left instanceof ProductUnit) {
            leftElems = ((ProductUnit)left)._elements;
        } else {
            leftElems = new Element[] { new Element(left, 1, 1) };
        }
        Element[] rightElems;
        if (right instanceof ProductUnit) {
            Element[] elems = ((ProductUnit)right)._elements;
            rightElems = new Element[elems.length];
            for (int i=0; i < elems.length; i++) {
                rightElems[i] = new Element(
                    elems[i]._unit, -elems[i]._pow, elems[i]._root);
            }
        } else {
            rightElems = new Element[] { new Element(right, -1, 1) };
        }
        return getInstance(leftElems, rightElems);
    }

    /**
     * Returns the product unit corresponding to the specified root of
     * the specified unit.
     *
     * @param  unit the unit.
     * @param  n the root's order (n &gt; 0).
     * @return <code>unit^(1/n)</code>
     * @throws ArithmeticException if <code>n == 0</code>.
     */
    static Unit getRootInstance(Unit unit, int n) {
        Element[] unitElems;
        if (unit instanceof ProductUnit) {
            Element[] elems = ((ProductUnit)unit)._elements;
            unitElems = new Element[elems.length];
            for (int i=0; i < elems.length; i++) {
                int gcd = gcd(Math.abs(elems[i]._pow), elems[i]._root * n);
                unitElems[i] = new Element(
                    elems[i]._unit,
                    elems[i]._pow / gcd,
                    elems[i]._root * n / gcd);
            }
        } else {
            unitElems = new Element[] { new Element(unit, 1, n) };
        }
        return getInstance(unitElems, new Element[0]);
    }

    /**
     * Returns the product unit corresponding to this unit raised to
     * the specified exponent.
     *
     * @param  unit the unit.
     * @param  n the exponent (n &gt; 0).
     * @return <code>unit^n</code>
     */
    static Unit getPowInstance(Unit unit, int n) {
        Element[] unitElems;
        if (unit instanceof ProductUnit) {
            Element[] elems = ((ProductUnit)unit)._elements;
            unitElems = new Element[elems.length];
            for (int i=0; i < elems.length; i++) {
                int gcd = gcd(Math.abs(elems[i]._pow * n), elems[i]._root);
                unitElems[i] = new Element(
                    elems[i]._unit,
                    elems[i]._pow * n / gcd,
                    elems[i]._root / gcd);
            }
        } else {
            unitElems = new Element[] { new Element(unit, n, 1) };
        }
        return getInstance(unitElems, new Element[0]);
    }

    /**
     * Returns the number of units in this product.
     *
     * @return  the number of units being multiplied.
     */
    public int size() {
	return _elements.length;
    }

    /**
     * Returns the product element at the specified position.
     *
     * @param  index the index of the element to return.
     * @return the element at the specified position.
     * @throws IndexOutOfBoundsException if index is out of range
     *         <code>(index &lt; 0 || index &gt;= size())</code>.
     */
    public Element get(int index) {
	return _elements[index];
    }

    // Implements abstract method.
    public Unit getSystemUnit() {
        if (_systemUnitCache == null) {
            Unit systemUnit = ONE;
            for (int i=0; i < _elements.length; i++) {
                Unit unit = _elements[i]._unit.getSystemUnit();
                unit = unit.pow(_elements[i]._pow);
                unit = unit.root(_elements[i]._root);
                systemUnit = systemUnit.multiply(unit);
            }
            _systemUnitCache = systemUnit;
        }
        return _systemUnitCache;
    }
    private transient Unit _systemUnitCache;

    // Implements abstract method.
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        } else if (that instanceof ProductUnit) {
            // Two products are equals if they have the same elements
            // regardless of the elements' order.
            Element[] elems = ((ProductUnit)that)._elements;
            if (_elements.length == elems.length) {
                for (int i=0; i < _elements.length; i++) {
                    boolean unitFound = false;
                    for (int j=0; j < elems.length; j++) {
                        if (_elements[i]._unit == elems[j]._unit) {
                            if (    (_elements[i]._pow != elems[j]._pow) ||
                                    (_elements[i]._root != elems[j]._root) ) {
                                return false;
                            } else {
                                unitFound = true;
                                break;
                            }
                        }
                    }
                    if (!unitFound) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    // Implements abstract method.
    int calculateHashCode() { // Package private.
        int code = 0;
        for (int i=0; i < _elements.length; i++) {
            code += _elements[i]._unit.hashCode() *
                      (_elements[i]._pow * 3 - _elements[i]._root * 2);
        }
        return code;
    }

    // Implements abstract method.
    Unit getCtxDimension() {
        Unit dimension = ONE;
        for (int i=0; i < _elements.length; i++) {
            Unit unit = _elements[i]._unit.getCtxDimension();
            unit = unit.pow(_elements[i]._pow);
            unit = unit.root(_elements[i]._root);
            dimension = dimension.multiply(unit);
        }
        return dimension;
    }

    // Implements abstract method.
    Converter getCtxToDimension() {
        double factor = 1.0;
        for (int i=0; i < _elements.length; i++) {
            Converter toDimension = _elements[i]._unit.getCtxToDimension();
            if (toDimension.isLinear()) {
                factor *= Math.pow(toDimension.derivative(0),
                                   ((double)_elements[i]._pow) /
                                   ((double)_elements[i]._root));
            } else {
                // Non-linear, cannot convert.
                throw new ConversionException(
                    _elements[i]._unit + " is non-linear, cannot convert");
            }
        }
        if (Math.abs(factor - 1.0) < 1e-9) {
            return Converter.IDENTITY;
        } else {
            return new MultiplyConverter(factor);
        }
    }

    /**
     * Returns the greatest common divisor (Euclid's algorithm).
     *
     * @param  m the first number.
     * @param  n the second number.
     * @return the greatest common divisor.
     */
    private static int gcd(int m, int n) {
        if (n == 0) {
            return m;
        } else {
            return gcd(n, m % n);
        }
    }

    /**
     * Inner product element represents a rational power of a single unit.
     */
    public final static class Element implements Serializable {

        /**
         * Holds the single unit.
         */
        private final Unit _unit;

        /**
         * Holds the power exponent.
         */
        private final int _pow;

        /**
         * Holds the root exponent.
         */
        private final int _root;

        /**
         * Structural constructor.
         *
         * @param  unit the unit.
         * @param  pow the power exponent.
         * @param  root the root exponent.
         */
        private Element(Unit unit, int pow, int root) {
            _unit = unit;
            _pow = pow;
            _root = root;
        }

        /**
         * Returns this element's unit.
         *
         * @return the single unit.
         */
        public Unit getUnit() {
            return _unit;
        }

        /**
         * Returns the power exponent. The power exponent can be negative
         * but is always different from zero.
         *
         * @return the power exponent of the single unit.
         */
        public int getPow() {
            return _pow;
        }

        /**
         * Returns the root exponent. The root exponent is always greater
         * than zero.
         *
         * @return the root exponent of the single unit.
         */
        public int getRoot() {
            return _root;
        }
    }
}
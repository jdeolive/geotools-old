/*
 * Units - Temporary implementation for Geotools 2
 * Copyright (C) 1998 University Corporation for Atmospheric Research (Unidata)
 *               1998 Bill Hibbard & al. (VisAD)
 *               1999 Pêches et Océans Canada
 *               2000 Institut de Recherche pour le Développement
 *               2002 Centre for Computational Geography
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Library General Public
 *    License as published by the Free Software Foundation; either
 *    version 2 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Library General Public License for more details (http://www.gnu.org/).
 *
 *
 *    This package is inspired from the units package of VisAD.
 *    Unidata and Visad's work is fully acknowledged here.
 *
 *                   THIS IS A TEMPORARY CLASS
 *
 *    This is a placeholder for future <code>Unit</code> class.
 *    This skeleton will be removed when the real classes from
 *    JSR-108: Units specification will be publicly available.
 */
package org.geotools.units;

// Entrés/sorties
import java.io.ObjectStreamException;


/**
 * Représente une transformation entre deux unités
 * identiques. Cette transformation ne fait rien.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
final class IdentityTransform extends UnitTransform {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -228762891600718327L;
    
    /**
     * Construit un objet qui aura la charge de convertir
     * des données exprimées selon les unités spécifiées.
     */
    private IdentityTransform(final Unit fromUnit, final Unit toUnit) {
        super(fromUnit, toUnit);
    }
    
    /**
     * Retourne une transformation identitée si <code>fromUnit</code> est identique
     * à <code>this</code>. Cette méthode sert à faciliter les implémentations des
     * méthodes <code>get[Inverse]Transform(Unit)</code>.
     *
     * @throws UnitException Si <code>fromUnit</code> n'est pas identique à <code>this</code>.
     */
    public static UnitTransform getInstance(final Unit fromUnit, final Unit toUnit) throws UnitException {
        if (toUnit.equalsIgnoreSymbol(fromUnit)) {
            return new IdentityTransform(fromUnit, toUnit).intern();
        } else {
            throw toUnit.incompatibleUnitsException(fromUnit);
        }
    }
    
    /**
     * Retourne <code>value</code> sans rien faire.
     */
    public double convert(final double value) {
        return value;
    }
    
    /**
     * Ne fait rien.
     */
    public void convert(final double[] values) {
    }
    
    /**
     * Ne fait rien.
     */
    public void convert(final float[] values) {
    }
    
    /**
     * Retourne <code>value</code> sans rien faire.
     */
    public double inverseConvert(final double value) {
        return value;
    }
    
    /**
     * Ne fait rien.
     */
    public void inverseConvert(final double[] values) {
    }
    
    /**
     * Ne fait rien.
     */
    public void inverseConvert(final float[] values) {
    }
}

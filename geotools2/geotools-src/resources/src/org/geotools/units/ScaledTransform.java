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

// Divers
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;


/**
 * Représente une transformation entre deux unités
 * qui diffèrent seulement par un facteur d'échelle.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
final class ScaledTransform extends UnitTransform {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4620148316930810993L;
    
    /**
     * Facteur par lequel diviser {@link #fromUnit}
     * pour obtenir {@link #toUnit}.
     */
    public final double amount;
    
    /**
     * Construit un objet qui aura la charge de convertir
     * des données exprimées selon les unités spécifiées.
     *
     * @throws UnitException si <code>amount</code> n'est pas valide.
     */
    private ScaledTransform(final Unit fromUnit, final Unit toUnit, final double amount) throws UnitException {
        super(fromUnit, toUnit);
        this.amount = amount;
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount==0) {
            throw new UnitException(Resources.format(
                                    ResourceKeys.ERROR_NOT_DIFFERENT_THAN_ZERO_$1,
                                    new Double(amount)));
        }
    }
    
    /**
     * Construit un objet qui aura la charge de convertir
     * des données exprimées selon les unités spécifiées.
     *
     * @throws UnitException si <code>amount</code> n'est pas valide.
     */
    public static UnitTransform getInstance(final Unit fromUnit, final Unit toUnit, final double amount) throws UnitException {
        if (amount==1) {
            return IdentityTransform.getInstance(fromUnit, toUnit);
        } else {
            return new ScaledTransform(fromUnit, toUnit, amount).intern();
        }
    }
    
    /**
     * Indique si cette transformation représente la transformation
     * identitée. Cette méthode retourne toujours <code>false</code>,
     * sauf si {@link #offset} égal 1.
     */
    public boolean isIdentity() {
        return amount==1;
    }
    
    /**
     * Effectue la conversion d'unités d'une valeur.
     * @param value Valeur exprimée selon les unités {@link #fromUnit}.
     * @return Valeur exprimée selon les unités {@link #toUnit}.
     */
    public double convert(final double value) {
        return value/amount;
    }
    
    /**
     * Effectue la conversion d'unités d'un tableaux de valeurs.
     * @param values Valeurs exprimées selon les unités {@link #fromUnit}.
     *        Elles seront converties sur place en valeurs exprimées selon
     *        les unités <code>this</code>.
     */
    public void convert(final double[] values) {
        for (int i=0; i<values.length; i++) {
            values[i] /= amount;
        }
    }
    
    /**
     * Effectue la conversion d'unités d'un tableaux de valeurs.
     * @param values Valeurs exprimées selon les unités {@link #fromUnit}.
     *        Elles seront converties sur place en valeurs exprimées selon
     *        les unités <code>this</code>.
     */
    public void convert(final float[] values) {
        for (int i=0; i<values.length; i++) {
            values[i] = (float) (values[i]/amount);
        }
    }
    
    /**
     * Effectue la conversion inverse d'unités d'une valeur.
     * @param value Valeur exprimée selon les unités {@link #toUnit}.
     * @return Valeur exprimée selon les unités {@link #fromUnit}.
     */
    public double inverseConvert(final double value) {
        return value*amount;
    }
    
    /**
     * Effectue la conversion inverse d'unités d'un tableaux de valeurs.
     * @param values Valeurs exprimées selon les unités {@link #toUnit}.
     *        Elles seront converties sur place en valeurs exprimées selon
     *        les unités {@link #fromUnits}.
     */
    public void inverseConvert(final double[] values) {
        for (int i=0; i<values.length; i++) {
            values[i] *= amount;
        }
    }
    
    /**
     * Effectue la conversion inverse d'unités d'un tableaux de valeurs.
     * @param values Valeurs exprimées selon les unités {@link #toUnit}.
     *        Elles seront converties sur place en valeurs exprimées selon
     *        les unités {@link #fromUnit}.
     */
    public void inverseConvert(final float[] values) {
        for (int i=0; i<values.length; i++) {
            values[i] = (float) (values[i]*amount);
        }
    }
    
    /**
     * Vérifie si cette transformation d'unités est
     * identique à la transformation spécifiée.
     */
    public boolean equals(final Object o) {
        return super.equals(o) && Double.doubleToLongBits(amount)==Double.doubleToLongBits(((ScaledTransform) o).amount);
    }
}

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


/**
 * Classe représentant des unités d'angle écrit sous la forme "degrés,
 * minutes, secondes". Ce travail ne devrait pas être le travail d'un
 * objet {@link Unit}, mais plutôt celui de {@link org.geotools.pt.AngleFormat}.
 * Mais la base de données EPSG insiste pour utiliser des nombres de
 * ce type, et nous en avons parfois besoin pour interagir avec des
 * logiciels existants...
 *
 * @version $Id: DMSUnit.java,v 1.2 2002/07/30 17:09:37 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class DMSUnit extends Unit {
    /**
     * Unité des degrés.
     */
    private static final Unit DEGREE = Unit.DEGREE;

    /**
     * The value to divide DMS unit by.
     * For "degree minute second" (EPSG code 9107), this is 1.
     * For "sexagesimal degree" (EPSG code 9110), this is 10000.
     */
    private final int divider;
    
    /**
     * Construit un objet <code>DMSUnit</code>.
     */
    public DMSUnit(final int divider) {
        super(getSymbol(divider), null);
        this.divider = divider;
    }

    /**
     * Returns a symbol for the specified divider.
     */
    private static String getSymbol(final int divider) {
        switch (divider) {
            case 1:     return "DMS";
            case 10000: return "D.MS";
            default: throw new IllegalArgumentException(String.valueOf(divider));
        }
    }
    
    /**
     * Indique si les unités <code>this</code> et <code>that</code> sont compatibles.
     */
    public boolean canConvert(final Unit that) {
        return DEGREE.canConvert(that);
    }
    
    /**
     * Effectue la conversion d'une mesure exprimée selon d'autres unités.
     */
    public double convert(double value, final Unit fromUnit) throws UnitException {
        value = DEGREE.convert(value, fromUnit);
        final int deg,min,sec;  deg = (int) value; // Round toward 0
        value = (value-deg)*60; min = (int) value; // Round toward 0
        value = (value-min)*60; sec = (int) value; // Round toward 0
        return (((deg*100 + min)*100 + sec) + value)/divider;
    }
    
    /**
     * Effectue sur-place la conversion de mesures exprimées selon d'autres
     * unités. Les valeurs converties remplaceront les anciennes valeurs.
     */
    public void convert(final double[] values, final Unit fromUnit) throws UnitException {
        DEGREE.convert(values, fromUnit);
        for (int i=0; i<values.length; i++) {
            double value;
            final int deg,min,sec;
            value = values[i];      deg = (int) value; // Round toward 0
            value = (value-deg)*60; min = (int) value; // Round toward 0
            value = (value-min)*60; sec = (int) value; // Round toward 0
            values[i] = (((deg*100 + min)*100 + sec) + value)/divider;
        }
    }
    
    /**
     * Effectue sur-place la conversion de mesures exprimées selon d'autres
     * unités. Les valeurs converties remplaceront les anciennes valeurs.
     */
    public void convert(float[] values, Unit fromUnit) throws UnitException {
        DEGREE.convert(values, fromUnit);
        for (int i=0; i<values.length; i++) {
            float value;
            final int deg,min,sec;
            value = values[i];      deg = (int) value; // Round toward 0
            value = (value-deg)*60; min = (int) value; // Round toward 0
            value = (value-min)*60; sec = (int) value; // Round toward 0
            values[i] = (((deg*100 + min)*100 + sec) + value)/divider;
        }
    }
    
    /**
     * Convertit une mesure vers d'autre unités.
     */
    double inverseConvert(double value, final Unit toUnit) throws UnitException {
        value *= divider;
        final int deg,min;
        deg = (int) (value/10000); value -= 10000*deg;
        min = (int) (value/  100); value -=   100*min;
        if (min<=-60 || min>=60) {
            throw new UnitException("Invalid minutes: "+min);
        }
        if (value<=-60 || value>=60) // Accept NaN
        {
            throw new UnitException("Invalid secondes: "+value);
        }
        value = ((value/60) + min)/60 + deg;
        return DEGREE.inverseConvert(value, toUnit);
    }
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unités.
     */
    void inverseConvert(final double[] values, final Unit toUnit) throws UnitException {
        for (int i=0; i<values.length; i++) {
            double value = values[i]*divider;
            final int deg,min;
            deg = (int) (value/10000); value -= 10000*deg;
            min = (int) (value/  100); value -=   100*min;
            if (min<=-60 || min>=60) {
                throw new UnitException("Invalid minutes: "+min);
            }
            if (value<=-60 || value>=60) // Accept NaN
            {
                throw new UnitException("Invalid secondes: "+value);
            }
            values[i] = ((value/60) + min)/60 + deg;
        }
        DEGREE.inverseConvert(values, toUnit);
    }
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unités.
     */
    void inverseConvert(final float[] values, final Unit toUnit) throws UnitException {
        for (int i=0; i<values.length; i++) {
            float value = values[i]*divider;
            final int deg,min;
            deg = (int) (value/10000); value -= 10000*deg;
            min = (int) (value/  100); value -=   100*min;
            if (min<=-60 || min>=60) {
                throw new UnitException("Invalid minutes: "+min);
            }
            if (value<=-60 || value>=60) // Accept NaN
            {
                throw new UnitException("Invalid secondes: "+value);
            }
            values[i] = ((value/60) + min)/60 + deg;
        }
        DEGREE.inverseConvert(values, toUnit);
    }
    
    /**
     * Retourne un objet qui saura convertir selon ces unités les valeurs exprimées
     * selon d'autres unités. Cette méthode est avantageuse si on prévoie faîre
     * plusieurs conversions, car la transformation à utiliser est déterminée une
     * fois pour toute.
     */
    public UnitTransform getTransform(Unit fromUnit) throws UnitException {
        throw new UnitException("Not implemented");
    }
    
    /**
     * Retourne un objet qui saura convertir selon d'autres unités les
     * valeurs exprimées selon ces unités. Cette méthode est l'inverse
     * de {@link #getTransform}.
     */
    UnitTransform getInverseTransform(final Unit toUnit) throws UnitException {
        throw new UnitException("Not implemented");
    }
    
    /**
     * Crée une nouvelle unité proportionnelle à cette unité.
     */
    public Unit scale(final double amount) {
        throw new UnitException("Not implemented");
    }
    
    /**
     * Crée une nouvelle unité décalée par rapport à cette unité.
     */
    public Unit shift(final double offset) {
        throw new UnitException("Not implemented");
    }
    
    /**
     * Retourne la quantité que représente cette unité. Les quantités sont des chaînes de
     * caractères qui décrivent le paramètre physique mesuré, comme "mass" ou "speed". Si
     * aucune quantité n'est définie pour cette unité, retourne <code>null</code>.
     */
    public String getQuantityName() {
        return null;
    }
    
    /**
     * Indique si deux unités sont égales, en ignorant leurs symboles. Le
     * champs {@link #symbol} de chacune des deux unités ne sera pas pris
     * en compte.
     */
    boolean equalsIgnoreSymbol(final Unit unit) {
        return (unit instanceof DMSUnit) && ((DMSUnit) unit).divider == divider;
    }
    
    /**
     * Renvoie une unité identique à celle-ci, mais
     * avec un nouveau symbole et de nouveaux préfix.
     */
    public Unit rename(final String symbol, final PrefixSet prefix) {
        throw new UnitException("Not implemented");
    }
    
    /**
     * Returns an hash code for this object.
     */
    public int hashCode() {
        return 457829627 + divider;
    }
}

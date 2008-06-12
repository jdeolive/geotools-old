/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.styling2;

import org.opengis.style.ContrastEnhancement;

/**
 *
 * @author Johann Sorel
 */
class DefaultContrastEnchancement implements ContrastEnhancement{

    private final boolean normalize;
    private final boolean histogram;
    private final double gamma;
    
    DefaultContrastEnchancement(boolean normalize, boolean histogram, double gamma){
        this.normalize = normalize;
        this.histogram = histogram;
        this.gamma = gamma;
    }
    
    public boolean usesNormalization() {
        return normalize;
    }

    public boolean usesHistogram() {
        return histogram;
    }

    public double getGammaValue() {
        return gamma;
    }

}

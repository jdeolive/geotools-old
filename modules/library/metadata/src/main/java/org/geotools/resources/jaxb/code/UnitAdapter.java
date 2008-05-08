/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.resources.jaxb.code;

import javax.units.Unit;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 *
 * @source $URL$
 * @author Cédric Briançon
 */
public class UnitAdapter extends XmlAdapter<String, Unit> {

    public Unit unmarshal(String v) throws Exception {
        return Unit.valueOf(v);
    }

    public String marshal(Unit v) throws Exception {
        if (v == null) {
            return (String)null;
        }
        return v.toString();
    }
}

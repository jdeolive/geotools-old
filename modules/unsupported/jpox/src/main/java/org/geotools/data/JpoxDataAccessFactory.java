/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.geotools.data.jpox.JpoxDataService;
import org.geotools.util.SimpleInternationalString;
import org.opengis.util.InternationalString;

/**
 * 
 * 
 * @author Thomas Marti
 * @author Stefan Schmid
 * 
 * @source $URL$
 * @version $Id$
 */

public class JpoxDataAccessFactory implements DataAccessFactory {

	public boolean canAccess( Object connectionPrametersBean ) {
		if ( connectionPrametersBean instanceof JpoxConnectionInfo ) {
			JpoxConnectionInfo info = (JpoxConnectionInfo)connectionPrametersBean;
			return ( info.getUrl() != null );
		}
		return false;
	}

	public DataAccess createAccess( Object bean ) throws IOException {
		InputStream is = ((JpoxConnectionInfo)bean).getUrl().openStream();
		return new JpoxDataService( is );
	}

	public Object createAccessBean() {
		return new JpoxConnectionInfo();
	}
	
	public boolean canCreateContent( Object bean ) {
		return false;
	}
	
	public DataAccess createContent( Object bean ) {
		throw new UnsupportedOperationException("No support for write-access yet!");
	}

	public Object createContentBean() {
		throw new UnsupportedOperationException("No support for write-access yet!");
	}

	public InternationalString getName() {
		return new SimpleInternationalString( "JPOX Spatial Data Access" );
	}

	public boolean isAvailable() {
		return true;
	}

	public Map getImplementationHints() {
		return null;
	}

}

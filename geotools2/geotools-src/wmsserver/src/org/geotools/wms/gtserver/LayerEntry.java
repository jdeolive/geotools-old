/**
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */

package org.geotools.wms.gtserver;

import java.util.Properties;
/** A single entry for a Layer in the layers.xml file
 */
public class LayerEntry
{
	/** The unique id of the Layer */
	public String id;
	/** A description for the layer */
	public String description;
	/** The classname for the DataSource to use to load maps for this layer */
	public String datasource;
	/** The properties for the DataSource */
	public Properties properties;
}


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


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

package org.geotools.wms;

import java.util.Vector;
import java.util.Hashtable;

import java.io.InputStream;
import java.io.IOException;

/** Placeholder for WMS Capabilities information. This object does not currently
 * support SLD Servers. A full Capabilities object is returned by an implementation of WMSServer
 * to the WMSServlet, and translated into a valid WMS Capabilities XML stream.
 * 
 * Implementors of WMSServer should construct this and fill it with relevent information for
 * use in the getCapabilities call.
 */
public class Capabilities
{
	/** Whether or not the server supports the GetFeatureInfo Call */
	protected boolean supportsGetFeatureInfo = false;
	
	/** If a Vendor wants to place extra information betwene the <VendorSpecificInformation> tags, it goes here (in XML format) */
	protected String vendorSpecificCapabilities;

	/** The installed Layers */	
	protected Vector layers;
	
	public Capabilities()
	{
		layers = new Vector();
	}
	
	/** Gets whether the server supports the GetFeatureInfo Call
	 */
	public boolean getSupportsGetFeatureInfo()
	{
		return supportsGetFeatureInfo;
	}
	
	/** Sets whether the server supports the GetFeatureInfo Call
	 */
	public void setSupportsGetFeatureInfo(boolean supports)
	{
		supportsGetFeatureInfo = supports;
	}
	/** Sets the Vendor-Specific capabilities for this server. This is purely optional.
	 * @param vecndorXML An XML string of any vendor specific capabilities this server supports.
	 */
	public void setVendorSpecificCapabilitiesXML(String vendorXML)
	{
		vendorSpecificCapabilities = vendorXML;
	}
	
	/** If Vendor-Specific capabilites have been set for this object, returns it.
	 * @return An XML string of the VendorSpecific information
	 */
	public String getVendorSpecificCapabilitiesXML()
	{
		return vendorSpecificCapabilities;
	}

	/** Adds a layer
	 */
	public void addLayer(String name, String title, String srs, double [] bbox)throws CapabilitiesException
	{
		// See if the layer exists already
		if (getLayer(name)!=null)
			throw new CapabilitiesException(CapabilitiesException.EXP_DUPLICATELAYER, "Layer already exists in call to addLayer");
			
		// Create new layer
		Layer l = new Layer(name, title, srs, bbox);
		layers.addElement(l);
	}
	
	/** Adds a layer to the given parent as a nested child Layer
	 */
	public void addLayer(String parent, String name, String title, String srs, double [] bbox)throws CapabilitiesException
	{
		// See if the layer exists already
		Layer lParent = getLayer(parent);
		if (lParent==null)
			throw new CapabilitiesException(CapabilitiesException.EXP_INVALIDLAYER, "Parent Layer does not exist in call to addLayer");
			
		// Create new layer
		Layer l = new Layer(name, title, srs, bbox);
		lParent.addChildLayer(l);
	}
	
	/** Adds a style for an existing layer. If a style with the given name is already available to
	 * the given layer, then no style is added.
	 */
	public void addStyle(String layerName, String styleName, String styleTitle, String legendUrl) throws CapabilitiesException
	{
		// Find the relevent layer
		Layer l = getLayer(layerName);
		if (l==null)
			throw new CapabilitiesException(CapabilitiesException.EXP_INVALIDLAYER, "Invalid layer name given in call to addStyle");
		
		// Check that a style with the given name is not already available to the layer
		Vector availStyles = getAvailableStyles(layerName);
		for (int i=0;i<availStyles.size();i++)
			if (availStyles.elementAt(i).toString().equalsIgnoreCase(styleName))
				return;
		
		// Add the style
		l.addStyle(styleName, styleTitle, legendUrl);
	}

	/** Finds the layer with the given unique name
	 * @return The Layer with the given name
	 */
	private Layer getLayer(String name)
	{
		for (int i=0;i<layers.size();i++)
		{
			Layer l = (Layer)layers.elementAt(i);
			if (l.name != null && l.name.equalsIgnoreCase(name))
				return l;
			if (l.getLayer(name)!=null)
				return l.getLayer(name);
		}
		return null;
	}
	
	public Vector getAvailableStyles(String layerName)
	{
		// Layer to use as temporary root
		Layer root = new Layer("root", "root", "root", null);
		root.layers = layers;
		return getAvailableStyles(layerName, root);
	}
	
	/** Gets the available styles for a given layer. This method is recursive and awkward to program.
	 */
	private Vector getAvailableStyles(String layerName, Layer node)
	{
		// Styles for this node
		Vector styles = new Vector(node.styles.keySet());
		// Check that this is the node we're looking for
		if (layerName.equalsIgnoreCase(node.name))
			return styles;
		// Check the child nodes
		Vector children = node.layers;
		for (int i=0;i<children.size();i++)
		{
			// Recurse children. The only result to return anything will be the branch containing the layer we're looking for
			Vector childStyles = getAvailableStyles(layerName, (Layer)children.elementAt(i));
			if (childStyles!=null)
			{
				styles.addAll(childStyles);
				return styles;
			}
		}
		return null;
	}

	/** Finds the given layer and removes it
	 */
	public void removeLayer(String name)
	{
		for (int i=0;i<layers.size();i++)
		{
			Layer l = (Layer)layers.elementAt(i);
			if (l.name != null && l.name.equalsIgnoreCase(name))
				layers.remove(l);
			// It may be in the child layer
			l.removeChildLayer(l);
		}
	}


	/** Holder class for Layer information
	 */
	protected class Layer
	{
		/** The unique name for this layer */
		String name;
		/** The title for this layer */
		String title;
		/** The Spatial Reference System of this layer */
		String srs;
		/** The bounding box (in lat/long) for this Layer */
		double [] bbox;
		/** The child layers of this Layer (if any) */
		Vector layers;
		/** The installed Styles of this Layer (in the form name=description) */
		Hashtable styles;

		/** Construct a new Layer
		 */		
		protected Layer(String name, String title, String srs, double [] bbox)
		{
			this.name = name;
			this.title = title;
			this.srs = srs;
			this.bbox = bbox;
			
			layers = new Vector();
			styles = new Hashtable();
		}
		
		/** Gets a Layer object stored in this Layer's child layers
		 * @param name The unique name of the layer to find
		 */
		protected Layer getLayer(String name)
		{
			for (int i=0;i<layers.size();i++)
			{
				Layer l = (Layer)layers.elementAt(i);
				if (name.equalsIgnoreCase(l.name))
					return l;
				Layer ch = l.getLayer(name);
				if (ch!=null)
					return ch;
			}
			return null;
		}
		
		/** Adds a child layer to this layer
		 */
		protected void addChildLayer(Layer layer)
		{
			layers.addElement(layer);
		}

		/** Removes a child layer from this layer
		 */
		protected void removeChildLayer(Layer layer)
		{
			if (layers.contains(layer))
				layers.remove(layer);
			else
			{
				// Recurse children
				for (int i=0;i<layers.size();i++)
					((Layer)layers.elementAt(i)).removeChildLayer(layer);
			}
		}

		/** Gets all the children of this layer, in array form
		 */
		protected Layer[] getChildren()
		{
			return (Layer[])layers.toArray(new Layer[layers.size()]);
		}
		
		/** Adds a style for this layer
		 */
		protected void addStyle(String name, String title,  String legendUrl)
		{
			styles.put(name, new Style(name, title, legendUrl));
		}
		
		protected void removeStyle(String name)
		{
			styles.remove(name);
		}
		
		protected Hashtable getStyles()
		{
			return styles;
		}
	}		
	
	protected class Style
	{
		String name;
		String title;
		String legendUrl;
		
		protected Style(String name, String title,String legendUrl)
		{
			this.name = name;
			this.title = title;
			this.legendUrl = legendUrl;
		}
	}
}


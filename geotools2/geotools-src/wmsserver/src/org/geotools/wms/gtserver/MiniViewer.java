package org.geotools.wms.gtserver;

import uk.ac.leeds.ccg.geotools.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;

/** An implementation of a Geotools Viewer which draws it's Themes as a jpg stream. Useful for servlets, or reports.
 */
public class MiniViewer
{
	/** A Vector of ThemeData objects, one for each Theme added */
	private Vector themes = new Vector();
	/** The screen coords of this viewer */
	private Rectangle screen = new Rectangle();
	/** Image used for drawing */
	private Image buffer;
	/** The Scaler used to scale Layers onto the current screen */
	private Scaler scaler;
	/** The background color to use when drawing maps */	
	private Color bgcolor;	
	
	/** Creates a new ServletViewer with the given screen dimensions
	 */
	public MiniViewer(int width, int height, Color bgcolor)
	{
		screen.x = 0;
		screen.y = 0;
		screen.width = width;
		screen.height = height;
	
		if (bgcolor!=null)
			this.bgcolor = bgcolor;
		else
			this.bgcolor = Color.white;
		
		// Set up scaler
		scaler = new Scaler(new GeoRectangle(-180, -90, 360, 180), screen);
		
		// Set up buffer
		buffer = createImage(screen);
	}
	
	public void setDimensions(int width, int height)
	{
		scaler.setSize(width, height);
	}
	
	public Rectangle getViewerSize()
	{
		return screen;
	}
	
	public void setExtent(GeoRectangle gr)
	{
		scaler.setMapExtent(gr);
	}
	
	public GeoRectangle getExtent()
	{
		return scaler.getMapExtent();
	}
	
	public void addTheme(Theme t)
	{
		if (indexOf(t)==-1)
			themes.addElement(new ThemeData(t));
		sortThemes();
	}
	
	public void setThemeIsVisible(Theme t, boolean isVisible)
	{
		ThemeData d = getThemeData(t);
		if (d!=null)
			d.isVisible = isVisible;
	}

	public void setThemeWeighting(Theme t, int weight)
	{
		ThemeData d = getThemeData(t);
		if (d!=null)
			d.weight = weight;
			
		sortThemes();
	}

	/** Paints all the currently held Themes onto the given Graphics object, in order of their weight
	 */
	public void paintThemes(Graphics g)
	{
		// Clear in white
		g.setColor(bgcolor);
		g.fillRect(screen.x, screen.y, screen.width, screen.height);
		
		for (int i=0;i<themes.size();i++)
		{
			((ThemeData)themes.elementAt(i)).theme.paintScaled(g, scaler);
		}
	}
	
	public int indexOf(Theme t)
	{
		for (int i=0;i<themes.size();i++)
			if (((ThemeData)themes.elementAt(i)).theme == t)
				return i;
				
		return -1;
	}
	
	/** Creates an Image scaled to the given Rectangle
	 */
	private Image createImage(Rectangle r)
	{
		return new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_RGB);
	}

	private ThemeData getThemeData(Theme t)
	{
		for (int i=0;i<themes.size();i++)
			if (((ThemeData)themes.elementAt(i)).theme == t)
				return (ThemeData)themes.elementAt(i);
				
		return null;
	}

	/** Perform sort on the vector of themes
	 */
	private void sortThemes()
	{
		Collections.sort(themes);
	}
}

class ThemeData implements Comparable
{
	Theme theme;
	boolean isVisible = true;
	int weight = 0;

	public ThemeData(Theme t)
	{
		theme = t;
	}
	
	public int compareTo(Object themeData)
	{
		return ((ThemeData)themeData).weight - weight;
	}
}


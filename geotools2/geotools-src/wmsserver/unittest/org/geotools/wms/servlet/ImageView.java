package unittest.org.geotools.wms.servlet;

import java.awt.*;
import java.awt.event.*;

public class ImageView extends Panel
{
	private Image img;
	private String name;
	
	public ImageView(Image img, String name)
	{
		this.img = img;
		this.name = name;
		
		this.setSize(img.getWidth(null), img.getHeight(null));
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
		
		g.drawImage(img, 0, 0, null);
	}
	
	public void createFrame()
	{
        Frame f = new Frame(name);
        f.setSize(320,300);
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) { e.getWindow().dispose(); }
        });
        f.add(this);
        f.setVisible(true);
	}
}


package org.geotools.wms.servlet;

import java.awt.*;
import java.awt.event.*;

public class ImageView extends Panel
{
	private Image img;
	private String name;
	private Frame f;
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
        f = new Frame(name);
        f.setSize(img.getWidth(f),img.getHeight(f));
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) { e.getWindow().dispose(); }
        });
        f.add(this);
        f.setVisible(true);
	}
    public void close(){
        f.dispose();
    }
}


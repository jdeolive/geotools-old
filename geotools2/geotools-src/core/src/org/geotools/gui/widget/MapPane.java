package org.geotools.gui.widget;

/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Center for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

import java.util.EventObject;
import org.geotools.gui.tools.AbstractTool;
import org.geotools.map.events.BoundingBoxListener;
import org.geotools.map.events.LayerListListener;

/**
 * This class provides core functionality for drawing a map.  A redraw is
 * required if any of the parameters in the associated Context changes.
 * @version $Id: MapPane.java,v 1.3 2003/02/08 03:30:34 camerons Exp $
 * @author Cameron Shorter
 * @task TODO Should extend LayerListener as well.  Ie, if features inside a \
 * layer change, then a redraw is required.
 */

public interface MapPane
    extends Widget, BoundingBoxListener, LayerListListener  
{

    /**
     * Set the tool for this mapPane.  The tool handles all the mouse and key
     * actions on behalf of this mapPane.  Different tools can be assigned in
     * order to get the mapPane to behave differently.
     * @param tool The tool to use for this mapPane.
     * @throws IllegalArgumentException if tool is null.
     */
    public void setTool(AbstractTool tool) throws IllegalArgumentException;

    /**
     * Get the tool assigned to this mapPane.  If none is assigned, then null
     * is returned.
     * @return The tool assigned to this mapPane.
     */
    public AbstractTool getTool();
}

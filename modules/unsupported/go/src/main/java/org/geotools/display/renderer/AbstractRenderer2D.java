/*
 *    GeoTools - The Open Source Java GIS Toolkit
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
package org.geotools.display.renderer;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.geotools.display.primitive.ReferencedGraphic2D;
import org.geotools.factory.Hints;
import org.geotools.resources.UnmodifiableArrayList;

import org.opengis.display.primitive.Graphic;
import org.opengis.display.renderer.Renderer;
import org.opengis.display.renderer.RendererEvent;

/**
 * Abstract Renderer 2D extends Abstract Renderer by providing a convinient method
 * to grab a sorted list of graphic sorted on Z order.
 *
 * @since 2.5
 * @author Martin Desruisseaux (IRD)
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractRenderer2D extends AbstractRenderer implements Renderer{
    /**
     * A comparator for sorting {@link Graphic} objects by increasing <var>z</var> order.
     */
    private static final Comparator<Graphic> COMPARATOR = new Comparator<Graphic>() {
        public int compare(final Graphic graphic1, final Graphic graphic2) {
            if(graphic1 instanceof ReferencedGraphic2D && graphic2 instanceof ReferencedGraphic2D){
                return Double.compare(((ReferencedGraphic2D)graphic1).getZOrderHint(), ((ReferencedGraphic2D)graphic2).getZOrderHint());
            }else{
                return 0;
            }

        }
    };

    /**
     * The set of {@link Graphic}s to display, sorted in increasing <var>z</var> value. If
     * {@code null}, then {@code Collections.sort(graphics, COMPARATOR)} need to be invoked
     * and its content copied into {@code sortedGraphics}.
     *
     * @see #getGraphics
     */
    private transient List<Graphic> sortedGraphics;


    /**
     * Create a Default Abstract 2D renderer with no particular hints.
     */
    protected AbstractRenderer2D(){
        super(null);
    }

    /**
     * Create a Default Abstract 2D renderer with particular hints.
     *
     * @param hints Hints object or null, if null the renderer will create
     * an empty Hints object.
     */
    protected AbstractRenderer2D(Hints hints){
        super(hints);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void graphicPropertyChanged(final Graphic graphic, final PropertyChangeEvent event){
        super.graphicPropertyChanged(graphic, event);
        assert Thread.holdsLock(this);
        final String propertyName = event.getPropertyName();

        if (propertyName.equals(ReferencedGraphic2D.Z_ORDER_HINT_PROPERTY)) {
            sortedGraphics = null; // Will force a new sorting according z-order.
        }else if(propertyName.equals(ReferencedGraphic2D.DISPLAY_BOUNDS_PROPERTY)){
            RendererEvent rendererEvent = new DefaultRendererEvent(this, graphic);
            fireGraphicChanged(rendererEvent);
        }else if(propertyName.equals(ReferencedGraphic2D.VISIBLE_PROPERTY)){
            RendererEvent rendererEvent = new DefaultRendererEvent(this, graphic);
            fireGraphicChanged(rendererEvent);
        }

    }


    //------------ graphic methods ---------------------------------------------
    /**
     * The returned list is sorted in increasing
     * {@linkplain Graphic#getZOrderHint z-order}: element at index 0 contains the first
     * graphic to be drawn.
     *
     * @return The sorted graphic list by Z order
     */
    protected synchronized List<Graphic> getSortedGraphics(){
        if (sortedGraphics == null) {
            final Set<Graphic> keys = graphics.keySet();
            final Graphic[] list = keys.toArray(new Graphic[keys.size()]);
            Arrays.sort(list, COMPARATOR);
            sortedGraphics = UnmodifiableArrayList.wrap(list);
        }
        assert sortedGraphics.size() == graphics.size();
        assert graphics.keySet().containsAll(sortedGraphics);

        return sortedGraphics;
    }

    /**
     * {@inheritDoc}
     * <p>
     * A call to this method will set to null the sorted graphic list.
     * The list will be recreated on the first call to {@link #getSortedGraphics() }.
     * <p>
     */
    @Override
    protected synchronized Graphic add(Graphic graphic) throws IllegalArgumentException {
        final List<Graphic> oldGraphics = sortedGraphics; // May be null.
        graphic = super.add(graphic);
        sortedGraphics = null;
        assert oldGraphics == null || getGraphics().containsAll(oldGraphics) : oldGraphics;
        return graphic;
    }

    /**
     * {@inheritDoc}
     * <p>
     * A call to this method will set to null the sorted graphic list.
     * The list will be recreated on the first call to {@link #getSortedGraphics() }.
     * <p>
     */
    @Override
    protected synchronized void remove(final Graphic graphic) throws IllegalArgumentException {
        final List<Graphic> oldGraphics = sortedGraphics; // May be null.
        super.remove(graphic);
        sortedGraphics = null;
        assert oldGraphics==null || oldGraphics.containsAll(getGraphics()) : oldGraphics;
    }

    /**
     * {@inheritDoc}
     * <p>
     * A call to this method will set to null the sorted graphic list.
     * The list will be recreated on the first call to {@link #getSortedGraphics() }.
     * <p>
     */
    @Override
    protected synchronized void removeAll() {
        super.removeAll();
        sortedGraphics = null;
        clearCache();
    }


}

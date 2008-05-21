
package org.geotools.display.renderer;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.geotools.display.canvas.AbstractCanvas;
import org.geotools.display.canvas.DisplayObject;
import org.geotools.display.primitive.AbstractGraphic;
import org.geotools.factory.Hints;
import org.geotools.resources.UnmodifiableArrayList;

import org.opengis.display.primitive.Graphic;
import org.opengis.display.renderer.Renderer;

/**
 *
 * @author johann sorel
 */
public abstract class AbstractRenderer2D extends AbstractRenderer implements Renderer{            
    /**
     * A comparator for sorting {@link Graphic} objects by increasing <var>z</var> order.
     */
    private static final Comparator<Graphic> COMPARATOR = new Comparator<Graphic>() {
        public int compare(final Graphic graphic1, final Graphic graphic2) {
            if(graphic1 instanceof AbstractGraphic && graphic2 instanceof AbstractGraphic){
                return Double.compare(((AbstractGraphic)graphic1).getZOrderHint(), ((AbstractGraphic)graphic2).getZOrderHint());
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


    public AbstractRenderer2D(){
        this(null);
    }

    public AbstractRenderer2D(Hints hints){
        super(hints);
    }
                
    /**
     * Invoked automatically when a graphic registered in this canvas changed. Subclasses can
     * override this method if they need to react to some graphic change events, but should
     * always invoke {@code super.graphicPropertyChanged(graphic, event)}.
     *
     * @param graphic The graphic that changed.
     * @param event   The property change event.
     */
    @Override
    protected void graphicPropertyChanged(final Graphic graphic, final PropertyChangeEvent event){       
        assert Thread.holdsLock(this);
        final String propertyName = event.getPropertyName();
        
        if (propertyName.equals(AbstractGraphic.Z_ORDER_HINT_PROPERTY)) {
            sortedGraphics = null; // Will force a new sorting according z-order.
        }
        super.graphicPropertyChanged(graphic, event);
    }
        
    
    //------------ graphic methods --------------------------------------------- 
    /**
     * This method sort the sortedGraphic list using graphic Z-order.
     * If two graphics have the same order then the first one added will have
     * priority.
     * If the sortedList is null then this method will create it.
     * 
     * @return The sorted graphic list
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
     * Adds the given {@code Graphic} to this {@code Canvas}. This implementation respect the
     * <var>z</var>-order retrieved by calling {@link Graphic#getZOrderHint()}. When two added
     * {@code Graphic}s have the same <var>z</var>-order, the most recently added will be on top.
     * <p>
     * Most {@code Canvas} do not draw anything as long as at least one graphic hasn't be added.
     * In Geotools implementation, an {@link AbstractGraphic} can be added to only one
     * {@link AbstractCanvas} object. If the specified graphic has already been added to
     * an other canvas, then this method {@linkplain AbstractGraphic#clone creates a clone}
     * before to add the graphic.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#GRAPHICS_PROPERTY}
     * property change event.
     *
     * @param  graphic Graphic to add to this canvas. This method call will be ignored if
     *         {@code graphic} has already been added to this canvas.
     * @return The graphic added. This is usually the supplied graphic, but may also be a
     *         new one if this method cloned the graphic.
     * @throws IllegalArgumentException If {@code graphic} has already been added to an other
     *         {@code Canvas} and the graphic is not cloneable.
     *
     * @see #remove
     * @see #removeAll
     * @see #getGraphics
     *
     * @todo Current implementation has a risk of thread lock if {@code canvas1.add(graphic2)} and
     *       {@code canvas2.add(graphic1)} are invoked in same time in two concurrent threads, where
     *       {@code canvas1} and {@code canvas2} are two instances of {@code AbstractCanvas},
     *       {@code graphic1} and {@code graphic2} are two instances of {@code AbstractGraphic},
     *       {@code graphic1} was already added to {@code canvas1} and {@code graphic2} was already
     *       added to {@code canvas2} before the above-cited {@code add} method calls.
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
     * Removes the given {@code Graphic} from this {@code Canvas}. Note that if the graphic is
     * going to be added back to the same canvas later, then it is more efficient to invoke
     * {@link Graphic#setVisible} instead.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#GRAPHICS_PROPERTY}
     * property change event.
     *
     * @param  graphic The graphic to remove. This method call will be ignored if {@code graphic}
     *         has already been removed from this canvas.
     * @throws IllegalArgumentException If {@code graphic} is owned by an other {@code Canvas}
     *         than {@code this}.
     *
     * @see #add
     * @see #removeAll
     * @see #getGraphics
     */
    @Override
    protected synchronized void remove(final Graphic graphic) throws IllegalArgumentException {
        final List<Graphic> oldGraphics = sortedGraphics; // May be null.        
        super.remove(graphic);        
        sortedGraphics = null;
        assert oldGraphics==null || oldGraphics.containsAll(getGraphics()) : oldGraphics;
    }

    /**
     * Remove all graphics from this canvas.
     * <p>
     * This method fires a {@value org.geotools.display.canvas.DisplayObject#GRAPHICS_PROPERTY}
     * property change event.
     *
     * @see #add
     * @see #remove
     * @see #getGraphics
     */
    @Override
    protected synchronized void removeAll() {
        sortedGraphics = null;
        clearCache();
    }

    
}

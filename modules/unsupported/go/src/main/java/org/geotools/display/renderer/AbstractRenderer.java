
package org.geotools.display.renderer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.display.canvas.AbstractCanvas;
import org.geotools.display.canvas.DisplayObject;
import org.geotools.display.primitive.AbstractGraphic;
import org.geotools.factory.Hints;
import org.geotools.resources.UnmodifiableArrayList;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;

import org.opengis.display.canvas.Canvas;
import org.opengis.display.primitive.Graphic;
import org.opengis.display.renderer.Renderer;
import org.opengis.display.renderer.RendererListener;

/**
 *
 * @author johann sorel
 */
public abstract class AbstractRenderer extends DisplayObject implements Renderer{        
    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractCanvas#getGraphics set of graphics} in this canvas changed.
     */
    public static final String GRAPHICS_PROPERTY = "graphics";
    
    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * canvas {@linkplain ReferencedCanvas2D#getDisplayBounds display bounds} changed.
     */
    public static final String DISPLAY_BOUNDS_PROPERTY = "displayBounds";

    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AbstractGraphic#getZOrderHint z order hint} changed.
     */
    public static final String Z_ORDER_HINT_PROPERTY = "zOrderHint";
    
    /**
     * A listener to be notified when a graphic property changed.
     */
    public static final PropertyChangeListener PROPERTIES_LISTENER = new PropertyChangeListener() {
        public void propertyChange(final PropertyChangeEvent event) {
            final Object source = event.getSource();
            if (source instanceof Graphic) {
                final AbstractGraphic graphic = (AbstractGraphic) source;
                final Canvas target = (Canvas) graphic.getCanvas();
                if (target instanceof AbstractCanvas) {
                    synchronized (target) {
//                        ((AbstractCanvas) target).graphicPropertyChanged(graphic, event); -----------------------------
                    }
                }
            }
        }
    };

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
    
    
    private final AbstractRenderer This = this;

    /**
     * The set of {@link Graphic}s to display. Keys and values are identical; values are used as
     * a way to recognize existing graphics that are equals to the {@linkplain #add added} ones.
     * <p>
     * This map must preserve the order in which the user added graphics. This order must be
     * preserved no matter how {@link #sortedGraphics} reorder graphics. This is because we
     * want to preserve to {@link #add} contract even when z-value hints change.
     */
    private final Map<Graphic,Graphic> graphics = new LinkedHashMap<Graphic,Graphic>();

    /**
     * The set of {@link Graphic}s to display, sorted in increasing <var>z</var> value. If
     * {@code null}, then {@code Collections.sort(graphics, COMPARATOR)} need to be invoked
     * and its content copied into {@code sortedGraphics}.
     *
     * @see #getGraphics
     */
    private transient List<Graphic> sortedGraphics;

    /**
     * The set of {@link Graphic}s given to the user. This set is act like a
     * proxy, this set delegate his methods to the graphics map.
     */
    private final AbstractSet<Graphic> userGraphics = new AbstractSet<Graphic>() {

        @Override
        public Iterator<Graphic> iterator() {
            return graphics.keySet().iterator();
        }

        @Override
        public int size() {
            return graphics.size();
        }
    };

    /**
     * A set of rendering hints.
     *
     * @see Hints#COORDINATE_OPERATION_FACTORY
     */
    protected final Hints hints;


    public AbstractRenderer(){
        this(null);
    }

    public AbstractRenderer(Hints hints){
        this.hints = (hints != null) ? hints : new Hints() ;
    }

    /**
     * This method sort the sortedGraphic list using graphic Z-order.
     * If two graphics have the same order then the first one added will have
     * priority.
     * If the sortedList is null then this method will create it.
     * 
     * @return The sorted graphic list
     */
    protected List<Graphic> getSortedGraphics(){
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
    protected synchronized Graphic add(Graphic graphic) throws IllegalArgumentException {
        final List<Graphic> oldGraphics = sortedGraphics; // May be null.

        if (graphic instanceof AbstractGraphic) {
            AbstractGraphic candidate = (AbstractGraphic) graphic;
            synchronized (candidate.getTreeLock()) {
                final Canvas canvas = candidate.getCanvas();
                if (canvas == this) {
                    // The supplied graphic is already part of this canvas.
                    assert graphics.containsKey(candidate) : candidate;
                } else {
                    assert !graphics.containsKey(candidate) : candidate;
                    if (canvas != null) {
//                        try {
                            graphic = candidate;
//                        graphic = candidate.clone();
//                        } catch (CloneNotSupportedException e) {
//                            throw new IllegalArgumentException(
//                                    Errors.format(ErrorKeys.CANVAS_NOT_OWNER_$1, candidate.getName()), e);
//                        }

                    }
                    candidate.setCanvas(getCanvas());
                    candidate.addPropertyChangeListener(PROPERTIES_LISTENER);
                }
            }
            // The graphic lock should now be the same as the canvas lock.
            assert Thread.holdsLock(candidate.getTreeLock());
        }
        /*
         * Add the new graphic in the 'graphics' array. The array will growth as needed and
         * 'sortedGraphics' is set to null  so that the array will be resorted when needed.
         * If an identical graphic (in the sense of Object.equals(....)) existed prior this
         * method call, then the previous graphic instance will be kept (instead of the new
         * supplied one).
         */
        final Graphic previous = graphics.put(graphic, graphic);
        if (previous != null) {
            graphic = previous;
            graphics.put(graphic, graphic);
        }
        sortedGraphics = null;
        assert oldGraphics == null || getGraphics().containsAll(oldGraphics) : oldGraphics;
//        if (hasGraphicsListeners) {   ----------------------------------------------------------------------------OPTIMISATION
        propertyListeners.firePropertyChange(GRAPHICS_PROPERTY, oldGraphics, getGraphics());
//        }
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
    protected synchronized void remove(final Graphic graphic) throws IllegalArgumentException {
        final List<Graphic> oldGraphics = sortedGraphics; // May be null.
        if (graphic instanceof AbstractGraphic) {
            final AbstractGraphic candidate = (AbstractGraphic) graphic;
            final Canvas canvas = candidate.getCanvas();
            if (canvas == null) {
                assert !graphics.containsKey(candidate) : candidate;
                return;
            }
            if (canvas != this) {
                assert !graphics.containsKey(candidate) : candidate;
                throw new IllegalArgumentException(Errors.format(
                            ErrorKeys.CANVAS_NOT_OWNER_$1, candidate.getName()));
            }
            assert Thread.holdsLock(candidate.getTreeLock());
            candidate.removePropertyChangeListener(PROPERTIES_LISTENER);
            candidate.clearCache();
            candidate.setCanvas(null);
        } else {
            if (!graphics.containsKey(graphic)) {
                return;
            }
        }
        if (graphics.remove(graphic) != graphic) {
            throw new AssertionError(graphic); // Should never happen.
        }
        sortedGraphics = null;
        assert oldGraphics==null || oldGraphics.containsAll(getGraphics()) : oldGraphics;
//        if (hasGraphicsListeners) {   --------------------------------------------------------------------------------OPTIMISATION
            propertyListeners.firePropertyChange(GRAPHICS_PROPERTY, oldGraphics, getGraphics());
//        }
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
    protected synchronized void removeAll() {
        final List<Graphic> oldGraphics = sortedGraphics; // May be null.
        for (final Graphic graphic : graphics.keySet()) {
            if (graphic instanceof AbstractGraphic) {
                final AbstractGraphic candidate = (AbstractGraphic) graphic;
                assert Thread.holdsLock(candidate.getTreeLock());
                candidate.removePropertyChangeListener(PROPERTIES_LISTENER);
                candidate.clearCache();
                candidate.setCanvas(null);
            }
        }
        sortedGraphics = null;
        clearCache(); // Must be after 'sortedGraphics=null'
//        if (hasGraphicsListeners) {   -----------------------------------------------------------------------------------------OPTIMISATION
            propertyListeners.firePropertyChange(GRAPHICS_PROPERTY, oldGraphics, getGraphics());
//        }
    }

    /**
     * Returns all graphics in this canvas. The returned list is sorted in increasing
     * {@linkplain Graphic#getZOrderHint z-order}: element at index 0 contains the first
     * graphic to be drawn.
     * <p>
     * This method returns an unmodifiable snapshot of current canvas state.
     * {@linkplain #add Adding} or {@linkplain #remove removing} graphics will
     * not affect the content of previous list returned by previous call to this method.
     */
    public synchronized Collection<Graphic> getGraphics() {
        return userGraphics;
    }

    /**
     * Returns a rendering hint.
     *
     * @param  key The hint key (e.g. {@link #FINEST_RESOLUTION}).
     * @return The hint value for the specified key, or {@code null} if none.
     */
    public synchronized Object getRenderingHint(final RenderingHints.Key key) {
        return hints.get(key);
    }

    /**
     * Adds a rendering hint. Hints provides optional information used by some rendering code.
     *
     * @param key   The hint key (e.g. {@link #FINEST_RESOLUTION}).
     * @param value The hint value. A {@code null} value remove the hint.
     *
     * @see #FINEST_RESOLUTION
     * @see #REQUIRED_RESOLUTION
     * @see #PREFETCH
     * @see Hints#COORDINATE_OPERATION_FACTORY
     * @see RenderingHints#KEY_RENDERING
     * @see RenderingHints#KEY_COLOR_RENDERING
     * @see RenderingHints#KEY_INTERPOLATION
     */
    public synchronized void setRenderingHint(final RenderingHints.Key key, final Object value) {
        if (value != null) {
            hints.put(key, value);
        } else {
            hints.remove(key);
        }
    }

    /**
     * Clears all cached data. Invoking this method may help to release some resources for other
     * applications. It should be invoked when we know that the map is not going to be rendered
     * for a while. For example it may be invoked from {@link java.applet.Applet#stop}. Note
     * that this method doesn't changes the renderer setting; it will just slow down the first
     * rendering after this method call.
     *
     * @see #dispose
     */
    @Override
    public void clearCache() {
        assert Thread.holdsLock(this);
        final Collection<Graphic> graphics = getGraphics();
        for (final Graphic graphic : graphics) {
            if (graphic instanceof DisplayObject) {
                ((DisplayObject) graphic).clearCache();
            }
        }
        super.clearCache();
    }

    @Override
    public void dispose() {
        final Collection<Graphic> graphics = getGraphics();
        removeAll();
        for (final Graphic graphic : graphics) {
            graphic.dispose();
        }
        super.dispose();
    }


    public RenderedImage getSnapShot() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void add(Collection<Graphic> graphics) {
        for(Graphic g : graphics){
            add(g);
        }
    }

    protected void remove(Collection<Graphic> graphics) {
        for(Graphic g : graphics){
            remove(g);
        }
    }

    public void addRendererListener(RendererListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeRendererListener(RendererListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public abstract boolean paint(
            Graphics2D output,
            AffineTransform zoom);
    
    /**
     * Invoked automatically when a graphic registered in this canvas changed. Subclasses can
     * override this method if they need to react to some graphic change events, but should
     * always invoke {@code super.graphicPropertyChanged(graphic, event)}.
     *
     * @param graphic The graphic that changed.
     * @param event   The property change event.
     */
    protected void graphicPropertyChanged(final AbstractGraphic graphic,
                                          final PropertyChangeEvent event){
        assert Thread.holdsLock(this);
        final String propertyName = event.getPropertyName();
//        if (propertyName.equalsIgnoreCase(Z_ORDER_HINT_PROPERTY)) {
//            sortedGraphics = null; // Will force a new sorting according z-order.
//            return;
//        }
    }


}

package org.geotools.renderer;
/**
 * This <strong>abstract</strong> class provides the platform independent
 * interface between objects (and stylers) and the output platform (awt,
 * java2, pdf, javaME, ...). To do this a series of "primitive" drawing
 * operations are provided for the feature/styler to call. The renderer
 * impelmentation (which is tied to an output type) does the actual drawing.
 * <p>
 * The render provides a draw level (1 is simplest - 11 is highest
 * quality/slowest) and a scale of the centre pixel of the screen to the
 * styler. The sytler then decides how to draw its feature (point, line,
 * dashed line, color line etc) and calls the relevant "primative" method
 * with <strong>geographic</strong> coordinates and a spatial reference
 * system (SRS). The renderer then transforms these into the output SRS and
 * screen coordinates and performs the output specific draw routine.
 * <p>
 * The render may also provide hints about other properties of the output
 * system, e.g. vector/raster, transparency availability, color depth etc.
 * However it is the responsability of the styler to ask these questions.
 * <p>
 * The renderer contains no application specific knowledge this is delegated
 * to the styler. So a renderer "knows" how to draw lines but a styler "knows"
 * how to draw roads as lines.
 */
import org.geotools.proj4.*;
import org.geotools.proj4.projections.*;
public abstract class Renderer{
/** The best quality the output can accept
 */
    int maxQuality;
/** The quality that is currently requested by the output
 */
    int currentQuality;
/** the size in geographic units of the centre pixel of the display
 */
    int scaledPixelSize;
/** does the output scheme support transparency
 */
    boolean transparent;
/** the required output SRS
 */
    Projection output;
    Viewer view;
    /** Constructs a Renderer
     *
     * @param v the output viewer
     * @param qual The maximum quality of this output type
     */
    public Renderer(Viewer v,int qual){
        maxQuality = qual;
        view = v;
    }
    
    /** Getter for property maxQuality.
     * @return Value of property maxQuality.
     */
    public int getMaxQuality() {
        return maxQuality;
    }
    
    /** Setter for property maxQuality.
     * @param maxQuality New value of property maxQuality.
     */
    public void setMaxQuality(int maxQuality) {
        this.maxQuality = maxQuality;
    }
    
    /** Getter for property currentQuality.
     * @return Value of property currentQuality.
     */
    public int getCurrentQuality() {
        return currentQuality;
    }
    
    /** Setter for property currentQuality.
     * @param currentQuality New value of property currentQuality.
     */
    public void setCurrentQuality(int currentQuality) {
        this.currentQuality = currentQuality;
    }
    
    /** Getter for property transparent.
     * @return Value of property transparent.
     */
    public boolean isTransparent() {
        return transparent;
    }
    
    /** Setter for property transparent.
     * @param transparent New value of property transparent.
     */
    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }
    
    /** Getter for property scaledPixelSize.
     * @return Value of property scaledPixelSize.
     */
    public int getScaledPixelSize() {
        return scaledPixelSize;
    }
    
    /** Setter for property scaledPixelSize.
     * @param scaledPixelSize New value of property scaledPixelSize.
     */
    protected void setScaledPixelSize(int scaledPixelSize) {
        this.scaledPixelSize = scaledPixelSize;
    }
    
    /** Getter for property output.
     * @return Value of property output.
     */
    public SRS getOutput() {
        return output;
    }
    
    /** Setter for property output.
     * @param output New value of property output.
     */
    protected void setOutput(SRS output) {
        this.output = output;
    }
    
    /** draw the piont provided by x and y in geographic space on the output device.
     * @param x geographic coordinate
     * @param y geographic coordinate
     * @param source input spatial reference system
     */
    public abstract void drawPoint(double x, double y, SRS source);
    /** draw a line on the output device
     * @param x geographic coordinates
     * @param y geographic coordinates
     * @param source input spatial reference system
     * @param s line style descriptor
     */
    public abstract void drawLine(double [] x, double [] y, LineStyle s, SRS source);
    /** draw a polygon on the output device
     * @param x geographic coordinates
     * @param y geographic coordinates
     * @param source input spatial reference system
     * @param s line style descriptor
     */
    public abstract void drawPolygon(double [] x, double [] y, LineStyle s, SRS source);
    /** draw a filled polygon on the output device
     * @param x geographic coordinates
     * @param y geographic coordinates
     * @param source input spatial reference system
     * @param f Fill style descriptor
     */
    public abstract void fillPolygon(double [] x, double [] y, FillStyle f, SRS source);
    /** draw a circle on the output device
     * @param x geographic coordinate
     * @param y geographic coordinate
     * @param r radius in geographic units
     * @param source input spatial reference system
     */
    public abstract void drawCircle(double x, double y, double r, SRS source);
   /** converts a point in the input SRS to the output SRS
     * if these equal each other then this is a null op otherwise the
     * point is converted to the common reference system from the input and then to the output system.
     * @param input the input SRS
     * @param inpt the point to be converted
     * @return point in output SRS
     */
    protected double[] convertPointToOutput(SRS input, double x, double y){
        double [] inpt = new double[2];
        inpt[0]=x;
        inpt[1]=y;
        return convertPointToOutput(input,inpt);
    }
    /** converts a point in the input SRS to the output SRS
     * if these equal each other then this is a null op otherwise the
     * point is converted to the common reference system from the input and then to the output system.
     * @param input the input SRS
     * @param inpt the point to be converted
     * @return point in output SRS
     */
    protected double[] convertPointToOutput(SRS input, double[] inpt){
        if(input.equals(output)) return inpt;
        return output.toInput(input.toCommon(inpt));
    }
    /** Converts a geographic point to a screen coordinate
     * implementations with a builtin afine transformtion should override this method
     * @param inpt input point
     * @return screen coordinates [x,y]
     */    
    protected int[] convertPointToScreen(double x, double y){
        // something with affine transforrms and scales 
        int [] out = new int[2];
        out[0]=(int)x;
        out[1]=(int)y;
        return out;
    }
    
    /** Converts a geographic point to a screen coordinate
     * implementations with a builtin afine transformtion should override this method
     * @param inpt input point
     * @return screen coordinates [x,y]
     */    
    protected int[] convertPointToScreen(double[] inpt){
       return convertPointToScreen(inpt[0],inpt[1]);
    }
    protected int convertDistanceToScreen(double d){
        // something else with scales
        return (int)d;
    }
}

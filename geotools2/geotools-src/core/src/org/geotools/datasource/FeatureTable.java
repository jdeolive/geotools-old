package org.geotools.datasource;

import java.util.List;
import java.util.Iterator;
import java.util.Vector;

public class FeatureTable {
    /** The column names loaded from the DataSource */
    private String [] columns = null;
    /** The DataSource used by this Featuretable to load data */
    private DataSource ds = null;
    /** The central table of loaded Features */
    private Vector table = null;
    /** The current loaded Extent */
    private Extent loadedExtent = null;
    /** The TableEventListeners attached to this object */
    private Vector vTableEventListeners = new Vector();
    /** The attached FeatureIndex objects */
    private Vector vIndexes = new Vector();
    /** The currently loading threads */
    private Vector vLoadingThreads = new Vector();
    
    /** This mode discards all the Features in this table before loading a new Extent */
    public static final int MODE_DISCARD_ALL = 0;
    /** This mode disacrds only those Features not needed by a new Extent */
    public static final int MODE_LOAD_INTERSECT = 1;
    
    private int iLoadMode = MODE_DISCARD_ALL;
    
    /** The normal, non-loading, non Index-rebuilding state of the FeatureTable */
    public static final int STATE_NORMAL = 0;
    /** Threaded loading is in progress */
    public static final int STATE_LOADING = 1;
    /** Indexes are being rebuilt (all TableChangedListeners are being notified) */
    public static final int STATE_BUILDING = 2;
    
    private int iState = STATE_NORMAL;
    
    /** Creates a new FeatureTable with the specified number of rows
     */
    public FeatureTable(DataSource ds) {
        // Set DataSource
        this.ds = ds;
        // Set columns names
        columns = ds.getColumnNames();
        // Initialize the table
        table = new Vector();
    }
    
    /** Gets the names of the columns in this FeatureTable
     */
    public String[] getColumnNames() {
        if (columns!=null)
            return (String[])columns.clone();
        else
            return null;
    }
    
    /** Sets the loaded Extent of this FeatureTable. Begins a load thread if ex is outside
     *  the loaded extent of this FeatureTable
     * @param ex The Extent to be loaded
     */
    public void requestExtent(Extent ex) throws DataSourceException {
 
        setState(FeatureTable.STATE_LOADING);
        // The new Extents to load Features from
        Extent [] newExtents = new Extent[] { ex };
        
        if (ex==null) throw new DataSourceException("Null Extent passed");
        
        // Get the load mode
        if (iLoadMode==MODE_DISCARD_ALL) {
            // Discard all the data in the old extent before loading the new one
            table = new Vector();
        }
        else {
            // Discard the difference between the two Extents
            if (loadedExtent!=null)
                newExtents = ex.difference(loadedExtent);
            if (newExtents!=null)
                for (int i=0;i<newExtents.length;i++)
                    removeFeatures(findFeatures(newExtents[i]), true);
        }
        // Stop any currently running loader threads
        for (int i=0;i<vLoadingThreads.size();i++) {
            FeatureTableThread ftt = (FeatureTableThread)vLoadingThreads.elementAt(i);
            ftt.stopLoading();
        }
        // Create and start the load thread(s)
        vLoadingThreads = new Vector();
        for (int i=0;i<newExtents.length;i++) {
            FeatureTableThread ft = new FeatureTableThread(this, newExtents[i]);
            vLoadingThreads.addElement(ft);
            ft.start();
        }
    }
    
    /** Gets the loaded Extent of this FeatureTable
     * The Extent of current loaded Features in this table
     */
    public Extent getLoadedExtent() {
        return loadedExtent;
    }
    
    /** Gets the rows of this FeatureTable. Each row is a List of Feature objects.
     */
    public List getRows() {
        return table;
    }
    
    /** Adds a listener for table events
     */
    public void addTableChangedListener(TableChangedListener fel) {
        vTableEventListeners.addElement(fel);
    }
    
    /** Removes a listener for table events
     */
    public void removeTableChangedListener(TableChangedListener fel) {
        vTableEventListeners.removeElement(fel);
    }
    
    /** Attaches a FeatureIndex to this FeatureTable (usually called from a FeatureIndex implementation)
     */
    public void addIndex(FeatureIndex fi) {
        vIndexes.addElement(fi);
    }
    
    /** Removes a FeatureIndex from this FeatureTable
     */
    public void removeIndex(FeatureIndex fi) {
        vIndexes.removeElement(fi);
    }
    
    /** Rebuilds all attached Indexes, and sends a TableChangedEvent to each attached listener
     */
    public void notifyTableChanged() {
        notifyTableChanged(null);
    }
    
    /** This rebuilds all attached indexes, then notifies the TableChangedListeners
     * In the event of an error rebuilding the FeatureIndexes, it will place that Exception
     * into the TableChangedEvent it sends to each TableChangedListener
     * @param exp The DataSourceException from an error during loading. If null, the load
     * is presumed to be ok.
     */
    public void notifyTableChanged(DataSourceException exp) {
        if (exp==null) {
            try {
                // rebuild all attached FeatureIndexes
                for (int i=0;i<vIndexes.size();i++) {
                    ((FeatureIndex)vIndexes.elementAt(i)).rebuild();
                }
            }
            catch(IndexException iexp) {
                exp = new DataSourceException("Exception rebuilding FeatureIndex : "+iexp.toString());
            }
        }
        
        // Construct Event
        TableChangedEvent tce = new TableChangedEvent(exp==null?TableChangedEvent.TABLE_OK:TableChangedEvent.TABLE_ERROR, this, loadedExtent, exp);
        
        // Notify tablechanged listeners
        for (int i=0;i<vTableEventListeners.size();i++)
            ((TableChangedListener)vTableEventListeners.elementAt(i)).tableChanged(tce);
    }
    
    /** Sets the Load Mode for this table
     */
    public void setLoadMode(int iMode) {
        iLoadMode = iMode;
    }
    
    /** Gets the Load Mode for this table
     */
    public int getLoadMode() {
        return iLoadMode;
    }
    
    /** Gets the current load state of the FeatureTable, these can be:	<br>
     * @return The load state of the FeatureTable
     * 	STATE_NORMAL : 		Nothing's happening - no loads or rebuilds
     * 	STATE_LOADING :		Threaded loading is in progress
     * 	STATE_BUILDING :	All TableChangedListeners (including Indexes) are being notified of the last change in the FeatureTable
     */
    public int getState() {
        return iState;
    }
    
    /** Gets the DataSource being used by this FeatureTable
     */
    public DataSource getDataSource() {
        return ds;
    }
    
    /** Finds a List of the Features currently held in this table, within the given Extent
     * @param ex The Extent to test the Features against.
     */
    public List findFeatures(Extent ex) {
        Vector vFound = new Vector();
        Iterator it = table.iterator();
        
        while (it.hasNext()) {
            Feature f = (Feature)it.next();
            if (ex.containsFeature(f))
                vFound.addElement(f);
        }
        return vFound;
    }
    
    /** Removes the given List of Features from this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param features The List of Features to remove
     */
    public void removeFeatures(List features) {
        removeFeatures(features, false);
    }
    
    /** Removes the given List of Features from this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param features The List of Features to remove
     * @param quiet If true, does not notify TableChangedListeners that the table has changed.
     */
    public void removeFeatures(List features, boolean quiet) {
        table.removeAll(features);
        if (!quiet)
            notifyTableChanged();
    }
    
    /** Adds the given List of Features to this FeatureTable, notifying TableChangedListeners that the table has changed
     * @param features The List of Features to add
     */
    public void addFeatures(List features) {
        addFeatures(features, false);
    }
    
    /** Adds the given List of Features to this FeatureTable
     * @param features The List of Features to add
     * @param quiet If true, does not notify TableChangedListeners that the table has changed.
     */
    public void addFeatures(List features, boolean quiet) {
        table.addAll(features);
        if (!quiet)
            notifyTableChanged();
    }
    
    /** Gets the List of currently loading threads
     */
    protected List getLoadThreads() {
        return vLoadingThreads;
    }
    
    /** Sets the Column names to be used by this FeatureTable
     */
    protected void setColumnNames(String[] sColumnNames) {
        columns = sColumnNames;
    }
    
    /** Sets the load state of the FeatureTable
     */
    protected void setState(int state) {
        iState = state;
    }
}

/** The Thread class used to load data using the given DataSource - it :	<br>
 * 	1. Calls DataSource.load(Extent), loading the List of Features requested	<br>
 *  2. Filters out any points which are in the FeatureTable's already loaded Extent (to avoid overlap)	<br>
 */
class FeatureTableThread extends Thread {
    private FeatureTable ft = null;
    private DataSource ds = null;
    private Extent ex = null;
    private boolean bStopped = false;
    
    public FeatureTableThread(FeatureTable ft, Extent ex) {
        this.ft = ft;
        this.ds = ft.getDataSource();
        this.ex = ex;
    }
    
    public void run() {
        // The List of Features to be loaded
        List lFeatures = new Vector();
        // The Exception (if any) returned from DataSource.load()
        DataSourceException exp = null;
        
        // Set the FeatureTable's load state
        ft.setState(FeatureTable.STATE_LOADING);
        
        try {
            // Load the mapshapes
            lFeatures = ds.load(ex);
        }
        catch(DataSourceException dsexp) {
            // Record the Exception to be passed back up the tree
            exp = dsexp;
        }
        catch(Exception nexp) {
            // Record any unexpected exceptions as part of a DataSourceException
            exp = new DataSourceException("Unexpected error loading from DataSource.load()\n"+nexp.toString());
        }
        
        // Check whether the thread has been stopped by an outside process
        if (bStopped) return;
        
        // if the points are not within the FeatureTable's already-loaded Extents, rmeove them
/*		if (ft.getLoadedExtent()!=null)
      for (int i=0;i<lFeatures.size();i++)
        if (ft.getLoadedExtent().containsFeature((Feature)lFeatures.get(i)))
          lFeatures.remove(lFeatures.get(i));
 */
        // Add them to the FeatureTable,
        ft.addFeatures(lFeatures, true);
        
        // Check whether this Thread is the last thread in the FeatureTable
        List lLoadThreads = ft.getLoadThreads();
        lLoadThreads.remove(this);
        if (lLoadThreads.size()==0) {
            // Set the FeatureTable's load state
            ft.setState(FeatureTable.STATE_BUILDING);
            
            // Set the Column Names
            ft.setColumnNames(ds.getColumnNames());
            // Call the TableChangedListeners		*** Should I put a try/catch around this?
            if (exp==null)
                ft.notifyTableChanged();
            else
                ft.notifyTableChanged(exp);
            
            // Set the FeatureTable's load state
            ft.setState(FeatureTable.STATE_NORMAL);
        }
    }
    
    /** Stops this thread from loading, and stops the thread itself. If the mapshapes have
     *  already been loaded, then it's too late to stop, and this method has no effect - the
     *  thread will continue updating FeatureTable with the mapshapes it's loaded, and call
     *  all TableChangedListeners.
     */
    public void stopLoading() {
        bStopped = true;
        ds.stopLoading();
    }
}
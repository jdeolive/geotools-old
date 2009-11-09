/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.process.idl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.util.logging.Logging;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

/**
 * Test Purpose class which handle progress by printing on Logger.
 */
public class PrintingProgressListener implements ProgressListener{

	private final static Logger LOGGER = Logging.getLogger("org.geotools.process.idl");
	
	/** The progress percentage */
    private float progressPercent;
    
    /** The operation description */
    private String description;
    
    /** The task description */
    private InternationalString task;
    
    /** 
     * @see org.opengis.util.ProgressListener 
     */
    public void complete() {
    	if (LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("Completed");
    }

    /** 
     * @see org.opengis.util.ProgressListener 
     */
    public void dispose() {
        
    }

    /** 
     * @see org.opengis.util.ProgressListener 
     */
    public void exceptionOccurred(Throwable arg0) {
    }

    /** Return the operation description */
    public String getDescription() {
        return description;
    }

    /** 
     * Return the current progress as percent completed
     * @see org.opengis.util.ProgressListener 
     */
    public float getProgress() {
        return progressPercent;
    }

    /** 
     * Return the description of the current task being performed.
     * @see org.opengis.util.ProgressListener 
     */
    public InternationalString getTask() {
        return task;
    }

    /** 
     * Return {@code true} if this job is canceled.
     * @see org.opengis.util.ProgressListener 
     */
    public boolean isCanceled() {
        return false;
    }

    /** 
     * Notify the listener of progress.
     * @see org.opengis.util.ProgressListener 
     */
    public void progress(final float progress) {
        progressPercent = progress;
        if (LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("Progress:" + Float.toString(progress));
    }

    public void setCanceled(boolean canceled) {
    }

    /** 
     * Set the description of the operation.
     * @see org.opengis.util.ProgressListener 
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /** 
     * Set the description of the current task being performed.
     * @see org.opengis.util.ProgressListener 
     */
    public void setTask(final InternationalString task) {
        this.task = task;
        if (LOGGER.isLoggable(Level.INFO))
    		LOGGER.info(task.toString());
    }

    /** 
     * Notifies the listener that the operation begins.
     * @see org.opengis.util.ProgressListener 
     */
    public void started() {
    	if (LOGGER.isLoggable(Level.INFO))
    		LOGGER.info("Started");
    }

    public void warningOccurred(String arg0, String arg1, String arg2) {
    	
    }

}

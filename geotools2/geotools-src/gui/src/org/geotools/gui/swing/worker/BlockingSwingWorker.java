/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.gui.swing.worker;

import java.awt.*;
import javax.swing.*;


/**
 * This is a variant of the SwingWorker It works in conjunction with the GlassPane class to allow
 * users to execute timeconsuming task on a separate thread The GlassPane addition can prevent
 * users from executing another SwingWorker task while one SwingWorker task is already executing.
 * A message popup to provide an explaination for the long wait and beep on click capability can
 * be also requested
 *
 * @author Yexin Chen
 * @author Andrea Aime
 */
public abstract class BlockingSwingWorker {
    private Thread thread;
    private ThreadVar threadVar;
    private GlassPane glassPane;
    private MessagePopup dialog;
    private String message;
    private String title;
    private boolean beep;
    private java.awt.Component aComponent;

    /**
     * Start a thread that will call the <code>construct</code> method and then exit. No message
     * nor beeps will be activated
     *
     * @param aComponent a reference to the UI component that's directly using SwingWorker
     */
    public BlockingSwingWorker(Component aComponent) {
        this(aComponent, null, null, false);
    }

    /**
     * Start a thread that will call the <code>construct</code> method and then exit.
     *
     * @param aComponent a reference to the UI component that's directly using SwingWorker
     * @param title DOCUMENT ME!
     * @param message if non null, a pupup showing the message will appar while the worker is
     *        performing the long task
     * @param beep if true, when the user clicks on the blocked user interface or pressed a key, a
     *        beep will be produced using the standard operating system beep
     */
    public BlockingSwingWorker(Component aComponent, String title, String message, boolean beep) {
        setAComponent(aComponent);
        this.message = message;
        this.title = title;
        this.beep = beep;

        final Runnable doFinished = new Runnable() {
                public void run() {
                    finished();
                }
            };

        Runnable doConstruct = new Runnable() {
                public void run() {
                    try {
                        construct();
                    } finally {
                        threadVar.clear();
                    }

                    // Execute the doFinished runnable on the Swing dispatcher thread
                    SwingUtilities.invokeLater(doFinished);
                }
            };

        // Group the new worker thread in the same group as the "spawner" thread
        Thread t = new Thread(Thread.currentThread().getThreadGroup(), doConstruct);
        threadVar = new ThreadVar(t);
    }

    /**
     * Activate the capabilities of glasspane
     */
    private void activateGlassPane() {
        // create the message dialog
        if ((message != null) && !message.trim().equals("")) {
            dialog = new MessagePopup(getAComponent(), title, message, beep);
            dialog.show();
        }

        // Mount the glasspane on the component window
        GlassPane aPane = GlassPane.mount(getAComponent(), true, beep);

        // keep track of the glasspane as an instance variable
        setGlassPane(aPane);

        if (getGlassPane() != null) {
            // Start interception UI interactions
            getGlassPane().setVisible(true);
        }
    }

    /**
     * Enable the glass pane (to disable unwanted UI manipulation), then spawn the non-UI logic on
     * a separate thread.
     */
    private void construct() {
        activateGlassPane();

        try {
            doNonUILogic();
        } catch (RuntimeException e) {
        }
    }

    /**
     * Deactivate the glasspane
     */
    private void deactivateGlassPane() {
        if (dialog != null) {
            dialog.close();
        }

        if (getGlassPane() != null) {
            // Stop UI interception
            getGlassPane().setVisible(false);
        }
    }

    /**
     * This method will be implemented by the inner class of SwingWorker It should only consist of
     * the logic that's unrelated to UI
     *
     * @throws java.lang.RuntimeException thrown if there are any errors in the non-ui logic
     */
    protected abstract void doNonUILogic() throws RuntimeException;

    /**
     * This method will be implemented by the inner class of SwingWorker It should only consist of
     * the logic that's related to UI updating, after the doNonUILogic() method is done.
     *
     * @throws RuntimeException thrown if there are any problems executing the ui update logic
     */
    protected void doUIUpdateLogic() throws RuntimeException {
    }

    /**
     * Called on the event dispatching thread (not on the worker thread) after the
     * <code>construct</code> method has returned.
     */
    protected void finished() {
        try {
            deactivateGlassPane();
            doUIUpdateLogic();
        } catch (RuntimeException e) {
            // Do nothing, simply cleanup below
            System.out.println("SwingWorker error" + e);
        } finally {
            // Allow original component to get the focus
            if (getAComponent() != null) {
                getAComponent().requestFocus();
            }
        }
    }

    /**
     * Getter method
     *
     * @return java.awt.Component
     */
    protected Component getAComponent() {
        return aComponent;
    }

    /**
     * Getter method
     *
     * @return GlassPane
     */
    protected GlassPane getGlassPane() {
        return glassPane;
    }

    /**
     * A new method that interrupts the worker thread.  Call this method to force the worker to
     * stop what it's doing.
     */
    public void interrupt() {
        Thread t = threadVar.get();

        if (t != null) {
            t.interrupt();
        }

        threadVar.clear();
    }

    /**
     * Setter method
     *
     * @param newAComponent java.awt.Component
     */
    protected void setAComponent(Component newAComponent) {
        aComponent = newAComponent;
    }

    /**
     * Setter method
     *
     * @param newGlassPane GlassPane
     */
    protected void setGlassPane(GlassPane newGlassPane) {
        glassPane = newGlassPane;
    }

    /**
     * Start the worker thread.
     */
    public void start() {
        Thread t = threadVar.get();

        if (t != null) {
            t.start();
        }
    }

    /**
     * Class to maintain reference to current worker thread under separate synchronization control.
     */
    private static class ThreadVar {
        private Thread thread;

        ThreadVar(Thread t) {
            thread = t;
        }

        synchronized Thread get() {
            return thread;
        }

        synchronized void clear() {
            thread = null;
        }
    }
}

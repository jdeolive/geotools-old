/*
 * Created on 8-gen-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.io;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for managing memory mapped buffers 
 * 
 * @author wolf
 */
public class NIOBufferUtils {
	/**
	 * The logger for reporting io problems
	 */
	private static final Logger LOGGER = Logger.getLogger("org.geotools.io");
    
    private NIOBufferUtils() {
        // utility class, don't instantiate
    }
	
	/**
	 * Really closes a MappedByteBuffer without the need to wait for
	 * garbage collection
	 * @param buffer
	 * @see MappedByteBuffer
	 */
	public static void clean(final Object buffer) {
		AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				try {
					Method getCleanerMethod =
						buffer.getClass().getMethod("cleaner", null);
					getCleanerMethod.setAccessible(true);
                    Object cleaner = getCleanerMethod.invoke(buffer,  null);
                    Method clean = cleaner.getClass().getMethod("clean", null);
                    clean.invoke(cleaner, null);
				} catch (Exception e) {
                    // This really is a show stopper on windows
					LOGGER.log(Level.SEVERE, "Error attempting to close a mapped byte buffer", e);
				}
				return null;
			}
		});
	}

}

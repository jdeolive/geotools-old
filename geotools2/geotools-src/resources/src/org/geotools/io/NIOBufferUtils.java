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
 * Utility class for managing memory mapped buffers. 
 * 
 * @author wolf
 */
public class NIOBufferUtils {
	/**
	 * The logger for reporting io problems
	 */
	private static final Logger LOGGER = Logger.getLogger("org.geotools.io");
    private static byte warned = 0;
	
	/**
     * Really closes a MappedByteBuffer without the need to wait for
     * garbage collection. Any problems with closing
     * a buffer on Windows (the problem child in this case) will be logged as
     * SEVERE to the logger of the package name. To force logging of errors, 
     * set the System property "org.geotools.io.debugBuffer" to "true".
     * @param buffer
     * @see MappedByteBuffer
     * @return true if the operation was successful, false otherwise.
     */
	public static boolean clean(final java.nio.ByteBuffer buffer) {
        if (buffer == null || ! buffer.isDirect() ) 
            return false;
        
		Boolean b = (Boolean) AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
                Boolean success = Boolean.FALSE;
				try {
					Method getCleanerMethod =
						buffer.getClass().getMethod("cleaner", null);
					getCleanerMethod.setAccessible(true);
                    Object cleaner = getCleanerMethod.invoke(buffer,  null);
                    Method clean = cleaner.getClass().getMethod("clean", null);
                    clean.invoke(cleaner, null);
                    success = Boolean.TRUE;
				} catch (Exception e) {
                    // This really is a show stopper on windows
                    if (isLoggable())
                        log(e,buffer);
				}
				return success;
			}
		});
        
        return b.booleanValue();
	}
    
    private static boolean isLoggable() {
        return warned == 0 && (
            System.getProperty("org.geotools.io.debugBuffer","false").equals("true") ||
            System.getProperty("os.name").indexOf("Windows") >= 0
        );
    }
    
    private static void log(Exception e,java.nio.ByteBuffer buffer) {
        warned = (byte) 1;
        String message = "Error attempting to close a mapped byte buffer : " + buffer.getClass().getName();
        message += "\n JVM : " + System.getProperty("java.version") + " " + System.getProperty("java.vendor");
        LOGGER.log(Level.SEVERE, message, e);
    }

}

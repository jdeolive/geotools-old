package org.opengis.cs;

// JDK's classes
import java.rmi.RemoteException;


/**
 * Definition of linear units.
 *
 * @version 1.01
 * @since   1.00
 * @author Martin Daly
 */
public interface CS_LinearUnit extends CS_Unit
{
    /**
     * Returns the number of meters per LinearUnit.
     *
     * @throws RemoteException if a remote method call failed.
     */
    double getMetersPerUnit() throws RemoteException;
}

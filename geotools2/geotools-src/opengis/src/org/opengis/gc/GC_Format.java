/*
 * OpenGIS® Grid Coverage Services Implementation Specification
 * Copyright (2001) OpenGIS consortium
 *
 * THIS COPYRIGHT NOTICE IS A TEMPORARY PATCH.   Version 1.00 of official
 * OpenGIS's interface files doesn't contain a copyright notice yet. This
 * file is a slightly modified version of official OpenGIS's interface.
 * Changes have been done in order to fix RMI problems and are documented
 * on the SEAGIS web site (seagis.sourceforge.net). THIS FILE WILL LIKELY
 * BE REPLACED BY NEXT VERSION OF OPENGIS SPECIFICATIONS.
 */
package org.opengis.gc;

// Remote Method Invocation
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * This interface is a discovery mechanism to determine the formats supported by a
 * {@link GC_GridCoverageExchange} implementation.
 * A <code>GC_GridCoverageExchange</code> implementation can support a number of
 * file format or resources.
 *
 * @version 1.00
 * @since   1.00
 */
public interface GC_Format extends Remote
{
    /**
     * Name of the file format.
     * This name is used as the name of the file in the
     * {@link GC_GridCoverageExchange#exportTo exportTo} operation.
     *
     * @return the name of the file format.
     * @throws RemoteException if a remote method call failed.
     */
    String getName() throws RemoteException;

    /**
     * Description of the file format.
     * If no description, the value will be a null or empty string.
     *
     * @return the description of the file format.
     * @throws RemoteException if a remote method call failed.
     */
    String getDescription() throws RemoteException;

    /**
     * Vendor or agency for the format.
     *
     * @return the vendor or agency for the format.
     * @throws RemoteException if a remote method call failed.
     */
    String getVendor() throws RemoteException;

    /**
     * Documentation URL for the format.
     *
     * @return the documentation URL for the format.
     * @throws RemoteException if a remote method call failed.
     */
    String getDocURL() throws RemoteException;

    /**
     * Version number of the format.
     *
     * @return the version number of the format.
     * @throws RemoteException if a remote method call failed.
     */
    String getVersion() throws RemoteException;

    /**
     * Number of optional parameters for the
     * {@link GC_GridCoverageExchange#exportTo exportTo} operation.
     *
     * @return the number of optional parameters for the exportTo operation.
     * @throws RemoteException if a remote method call failed.
     */
    int getNumParameters() throws RemoteException;

    /**
     * Retrieve the parameter information for a given index.
     *
     * @param index Index to the parameter.
     * @return the parameter information for the given index.
     * @throws RemoteException if a remote method call failed.
     */
    GC_ParameterInfo getParameterInfo(int index) throws RemoteException;
}

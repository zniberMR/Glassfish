/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise;

import java.rmi.*;

import com.sun.ejb.Container;


/**
 * The ProtocolManager interface specifies the functionality of the
 * remote communication layer, which provides the support for
 * distribution described in Chapter 13 of the EJB spec.
 * Possible implementations of the ProtocolManager include RMI/IIOP,
 * RMI/JRMP, RMI/DCOM, RMI/HTTP ....
 * @author Vivek Nagar
 */
public interface ProtocolManager {

    /**
     * Return a factory that can be used to create/destroy remote
     * references for a particular EJB type.
     */
    RemoteReferenceFactory getRemoteReferenceFactory(Container container,
                                                     boolean remoteHomeView,
                                                     String id);


    /**
     * Return true if the two object references refer to the same
     * remote object.
     */
    boolean isIdentical(Remote obj1, Remote obj2);

    /**
     * Check that all Remote interfaces implemented by target object
     * conform to the rules for valid RMI-IIOP interfaces.  Throws
     * runtime exception if validation fails.  
     */
    void validateTargetObjectInterfaces(Remote targetObj);

    /**
     * Map the RMI exception to a protocol-specific (e.g. CORBA) exception
     */
    Throwable mapException(Throwable exception);


    /**
     * Connect the RMI object to the protocol.
     */
    void connectObject(Remote remoteObj) throws RemoteException;



    /* All these APIs are used by J2EEServer at startup *****************/
    /* Not used any more - DHIRU
    public void setPersistentServerId(int id);
    public void setPersistentServerPort(int port);
    public void initTransactionService(String jtsclass) ;
    */
    public void initializeNaming(java.io.File dbDir, int orbInitialPort) throws Exception;
    /***********************************************************************
    public void setDaemonPort(int port);
    public void setLocator(com.sun.enterprise.activation.Locator locator);
    public int getListenerPort(String type) ;
    /***********************************************************************/

}
    


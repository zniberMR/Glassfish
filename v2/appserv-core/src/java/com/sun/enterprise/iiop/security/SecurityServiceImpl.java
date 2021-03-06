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

package com.sun.enterprise.iiop.security;

import java.net.Socket;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.security.CodeSource;
import java.security.Policy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.X509Certificate;
import javax.security.auth.Subject;
import com.sun.enterprise.log.Log;
import com.sun.enterprise.util.ORBManager;
import com.sun.enterprise.InvocationManager;
import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.InvocationException;
import com.sun.enterprise.Switch;
import com.sun.enterprise.security.LoginContext;
import com.sun.enterprise.security.CORBAObjectPermission;
import com.sun.enterprise.security.auth.login.PasswordCredential;
import com.sun.enterprise.security.auth.LoginContextDriver;
import com.sun.enterprise.iiop.POAProtocolMgr;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

import java.util.Set;
import java.util.logging.*;
import com.sun.logging.*;

/** 
 * This class provides the implementation of the SPI between the 
 * CSIV2 layer and the RI.
 * @author Vivek Nagar
 * @author Harpreet Singh
 */

public class SecurityServiceImpl implements SecurityService
{

    private static java.util.logging.Logger _logger=null;
    static{
       _logger=LogDomains.getLogger(LogDomains.CORBA_LOGGER);
        }
    // ignoring exceptions thrown for _is_a calls from the ORB
    private static String IS_A = "_is_a"; 

    private Policy policy;


    public SecurityServiceImpl() {

	policy = Policy.getPolicy();
    }


    /**
     * This is called by the CSIv2 interceptor on the client before
     * sending the IIOP message.
     * @param the effective_target field of the PortableInterceptor
     * ClientRequestInfo object.
     * @return a SecurityContext which is marshalled into the IIOP msg
     * by the CSIv2 interceptor.
     */
    public SecurityContext getSecurityContext(
                            org.omg.CORBA.Object effective_target) 
        throws InvalidMechanismException, InvalidIdentityTokenException
        {
        SecurityContext context = null;

        IOR ior = ((com.sun.corba.ee.spi.orb.ORB)ORBManager.getORB()).getIOR(effective_target, false);
	if (StubAdapter.isStub(effective_target)) {
	    if (StubAdapter.isLocal(effective_target)) {
		return null;
	    }
	}

        try {
            SecurityMechanismSelector sms = new SecurityMechanismSelector();
            context = sms.selectSecurityContext(ior);
        } catch (InvalidMechanismException ime){ // let this pass ahead
            _logger.log(Level.SEVERE,"iiop.invalidmechanism_exception",ime);
            throw new InvalidMechanismException (ime.getMessage());
        } catch(InvalidIdentityTokenException iite){
           _logger.log(Level.SEVERE,"iiop.invalididtoken_exception",iite);
            throw new InvalidIdentityTokenException(iite.getMessage());
            // let this pass ahead
        } catch(SecurityMechanismException sme) {
           _logger.log(Level.SEVERE,"iiop.secmechanism_exception",sme);
            throw new RuntimeException(sme.getMessage());
        }
        return context;
    }

    /**
     * This is called by the CSIv2 interceptor on the client after
     * a reply is received.
     * @param the reply status from the call. The reply status field
     * could indicate an authentication retry.
     * The following is the mapping of PI status to the reply_status field
     * PortableInterceptor::SUCCESSFUL -> STATUS_PASSED
     * PortableInterceptor::SYSTEM_EXCEPTION -> STATUS_FAILED
     * PortableInterceptor::USER_EXCEPTION -> STATUS_PASSED
     * PortableInterceptor::LOCATION_FORWARD -> STATUS_RETRY
     * PortableInterceptor::TRANSPORT_RETRY -> STATUS_RETRY
     * @param the effective_target field of the PI ClientRequestInfo object.
     */
    public void receivedReply(int reply_status, 
                                org.omg.CORBA.Object effective_target)
    {
        if(reply_status == STATUS_FAILED) {
            if(_logger.isLoggable(Level.FINE)){
                _logger.log(Level.FINE,"Failed status");
            }
            // what kind of exception should we throw? 
            throw new RuntimeException("Target did not accept security context");
        } else if(reply_status == STATUS_RETRY) {
            if(_logger.isLoggable(Level.FINE)){
		_logger.log(Level.FINE,"Retry status");
            }
        } else {
            if(_logger.isLoggable(Level.FINE)){
		_logger.log(Level.FINE,"Passed status");
            }
        }
    }

    /**
     * This is called by the CSIv2 interceptor on the server after
     * receiving the IIOP message. If authentication fails a FAILED status
     * is returned. If a FAILED status is returned the CSIV2 interceptor will
     * marshall the MessageError service context and throw the NO_PERMISSION
     * exception.
     * @param the SecurityContext which arrived in the IIOP message.
     * @return the status
     */
    public int setSecurityContext(SecurityContext context, byte[] object_id,
                                  String method)
    {
        if(_logger.isLoggable(Level.FINE)){
            _logger.log(Level.FINE,"ABOUT TO EVALUATE TRUST");
        }

        try {
	    // First check if the client sent the credentials
	    // as required by the object's CSIv2 policy.
	    // evaluateTrust will throw an exception if client did not
	    // conform to security policy.
            SecurityMechanismSelector sms = new SecurityMechanismSelector();
	    SecurityContext ssc = sms.evaluateTrust(context, object_id); 

	    Class cls = null;
            Subject s = null;
            if(ssc == null) {
                return STATUS_PASSED;
            } else {
                if(ssc.authcls != null) {
                    cls = ssc.authcls;
                } else {
                    cls = ssc.identcls;
                }
                s = ssc.subject;
            }

	    // Authenticate the client. An Exception is thrown if login fails.
	    // SecurityContext is set on current thread if login succeeds.
            authenticate(s, cls);

	    // Authorize the client for non-EJB objects.
	    // Auth for EJB objects is done in BaseContainer.preInvoke().
	    if ( authorizeCORBA(object_id, method) )
		return STATUS_PASSED;
	    else 
		return STATUS_FAILED;

        } catch(Exception e) {
            if (!method.equals(IS_A)){
		if(_logger.isLoggable(Level.FINE)){
                    _logger.log(Level.FINE,"iiop.authenticate_exception",e.toString());
                }
            if(_logger.isLoggable(Level.FINE)){
                    _logger.log(Level.FINE,"Authentication Exception",e);
                }
            }
            return STATUS_FAILED;
        }
    }


    // return true if authorization succeeds, false otherwise.
    private boolean authorizeCORBA(byte[] object_id, String method) 
	    throws Exception {

	// Check if target is an EJB
        POAProtocolMgr protocolMgr = (POAProtocolMgr)
				    Switch.getSwitch().getProtocolManager();
        // Check to make sure protocolMgr is not null. 
        // This could happen during server initialization or if this call
        // is on a callback object in the client VM. 
        if ( protocolMgr == null )
            return true;

	if ( protocolMgr.getEjbDescriptor(object_id) != null )
	    return true; // an EJB object

	CORBAObjectPermission perm = new CORBAObjectPermission("*", method);

	// Create a ProtectionDomain for principal on current thread.
	com.sun.enterprise.security.SecurityContext sc = 
		    com.sun.enterprise.security.SecurityContext.getCurrent();
        Set principalSet = sc.getPrincipalSet();
	Principal[] principals = (principalSet == null ? null :
                         (Principal []) principalSet.toArray(new Principal[0]));
	CodeSource cs = new CodeSource(new java.net.URL("file://"),  
                            (java.security.cert.Certificate[]) null);
        ProtectionDomain prdm = new ProtectionDomain(cs, null, null, principals);

	// Check if policy gives principal the permissions
	boolean result = policy.implies(prdm, perm);

	if ( _logger.isLoggable(Level.FINE) ) {
	    _logger.log(Level.FINE, "CORBA Object permission evaluation result="
				    + result + " for method=" + method);
	}
	return result;
    }


    /**
     * This is called by the CSIv2 interceptor on the server before
     * sending the reply.
     * @param the SecurityContext which arrived in the IIOP message.
     */
    public void sendingReply(SecurityContext context)
    {
        // NO OP
    }
    /**
     * This is called on the server to unset the security context 
     * this is introduced to prevent the re-use of the thread
     * security context on re-use of the thread.
     */
    public void unsetSecurityContext(){
        // logout method from LoginContext not called 
        // as we dont want to unset the appcontainer context

        // First check if this is a local call.
        boolean isLocal = true;
        ServerConnectionContext scc = 
                        SecurityMechanismSelector.getServerConnectionContext();
        if ( scc != null && scc.getSocket() != null )
            isLocal = false;

        if ( !isLocal) 
            com.sun.enterprise.security.SecurityContext.setCurrent(null); 
    }

    /**
     * Authenticate the user with the specified subject and
     * credential class.
     */
    private void authenticate(Subject s, Class cls) 
        throws SecurityMechanismException
    {
        // authenticate
        try {
            final Subject fs = s;
            final Class cl = cls;
            AccessController.doPrivileged(new PrivilegedAction() {
                public java.lang.Object run() {
                    LoginContextDriver.login(fs, cl);
                    return null;
                }
            });
        } catch(Exception e) {
            if(_logger.isLoggable(Level.SEVERE)){
		_logger.log(Level.SEVERE,"iiop.login_exception",e.toString());
            }
            if(_logger.isLoggable(Level.FINE)){
		_logger.log(Level.FINE,"Login Exception",e);
            }
            throw new SecurityMechanismException("Cannot login user:" + 
                        e.getMessage());
        }
    }

}



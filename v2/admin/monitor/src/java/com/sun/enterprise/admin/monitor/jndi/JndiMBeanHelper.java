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

/*
 * JndiMBeanHelper.java
 *
 * Created on March 10, 2004, 9:36 AM
 */

package com.sun.enterprise.admin.monitor.jndi;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import com.sun.enterprise.util.i18n.StringManager;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * The JndiMBeanHelper serves as a delegator object for the JndiMBeanImpl
 * for interacting with the application server's naming service. The client
 * of the JndiMBean will call the getNames method which in turn calls this 
 * object's getJndiEntriesByContextPath to retrieve all those names bound
 * in that naming service's subcontext.
 *
 * @author  Rob Ruyak
 */
public class JndiMBeanHelper {
    
    private InitialContext context;
    private static final Logger logger = 
        Logger.getLogger(AdminConstants.kLoggerName);
    private static final StringManager sm = 
        StringManager.getManager(JndiMBeanHelper.class);
    private final String SYSTEM_SUBCONTEXT = "__SYSTEM";
    
    /** Creates a new instance of JndiMBeanHelper */
    public JndiMBeanHelper() {
        initialize();
    }
    
    /**
     * Initializes the JndiMBeanHelper object upon creation. It specifically 
     * creates an InitialContext instance for querying the naming service
     * during certain method invocations.
     */
    void initialize() {
        try {
            context = new InitialContext(); 
        } catch(javax.naming.NamingException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    /**
     * Gets the jndi entries from the application server's naming 
     * service given a particular context/subcontext. 
     *
     * @param contextPath The naming context or subcontext to query.
     * @return An {@link ArrayList} of {@link NameClassPair} objects.
     * @throws NamingException if an error occurs when connection with
     *         the naming service is established or retrieval fails. 
     */
    ArrayList getJndiEntriesByContextPath(String contextPath) 
            throws NamingException {
        ArrayList names = new ArrayList();
        NamingEnumeration ee = null;
        if(contextPath == null) { contextPath = ""; }
        try {
            ee = context.list(contextPath);
        } catch(NameNotFoundException e) {
            String msg = sm.getString("monitor.jndi.context_notfound", 
                new Object[]{contextPath});
            logger.log(Level.WARNING, msg);
            NamingException ne = new NamingException(msg);
            //ne.initCause(e);
            System.out.println("Exception print: " + ne);
            throw ne;
        }
        names = toNameClassPairArray(ee);
        return names;
    }
    
    /**
     * Changes a NamingEnumeration object into an ArrayList of 
     * NameClassPair objects.
     *
     * @param ee An {@link NamingEnumeration} object to be transformed.
     * @return An {@link ArrayList} of {@link NameClassPair} objects.
     * @throws Exception if an error occurs when iterating over the enum.
     */
    ArrayList toNameClassPairArray(NamingEnumeration ee)  
            throws javax.naming.NamingException{
        ArrayList names = new ArrayList();
        while(ee.hasMore()) {
            // don't add the __SYSTEM subcontext - Fix for 6041360
            Object o = ee.next();
            if(o.toString().indexOf(SYSTEM_SUBCONTEXT) == -1) {
                names.add(o);
            }
        }
        return names;
    }
}

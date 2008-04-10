/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.logging;

import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.ResourceBundle;

/**
 * Class LogDomains
 */
public class LogDomains
{

    /**
     * DOMAIN_ROOT the prefix for the logger name. This is public only
     * so it can be accessed w/in the ias package space.
     */
    public static final String DOMAIN_ROOT = "javax.";

    /**
     * PACKAGE_ROOT the prefix for the packages where logger resource 
     * bundles reside. This is public only so it can be accessed w/in 
     * the ias package space.
     */
    public static final String PACKAGE_ROOT = "com.sun.logging.";

    /**
     * RESOURCE_BUNDLE the name of the logging resource bundles.
     */
    public static final String RESOURCE_BUNDLE = "LogStrings";

    /**
     * Field
     */
    public static final String TOOLS_LOGGER = DOMAIN_ROOT + "enterprise.system.tools";

    /**
     * Field
     */
    public static final String EJB_LOGGER = DOMAIN_ROOT + "enterprise.system.container.ejb";

    /**
     * JavaMail Logger 
     */
    public static final String JAVAMAIL_LOGGER = DOMAIN_ROOT + "enterprise.resource.javamail";
    
    /**
     * IIOP Logger
    public static final String IIOP_LOGGER = DOMAIN_ROOT + "enterprise.resource.iiop";
     */


    /**
     * JMS Logger
     */
    public static final String JMS_LOGGER = DOMAIN_ROOT + "enterprise.resource.jms";

    /**
     * Field
     */
    public static final String WEB_LOGGER = DOMAIN_ROOT + "enterprise.system.container.web";
    
    /**
     * Field
     */
    public static final String CMP_LOGGER = DOMAIN_ROOT + "enterprise.system.container.cmp";

    /**
     * Field
     */
    public static final String JDO_LOGGER = DOMAIN_ROOT + "enterprise.resource.jdo";
    
    /**
     * Field
     */
    public static final String ACC_LOGGER = DOMAIN_ROOT + "enterprise.system.container.appclient";

    /**
     * Field
     */
    public static final String MDB_LOGGER = DOMAIN_ROOT + "enterprise.system.container.ejb.mdb";

    /**
     * Field
     */
    public static final String SECURITY_LOGGER = DOMAIN_ROOT + "enterprise.system.core.security";

    /**
     * Field
     */
    public static final String TRANSACTION_LOGGER = DOMAIN_ROOT + "enterprise.system.core.transaction";

    /**
     * Field
     */
    public static final String CORBA_LOGGER = DOMAIN_ROOT + "enterprise.resource.corba";

    /**
     * Field
     */
    public static final String ROOT_LOGGER = DOMAIN_ROOT + "enterprise";
    /**
     * Field
     */
    //START OF IASRI 4660742
    /**
     * Field
     */
    public static final String UTIL_LOGGER = DOMAIN_ROOT + "enterprise.system.util";
    /**
     * Field
     */
    public static final String NAMING_LOGGER = DOMAIN_ROOT + "enterprise.system.core.naming";

    /**
     * Field
     */
    public static final String JNDI_LOGGER = DOMAIN_ROOT + "enterprise.system.core.naming";
    /**
     * Field
     */
    public static final String APPVERIFY_LOGGER = DOMAIN_ROOT + "enterprise.system.tools.verifier";
    /**
     * Field
     */
    public static final String ACTIVATION_LOGGER = DOMAIN_ROOT + "enterprise.system.activation";
    /**
     * Field
     */
    public static final String JTA_LOGGER = DOMAIN_ROOT + "enterprise.resource.jta";
    
    /**
     * Resource Logger 
     */
    
	public static final String RSR_LOGGER = DOMAIN_ROOT + "enterprise.resource.resourceadapter";
    //END OF IASRI 4660742

	/**
	* Deployment Logger 
	*/
    public static final String DPL_LOGGER = DOMAIN_ROOT + "enterprise.system.tools.deployment";

    /**
     * Deployment audit logger
     */
    public static final String DPLAUDIT_LOGGER = DOMAIN_ROOT + "enterprise.system.tools.deployment.audit";
    
    /**
     * Field
     */
    public static final String DIAGNOSTICS_LOGGER = DOMAIN_ROOT + "enterprise.system.tools.diagnostics";
    
    /** JAXRPC Logger */
        public static final String JAXRPC_LOGGER = DOMAIN_ROOT + "enterprise.system.webservices.rpc";

        /** JAXR Logger */
        public static final String JAXR_LOGGER = DOMAIN_ROOT + "enterprise.system.webservices.registry";

        /** SAAJ Logger */
        public static final String SAAJ_LOGGER = DOMAIN_ROOT + "enterprise.system.webservices.saaj";
        
       /** Self Management Logger */
       public static final String SELF_MANAGEMENT_LOGGER = DOMAIN_ROOT + "enterprise.system.core.selfmanagement";        

    
    /** 
     * Admin Logger
    */
    public static final String ADMIN_LOGGER = 
            DOMAIN_ROOT + "enterprise.system.tools.admin";
	/** Server Logger */
	public static final String SERVER_LOGGER= DOMAIN_ROOT + "enterprise.system";
	/** core Logger */
	public static final String CORE_LOGGER= DOMAIN_ROOT + "enterprise.system.core";
	/** classloader Logger */
	public static final String LOADER_LOGGER= DOMAIN_ROOT + "enterprise.system.core.classloading";

    /** Config Logger */
	public static final String CONFIG_LOGGER = DOMAIN_ROOT + "enterprise.system.core.config";

    /** Process Launcher Logger */
	public static final String PROCESS_LAUNCHER_LOGGER = DOMAIN_ROOT + "enterprise.tools.launcher";

    /** GMS Logger */
    public static final String GMS_LOGGER = DOMAIN_ROOT +"ee.enterprise.system.gms";


    /**
     * This is temporary and needed so that IAS can run with or without
     * the com.sun.enterprise.server.logging.ServerLogger. The subclassed 
     * addLogger() method there automatically appends the logger name.
     **/
    private static String getLoggerResourceBundleName(String loggerName) {      
        String result = loggerName + "." + RESOURCE_BUNDLE;        
        return result.replaceFirst(DOMAIN_ROOT, PACKAGE_ROOT);
    } 
	
    /**
     * Method getLogger
     *
     *
     * @param name
     *
     * @return
     */
    public static Logger getLogger(String name) {
        Logger l = LogManager.getLogManager().getLogger(name);
        if (l==null) {
            l = new Logger(name, null) {
                /**
                 * Retrieve the localization resource bundle for this
                 * logger for the current default locale.  Note that if
                 * the result is null, then the Logger will use a resource
                 * bundle inherited from its parent.
                 *
                 * @return localization bundle (may be null)
                 */
                public ResourceBundle getResourceBundle() {                    
                    return null;
                }
            };
            LogManager.getLogManager().addLogger(l);
        }
        return l;
        //return Logger.getLogger(name, getLoggerResourceBundleName(name));
    }
}

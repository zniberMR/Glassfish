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

package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.*;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.impl.CookedModuleDefinition;
import com.sun.enterprise.module.impl.DirectoryBasedRepository;
import com.sun.logging.LogDomains;
import org.glassfish.api.Startup;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Habitat;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.jar.Attributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for Glassfish v3 startup
 *
 * @author dochez
 */
@Service
public class AppServerStartup implements ModuleStartup {
    
    StartupContext context;

    final static Logger logger = LogDomains.getLogger(LogDomains.CORE_LOGGER);

    @Inject
    V3Environment env;

    @Inject
    Habitat habitat;

    @Inject
    public void setStartupContext(StartupContext context) {
        this.context = context;
    }
    
    public void run() {

        logger.fine("GlassFish v3 starting");
        logger.fine("HK2 initialized in " + (System.currentTimeMillis() - context.getCreationTime()) + " ms");
        if (context==null) {
            System.err.println("Startup context not provided, cannot continue");
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Startup class : " + this.getClass().getName());
        }

        // get the system registry
        Module mainModule = Module.find(this.getClass());
        ModulesRegistry systemRegistry = mainModule.getRegistry();

        // set the parent class loader to the shared module class loader, if packaged
        Module parentModule = systemRegistry.makeModuleFor("org.glassfish.core:shared-components", null);
        if(parentModule!=null) {
            systemRegistry.setParentClassLoader(parentModule.getClassLoader());
        }
        
        // prepare the global variables
        habitat.addComponent(null, systemRegistry);
        habitat.addComponent(LogDomains.CORE_LOGGER, logger);

        // run the init services
        Collection<Init> inits = habitat.getAllByContract(Init.class);
        for (Init init : inits) {
            logger.info("Init service : " + init);
        }

        // run the startup services
        Collection<Startup> startups = habitat.getAllByContract(Startup.class);
        for (Startup startup : startups) {
            logger.info("Startup service : " + startup);
        }

        logger.info("Glassfish v3 started in "
                    + (Calendar.getInstance().getTimeInMillis() - context.getCreationTime()) + " ms");

        
    }
}

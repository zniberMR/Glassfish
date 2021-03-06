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

package com.sun.enterprise.tools.upgrade.logging;

import java.io.*;
import java.util.logging.*;
import com.sun.enterprise.tools.upgrade.common.*;

/**
 *
 * author : Servesh Singh
 *
 */

public class LogService {

    public static final String UPGRADE_LOGGER="com.sun.enterprise.tools.upgrade";
    public static final String UPGRADE_CLI_LOGGER="com.sun.enterprise.tools.upgrade.cli";
    public static final String UPGRADE_CERTCONVERSION_LOGGER="com.sun.enterprise.tools.upgrade.certconversion";
    public static final String UPGRADE_GUI_LOGGER = "com.sun.enterprise.tools.upgrade.gui";   
    public static final String UPGRADE_COMMON_LOGGER = "com.sun.enterprise.tools.upgrade.common";    
    public static final String UPGRADE_MISCCONFIG_LOGGER = "com.sun.enterprise.tools.upgrade.miscconfig";
    public static final String UPGRADE_TRANSFORM_LOGGER = "com.sun.enterprise.tools.upgrade.transform";
    public static final String UPGRADE_REALM_LOGGER = "com.sun.enterprise.tools.upgrade.realm";    
    private static LogFormatter formatter;
    private static FileHandler loghandler;
    //Default log level
    private static final Level DEFAULT_LEVEL = Level.INFO;

    private static Level logLevel = null;


    public static void initialize(String fileName) throws IOException{
	LogManager.getLogManager().reset();
	formatter = new LogFormatter();
    boolean append = true;
    int limit = 1000000;
    loghandler = new FileHandler(fileName,limit,1,append);
    logLevel =  getLogLevel();
    loghandler.setLevel(logLevel );
	loghandler.setFormatter(formatter);
    }

    /**
     * private helper method to decipher the log level from the system property.
     * Defaults to the statically defined field DEFAULT_LEVEL
     */
    private static Level getLogLevel()
    {
        String logLevel = System.getProperty("com.sun.aas.utool.LogLevel");
        if (logLevel != null )
        {
            try{
                return Level.parse(logLevel);
            }catch(IllegalArgumentException  e) {
                 return DEFAULT_LEVEL;
            }
        }
        return DEFAULT_LEVEL;
    }

    public static Logger getLogger(String name) {
	Logger logger = Logger.getLogger(name);

        logger.setLevel(logLevel);
	Handler[] h = logger.getHandlers();
	for (int i = 0; i < h.length; i++) {
	    logger.removeHandler(h[i]);
	}
	logger.addHandler(loghandler);
        loghandler.setLevel(logLevel);
	return logger;
    }

    public static void addLogMessageListener(LogMessageListener listener){
        formatter.addLogMessageListener(listener);
    }

    public static void removeLogMessageListener(LogMessageListener listener){
	formatter.removeLogMessageListener(listener);
    }

}


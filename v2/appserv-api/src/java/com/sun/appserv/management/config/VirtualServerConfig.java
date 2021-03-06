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
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/config/VirtualServerConfig.java,v 1.1 2006/12/02 06:03:27 llc Exp $
 * $Revision: 1.1 $
 * $Date: 2006/12/02 06:03:27 $
 */

package com.sun.appserv.management.config;

import java.util.Map;


import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.base.Container;


/**
	 Configuration for the &lt;virtual-server&gt; element.
*/

public interface VirtualServerConfig
	extends NamedConfigElement, PropertiesAccess, Container
{
/** The j2eeType as returned by {@link com.sun.appserv.management.base.AMX#getJ2EEType}. */
	public static final String	J2EE_TYPE			= XTypes.VIRTUAL_SERVER_CONFIG;


	public String	getDefaultWebModule();
	public void	setDefaultWebModule( String value );

	public String	getHosts();
	public void	setHosts( String value );

	public String	getHTTPListeners();
	public void	setHTTPListeners( String value );

    //** default: "${com.sun.aas.instanceRoot}/logs/server.log" */
	public String	getLogFile();
	public void	setLogFile( String value );

	public String	getState();
	public void	setState( String value );
	
	public String	getDocRoot();
	public void	setDocRoot( String value );

	/**
		Removes http-access-log element.
	 */
	void removeHTTPAccessLogConfig();

	/**
		Get the HTTPAccessLogConfig MBean.
	 */
	public HTTPAccessLogConfig	getHTTPAccessLogConfig();

	/**
		Creates new http-access-log element.
	 
		@param	ipOnly
		@param	logDirectory
		@param	reserved
		@return A proxy to the HTTPAccessLogConfig MBean.
	 */
	public HTTPAccessLogConfig createHTTPAccessLogConfig(
		final boolean	ipOnly,
		final String	logDirectory,
		final Map<String,String>		reserved );
}

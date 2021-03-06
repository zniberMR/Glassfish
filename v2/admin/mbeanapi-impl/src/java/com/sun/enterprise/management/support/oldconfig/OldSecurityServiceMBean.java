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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/**
	Generated: Sat Jun 26 15:25:11 PDT 2004
	Generated from:
	com.sun.appserv:type=security-service,config=default-config,category=config
	com.sun.appserv:type=security-service,config=server-config,category=config
*/

package com.sun.enterprise.management.support.oldconfig;

import java.util.Properties;

import javax.management.ObjectName;
import javax.management.AttributeList;


public interface OldSecurityServiceMBean extends OldProperties
{
	public String	getAnonymousRole();
	public void	setAnonymousRole( final String value );

	public boolean	getAuditEnabled();
	public void	setAuditEnabled( final boolean value );

	public String	getAuditModules();
	public void	setAuditModules( final String value );

	public String	getDefaultPrincipalPassword();
	public void	setDefaultPrincipalPassword( final String value );

	public String	getDefaultPrincipal();
	public void	setDefaultPrincipal( final String value );

	public String	getDefaultRealm();
	public void	setDefaultRealm( final String value );

	public String	getJacc();
	public void	setJacc( final String value );


// -------------------- Operations --------------------
	public ObjectName	createAuditModule( final AttributeList attribute_list );
	public ObjectName	createAuthRealm( final AttributeList attribute_list );
	public ObjectName	createJaccProvider( final AttributeList attribute_list );
	public ObjectName	createMessageSecurityConfig( final AttributeList attribute_list );
	public boolean	destroyConfigElement();
	public javax.management.ObjectName[]	getAuditModule();
	public ObjectName	getAuditModuleByName( final String key );
	public javax.management.ObjectName[]	getAuthRealm();
	public ObjectName	getAuthRealmByName( final String key );
	public String	getDefaultAttributeValue( final String attributeName );
	public javax.management.ObjectName[]	getJaccProvider();
	public ObjectName	getJaccProviderByName( final String key );
	public javax.management.ObjectName[]	getMessageSecurityConfig();
	public ObjectName	getMessageSecurityConfigByAuthLayer( final String key );
	public void	removeAuditModuleByName( final String key );
	public void	removeAuthRealmByName( final String key );
	public void	removeJaccProviderByName( final String key );
	public void	removeMessageSecurityConfigByAuthLayer( final String key );

}
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
package com.sun.enterprise.management.config;

import java.util.Set;
import java.util.HashSet;

import javax.management.ObjectName;

import com.sun.appserv.management.config.PropertiesAccess;
import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;

import com.sun.appserv.management.config.ResourceConfig;

import com.sun.appserv.management.util.misc.GSetUtil;


import com.sun.enterprise.management.AMXTestBase;
import com.sun.enterprise.management.Capabilities;
import com.sun.enterprise.management.TestUtil;

/**
 */
public final class PropertiesAccessTest extends AMXTestBase
{
		public
	PropertiesAccessTest( )
	{
	}
	
		private Set<ObjectName>
	getAllImplementorsOfProperties()
		throws Exception
	{
		final Set<AMX>	amxs	= getQueryMgr().queryInterfaceSet(
			PropertiesAccess.class.getName(), null);
		
		return( TestUtil.newSortedSet( Util.toObjectNames( amxs ) ) );
			
	}
	
	
		private void
	testCreateEmptyProperty( final PropertiesAccess props )
	{
	    final String NAME   = "test.empty";
	    
		props.createProperty( NAME, "" );
		assert( props.existsProperty( NAME ) );
		props.removeProperty( NAME );
		assert( ! props.existsProperty( NAME ) );
	}
	
		private void
	testPropertiesGet( final PropertiesAccess props )
	{
		final String[]	propNames			= props.getPropertyNames();
		
		for( final String name : propNames )
		{
		    assert( props.existsProperty( name ) );
			final String	value	= props.getPropertyValue( name );
		}
	}
	
		private void
	testPropertiesSetToSameValue( final PropertiesAccess props )
	{
		final String[]	propNames			= props.getPropertyNames();
		
		// get each property, set it to the same value, the verify
		// it's the same.
		for( int i = 0; i < propNames.length; ++i )
		{
			final String	propName	= propNames[ i ];
			
			assert( props.existsProperty( propName ) );
			final String	value	= props.getPropertyValue( propName );
			props.setPropertyValue( propName, value );
			
			assert( props.getPropertyValue( propName ).equals( value ) );
		}
	}
	
	/**
	    Adding or removing test properties to these types does not
	    cause any side effects.  Plus, there is no need to test
	    every MBean.
	 */
	private static final Set<String> TEST_CREATE_REMOVE_TYPES  =
	    GSetUtil.newUnmodifiableStringSet(
	        XTypes.DOMAIN_CONFIG,
	        XTypes.CONFIG_CONFIG,
	        XTypes.PROFILER_CONFIG,
	        XTypes.STANDALONE_SERVER_CONFIG,
	        XTypes.CLUSTERED_SERVER_CONFIG,
	        XTypes.ORB_CONFIG,
	        XTypes.MODULE_MONITORING_LEVELS_CONFIG,
	        XTypes.NODE_AGENT_CONFIG
	        );
	    
		private void
	testPropertiesCreateRemove( final PropertiesAccess props )
	{
		final String[]	propNames			= props.getPropertyNames();

        final AMX amx           = Util.asAMX( props );
        final String j2eeType   = amx.getJ2EEType();
        if ( ! TEST_CREATE_REMOVE_TYPES.contains( j2eeType ) )
        {
            return;
        }

		// add some properties, then delete them
		final int	numToAdd	= 1;
		final long	now	= System.currentTimeMillis();
		for( int i = 0; i < numToAdd; ++i )
		{
			final String	testName	= "__junittest_" + i + now;
			
			if ( props.existsProperty( testName ) )
			{
				failure( "test property already exists: " + testName );
			}
			
			props.createProperty( testName, "value_" + i );
			assert( props.existsProperty( testName ) );
		}
		final int	numProps	= props.getPropertyNames().length;
		
		if ( numProps != numToAdd + propNames.length )
		{
			failure( "expecting " + numProps + " have " + numToAdd + propNames.length );
		}
		
		
		// remove the ones we added
		for( int i = 0; i < numToAdd; ++i )
		{
			final String	testName	= "__junittest_" + i + now;
			
			props.removeProperty( testName );
			assert( ! props.existsProperty( testName ) );
		}
		
		assert( props.getPropertyNames().length == propNames.length );
	}
	
		public void
	checkGetProperties( final ObjectName src )
		throws Exception
	{
		final AMX	proxy	= getProxy( src );
		
		if ( ! (proxy instanceof PropertiesAccess) )
		{
			throw new IllegalArgumentException(
				"MBean does not implement PropertiesAccess: " + quote( src ) );
		}
		
		final PropertiesAccess	props	= (PropertiesAccess)proxy;
		testPropertiesGet( props );
	}
	
		public void
	checkSetPropertiesSetToSameValue( final ObjectName src )
		throws Exception
	{
		final PropertiesAccess	props	= (PropertiesAccess)getProxy( src );
		
		testPropertiesSetToSameValue( props );
	}
	
	
		public void
	checkCreateRemove( final ObjectName src )
		throws Exception
	{
		final PropertiesAccess	props	= (PropertiesAccess)getProxy( src );
		
		testPropertiesCreateRemove( props );
	}

		public synchronized void
	testPropertiesGet()
		throws Exception
	{
		final Set<ObjectName>	all	= getAllImplementorsOfProperties();
		
		testAll( all, "checkGetProperties" );
	}
	
		public synchronized  void
	testPropertiesSetToSameValue()
		throws Exception
	{
		final Set<ObjectName>	all	= getAllImplementorsOfProperties();
		
		testAll( all, "checkSetPropertiesSetToSameValue" );
	}
	
	
		public synchronized void
	testPropertiesCreateRemove()
		throws Exception
	{
	    if ( checkNotOffline( "testPropertiesCreateRemove" ) )
	    {
    		final Set<ObjectName>		all	= getAllImplementorsOfProperties();
    		    
    		testAll( all, "checkCreateRemove" );
		}
	}
	
}



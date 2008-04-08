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
package org.glassfish.admin.amx.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.MBeanServerNotification;
import javax.management.InstanceNotFoundException;

import com.sun.appserv.management.util.jmx.JMXUtil;

/**
    Blocks until an MBean is UNregistered using a CountdownLatch (highly efficient).
    
 */
 public final class UnregistrationListener implements NotificationListener
 {
    final MBeanServerConnection mMBeanServer;
    final ObjectName            mObjectName;
    final CountDownLatch        mLatch;

    public UnregistrationListener( final MBeanServerConnection conn, final ObjectName objectName )
    {
        mMBeanServer = conn;
        mObjectName  = objectName;
        mLatch       = new CountDownLatch(1);
        // DO NOT listen here; thread-safety problem
    }
    
    public void handleNotification( final Notification notifIn, final Object handback )
    {
        if ( notifIn instanceof MBeanServerNotification )
        {
            final MBeanServerNotification notif = (MBeanServerNotification)notifIn;
            
            if ( notif.getType().equals( MBeanServerNotification.UNREGISTRATION_NOTIFICATION ) &&
                    mObjectName.equals( notif.getMBeanName() ) )
            {
                mLatch.countDown();
            }
        }
    }
    
    private static void cdebug( final String s ) { System.out.println(s); }
    /**
        Wait (block) until the MBean is unregistered.
        @return true if unregistered, false if an error
     */
    public boolean waitForUnregister( final long timeoutMillis )
    {
        boolean unregisteredOK = false;
        
        try
        {
cdebug( "waitForUnregister: 1" );
            // could have already been unregistered
            if ( mMBeanServer.isRegistered(mObjectName) )
            {
cdebug( "waitForUnregister: is currently registered: " + mObjectName);
                try
                {
                    // CAUTION: we must register first to avoid a race condition
                    JMXUtil.listenToMBeanServerDelegate( mMBeanServer, this, null, mObjectName );
cdebug( "waitForUnregister: listening ");

                    // block
                    mLatch.await( timeoutMillis, TimeUnit.MILLISECONDS );
                    
cdebug( "waitForUnregister: unregisteredOK ");
                    unregisteredOK = true;
                }
                catch ( final java.lang.InterruptedException e)
                {
cdebug( "waitForUnregister: InterruptedException ");
                    throw new RuntimeException(e);
                }
                catch( final InstanceNotFoundException e )
                {
cdebug( "waitForUnregister: InstanceNotFoundException ");
                    // fine, we're expecting it to be unregistered anyway
                }
                finally
                {
cdebug( "waitForUnregister: unregistering listener ");
                    mMBeanServer.removeNotificationListener( JMXUtil.getMBeanServerDelegateObjectName(), this );
cdebug( "waitForUnregister: unregistering listener done");
                }
            }
            else
            {
cdebug( "waitForUnregister: NOT currently registered: " + mObjectName);
                unregisteredOK = true;
            }
        }
        catch( final Exception e )
        {
cdebug( "waitForUnregister: exception: " + e);
            throw new RuntimeException(e);
        }
        return unregisteredOK;
     }
}
































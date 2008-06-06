/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.admin.mbeanserver;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.config.ConfigBean;


/**
    Called when ConfigBeans come into the habitat (see GlassfishConfigBean); a job queue
    is maintained for processing by the AMXConfigLoader, which is lazily loaded.
 * @author llc
 */
@Service(name="PendingConfigBeans")
public final class PendingConfigBeans implements CageBuilder, PostConstruct
{
    private static void debug( final String s ) { System.out.println(s); }
    
    private final LinkedBlockingQueue<PendingConfigBeanJob> mJobs = new LinkedBlockingQueue<PendingConfigBeanJob>();
    
    /**
        Exists only to force the MBeanServer and related infrastructure to load
        via AppserverMBeanServerFactory.
     */
    //@org.jvnet.hk2.annotations.Inject
    //javax.management.MBeanServer mMBeanServer;
    
    /**
        Singleton: there should be only one instance and hence a private constructor.
        But the framework using this wants to instantiate things with a public constructor.
     */
    public PendingConfigBeans()
    {
        //debug( "PendingConfigBeans.PendingConfigBeans()" );
    }
    
    public void postConstruct()
    {
        //debug( "PendingConfigBeans.postConstruct" );
    }
    
    /**
        @return a ConfigBean, or null if it's not a ConfigBean
     */
    final ConfigBean asConfigBean( final Object o )
    {
        return (o instanceof ConfigBean) ? ConfigBean.class.cast(o) : null;
    }
    
    public void onEntered( final Inhabitant<?> inhabitant)
    {
        final ConfigBean cb = asConfigBean(inhabitant);
        if ( cb != null )
        {
            //final ConfigBean parent = asConfigBean(cb.parent());
            //debug( "PendingConfigBeans.onEntered: " + cb.getProxyType().getName() + " with parent " + (parent == null ? "null" : parent.getProxyType().getName()) );
            add( cb );
        }
    }
    
    public PendingConfigBeanJob take() throws InterruptedException { return mJobs.take(); }
    public PendingConfigBeanJob peek() throws InterruptedException { return mJobs.peek(); }
    
        private PendingConfigBeanJob
    addJob( final PendingConfigBeanJob job )
    {
        if ( job == null ) throw new IllegalArgumentException();
        
        mJobs.add( job );
        return job;
    }
    
    public PendingConfigBeanJob  add( final ConfigBean cb ) {  return add(cb, null); }
    
    public PendingConfigBeanJob  add( final ConfigBean cb, final boolean useLatch )
    { 
        return add(cb, useLatch ? new CountDownLatch(1) : null );
    }
    
        public PendingConfigBeanJob
    add( final ConfigBean cb, final CountDownLatch latch)
    {
       return addJob( new PendingConfigBeanJob( cb, latch ) );
    }
    
        public boolean
    remove( final ConfigBean cb )
    {
        boolean  found = false;
        
        for( final PendingConfigBeanJob job : mJobs )
        {
            if ( job.getConfigBean() == cb )
            {
                job.releaseLatch();
                mJobs.remove(job);
                found = true;
                break;
            }
        }
        return found;
    }
}























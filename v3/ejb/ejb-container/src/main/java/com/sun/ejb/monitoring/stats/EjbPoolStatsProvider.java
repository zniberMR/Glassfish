/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.ejb.monitoring.stats;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.ejb.containers.util.pool.AbstractPool;

import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.external.probe.provider.annotations.*;
import org.glassfish.external.statistics.*;
import org.glassfish.external.statistics.impl.*;
import org.glassfish.gmbal.*;

/**
 * Probe listener for the Ejb Pool monitoring events. 
 *
 * @author Marina Vatkina
 */
// TODO: find the right names
// v2: com.sun.appserv:application=MEjbApp,name=bean-pool,type=bean-pool,category=monitor,ejb-module=mejb_jar,server=server,stateless-session-bean=MEJBBean
// v3: amx:pp=/mon/server-mon[server],type=bean-pool-mon,name=??????????
@AMXMetadata(type="bean-pool-mon", group="monitoring", isSingleton=false)
@ManagedObject
@Description("Bean Pool Statistics")
public class EjbPoolStatsProvider {

    private CountStatisticImpl createdStat = new CountStatisticImpl(
            "TotalBeansCreated", "count", "Number of beans created in the associated pool");

    private CountStatisticImpl destroyedStat = new CountStatisticImpl(
            "TotalBeansDestroyed", "count", "Number of beans destroyed from the associated pool");

    private CountStatisticImpl jmsStat = new CountStatisticImpl(
            "JmsMaxMessagesLoad", "count", 
            "Provides the maximum number of messages to load into a JMS session, at a time.");

    private BoundedRangeStatisticImpl beansInPool;
    private BoundedRangeStatisticImpl threadsWaiting;

    private static final Logger _logger =
            EjbContainerUtilImpl.getInstance().getLogger();

    private String appName = null;
    private String moduleName = null;
    private String beanName = null;
    private boolean registered = false;
    private AbstractPool delegate;

    public EjbPoolStatsProvider(AbstractPool delegate, String appName, 
            String moduleName, String beanName) {

        this.delegate = delegate;
        this.appName = appName;
        this.moduleName = moduleName;
        this.beanName = beanName;
        delegate.setInfo(appName, moduleName, beanName);

        long now = System.currentTimeMillis();

        beansInPool = new BoundedRangeStatisticImpl(
            0, 0, 0, delegate.getMaxPoolSize(), delegate.getSteadyPoolSize(),
            "NumBeansInPool", "count", "Number of EJBs in associated pool",
            now, now);
        threadsWaiting = new BoundedRangeStatisticImpl(
            0, 0, 0, Long.MAX_VALUE, 0,
            "NumThreadsWaiting", "count", "Number of threads waiting for free beans",
            now, now);
    }

    public void register() {
        String node = EjbMonitoringUtils.registerSubComponent(
                appName, moduleName, beanName, "bean-pool", this);
        if (node != null) {
            registered = true;
        }
    }

    public void unregister() {
        if (registered) {
            registered = false;
            StatsProviderManager.unregister(this);
        }
    }

    @ManagedAttribute(id="numbeansinpool")
    @Description( "Number of EJBs in associated pool")
    public RangeStatistic getNumBeansInPool() {
        beansInPool.setCurrent(delegate.getNumBeansInPool());
        return beansInPool.getStatistic();
    }

    @ManagedAttribute(id="numthreadswaiting")
    @Description( "Number of threads waiting for free beans")
    public RangeStatistic getNumThreadsWaiting() {
        threadsWaiting.setCurrent(delegate.getNumThreadsWaiting());
        return threadsWaiting.getStatistic();
    }

    @ManagedAttribute(id="totalbeanscreated")
    @Description( "Number of Beans created in associated pool")
    public CountStatistic getTotalBeansCreated() {
        return createdStat;
    }

    @ManagedAttribute(id="totalbeansdestroyed")
    @Description( "Number of Beans destroyed in associated pool")
    public CountStatistic getTotalBeansDestroyed() {
        return destroyedStat;
    }

    @ManagedAttribute(id="jmsmaxmessagesload")
    @Description( "Provides the maximum number of messages to load into a JMS session, at a time")
    public CountStatistic getJmsMaxMessagesLoad() {
        jmsStat.setCount(delegate.getJmsMaxMessagesLoad());
        return jmsStat;
    }

    @ProbeListener("glassfish:ejb:pool:objectAddedEvent")
    public void ejbObjectAddedEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (isValidRequest(appName, modName, ejbName)) {
            createdStat.increment();
        } else {
            logWrongEvent(appName, modName, ejbName);
        }
    }

    @ProbeListener("glassfish:ejb:pool:objectAddFailedEvent")
    public void ejbObjectAddFailedEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (isValidRequest(appName, modName, ejbName)) {
            createdStat.decrement();
        } else {
            logWrongEvent(appName, modName, ejbName);
        }
    }

    @ProbeListener("glassfish:ejb:pool:objectDestroyedEvent")
    public void ejbObjectDestroyedEvent(
            @ProbeParam("appName") String appName,
            @ProbeParam("modName") String modName,
            @ProbeParam("ejbName") String ejbName) {
        if (isValidRequest(appName, modName, ejbName)) {
            destroyedStat.increment();
        } else {
            logWrongEvent(appName, modName, ejbName);
        }
    }

    private boolean isValidRequest(String appName, String moduleName,
            String beanName) {
        if ((this.appName == null && appName != null)
                || (this.appName != null && !this.appName.equals(appName))) {
            return false;
        }
        if ((this.moduleName == null && moduleName != null)
                || (this.moduleName != null && !this.moduleName.equals(moduleName))) {
            return false;
        }
        if ((this.beanName == null && beanName != null)
                || (this.beanName != null && !this.beanName.equals(beanName))) {
            return false;
        }

        return true;
    }
  

    private void logWrongEvent(String appName, String moduleName,
            String beanName) {
        _logger.fine("Recieved event for: [" + this.appName + ":" + 
                this.moduleName + ":" + this.beanName + "] but this provider is for [" + 
                appName + ":" + moduleName + ":" + beanName+ "]");
    }
}

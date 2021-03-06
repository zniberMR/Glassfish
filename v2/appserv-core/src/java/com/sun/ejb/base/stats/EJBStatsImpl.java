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

package com.sun.ejb.base.stats;

import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.EJBStats;

import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;

import com.sun.ejb.spi.stats.EJBStatsProvider;

/**
 * A Class for providing stats for EJB Container
 *  All S1AS(EJB)Containers act as the provider
 *
 * @author Mahesh Kannan
 */

public abstract class EJBStatsImpl
    extends StatsImpl
    implements javax.management.j2ee.statistics.EJBStats
{
    protected EJBStatsProvider delegate;

    private MutableCountStatisticImpl createStat;
    private MutableCountStatisticImpl removeStat;

    public EJBStatsImpl(EJBStatsProvider delegate, String intfName) {
	this.delegate = delegate;
	super.initialize(intfName);

	initStats();
    }

    private void initStats() {
	createStat = new MutableCountStatisticImpl(
		new CountStatisticImpl("CreateCount"));
	removeStat = new MutableCountStatisticImpl(
	    new CountStatisticImpl("RemoveCount"));
    }

    public CountStatistic getCreateCount(){
	createStat.setCount(delegate.getCreateCount());
	return (CountStatistic) createStat.modifiableView();
    }

    public CountStatistic getRemoveCount(){
	removeStat.setCount(delegate.getRemoveCount());
	return (CountStatistic) removeStat.modifiableView();
    }

}

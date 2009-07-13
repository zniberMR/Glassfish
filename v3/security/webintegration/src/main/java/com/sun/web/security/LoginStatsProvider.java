/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.web.security;

import org.glassfish.api.statistics.CountStatistic;
import org.glassfish.api.statistics.impl.CountStatisticImpl;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.probe.provider.annotations.ProbeListener;
import org.glassfish.probe.provider.annotations.ProbeParam;

/**
 *
 * @author nithyasubramanian
 */
@ManagedObject
@Description( "Login Statistics" )
public class LoginStatsProvider {
    
    CountStatisticImpl successLoginCount;
    CountStatisticImpl failedLoginCount;
    
    @ManagedAttribute
    public CountStatistic getSuccessLoginCount() {
        successLoginCount = new CountStatisticImpl("successlogincount", "count", "No of successful logins");
        return successLoginCount.getStatistic();
    }
    
    @ManagedAttribute
    public CountStatistic getFailedLoginCount() {
        failedLoginCount = new CountStatisticImpl("failedlogincount", "count", "No of failed logins");
        return failedLoginCount.getStatistic();
    }
    
    @ProbeListener("security:webintegration:login:loginSuccessfulEvent")
    public void loginSuccessfulEvent(@ProbeParam("username")String userName){
       successLoginCount.increment();
    }
        
    @ProbeListener("security:webintegration:login:loginFailedEvent")
    public void loginFailedEvent(@ProbeParam("username")String userName){
       failedLoginCount.increment();
    }
    
    

}

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
package com.sun.ejb.containers;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.logging.LogDomains;
import java.io.Serializable;
import java.util.Date;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.Application;

/**
 * RuntimeTimerState holds all runtime state of an EJB
 * timer, including what fine-grained state it's in,
 * stats about the number of expirations and failed
 * deliveries that have occurred, and any existing JDK
 * timer task that is currently scheduled for this timer.
 * It also caches read-only state of a timer to improve
 * performance. 
 *
 * @author Kenneth Saks
 */
class RuntimeTimerState {

    private static final Logger logger =
        LogDomains.getLogger(LogDomains.EJB_LOGGER);

    //
    // Fine-grained timer states
    //

    // Timer has been created but not committed.  
    private static final int CREATED     = 0;

    // There is a scheduled JDK timer task for this timer.
    private static final int SCHEDULED   = 1;

    // The JDK timer task has expired and the timer's ejbTimeout method
    // is being called.
    private static final int BEING_DELIVERED   = 2;

    // The timer has been cancelled but the cancellation can still be
    // rolled back.
    private static final int CANCELLED   = 3;
    
    private int state_;

    //
    // Immutable timer state.
    //
    private TimerPrimaryKey timerId_;

    private Date initialExpiration_;
    private long intervalDuration_;
    private long containerId_;
    private Object timedObjectPrimaryKey_;

    //
    private BaseContainer container_;

    // Handle to scheduled timer task. This is only set when timer is SCHEDULED 
    private EJBTimerTask currentTask_;

    //
    // stats
    //

    private int numExpirations_;
    private int numFailedDeliveries_;
    
    RuntimeTimerState(TimerPrimaryKey timerId,
                      Date initialExpiration, long intervalDuration, 
                      BaseContainer container, 
                      Object timedObjectPkey) {

        state_       = CREATED;
        currentTask_ = null;

        timerId_           = timerId;
        initialExpiration_ = initialExpiration;
        intervalDuration_  = intervalDuration;
        timedObjectPrimaryKey_ = timedObjectPkey;
        container_         = container;

        containerId_       = container.getContainerId();        

        if( logger.isLoggable(Level.FINE) ) {
            logger.log(Level.FINE, "RuntimeTimerState " + timerId_ + 
                       " created");
        }
    }

    TimerPrimaryKey getTimerId() {
        return timerId_;
    }

    Date getInitialExpiration() {
        return initialExpiration_;
    }

    BaseContainer getContainer() {
        return container_;
    }

    long getContainerId() {
        return containerId_;
    }

    Object getTimedObjectPrimaryKey() {
        return timedObjectPrimaryKey_;
    }

    long getIntervalDuration() {
        return intervalDuration_;
    }

   
    //
    // Operations for performing state transitions.
    //

    void scheduled(EJBTimerTask timerTask) {
        if( logger.isLoggable(Level.FINER) ) {
            printStateTransition(state_, SCHEDULED);
        }
        currentTask_ = timerTask;
        state_ = SCHEDULED;
        numFailedDeliveries_ = 0;
    }

    void rescheduled(EJBTimerTask timerTask) {
        if( logger.isLoggable(Level.FINER) ) {
            printStateTransition(state_, SCHEDULED);
        }
        currentTask_ = timerTask;
        state_ = SCHEDULED;
        numFailedDeliveries_++;
    }
    
    /**
     * Transition from CANCELLED to DELIVERED when ejbTimeout calls
     * cancel and then rolls back.  Don't reset numFailedDeliveries.
     */
    void restoredToDelivered() {
        if( logger.isLoggable(Level.FINER) ) {
            printStateTransition(state_, BEING_DELIVERED);
        }

        currentTask_ = null;
        state_ = BEING_DELIVERED;
    }

    void delivered() {
        if( logger.isLoggable(Level.FINER) ) {
            printStateTransition(state_, BEING_DELIVERED);
        }

        currentTask_ = null;

        if( numFailedDeliveries_ == 0 ) {
            numExpirations_++;
        }
        
        state_ = BEING_DELIVERED;
    }
    
    void cancelled() {
        if( logger.isLoggable(Level.FINER) ) {
            printStateTransition(state_, CANCELLED);
        }

        currentTask_ = null;
        state_ = CANCELLED;
    }

    String stateToString() {
        return stateToString(state_);
    }


    private String stateToString(int state) {
        switch(state) {
        case CREATED :
            return "CREATED";
        case SCHEDULED :
            return "SCHEDULED";
        case BEING_DELIVERED :
            return "BEING_DELIVERED";
        case CANCELLED :
            return "CANCELLED";
        }
        return state + " NOT FOUND";
    }

    private void printStateTransition(int fromState, int toState) {
        logger.log(Level.FINER, timerId_ + ": " + stateToString(fromState) +
                   " to " + stateToString(toState));
    }
    
    int getNumExpirations() {
        return numExpirations_;
    }

    /**
     * Number of failed deliveries since timer last transitioned to 
     * the SCHEDULED state.
     */
    int getNumFailedDeliveries() {
        return numFailedDeliveries_;
    }
    
    EJBTimerTask getCurrentTimerTask() {
        return currentTask_;
    }

    String getTimedObjectEjbName() {
        return container_.getEjbDescriptor().getName();
    }

    String getTimedObjectApplicationName() {
        EjbDescriptor ejbDesc = container_.getEjbDescriptor();
        Application app = ejbDesc.getApplication();
        return (app != null) ? app.getRegistrationName() : "";
    }

    boolean timedObjectIsEntity() {
        return (timedObjectPrimaryKey_ != null);
    }

    //
    // State-testing accessors.
    //

    boolean isActive() {
        return (state_ != CANCELLED);
    }
    
    boolean isCancelled() {
        return (state_ == CANCELLED);
    }

    boolean isCreated() {
        return (state_ == CREATED);
    }

    boolean isBeingDelivered() {
        return (state_ == BEING_DELIVERED);
    }

    boolean isScheduled() {
        return (state_ == SCHEDULED);
    }

    boolean isRescheduled() {
        return (isScheduled() && (numFailedDeliveries_ > 0));
    }

    Date getNextTimeout() {
        if( !isScheduled() && !isRescheduled() ) {
            throw new IllegalStateException();
        }
        return currentTask_.getTimeout();
    }

    long getTimeRemaining() {
        Date timeout = getNextTimeout();
        Date now = new Date();
        return (timeout.getTime() - now.getTime());
    }

    /**
     * @return true if interval timer and false otherwise
     */
    boolean isPeriodic() {
        return (intervalDuration_ > 0);
    }

    //
    // java.lang.Object methods.
    //

    public int hashCode() {
        return timerId_.hashCode();
    }

    public boolean equals(Object other) {
        boolean equal = false;
        if( other instanceof RuntimeTimerState ) {
            equal = timerId_.equals(((RuntimeTimerState) other).timerId_);
        }
        return equal;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();        
        buffer.append("'" + getTimerId() + "' ");
        buffer.append("'TimedObject = " + getTimedObjectEjbName() + "' ");
        buffer.append("'Application = " + getTimedObjectApplicationName()
                      + "' ");
        buffer.append("'" + stateToString() + "' ");
        buffer.append("'" + (isPeriodic() ? "PERIODIC' " : "SINGLE-ACTION' "));
        buffer.append("'Container ID = " + containerId_ + "' ");
        buffer.append("'" + getInitialExpiration() + "' ");
        buffer.append("'" + getIntervalDuration() + "' ");
        Object pk = getTimedObjectPrimaryKey();
        if( pk != null ) {
            buffer.append("'" + pk + "' ");
        }
        return buffer.toString();
    }
}

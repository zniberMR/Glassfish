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

package com.sun.enterprise.server;

import com.sun.enterprise.admin.event.AdminEventListener;
import com.sun.enterprise.admin.event.jms.*;
import com.sun.enterprise.admin.event.AdminEventListenerException;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.config.*;
import com.sun.enterprise.connectors.*;
import com.sun.enterprise.connectors.system.*;


/**
 * Listener interface to handle jms-service element events.
 *
 * @author: alexkrav
 */
public class JmsServiceEventListener implements 
       com.sun.enterprise.admin.event.jms.JmsServiceEventListener {

    private ConnectorRegistry registry = ConnectorRegistry.getInstance();

    /**
     * Handles jms-service element removal.
     * It is called whenever a JmsServiceEvent is received.
     *
     * @param event                        Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleDelete(JmsServiceEvent event)
             throws AdminEventListenerException {
    }

    /**
     * Handles jms-service element modification 
     * (attributes/properties values changed).
     * It is called whenever a JmsServiceEvent is received.
     *
     * @param event                        Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleUpdate(JmsServiceEvent event)
             throws AdminEventListenerException {
        try {
            JmsService service = getJmsService(event);
            ActiveJmsResourceAdapter ajr = (ActiveJmsResourceAdapter) 
                registry.getActiveResourceAdapter(ConnectorRuntime.DEFAULT_JMS_ADAPTER);
            if (ajr != null) {
                ajr.reloadRA(service);
            }
            JmsService oldService =  ServerBeansFactory.getJmsServiceBean(
                event.getOldConfigContext());
            if(!oldService.getType().equals(service.getType()))
                throw new AdminEventListenerException
                ("jms type is not dynamically configured");    
        } catch (ConnectorRuntimeException cre) {
            AdminEventListenerException ale = 
                new AdminEventListenerException(cre.getMessage());
            ale.initCause(cre);
            throw ale;
        } catch (Exception ce) {
            AdminEventListenerException ale = 
                new AdminEventListenerException(ce.getMessage());
            ale.initCause(ce);
            throw ale;
        }
    }

    /**
     * Handles element additions.
     * It is called whenever a JmsServiceEvent is received.
     *
     * @param event                        Event to be processed.
     *
     * @throws AdminEventListenerException when the listener is unable to
     *         process the event.
     */
    public void handleCreate(JmsServiceEvent event)
             throws AdminEventListenerException {
    }

    private JmsService getJmsService(JmsServiceEvent event) throws ConfigException {
        ConfigContext context = event.getConfigContext();
        return ServerBeansFactory.getJmsServiceBean(context);
    }
}

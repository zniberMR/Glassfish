/*******************************************************************************
 * Copyright (c) 1998, 2009 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.sessions.coordination;

import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sessions.coordination.RemoteCommandManager;
import org.eclipse.persistence.sessions.SessionProfiler;

/**
 * <p>
 * <b>Purpose</b>: This class provides an implementation of an internal RCM Command.
 * <p>
 * <b>Description</b>: This command is used for DMS profiling.
 * <p>
 *  @since TopLink 10.1.3
 */
public class ProfileMessageSentCommand extends RCMCommand {

    /**
     * INTERNAL:
     * Executed the internal RCM command
     */
    public void executeWithSession(AbstractSession session) {
        session.incrementProfile(SessionProfiler.RcmSent);
    }

    public void executeWithRCM(RemoteCommandManager rcm) {
        // not used for this internal command
    }
}

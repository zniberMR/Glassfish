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
package com.sun.enterprise;

/**
 * InvocationManager provides interface to keep track of
 * component context on a per-thread basis
 *
 * @author Tony Ng
 */
public interface InvocationManager {

    /**
     * To be called by a Container to indicate that the Container is
     * about to invoke a method on a component.
     * The preInvoke and postInvoke must be called in pairs and well-nested.
     * 
     * @param inv the Invocation object
     */
    public void preInvoke(ComponentInvocation inv) throws InvocationException;

    /**
     * To be called by a Container to indicate that the Container has
     * just completed the invocation of a method on a component.
     * The preInvoke and postInvoke must be called in pairs and well-nested.
     * 
     * @param inv the Invocation object
     */
    public void postInvoke(ComponentInvocation inv) throws InvocationException;

    // BEGIN IASRI# 4646060
    /**
     * Returns the current Invocation object associated with the current thread
     */
    public ComponentInvocation getCurrentInvocation();
    // END IASRI# 4646060

    /**
     * Returns the previous Invocation object associated with the current
     * thread.
     * Returns null if there is none. This is typically used when a component A
     * calls another component B within the same VM. In this case, it might be
     * necessary to obtain information related to both component A using
     * getPreviousInvocation() and B using getCurrentInvocation()
     */
    public ComponentInvocation getPreviousInvocation() 
        throws InvocationException;

    /**
     * return true iff no invocations on the stack for this thread
     */
    public boolean isInvocationStackEmpty();
    
    public java.util.List getAllInvocations();

    public boolean isStartupInvocation();
}

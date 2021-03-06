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

package javax.resource.spi.endpoint;

import java.lang.NoSuchMethodException;
import javax.resource.ResourceException;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.ApplicationServerInternalException;
import javax.resource.spi.IllegalStateException;
import javax.resource.spi.UnavailableException;

/**
 * This defines a contract for a message endpoint. This is implemented by an
 * application server.
 *
 * @version 1.0
 * @author  Ram Jeyaraman
 */
public interface MessageEndpoint {

    /**
     * This is called by a resource adapter before a message is delivered.
     *
     * @param method description of a target method. This information about
     * the intended target method allows an application server to decide 
     * whether to start a transaction during this method call, depending 
     * on the transaction preferences of the target method.
     * The processing (by the application server) of the actual message 
     * delivery method call on the endpoint must be independent of the 
     * class loader associated with this descriptive method object. 
     *
     * @throws NoSuchMethodException indicates that the specified method
     * does not exist on the target endpoint.
     *
     * @throws ResourceException generic exception.
     *
     * @throws ApplicationServerInternalException indicates an error 
     * condition in the application server.
     *
     * @throws IllegalStateException indicates that the endpoint is in an
     * illegal state for the method invocation. For example, this occurs when
     * <code>beforeDelivery</code> and <code>afterDelivery</code> 
     * method calls are not paired.
     *
     * @throws UnavailableException indicates that the endpoint is not 
     * available.
     */
    void beforeDelivery(java.lang.reflect.Method method)
	throws NoSuchMethodException, ResourceException;

    /**
     * This is called by a resource adapter after a message is delivered.
     *
     * @throws ResourceException generic exception.
     *
     * @throws ApplicationServerInternalException indicates an error 
     * condition in the application server.
     *
     * @throws IllegalStateException indicates that the endpoint is in an
     * illegal state for the method invocation. For example, this occurs when
     * beforeDelivery and afterDelivery method calls are not paired.
     *
     * @throws UnavailableException indicates that the endpoint is not 
     * available.
     */
    void afterDelivery() throws ResourceException;

    /**
     * This method may be called by the resource adapter to indicate that it
     * no longer needs a proxy endpoint instance. This hint may be used by
     * the application server for endpoint pooling decisions.
     */
    void release();
}

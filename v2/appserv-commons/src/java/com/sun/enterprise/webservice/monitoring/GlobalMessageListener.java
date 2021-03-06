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

package com.sun.enterprise.webservice.monitoring;

/**
 * This interface permits implementors to register a global message listener 
 * which will be notified for all the web services requests and responses 
 * on installed and enabled Web Services. Each invocation will be notified 
 * through founr callbacks (preProcessRequest, processRequest, processResponse,
 * postProcessResponse). 
 *
 * @author Jerome Dochez
 * @param T SOAPMessageContext type (jaxrpc or jaxws)
 * com.sun.xml.rpc.spi.runtime.SOAPMessageContext |  com.sun.enterprise.webservice.SOAPMessageContext
 */
public interface GlobalMessageListener<T> {

    /** 
     * Callback when a web service request entered the web service container
     * and before any system processing is done. 
     * @param endpoint is the endpoint the web service request is targeted to
     * @return a message ID to trace the request in the subsequent callbacks
     * or null if this invocation should not be traced further.
     */
    public String preProcessRequest(Endpoint endpoint);
    
    /** 
     * Callback when a web service request is about the be delivered to the 
     * Web Service Implementation Bean.
     * @param mid message ID returned by preProcessRequest call
     * @param ctx the SOAPMessageContext
     * @param info the message trace, transport dependent
     */
    public void processRequest(String mid, T ctx, TransportInfo info);
    
    /**
     * Callback when a web service response was returned by the Web Service
     * Implementation Bean
     * @param mid message ID returned by the preProcessRequest call
     * @param ctx the SOAPMessageContext
     */
    public void processResponse(String mid, T ctx);
    
    /** 
     * Callback when a 2.X web service request is about the be delivered to the 
     * Web Service Implementation Bean.
     * @param mid message ID returned by preProcessRequest call 
     * @param trace the jaxrpc message trace, transport dependent
     */
    public void processRequest(String mid, com.sun.enterprise.webservice.SOAPMessageContext ctx, TransportInfo info);
    
    /**
     * Callback when a 2.X web service response was returned by the Web Service 
     * Implementation Bean
     * @param mid message ID returned by the preProcessRequest call
     * @param trace jaxrpc message trace, transport dependent.
     */
    public void processResponse(String mid, com.sun.enterprise.webservice.SOAPMessageContext ctx);
    
    /**
     * Callback when a web service response has finished being processed
     * by the container and was sent back to the client
     * @param mid returned by the preProcessRequest call
     * @param info the response transport dependent information
     */
    public void postProcessResponse(String mid, TransportInfo info);
    
}

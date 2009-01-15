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

package com.sun.enterprise.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.core.StandardPipeline;
//import com.sun.web.security.RealmAdapter;

/**
 * Pipeline whose invoke logic checks if a given request path represents
 * an ad-hoc path: If so, this pipeline delegates the request to the
 * ad-hoc pipeline of its associated web module. Otherwise, this pipeline
 * processes the request.
 */
public class WebPipeline extends StandardPipeline {

    private WebModule webModule;
    
    /** 
     * creates an instance of WebPipeline
     * @param container
     */       
    public WebPipeline(Container container) {
        super(container);
        if(container instanceof WebModule) {
            this.webModule = (WebModule)container;
        }
    }    

    /**
     * Processes the specified request, and produces the appropriate
     * response, by invoking the first valve (if any) of this pipeline, or
     * the pipeline's basic valve.
     *
     * If the request path to process identifies an ad-hoc path, the
     * web module's ad-hoc pipeline is invoked.
     *
     * @param request The request to process
     * @param response The response to return
     */
    public void invoke(Request request, Response response)
            throws IOException, ServletException {

        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        if (webModule != null &&
                webModule.getAdHocServletName(hreq.getServletPath()) != null) {
            webModule.getAdHocPipeline().invoke(request, response);
        } else {
            /*
            RealmAdapter realmAdapter = (RealmAdapter)webModule.getRealm();
            if (realmAdapter != null &&
                    realmAdapter.isSecurityExtensionEnabled()){
                super.invoke(getRequest(request),
                             getResponse(request, response));
            } else {
            */
                super.invoke(request, response);
            /*
            }
            */
        }
    }


    private Request getRequest(Request request) {
	Request r = (Request) 
	    request.getNote(Globals.WRAPPED_REQUEST); 
	if (r == null) {
	    r = request;
	}
	return r;
    }


    private Response getResponse(Request request, Response response) {
	Response r = (Response) 
	    request.getNote(Globals.WRAPPED_RESPONSE); 
	if (r == null) {
	    r = response;
	}
	return r;
    }

}

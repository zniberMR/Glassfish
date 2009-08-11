/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package test;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class ServletTest extends HttpServlet{

    private String status = "DelegateTest::FAIL";
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet.init]");        
        
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {        
        System.out.println("[Servlet.doPost]");
        
        response.setContentType("text/html");
        
        DelegateTest delegate = null;
        try{
            Class clazz = Class.forName("test.DelegateTest");
            delegate = (DelegateTest)clazz.newInstance();
            clazz.newInstance();
        } catch (Exception ex){
            status = "DelegateTest::FAIL";
            ex.printStackTrace();
        }

        if (delegate != null){
            try{
                System.out.println("Delegate: " + delegate.getChildName());       
                status = "DelegateTest::PASS";
            } catch (Exception ex){
                status = "DelegateTest::FAIL";
                ex.printStackTrace();
            }
        }
        PrintWriter out = response.getWriter();
        out.println(status);

        try{
            Class clazz = Class.forName("javax.sql.rowset.BaseRowSet");

            if (clazz == null){
                status = "OverridableJavax::FAIL";
            } else {
                status = "OverridableJavax::PASS";
            }
            
        } catch (Exception ex){
            status = "OverridableJavax::FAIL";
            ex.printStackTrace();
        }
        out.println(status);
        
    }

}




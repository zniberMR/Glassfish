package com.sun.enterprise.tools.verifier.tests.ejb.runtime;

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

import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import java.util.*;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;

/** enterprise-beans
 *    unique-id ? [String]
 *
 * The unique-id is automatically generated and updated at deployment/redeployment
 * The test is crude and only checks if the value promoted is not null.
 * @author Irfan Ahmed
 */
public class ASEntBeanUniqueID extends EjbTest implements EjbCheck { 

    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        try{
            String s1 = ("/sun-ejb-jar/enterprise-beans/unique-id");
            String entBeanUniqueID = getXPathValue(s1);
            if(entBeanUniqueID == null)
            {
                result.addGoodDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                    "For [ {0} ]",
                    new Object[] {compName.toString()}));
                result.passed(smh.getLocalString(getClass().getName()+".passed",
                    "PASSED [AS-EJB enterprise-beans] The unique-id key should not be defined. It will be " +
                      "automatically generated at deployment time."));
            }
            else
            {
                result.addWarningDetails(smh.getLocalString
                                       ("tests.componentNameConstructor",
                                        "For [ {0} ]",
                                        new Object[] {compName.toString()}));
                result.warning(smh.getLocalString
                     (getClass().getName() + ".warning",
                      "WARNING [AS-EJB enterprise-beans] : unique-id Element should not be defined. It is " +
                      "automatically generated at deployment time."));
            }

        }catch(Exception ex){
            result.addErrorDetails(smh.getLocalString
                (getClass().getName() + ".notRun",
                "NOT RUN [AS-EJB] : Could not create a descriptor object"));
        }
        return result;
    }
}

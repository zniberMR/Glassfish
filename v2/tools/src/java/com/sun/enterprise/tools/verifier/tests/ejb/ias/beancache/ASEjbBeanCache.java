package com.sun.enterprise.tools.verifier.tests.ejb.ias.beancache;


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

import com.sun.enterprise.tools.common.dd.ejb.SunEjbJar;
import com.sun.enterprise.tools.common.dd.ejb.EnterpriseBeans;
import com.sun.enterprise.tools.common.dd.ejb.Ejb;
import com.sun.enterprise.deployment.EjbEntityDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import java.lang.reflect.*;

import com.sun.enterprise.tools.common.dd.ejb.BeanCache;

/** ejb [0,n]
 *    bean-cache ?
 *        max-cache-size ? [String]
 *        is-cache-overflow-allowed ? [String]
 *        cache-idle-timout-in-seconds ? [String]
 *        removal-timeout-in-seconds ? [String]
 *        victim-selection-policy ? [String]
 *
 * The bean-cache element specifies the bean cache properties for the bean.
 * This is valid only for entity beans and stateful session beans
 * @author Irfan Ahmed
 */
public class ASEjbBeanCache extends EjbTest implements EjbCheck { 

    public BeanCache beanCache;
    public Ejb testCase;
    
    public void getBeanCache(EjbDescriptor descriptor, SunEjbJar ejbJar)
    {
        testCase = getEjb(descriptor.getName(),ejbJar);
        beanCache = testCase.getBeanCache();
    }
    
    public Result check(EjbDescriptor descriptor)
    {
        Result result = getInitializedResult();
	ComponentNameConstructor compName = new ComponentNameConstructor(descriptor);
        
        SunEjbJar ejbJar = descriptor.getEjbBundleDescriptor().getIasEjbObject();
        if(ejbJar!=null)
        {
            getBeanCache(descriptor,ejbJar);
            if(beanCache!=null)
            {
                if(descriptor instanceof EjbEntityDescriptor
                    || (descriptor instanceof EjbSessionDescriptor 
                            && ((EjbSessionDescriptor)descriptor).getSessionTypeString().equals(EjbSessionDescriptor.STATEFUL)))
				{
                    result.passed(smh.getLocalString(getClass().getName()+".passed",
                        "PASSED [AS-EJB ejb] : bean-cache Element parsed"));
				}
				else
				{
                    result.warning(smh.getLocalString(getClass().getName()+".warning1",
                    "WARNING [AS-EJB ejb] : bean-cache should be defined only for Stateful Session and Entity Beans"));
				}
			/*
                if(descriptor instanceof EjbSessionDescriptor 
                    && ((EjbSessionDescriptor)descriptor).getSessionTypeString().equals(EjbSessionDescriptor.STATELESS))
                {
                    result.warning(smh.getLocalString(getClass().getName()+".warning1",
                    "WARNING [AS-EJB ejb] : bean-cache should be defined for Stateful Session and Entity Beans"));
                }
                else
                    result.passed(smh.getLocalString(getClass().getName()+".passed",
                        "PASSED [AS-EJB ejb] : bean-cache Element parsed"));
			*/
            }
            else
            {
                if(descriptor instanceof EjbEntityDescriptor
                    || (descriptor instanceof EjbSessionDescriptor 
                            && ((EjbSessionDescriptor)descriptor).getSessionTypeString().equals(EjbSessionDescriptor.STATEFUL)))
                {
                    result.warning(smh.getLocalString(getClass().getName()+".warning",
                        "WARNING [AS-EJB ejb] : bean-cache should be defined for Stateful Session and Entity Beans"));
                }
                else
                    result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ejb] : bean-cache element not defined"));
            }
        }
        else
        {
            result.addErrorDetails(smh.getLocalString
                                   ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
        }
        return result;
    }
}
        

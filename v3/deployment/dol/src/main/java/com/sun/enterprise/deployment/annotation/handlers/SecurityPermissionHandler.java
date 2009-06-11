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
package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.annotation.context.RarBundleContext;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.util.LocalStringManagerImpl;

import javax.resource.spi.Connector;
import javax.resource.spi.SecurityPermission;
import java.lang.annotation.Annotation;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.glassfish.apf.*;
import org.glassfish.apf.impl.AnnotationUtils;
import org.glassfish.apf.impl.HandlerProcessingResultImpl;
import org.jvnet.hk2.annotations.Service;

//TODO V3 no need to handle this as its already handled by @ConnectorAnnotationHandler
/**
 * @author Jagadish Ramu
 */
@Service
public class SecurityPermissionHandler extends AbstractHandler {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(AbstractHandler.class);
    
    public Class<? extends Annotation> getAnnotationType() {
        return SecurityPermission.class;
    }

    public HandlerProcessingResult processAnnotation(AnnotationInfo element) throws AnnotationProcessorException {
        AnnotatedElementHandler aeHandler = element.getProcessingContext().getHandler();
        SecurityPermission securityPermission = (SecurityPermission) element.getAnnotation();

        if (aeHandler instanceof RarBundleContext) {
            boolean isConnectionDefinition = hasConnectorAnnotation(element);
            if (isConnectionDefinition) {
                RarBundleContext rarContext = (RarBundleContext) aeHandler;
                ConnectorDescriptor desc = rarContext.getDescriptor();
                com.sun.enterprise.deployment.SecurityPermission permission =
                        new com.sun.enterprise.deployment.SecurityPermission(securityPermission.description(),
                                securityPermission.permissionSpec());
                desc.addSecurityPermission(permission);
            } else {
                getFailureResult(element, "Not a @Connector annotation : @SecurityPermission must " +
                        "be specified along with @Connector annotation", true);
            }
        } else {
            getFailureResult(element, "Not a rar bundle context", true);
        }
        return getDefaultProcessedResult();
    }

    public Class<? extends Annotation>[] getTypeDependencies() {
        return getConnectorAnnotationTypes();
    }

    /**
     * @return a default processed result
     */
    protected HandlerProcessingResult getDefaultProcessedResult() {
        return HandlerProcessingResultImpl.getDefaultResult(
                getAnnotationType(), ResultType.PROCESSED);
    }

    private boolean hasConnectorAnnotation(AnnotationInfo element) {
        Class c = (Class) element.getAnnotatedElement();
        return c.getAnnotation(Connector.class) != null;
    }

    private HandlerProcessingResultImpl getFailureResult(AnnotationInfo element, String message, boolean doLog) {
        HandlerProcessingResultImpl result = new HandlerProcessingResultImpl();
        result.addResult(getAnnotationType(), ResultType.FAILED);
        if (doLog) {
            Class c = (Class) element.getAnnotatedElement();
            String className = c.getName();
            //TODO V3 logStrings
            logger.log(Level.WARNING, "failed to handle annotation [ " + element.getAnnotation() + " ]" +
                    " on class [ " + className + " ], reason : " + message);
        }
        return result;
    }

}

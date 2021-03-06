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
package com.sun.ejb.codegen;

import java.lang.reflect.Method;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.sun.logging.*;
import com.sun.ejb.EJBUtils;

import com.sun.enterprise.util.LocalStringManagerImpl;

import static java.lang.reflect.Modifier.*;
import static com.sun.corba.ee.spi.codegen.Wrapper.*;
import com.sun.corba.ee.spi.codegen.Type;
import com.sun.corba.ee.impl.codegen.ClassGenerator;

/**
 * This class is used to generate a sub-interface of the
 * GenericEJBHome interface that will be loaded within each
 * application.
 */

public class GenericHomeGenerator extends Generator 
    implements ClassGeneratorFactory {

    private static LocalStringManagerImpl localStrings =
	new LocalStringManagerImpl(GenericHomeGenerator.class);
    private static final Logger _logger =
        LogDomains.getLogger(LogDomains.DPL_LOGGER);

    private String genericEJBHomeClassName;
    private ClassLoader loader;

    /**
     * Get the fully qualified name of the generated class.
     * @return the name of the generated class.
     */
    public String getGeneratedClass() {
        return genericEJBHomeClassName;
    }

    // For corba codegen infrastructure
    public String className() {
        return getGeneratedClass();
    }

    public GenericHomeGenerator(ClassLoader cl)
	throws GeneratorException 
    {
	super();

        genericEJBHomeClassName = EJBUtils.getGenericEJBHomeClassName();
        loader = cl;
    }

    public ClassGenerator evaluate() {

        _clear();

        String packageName = getPackageName(genericEJBHomeClassName);
        String simpleName = getBaseName (genericEJBHomeClassName);

        _package(packageName);

        _interface(PUBLIC, simpleName, 
                   _t("com.sun.ejb.containers.GenericEJBHome"));

        _method(PUBLIC | ABSTRACT, _t("java.rmi.Remote"),
                "create", _t("java.rmi.RemoteException"));

        _arg(_String(), "generatedBusinessIntf");

        _end();

        _end();

        return _classGenerator() ;
    }

    public void generate(OutputStream out)
	throws GeneratorException, IOException 
    {
        throw new IllegalStateException("not supported");
    }

}

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
package org.eclipse.persistence.jaxb.javamodel.reflection;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.jaxb.javamodel.JavaClass;
import org.eclipse.persistence.jaxb.javamodel.JavaModel;
import org.eclipse.persistence.jaxb.javamodel.JavaModelInput;

/**
 * INTERNAL:
 * <p><b>Purpose:</b>Provide access to an array of JavaClass instances 
 * and their associated JavaModel.  This class will transform an array 
 * of Class objects to an array of JavaClasses.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 * <li>Create an array of JavaClass instances from an array of Classes</li>
 * <li>Return an array of JavaClass objects to be used by the generator</li>
 * <li>Return the JavaModel to be used during generation</li>
 * </ul>
 * 
 * @since Oracle TopLink 11.1.1.0.0
 * @see org.eclipse.persistence.jaxb.javamodel.JavaClass 
 * @see org.eclipse.persistence.jaxb.javamodel.JavaModel 
 * @see org.eclipse.persistence.jaxb.javamodel.JavaModelInput 
 */
public class JavaModelInputImpl implements JavaModelInput {
    private JavaClass[] jClasses;
    private JavaModel jModel;
    private Map<JavaClass, Type> javaClassToType;
    
    /**
     * This constructor builds an array of JavaClass objects from an array
     * of Types.  The JavaModel instance to be used is also set here.
     * 
     * This constructor assumes that the a given type in the list will 
     * either be a Class or ParameterizedType.
     * 
     * @param types
     * @param javaModel
     */
    public JavaModelInputImpl(Type[] types, JavaModel javaModel) {
    	 jClasses = new JavaClass[types.length];
    	 javaClassToType = new HashMap<JavaClass, Type>();
         for (int i=0; i<types.length; i++) {
             Type type = types[i];
             // type should be a Class or ParameterizedType
             if (type instanceof Class) {
                 jClasses[i] = new JavaClassImpl((Class) type);                 
             } else {
                 // assume parameterized type
                 ParameterizedType pType = (ParameterizedType) type;
                 jClasses[i] = new JavaClassImpl(pType, (Class) pType.getRawType());                 
             }             
             javaClassToType.put(jClasses[i], type);
         }
         jModel = javaModel;

    }
    	  
    public JavaModelInputImpl(Class[] classes, JavaModel javaModel) {
        jClasses = new JavaClass[classes.length];
        for (int i=0; i<classes.length; i++) {
            jClasses[i] = new JavaClassImpl(classes[i]);            
        }
        jModel = javaModel;
    }
    
    public JavaClass[] getJavaClasses() {
        return jClasses;
    }
    
    public JavaModel getJavaModel() {
        return jModel;
    }

	public Map<JavaClass, Type> getJavaClassToType() {
		return javaClassToType;
	}
}

/*******************************************************************************
 * Copyright (c) 1998, 2011 Oracle. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0 
 * which accompanies this distribution. 
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *     dclarke - Dynamic Persistence INCUBATION - Enhancement 200045
 *               http://wiki.eclipse.org/EclipseLink/Development/Dynamic
 *     
 * This code is being developed under INCUBATION and is not currently included 
 * in the automated EclipseLink build. The API in this code may change, or 
 * may never be included in the product. Please provide feedback through mailing 
 * lists or the bug database.
 ******************************************************************************/
package org.eclipse.persistence.jpa.dynamic;

//EclipseLink imports
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.dynamic.DynamicType;
import org.eclipse.persistence.dynamic.DynamicTypeBuilder;

/**
 * 
 * @author dclarke
 * @since EclipseLink 1.2
 */
public class JPADynamicTypeBuilder extends DynamicTypeBuilder {

    public JPADynamicTypeBuilder(Class<?> dynamicClass, DynamicType parentType, String... tableNames) {
        super(dynamicClass, parentType, tableNames);
    }
    
    public JPADynamicTypeBuilder(DynamicClassLoader dcl, ClassDescriptor descriptor, DynamicType parentType) {
        super(dcl, descriptor, parentType);
    }

    @Override
    protected void configure(ClassDescriptor descriptor, String... tableNames) {
        super.configure(descriptor, tableNames);

        if (descriptor.getCMPPolicy() == null) {
            descriptor.setCMPPolicy(new DynamicIdentityPolicy());
        }
    }
}

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
package org.eclipse.persistence.testing.tests.queries;

import java.util.*;
import org.eclipse.persistence.queries.*;
import org.eclipse.persistence.testing.framework.*;
import org.eclipse.persistence.internal.helper.*;

/**
 * Test that a ReadAllQuery will return the appropriate collection depending on
 * what is specified in the CollectionPolicy of the query.
 */
public class CollectionReadAllTest extends org.eclipse.persistence.testing.framework.ReadAllTest {
    protected Object dbContainter;

    public CollectionReadAllTest(Class referenceClass, int originalObjectsSize, ReadAllQuery query) {
        super(referenceClass, originalObjectsSize);
        this.setQuery(query);
        setName("CollectionReadAllTest(" + org.eclipse.persistence.internal.helper.Helper.getShortClassName(getQuery().getContainerPolicy().getContainerClass()) + "," + org.eclipse.persistence.internal.helper.Helper.getShortClassName(referenceClass) + ")");
    }

    /**
     * Return the number of elements in a container.
     */
    protected int numElements(Object container) {
        if (Helper.classImplementsInterface(container.getClass(), java.util.Collection.class)) {
            return ((Collection)container).size();
        }
        if (Helper.classImplementsInterface(container.getClass(), java.util.Map.class)) {
            return ((Map)container).size();
        }
        return -1;
    }

    public void reset() {
        getSession().getIdentityMapAccessor().initializeIdentityMaps();
    }

    protected void test() {
        this.dbContainter = getSession().executeQuery(getQuery());
    }

    /**
     * Verify that the correct container class was returned.
     */
    protected void verify() {
        Class queryContainerClass = getQuery().getContainerPolicy().getContainerClass();
        if (!queryContainerClass.isInstance(dbContainter)) {
            throw new TestErrorException("The container class returned was" + dbContainter.getClass().toString() + " we expected a " + queryContainerClass.toString() + " to be returned.");
        }

        // check size
        int objectsFromDatabaseSize = numElements(dbContainter);
        if (!(getOriginalObjectsSize() == objectsFromDatabaseSize)) {
            throw new TestErrorException(objectsFromDatabaseSize + " objects were read from the database, but originially there were, " + getOriginalObjectsSize() + ".");
        }
    }
}

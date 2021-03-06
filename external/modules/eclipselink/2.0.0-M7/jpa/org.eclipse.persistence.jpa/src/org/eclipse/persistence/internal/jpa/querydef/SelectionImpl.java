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
 *     Gordon Yorke - Initial development
 *
 ******************************************************************************/
package org.eclipse.persistence.internal.jpa.querydef;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Selection;

import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.internal.localization.ExceptionLocalization;

/**
 * <p>
 * <b>Purpose</b>: Contains the implementation of the Selection interface of the JPA
 * criteria API.
 * <p>
 * <b>Description</b>: The Selection is the expression describing what should be returned by the query.
 * <p>
 * 
 * @see javax.persistence.criteria Join
 * 
 * @author gyorke
 * @since EclipseLink 2.0
 */
public class SelectionImpl<X> implements Selection<X> {
    
    protected Class<X> javaType;
    protected Expression currentNode;
    
    /**
     * Returns the current EclipseLink expression at this node in the criteria expression tree
     * @return the currentNode
     */
    public Expression getCurrentNode() {
        return currentNode;
    }

    protected String alias;
    
    public <T> SelectionImpl(Class<X> javaType, Expression expressionNode){
        this.javaType = javaType;
        this.currentNode = expressionNode;
    }

    /**
     * Assign an alias to the selection.
     * 
     * @param name
     *            alias
     */
    public Selection<X> alias(String name) {
        this.alias = name;
        return this;
    }
    public String getAlias() {
        return this.alias;
    }

    public Class<X> getJavaType() {
        return this.javaType;
    }
    /**
     * Return selection items composing a compound selection
     * @return list of selection items
     * @throws IllegalStateException if selection is not a compound
     *           selection
     */
    public List<Selection<?>> getCompoundSelectionItems(){
        throw new IllegalStateException(ExceptionLocalization.buildMessage("CRITERIA_NOT_A_COMPOUND_SELECTION"));
    }
    
    /**
     * Whether the selection item is a compound selection
     * @return boolean 
     */
    public boolean isCompoundSelection(){
        return false;
    }

}

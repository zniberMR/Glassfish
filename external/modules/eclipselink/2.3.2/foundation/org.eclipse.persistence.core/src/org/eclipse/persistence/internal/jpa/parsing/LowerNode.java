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
 *     Oracle - initial API and implementation from Oracle TopLink
 ******************************************************************************/  
package org.eclipse.persistence.internal.jpa.parsing;

import org.eclipse.persistence.expressions.*;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent a LOWER
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for LOWER
 * </ul>
 */
public class LowerNode extends StringFunctionNode {

    /**
     * LowerNode constructor comment.
     */
    public LowerNode() {
        super();
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        TypeHelper typeHelper = context.getTypeHelper();
        if (left != null) {
            left.validate(context);
            left.validateParameter(context, typeHelper.getStringType());
        }
        setType(typeHelper.getStringType());
    }

    /**
     * INTERNAL
     * Generate the EclipseLink expression for this node
     */
    public Expression generateExpression(GenerationContext context) {
        Expression whereClause = getLeft().generateExpression(context);
        whereClause = whereClause.toLowerCase();
        return whereClause;
    }
    
}



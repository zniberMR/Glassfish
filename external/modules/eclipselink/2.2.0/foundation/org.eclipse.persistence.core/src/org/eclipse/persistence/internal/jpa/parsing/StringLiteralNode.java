/*******************************************************************************
 * Copyright (c) 1998, 2010 Oracle. All rights reserved.
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


/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent a String literal
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for the string
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class StringLiteralNode extends LiteralNode {

    /**
     * Return a new StringLiteralNode
     */
    public StringLiteralNode() {
        super();
    }

    /**
     * Return a new StringLiteralNode with the internal string set to the
     * passed value
     */
    public StringLiteralNode(String theString) {
        super();
        setLiteral(theString);
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        TypeHelper typeHelper = context.getTypeHelper();
        setType(typeHelper.getStringType());
    }
}

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
package org.eclipse.persistence.testing.models.plsql;

import org.eclipse.persistence.sessions.*;
import org.eclipse.persistence.testing.tests.workbenchintegration.WorkbenchIntegrationSystemHelper;

public class PLSQLXMLSystem extends PLSQLSystem {
    public PLSQLXMLSystem() {
        this.project = new PLSQLProject();
    }

    public void addDescriptors(DatabaseSession session) {
        if (this.project == null) {
        	this.project = new PLSQLProject();
        }
        this.project = WorkbenchIntegrationSystemHelper.buildProjectXML(this.project, "plsql-project");
        session.addDescriptors(this.project);
    }
}

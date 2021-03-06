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
package org.eclipse.persistence.internal.localization.i18n;

/**
 * English ResourceBundle for EclipseLinkLocalization messages.
 *
 * Creation date: (07/25/02)
 * @author: Shannon Chen
 * @since TOPLink/Java 5.0
 */
public class EclipseLinkLocalizationResource extends java.util.ListResourceBundle {
    static final Object[][] contents = {
                                           { "NoTranslationForThisLocale", " (There is no English translation for this message.)" },
    };

    /**
     * Return the lookup table.
     */
    protected Object[][] getContents() {
        return contents;
    }
}

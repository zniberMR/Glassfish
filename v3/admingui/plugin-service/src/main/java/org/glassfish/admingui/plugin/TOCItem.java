/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admingui.plugin;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.util.List;


/**
 *  <p>	This class is configured via XML (i.e. a console-config.xml file).
 *  	This is done via the HK2 <code>ConfigParser</code>.</p>
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
@Configured(name="tocitem")
public class TOCItem {

    /**
     *	<p> Accessor for child {@link TOCItem}s.</p>
     */
    public List<TOCItem> getTOCItems() {
	return this.tocItems;
    }

    /**
     *	<p> {@link IntegrationPoint}s setter.</p>
     */
    @Element("tocitem")
    void setTOCItems(List<TOCItem> tocItems) {
	this.tocItems = tocItems;
    }

    /**
     *
     */
    public String getExpand() {
	return this.expand;
    }

    /**
     *
     */
    @Attribute(required=true)
    void setExpand(String expand) {
	this.expand = expand;
    }

    /**
     *
     */
    public String getTarget() {
	return this.target;
    }

    /**
     *
     */
    @Attribute(required=true)
    void setTarget(String target) {
	this.target = target;
    }

    /**
     *	<p> This method returns the path to the target HTML page, starting
     *	    with the moduleId.  It does not add anything before the module id,
     *	    and does not have a leading '/' character.  It does append ".html"
     *	    to the end of the target.</p>
     */
    public String getTargetPath() {
	return this.targetPath;
    }

    /**
     *	<p> Sets the target path.  If the "target" is <code>foo</code>, the
     *	    target path should look something like:
     *	    <code>moduleId/en/help/foo.html</code></p>.  This value is NOT
     *	    automatically set, it must be calculated and set during
     *	    initialization code.</p>
     */
    void setTargetPath(String targetPath) {
	this.targetPath = targetPath;
    }

    /**
     *
     */
    public String getText() {
	return this.text;
    }

    /**
     *
     */
    @Attribute(required=true)
    void setText(String text) {
	this.text = text;
    }

    /**
     *	<p> This method provides the "equals" functionality for TOCItem.  The
     *	    behavior of equals ONLY compares the <code>target</code> value.
     *	    The <code>text</code> and <code>expand</code> values are not used
     *	    to test for equality.</p>
     */
    public boolean equals(Object obj) {
	boolean result = false;
	if (obj instanceof TOCItem) {
	    result = ((TOCItem) obj).getTarget().equals(getTarget());
	}
	return result;
    }


    private String expand;
    private String target;
    private String targetPath;
    private String text;
    private List<TOCItem> tocItems;
}

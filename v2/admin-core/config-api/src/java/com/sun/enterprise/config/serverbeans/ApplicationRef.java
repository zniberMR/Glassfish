/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
 
/**
 *	This generated bean class ApplicationRef matches the DTD element application-ref
 *
 */

package com.sun.enterprise.config.serverbeans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
import java.io.Serializable;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.StaleWriteConfigException;
import com.sun.enterprise.util.i18n.StringManager;

// BEGIN_NOI18N

public class ApplicationRef extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);


	public ApplicationRef() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ApplicationRef(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(0);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	/**
	* Getter for Enabled of the Element application-ref
	* @return  the Enabled of the Element application-ref
	*/
	public boolean isEnabled() {
		return toBoolean(getAttributeValue(ServerTags.ENABLED));
	}
	/**
	* Modify  the Enabled of the Element application-ref
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the Enabled of the Element application-ref
	* @param v the new value
	*/
	public void setEnabled(boolean v) {
		setAttributeValue(ServerTags.ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of Enabled from dtd
	*/
	public static String getDefaultEnabled() {
		return "true".trim();
	}
	/**
	* Getter for VirtualServers of the Element application-ref
	* @return  the VirtualServers of the Element application-ref
	*/
	public String getVirtualServers() {
			return getAttributeValue(ServerTags.VIRTUAL_SERVERS);
	}
	/**
	* Modify  the VirtualServers of the Element application-ref
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setVirtualServers(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.VIRTUAL_SERVERS, v, overwrite);
	}
	/**
	* Modify  the VirtualServers of the Element application-ref
	* @param v the new value
	*/
	public void setVirtualServers(String v) {
		setAttributeValue(ServerTags.VIRTUAL_SERVERS, v);
	}
	/**
	* Getter for LbEnabled of the Element application-ref
	* @return  the LbEnabled of the Element application-ref
	*/
	public boolean isLbEnabled() {
		return toBoolean(getAttributeValue(ServerTags.LB_ENABLED));
	}
	/**
	* Modify  the LbEnabled of the Element application-ref
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLbEnabled(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LB_ENABLED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the LbEnabled of the Element application-ref
	* @param v the new value
	*/
	public void setLbEnabled(boolean v) {
		setAttributeValue(ServerTags.LB_ENABLED, ""+(v==true));
	}
	/**
	* Get the default value of LbEnabled from dtd
	*/
	public static String getDefaultLbEnabled() {
		return "false".trim();
	}
	/**
	* Getter for DisableTimeoutInMinutes of the Element application-ref
	* @return  the DisableTimeoutInMinutes of the Element application-ref
	*/
	public String getDisableTimeoutInMinutes() {
		return getAttributeValue(ServerTags.DISABLE_TIMEOUT_IN_MINUTES);
	}
	/**
	* Modify  the DisableTimeoutInMinutes of the Element application-ref
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setDisableTimeoutInMinutes(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.DISABLE_TIMEOUT_IN_MINUTES, v, overwrite);
	}
	/**
	* Modify  the DisableTimeoutInMinutes of the Element application-ref
	* @param v the new value
	*/
	public void setDisableTimeoutInMinutes(String v) {
		setAttributeValue(ServerTags.DISABLE_TIMEOUT_IN_MINUTES, v);
	}
	/**
	* Get the default value of DisableTimeoutInMinutes from dtd
	*/
	public static String getDefaultDisableTimeoutInMinutes() {
		return "30".trim();
	}
	/**
	* Getter for Ref of the Element application-ref
	* @return  the Ref of the Element application-ref
	*/
	public String getRef() {
		return getAttributeValue(ServerTags.REF);
	}
	/**
	* Modify  the Ref of the Element application-ref
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setRef(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.REF, v, overwrite);
	}
	/**
	* Modify  the Ref of the Element application-ref
	* @param v the new value
	*/
	public void setRef(String v) {
		setAttributeValue(ServerTags.REF, v);
	}
	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "application-ref" + (canHaveSiblings() ? "[@ref='" + getAttributeValue("ref") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.ENABLED)) return "true".trim();
		if(attr.equals(ServerTags.LB_ENABLED)) return "false".trim();
		if(attr.equals(ServerTags.DISABLE_TIMEOUT_IN_MINUTES)) return "30".trim();
	return null;
	}
	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ApplicationRef\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


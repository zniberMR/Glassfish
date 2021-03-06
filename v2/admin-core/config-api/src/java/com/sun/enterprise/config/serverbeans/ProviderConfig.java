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
 *	This generated bean class ProviderConfig matches the DTD element provider-config
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

public class ProviderConfig extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String REQUEST_POLICY = "RequestPolicy";
	static public final String RESPONSE_POLICY = "ResponsePolicy";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public ProviderConfig() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ProviderConfig(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("request-policy", REQUEST_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			RequestPolicy.class);
		this.createAttribute(REQUEST_POLICY, "auth-source", "AuthSource", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(REQUEST_POLICY, "auth-recipient", "AuthRecipient", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("response-policy", RESPONSE_POLICY, 
			Common.TYPE_0_1 | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ResponsePolicy.class);
		this.createAttribute(RESPONSE_POLICY, "auth-source", "AuthSource", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createAttribute(RESPONSE_POLICY, "auth-recipient", "AuthRecipient", 
						AttrProp.CDATA | AttrProp.IMPLIED,
						null, null);
		this.createProperty("property", ELEMENT_PROPERTY, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			ElementProperty.class);
		this.createAttribute(ELEMENT_PROPERTY, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.createAttribute(ELEMENT_PROPERTY, "value", "Value", 
						AttrProp.CDATA | AttrProp.REQUIRED,
						null, null);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is optional
	public void setRequestPolicy(RequestPolicy value) {
		this.setValue(REQUEST_POLICY, value);
	}

	// Get Method
	public RequestPolicy getRequestPolicy() {
		return (RequestPolicy)this.getValue(REQUEST_POLICY);
	}

	// This attribute is optional
	public void setResponsePolicy(ResponsePolicy value) {
		this.setValue(RESPONSE_POLICY, value);
	}

	// Get Method
	public ResponsePolicy getResponsePolicy() {
		return (ResponsePolicy)this.getValue(RESPONSE_POLICY);
	}

	// Get Method
	public ElementProperty getElementProperty(int index) {
		return (ElementProperty)this.getValue(ELEMENT_PROPERTY, index);
	}

	// This attribute is an array, possibly empty
	public void setElementProperty(ElementProperty[] value) {
		this.setValue(ELEMENT_PROPERTY, value);
	}

	// Getter Method
	public ElementProperty[] getElementProperty() {
		return (ElementProperty[])this.getValues(ELEMENT_PROPERTY);
	}

	// Return the number of properties
	public int sizeElementProperty() {
		return this.size(ELEMENT_PROPERTY);
	}

	// Add a new element returning its index in the list
	public int addElementProperty(ElementProperty value)
			throws ConfigException{
		return addElementProperty(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addElementProperty(ElementProperty value, boolean overwrite)
			throws ConfigException{
		ElementProperty old = getElementPropertyByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(ProviderConfig.class).getString("cannotAddDuplicate",  "ElementProperty"));
		}
		return this.addValue(ELEMENT_PROPERTY, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeElementProperty(ElementProperty value){
		return this.removeValue(ELEMENT_PROPERTY, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeElementProperty(ElementProperty value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(ELEMENT_PROPERTY, value, overwrite);
	}

	public ElementProperty getElementPropertyByName(String id) {
	 if (null != id) { id = id.trim(); }
	ElementProperty[] o = getElementProperty();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
	}
	/**
	* Getter for ProviderId of the Element provider-config
	* @return  the ProviderId of the Element provider-config
	*/
	public String getProviderId() {
		return getAttributeValue(ServerTags.PROVIDER_ID);
	}
	/**
	* Modify  the ProviderId of the Element provider-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setProviderId(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.PROVIDER_ID, v, overwrite);
	}
	/**
	* Modify  the ProviderId of the Element provider-config
	* @param v the new value
	*/
	public void setProviderId(String v) {
		setAttributeValue(ServerTags.PROVIDER_ID, v);
	}
	/**
	* Getter for ProviderType of the Element provider-config
	* @return  the ProviderType of the Element provider-config
	*/
	public String getProviderType() {
		return getAttributeValue(ServerTags.PROVIDER_TYPE);
	}
	/**
	* Modify  the ProviderType of the Element provider-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setProviderType(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.PROVIDER_TYPE, v, overwrite);
	}
	/**
	* Modify  the ProviderType of the Element provider-config
	* @param v the new value
	*/
	public void setProviderType(String v) {
		setAttributeValue(ServerTags.PROVIDER_TYPE, v);
	}
	/**
	* Getter for ClassName of the Element provider-config
	* @return  the ClassName of the Element provider-config
	*/
	public String getClassName() {
		return getAttributeValue(ServerTags.CLASS_NAME);
	}
	/**
	* Modify  the ClassName of the Element provider-config
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setClassName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CLASS_NAME, v, overwrite);
	}
	/**
	* Modify  the ClassName of the Element provider-config
	* @param v the new value
	*/
	public void setClassName(String v) {
		setAttributeValue(ServerTags.CLASS_NAME, v);
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public RequestPolicy newRequestPolicy() {
		return new RequestPolicy();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ResponsePolicy newResponsePolicy() {
		return new ResponsePolicy();
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public ElementProperty newElementProperty() {
		return new ElementProperty();
	}

	/**
	* get the xpath representation for this element
	* returns something like abc[@name='value'] or abc
	* depending on the type of the bean
	*/
	protected String getRelativeXPath() {
	    String ret = null;
	    ret = "provider-config" + (canHaveSiblings() ? "[@provider-id='" + getAttributeValue("provider-id") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
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
		str.append(indent);
		str.append("RequestPolicy");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getRequestPolicy();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(REQUEST_POLICY, 0, str, indent);

		str.append(indent);
		str.append("ResponsePolicy");	// NOI18N
		n = (org.netbeans.modules.schema2beans.BaseBean) this.getResponsePolicy();
		if (n != null)
			n.dump(str, indent + "\t");	// NOI18N
		else
			str.append(indent+"\tnull");	// NOI18N
		this.dumpAttributes(RESPONSE_POLICY, 0, str, indent);

		str.append(indent);
		str.append("ElementProperty["+this.sizeElementProperty()+"]");	// NOI18N
		for(int i=0; i<this.sizeElementProperty(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getElementProperty(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(ELEMENT_PROPERTY, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("ProviderConfig\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


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
 *	This generated bean class ConnectorConnectionPool matches the DTD element connector-connection-pool
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

public class ConnectorConnectionPool extends ConfigBean implements Serializable
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(4, 2, 0);

	static public final String DESCRIPTION = "Description";
	static public final String SECURITY_MAP = "SecurityMap";
	static public final String ELEMENT_PROPERTY = "ElementProperty";

	public ConnectorConnectionPool() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public ConnectorConnectionPool(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(3);
		this.createProperty("description", DESCRIPTION, 
			Common.TYPE_0_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("security-map", SECURITY_MAP, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			SecurityMap.class);
		this.createAttribute(SECURITY_MAP, "name", "Name", 
						AttrProp.CDATA | AttrProp.REQUIRED,
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

	/**
	* Return  the Description of the Element connector-connection-pool
	*/
	public String getDescription() {
		return (String) getValue(ServerTags.DESCRIPTION);
	}
	/**
	* Modify  the Description of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setDescription(String v){
		setValue(ServerTags.DESCRIPTION, (null != v ? v.trim() : null));
		}
	// Get Method
	public SecurityMap getSecurityMap(int index) {
		return (SecurityMap)this.getValue(SECURITY_MAP, index);
	}

	// This attribute is an array, possibly empty
	public void setSecurityMap(SecurityMap[] value) {
		this.setValue(SECURITY_MAP, value);
	}

	// Getter Method
	public SecurityMap[] getSecurityMap() {
		return (SecurityMap[])this.getValues(SECURITY_MAP);
	}

	// Return the number of properties
	public int sizeSecurityMap() {
		return this.size(SECURITY_MAP);
	}

	// Add a new element returning its index in the list
	public int addSecurityMap(SecurityMap value)
			throws ConfigException{
		return addSecurityMap(value, true);
	}

	// Add a new element returning its index in the list with a boolean flag
	public int addSecurityMap(SecurityMap value, boolean overwrite)
			throws ConfigException{
		SecurityMap old = getSecurityMapByName(value.getName());
		if(old != null) {
			throw new ConfigException(StringManager.getManager(ConnectorConnectionPool.class).getString("cannotAddDuplicate",  "SecurityMap"));
		}
		return this.addValue(SECURITY_MAP, value, overwrite);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeSecurityMap(SecurityMap value){
		return this.removeValue(SECURITY_MAP, value);
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	// with boolean overwrite
	//
	public int removeSecurityMap(SecurityMap value, boolean overwrite)
			throws StaleWriteConfigException{
		return this.removeValue(SECURITY_MAP, value, overwrite);
	}

	public SecurityMap getSecurityMapByName(String id) {
	 if (null != id) { id = id.trim(); }
	SecurityMap[] o = getSecurityMap();
	 if (o == null) return null;

	 for (int i=0; i < o.length; i++) {
	     if(o[i].getAttributeValue(Common.convertName(ServerTags.NAME)).equals(id)) {
	         return o[i];
	     }
	 }

		return null;
		
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
			throw new ConfigException(StringManager.getManager(ConnectorConnectionPool.class).getString("cannotAddDuplicate",  "ElementProperty"));
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
	* Getter for Name of the Element connector-connection-pool
	* @return  the Name of the Element connector-connection-pool
	*/
	public String getName() {
		return getAttributeValue(ServerTags.NAME);
	}
	/**
	* Modify  the Name of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.NAME, v, overwrite);
	}
	/**
	* Modify  the Name of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setName(String v) {
		setAttributeValue(ServerTags.NAME, v);
	}
	/**
	* Getter for ResourceAdapterName of the Element connector-connection-pool
	* @return  the ResourceAdapterName of the Element connector-connection-pool
	*/
	public String getResourceAdapterName() {
		return getAttributeValue(ServerTags.RESOURCE_ADAPTER_NAME);
	}
	/**
	* Modify  the ResourceAdapterName of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setResourceAdapterName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.RESOURCE_ADAPTER_NAME, v, overwrite);
	}
	/**
	* Modify  the ResourceAdapterName of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setResourceAdapterName(String v) {
		setAttributeValue(ServerTags.RESOURCE_ADAPTER_NAME, v);
	}
	/**
	* Getter for ConnectionDefinitionName of the Element connector-connection-pool
	* @return  the ConnectionDefinitionName of the Element connector-connection-pool
	*/
	public String getConnectionDefinitionName() {
		return getAttributeValue(ServerTags.CONNECTION_DEFINITION_NAME);
	}
	/**
	* Modify  the ConnectionDefinitionName of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setConnectionDefinitionName(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CONNECTION_DEFINITION_NAME, v, overwrite);
	}
	/**
	* Modify  the ConnectionDefinitionName of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setConnectionDefinitionName(String v) {
		setAttributeValue(ServerTags.CONNECTION_DEFINITION_NAME, v);
	}
	/**
	* Getter for SteadyPoolSize of the Element connector-connection-pool
	* @return  the SteadyPoolSize of the Element connector-connection-pool
	*/
	public String getSteadyPoolSize() {
		return getAttributeValue(ServerTags.STEADY_POOL_SIZE);
	}
	/**
	* Modify  the SteadyPoolSize of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setSteadyPoolSize(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.STEADY_POOL_SIZE, v, overwrite);
	}
	/**
	* Modify  the SteadyPoolSize of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setSteadyPoolSize(String v) {
		setAttributeValue(ServerTags.STEADY_POOL_SIZE, v);
	}
	/**
	* Get the default value of SteadyPoolSize from dtd
	*/
	public static String getDefaultSteadyPoolSize() {
		return "8".trim();
	}
	/**
	* Getter for MaxPoolSize of the Element connector-connection-pool
	* @return  the MaxPoolSize of the Element connector-connection-pool
	*/
	public String getMaxPoolSize() {
		return getAttributeValue(ServerTags.MAX_POOL_SIZE);
	}
	/**
	* Modify  the MaxPoolSize of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxPoolSize(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_POOL_SIZE, v, overwrite);
	}
	/**
	* Modify  the MaxPoolSize of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setMaxPoolSize(String v) {
		setAttributeValue(ServerTags.MAX_POOL_SIZE, v);
	}
	/**
	* Get the default value of MaxPoolSize from dtd
	*/
	public static String getDefaultMaxPoolSize() {
		return "32".trim();
	}
	/**
	* Getter for MaxWaitTimeInMillis of the Element connector-connection-pool
	* @return  the MaxWaitTimeInMillis of the Element connector-connection-pool
	*/
	public String getMaxWaitTimeInMillis() {
		return getAttributeValue(ServerTags.MAX_WAIT_TIME_IN_MILLIS);
	}
	/**
	* Modify  the MaxWaitTimeInMillis of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxWaitTimeInMillis(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_WAIT_TIME_IN_MILLIS, v, overwrite);
	}
	/**
	* Modify  the MaxWaitTimeInMillis of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setMaxWaitTimeInMillis(String v) {
		setAttributeValue(ServerTags.MAX_WAIT_TIME_IN_MILLIS, v);
	}
	/**
	* Get the default value of MaxWaitTimeInMillis from dtd
	*/
	public static String getDefaultMaxWaitTimeInMillis() {
		return "60000".trim();
	}
	/**
	* Getter for PoolResizeQuantity of the Element connector-connection-pool
	* @return  the PoolResizeQuantity of the Element connector-connection-pool
	*/
	public String getPoolResizeQuantity() {
		return getAttributeValue(ServerTags.POOL_RESIZE_QUANTITY);
	}
	/**
	* Modify  the PoolResizeQuantity of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setPoolResizeQuantity(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.POOL_RESIZE_QUANTITY, v, overwrite);
	}
	/**
	* Modify  the PoolResizeQuantity of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setPoolResizeQuantity(String v) {
		setAttributeValue(ServerTags.POOL_RESIZE_QUANTITY, v);
	}
	/**
	* Get the default value of PoolResizeQuantity from dtd
	*/
	public static String getDefaultPoolResizeQuantity() {
		return "2".trim();
	}
	/**
	* Getter for IdleTimeoutInSeconds of the Element connector-connection-pool
	* @return  the IdleTimeoutInSeconds of the Element connector-connection-pool
	*/
	public String getIdleTimeoutInSeconds() {
		return getAttributeValue(ServerTags.IDLE_TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the IdleTimeoutInSeconds of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setIdleTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.IDLE_TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the IdleTimeoutInSeconds of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setIdleTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.IDLE_TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of IdleTimeoutInSeconds from dtd
	*/
	public static String getDefaultIdleTimeoutInSeconds() {
		return "300".trim();
	}
	/**
	* Getter for FailAllConnections of the Element connector-connection-pool
	* @return  the FailAllConnections of the Element connector-connection-pool
	*/
	public boolean isFailAllConnections() {
		return toBoolean(getAttributeValue(ServerTags.FAIL_ALL_CONNECTIONS));
	}
	/**
	* Modify  the FailAllConnections of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setFailAllConnections(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.FAIL_ALL_CONNECTIONS, ""+(v==true), overwrite);
	}
	/**
	* Modify  the FailAllConnections of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setFailAllConnections(boolean v) {
		setAttributeValue(ServerTags.FAIL_ALL_CONNECTIONS, ""+(v==true));
	}
	/**
	* Get the default value of FailAllConnections from dtd
	*/
	public static String getDefaultFailAllConnections() {
		return "false".trim();
	}
	/**
	* Getter for TransactionSupport of the Element connector-connection-pool
	* @return  the TransactionSupport of the Element connector-connection-pool
	*/
	public String getTransactionSupport() {
			return getAttributeValue(ServerTags.TRANSACTION_SUPPORT);
	}
	/**
	* Modify  the TransactionSupport of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setTransactionSupport(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.TRANSACTION_SUPPORT, v, overwrite);
	}
	/**
	* Modify  the TransactionSupport of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setTransactionSupport(String v) {
		setAttributeValue(ServerTags.TRANSACTION_SUPPORT, v);
	}
	/**
	* Getter for IsConnectionValidationRequired of the Element connector-connection-pool
	* @return  the IsConnectionValidationRequired of the Element connector-connection-pool
	*/
	public boolean isIsConnectionValidationRequired() {
		return toBoolean(getAttributeValue(ServerTags.IS_CONNECTION_VALIDATION_REQUIRED));
	}
	/**
	* Modify  the IsConnectionValidationRequired of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setIsConnectionValidationRequired(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.IS_CONNECTION_VALIDATION_REQUIRED, ""+(v==true), overwrite);
	}
	/**
	* Modify  the IsConnectionValidationRequired of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setIsConnectionValidationRequired(boolean v) {
		setAttributeValue(ServerTags.IS_CONNECTION_VALIDATION_REQUIRED, ""+(v==true));
	}
	/**
	* Get the default value of IsConnectionValidationRequired from dtd
	*/
	public static String getDefaultIsConnectionValidationRequired() {
		return "false".trim();
	}
	/**
	* Getter for ValidateAtmostOncePeriodInSeconds of the Element connector-connection-pool
	* @return  the ValidateAtmostOncePeriodInSeconds of the Element connector-connection-pool
	*/
	public String getValidateAtmostOncePeriodInSeconds() {
		return getAttributeValue(ServerTags.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS);
	}
	/**
	* Modify  the ValidateAtmostOncePeriodInSeconds of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setValidateAtmostOncePeriodInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the ValidateAtmostOncePeriodInSeconds of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setValidateAtmostOncePeriodInSeconds(String v) {
		setAttributeValue(ServerTags.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS, v);
	}
	/**
	* Get the default value of ValidateAtmostOncePeriodInSeconds from dtd
	*/
	public static String getDefaultValidateAtmostOncePeriodInSeconds() {
		return "0".trim();
	}
	/**
	* Getter for ConnectionLeakTimeoutInSeconds of the Element connector-connection-pool
	* @return  the ConnectionLeakTimeoutInSeconds of the Element connector-connection-pool
	*/
	public String getConnectionLeakTimeoutInSeconds() {
		return getAttributeValue(ServerTags.CONNECTION_LEAK_TIMEOUT_IN_SECONDS);
	}
	/**
	* Modify  the ConnectionLeakTimeoutInSeconds of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setConnectionLeakTimeoutInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CONNECTION_LEAK_TIMEOUT_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the ConnectionLeakTimeoutInSeconds of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setConnectionLeakTimeoutInSeconds(String v) {
		setAttributeValue(ServerTags.CONNECTION_LEAK_TIMEOUT_IN_SECONDS, v);
	}
	/**
	* Get the default value of ConnectionLeakTimeoutInSeconds from dtd
	*/
	public static String getDefaultConnectionLeakTimeoutInSeconds() {
		return "0".trim();
	}
	/**
	* Getter for ConnectionLeakReclaim of the Element connector-connection-pool
	* @return  the ConnectionLeakReclaim of the Element connector-connection-pool
	*/
	public boolean isConnectionLeakReclaim() {
		return toBoolean(getAttributeValue(ServerTags.CONNECTION_LEAK_RECLAIM));
	}
	/**
	* Modify  the ConnectionLeakReclaim of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setConnectionLeakReclaim(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CONNECTION_LEAK_RECLAIM, ""+(v==true), overwrite);
	}
	/**
	* Modify  the ConnectionLeakReclaim of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setConnectionLeakReclaim(boolean v) {
		setAttributeValue(ServerTags.CONNECTION_LEAK_RECLAIM, ""+(v==true));
	}
	/**
	* Get the default value of ConnectionLeakReclaim from dtd
	*/
	public static String getDefaultConnectionLeakReclaim() {
		return "false".trim();
	}
	/**
	* Getter for ConnectionCreationRetryAttempts of the Element connector-connection-pool
	* @return  the ConnectionCreationRetryAttempts of the Element connector-connection-pool
	*/
	public String getConnectionCreationRetryAttempts() {
		return getAttributeValue(ServerTags.CONNECTION_CREATION_RETRY_ATTEMPTS);
	}
	/**
	* Modify  the ConnectionCreationRetryAttempts of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setConnectionCreationRetryAttempts(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CONNECTION_CREATION_RETRY_ATTEMPTS, v, overwrite);
	}
	/**
	* Modify  the ConnectionCreationRetryAttempts of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setConnectionCreationRetryAttempts(String v) {
		setAttributeValue(ServerTags.CONNECTION_CREATION_RETRY_ATTEMPTS, v);
	}
	/**
	* Get the default value of ConnectionCreationRetryAttempts from dtd
	*/
	public static String getDefaultConnectionCreationRetryAttempts() {
		return "0".trim();
	}
	/**
	* Getter for ConnectionCreationRetryIntervalInSeconds of the Element connector-connection-pool
	* @return  the ConnectionCreationRetryIntervalInSeconds of the Element connector-connection-pool
	*/
	public String getConnectionCreationRetryIntervalInSeconds() {
		return getAttributeValue(ServerTags.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS);
	}
	/**
	* Modify  the ConnectionCreationRetryIntervalInSeconds of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setConnectionCreationRetryIntervalInSeconds(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS, v, overwrite);
	}
	/**
	* Modify  the ConnectionCreationRetryIntervalInSeconds of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setConnectionCreationRetryIntervalInSeconds(String v) {
		setAttributeValue(ServerTags.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS, v);
	}
	/**
	* Get the default value of ConnectionCreationRetryIntervalInSeconds from dtd
	*/
	public static String getDefaultConnectionCreationRetryIntervalInSeconds() {
		return "10".trim();
	}
	/**
	* Getter for LazyConnectionEnlistment of the Element connector-connection-pool
	* @return  the LazyConnectionEnlistment of the Element connector-connection-pool
	*/
	public boolean isLazyConnectionEnlistment() {
		return toBoolean(getAttributeValue(ServerTags.LAZY_CONNECTION_ENLISTMENT));
	}
	/**
	* Modify  the LazyConnectionEnlistment of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLazyConnectionEnlistment(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LAZY_CONNECTION_ENLISTMENT, ""+(v==true), overwrite);
	}
	/**
	* Modify  the LazyConnectionEnlistment of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setLazyConnectionEnlistment(boolean v) {
		setAttributeValue(ServerTags.LAZY_CONNECTION_ENLISTMENT, ""+(v==true));
	}
	/**
	* Get the default value of LazyConnectionEnlistment from dtd
	*/
	public static String getDefaultLazyConnectionEnlistment() {
		return "false".trim();
	}
	/**
	* Getter for LazyConnectionAssociation of the Element connector-connection-pool
	* @return  the LazyConnectionAssociation of the Element connector-connection-pool
	*/
	public boolean isLazyConnectionAssociation() {
		return toBoolean(getAttributeValue(ServerTags.LAZY_CONNECTION_ASSOCIATION));
	}
	/**
	* Modify  the LazyConnectionAssociation of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setLazyConnectionAssociation(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.LAZY_CONNECTION_ASSOCIATION, ""+(v==true), overwrite);
	}
	/**
	* Modify  the LazyConnectionAssociation of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setLazyConnectionAssociation(boolean v) {
		setAttributeValue(ServerTags.LAZY_CONNECTION_ASSOCIATION, ""+(v==true));
	}
	/**
	* Get the default value of LazyConnectionAssociation from dtd
	*/
	public static String getDefaultLazyConnectionAssociation() {
		return "false".trim();
	}
	/**
	* Getter for AssociateWithThread of the Element connector-connection-pool
	* @return  the AssociateWithThread of the Element connector-connection-pool
	*/
	public boolean isAssociateWithThread() {
		return toBoolean(getAttributeValue(ServerTags.ASSOCIATE_WITH_THREAD));
	}
	/**
	* Modify  the AssociateWithThread of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setAssociateWithThread(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.ASSOCIATE_WITH_THREAD, ""+(v==true), overwrite);
	}
	/**
	* Modify  the AssociateWithThread of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setAssociateWithThread(boolean v) {
		setAttributeValue(ServerTags.ASSOCIATE_WITH_THREAD, ""+(v==true));
	}
	/**
	* Get the default value of AssociateWithThread from dtd
	*/
	public static String getDefaultAssociateWithThread() {
		return "false".trim();
	}
	/**
	* Getter for MatchConnections of the Element connector-connection-pool
	* @return  the MatchConnections of the Element connector-connection-pool
	*/
	public boolean isMatchConnections() {
		return toBoolean(getAttributeValue(ServerTags.MATCH_CONNECTIONS));
	}
	/**
	* Modify  the MatchConnections of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMatchConnections(boolean v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MATCH_CONNECTIONS, ""+(v==true), overwrite);
	}
	/**
	* Modify  the MatchConnections of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setMatchConnections(boolean v) {
		setAttributeValue(ServerTags.MATCH_CONNECTIONS, ""+(v==true));
	}
	/**
	* Get the default value of MatchConnections from dtd
	*/
	public static String getDefaultMatchConnections() {
		return "true".trim();
	}
	/**
	* Getter for MaxConnectionUsageCount of the Element connector-connection-pool
	* @return  the MaxConnectionUsageCount of the Element connector-connection-pool
	*/
	public String getMaxConnectionUsageCount() {
		return getAttributeValue(ServerTags.MAX_CONNECTION_USAGE_COUNT);
	}
	/**
	* Modify  the MaxConnectionUsageCount of the Element connector-connection-pool
	* @param v the new value
	* @throws StaleWriteConfigException if overwrite is false and file changed on disk
	*/
	public void setMaxConnectionUsageCount(String v, boolean overwrite) throws StaleWriteConfigException {
		setAttributeValue(ServerTags.MAX_CONNECTION_USAGE_COUNT, v, overwrite);
	}
	/**
	* Modify  the MaxConnectionUsageCount of the Element connector-connection-pool
	* @param v the new value
	*/
	public void setMaxConnectionUsageCount(String v) {
		setAttributeValue(ServerTags.MAX_CONNECTION_USAGE_COUNT, v);
	}
	/**
	* Get the default value of MaxConnectionUsageCount from dtd
	*/
	public static String getDefaultMaxConnectionUsageCount() {
		return "0".trim();
	}
	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public SecurityMap newSecurityMap() {
		return new SecurityMap();
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
	    ret = "connector-connection-pool" + (canHaveSiblings() ? "[@name='" + getAttributeValue("name") +"']" : "") ;
	    return (null != ret ? ret.trim() : null);
	}

	/*
	* generic method to get default value from dtd
	*/
	public static String getDefaultAttributeValue(String attr) {
		if(attr == null) return null;
		attr = attr.trim();
		if(attr.equals(ServerTags.STEADY_POOL_SIZE)) return "8".trim();
		if(attr.equals(ServerTags.MAX_POOL_SIZE)) return "32".trim();
		if(attr.equals(ServerTags.MAX_WAIT_TIME_IN_MILLIS)) return "60000".trim();
		if(attr.equals(ServerTags.POOL_RESIZE_QUANTITY)) return "2".trim();
		if(attr.equals(ServerTags.IDLE_TIMEOUT_IN_SECONDS)) return "300".trim();
		if(attr.equals(ServerTags.FAIL_ALL_CONNECTIONS)) return "false".trim();
		if(attr.equals(ServerTags.IS_CONNECTION_VALIDATION_REQUIRED)) return "false".trim();
		if(attr.equals(ServerTags.VALIDATE_ATMOST_ONCE_PERIOD_IN_SECONDS)) return "0".trim();
		if(attr.equals(ServerTags.CONNECTION_LEAK_TIMEOUT_IN_SECONDS)) return "0".trim();
		if(attr.equals(ServerTags.CONNECTION_LEAK_RECLAIM)) return "false".trim();
		if(attr.equals(ServerTags.CONNECTION_CREATION_RETRY_ATTEMPTS)) return "0".trim();
		if(attr.equals(ServerTags.CONNECTION_CREATION_RETRY_INTERVAL_IN_SECONDS)) return "10".trim();
		if(attr.equals(ServerTags.LAZY_CONNECTION_ENLISTMENT)) return "false".trim();
		if(attr.equals(ServerTags.LAZY_CONNECTION_ASSOCIATION)) return "false".trim();
		if(attr.equals(ServerTags.ASSOCIATE_WITH_THREAD)) return "false".trim();
		if(attr.equals(ServerTags.MATCH_CONNECTIONS)) return "true".trim();
		if(attr.equals(ServerTags.MAX_CONNECTION_USAGE_COUNT)) return "0".trim();
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
		str.append("Description");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getDescription();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(DESCRIPTION, 0, str, indent);

		str.append(indent);
		str.append("SecurityMap["+this.sizeSecurityMap()+"]");	// NOI18N
		for(int i=0; i<this.sizeSecurityMap(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getSecurityMap(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(SECURITY_MAP, i, str, indent);
		}

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
		str.append("ConnectorConnectionPool\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

// END_NOI18N


/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
/*
 * JmxHandlers.java
 *
 * Created on September 28, 2006, 11:20 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.tools.admingui.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.Vector;
import java.util.ArrayList;

import javax.faces.model.SelectItem;

import com.sun.jsftemplating.annotation.Handler; 
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.NodeAgentConfig;
import com.sun.appserv.management.config.JMXConnectorConfig;
import com.sun.appserv.management.config.HTTPListenerConfig;
import com.sun.appserv.management.config.IIOPListenerConfig;
import com.sun.appserv.management.config.SSLConfigContainer;
import com.sun.appserv.management.config.SSLConfig;
import com.sun.appserv.management.config.VirtualServerConfig;
import com.sun.enterprise.tools.admingui.util.AMXUtil;
import com.sun.enterprise.tools.admingui.util.JMXUtil;
import com.sun.enterprise.tools.admingui.util.GuiUtil;

import javax.management.Attribute;
import javax.net.ssl.SSLServerSocketFactory;

/**
 *
 * @author anilam
 */
public class JmxHandlers {
    
    static String[] COMMON_CIPHERS = {"SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA",
        "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA"};
    
    static String[] BIT_CIPHERS = {"SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
        "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
        "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA"};
    
    /** Creates a new instance of JmxHandlers */
    public JmxHandlers() {
    }
    
    /**
     *  <p> This handler returns the values for the attributes in
     *      Admin Service Page </p>
     *  <p> Input value: "ConfigName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "JmxConnectorName"  -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "Properties"        -- Type: <code>java.util.Map</code></p>
     *  @param context The HandlerContext.
     */
    @Handler(id="getAdminServiceProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true) },
    output={
        @HandlerOutput(name="JmxConnectorName",  type=String.class),
        @HandlerOutput(name="Properties",        type=Map.class)})

    public static void getAdminServiceProps(HandlerContext handlerCtx) {
        String configName = ((String)handlerCtx.getInputValue("ConfigName"));
        ConfigConfig config = AMXUtil.getConfig(configName);
        handlerCtx.setOutputValue("JmxConnectorName", config.getAdminServiceConfig().getSystemJMXConnectorName());
        handlerCtx.setOutputValue("Properties", config.getAdminServiceConfig().getProperties());
    }
     
    /**
     *  <p> This handler saves the values for the properties in
     *      Admin Service Page </p>
     *  <p> Input value: "ConfigName"         -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AddProps"           -- Type: <code>java.util.Map</code></p>
     *  <p> Input value: "RemoveProps"        -- Type: <code>java.util.ArrayList</code></p>
     *  @param context The HandlerContext.
     */
    @Handler(id="saveAdminServiceProps",
    input={
        @HandlerInput(name="ConfigName", type=String.class, required=true),
        @HandlerInput(name="AddProps", type=Map.class),
        @HandlerInput(name="RemoveProps", type=ArrayList.class)})

    public static void saveAdminServiceProps(HandlerContext handlerCtx) {
        String configName = ((String)handlerCtx.getInputValue("ConfigName"));
        ConfigConfig config = AMXUtil.getConfig(configName);
        AMXUtil.editProperties(handlerCtx, config.getAdminServiceConfig());
    }
    
       /**
         *  <p> This handler returns the values for all the attributes in
         *      Edit JMX Connector Page </p>
         *  <p> Input value: "ConfigName"         -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "JmxName"            -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "JmxProtocol"       -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "JmxServUrl"        -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "Address"           -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "Port"              -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "RealmName"         -- Type: <code>java.lang.String</code></p>
         *  @param context The HandlerContext.
         */
        @Handler(id="getJmxConnectorSettings",
            input={
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="JmxName",       type=String.class, required=true)   },
            output={
            @HandlerOutput(name="JmxProtocol",  type=String.class),
            @HandlerOutput(name="JmxServUrl",   type=String.class),
            @HandlerOutput(name="Address",      type=String.class),
            @HandlerOutput(name="security",      type=Boolean.class),
            @HandlerOutput(name="Port",         type=String.class),
            @HandlerOutput(name="RealmName",    type=String.class),
            @HandlerOutput(name="clientHostname",    type=String.class)})
            
            public static void getJmxConnectorSettings(HandlerContext handlerCtx) {
            String jmxConnName = ((String)handlerCtx.getInputValue("JmxName"));
            String configName = ((String)handlerCtx.getInputValue("ConfigName"));
            ConfigConfig config = AMXUtil.getConfig(configName);                       
            JMXConnectorConfig connectorConfig = config.getAdminServiceConfig().getJMXConnectorConfigMap().get(jmxConnName);
           
            handlerCtx.setOutputValue("JmxProtocol", connectorConfig.getProtocol());
            handlerCtx.setOutputValue("JmxServUrl", getJMXConnectorServURL(jmxConnName, configName));
            handlerCtx.setOutputValue("Address", connectorConfig.getAddress());
            handlerCtx.setOutputValue("security", connectorConfig.getSecurityEnabled());
            handlerCtx.setOutputValue("Port", connectorConfig.getPort());
            handlerCtx.setOutputValue("RealmName", connectorConfig.getAuthRealmName());
            if (AMXUtil.isEE()){
                String client = "";
                if (connectorConfig.existsProperty("client-hostname"))
                    client = connectorConfig.getPropertyValue("client-hostname");
                handlerCtx.setOutputValue("clientHostname", client);
            }
        }   
        
        /**
         *  <p> This handler returns the values for all the attributes in
         *      SSL Page </p>
         *  <p> Input value: "ConfigName"             -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Name"                   -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Type"                   -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Edit"                   -- Type: <code>java.lang.Boolean</code></p>
         *  <p> Output value: "Security"              -- Type: <code>java.lang.Boolean</code></p>
         *  <p> Output value: "ClientAuth"            -- Type: <code>java.lang.Boolean</code></p>
         *  <p> Output value: "CertNickname"          -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "CommonCiphersList"     -- Type: <code>java.util.Array</code></p>
         *  <p> Output value: "CiphersList"           -- Type: <code>java.util.Array</code></p>
         *  <p> Output value: "EphemeralCiphersList"  -- Type: <code>java.util.Array</code></p>
         *  <p> Output value: "OtherCiphersList"      -- Type: <code>java.util.Array</code></p>
         *  <p> Output value: "EccCiphersList"        -- Type: <code>java.util.Array</code></p>
         *  <p> Output value: "SelectedCommon"        -- Type: <code>java.util.Array</code></p>
         *  <p> Output value: "SelectedEph"           -- Type: <code>java.util.Array</code></p>
         *  <p> Output value: "SelectedOther"         -- Type: <code>java.util.Array</code></p>
         *  <p> Output value: "SelectedEcc"           -- Type: <code>java.util.Array</code></p>
         *  @param context The HandlerContext.
         */
        
        @Handler(id="getSSLProperties",
        input={
            @HandlerInput(name="ConfigName",            type=String.class, required=true),
            @HandlerInput(name="Name",                  type=String.class, required=true),
            @HandlerInput(name="Type",                  type=String.class, required=true),
            @HandlerInput(name="Edit",                  type=Boolean.class, required=true) },
        output={
            @HandlerOutput(name="ClientAuth",           type=Boolean.class),
            @HandlerOutput(name="CertNickname",         type=String.class),
            @HandlerOutput(name="SSL3Prop",             type=Boolean.class),
            @HandlerOutput(name="SSL2Prop",             type=Boolean.class),    
            @HandlerOutput(name="TLSProp",              type=Boolean.class),
            @HandlerOutput(name="CommonCiphersList",    type=SelectItem[].class),
            @HandlerOutput(name="EphemeralCiphersList", type=SelectItem[].class),
            @HandlerOutput(name="OtherCiphersList",     type=SelectItem[].class),
            @HandlerOutput(name="EccCiphersList",       type=SelectItem[].class),
            @HandlerOutput(name="SelectedCommon",       type=String[].class),
            @HandlerOutput(name="SelectedEph",          type=String[].class),
            @HandlerOutput(name="SelectedOther",        type=String[].class),
            @HandlerOutput(name="SelectedEcc",          type=String[].class)})
        
        public static void getSSLProperties(HandlerContext handlerCtx) {
            String oName = ((String)handlerCtx.getInputValue("Name"));
            ConfigConfig config = AMXUtil.getConfig(((String)handlerCtx.getInputValue("ConfigName")));
            String selectedCiphers = null;
            String type = (String)handlerCtx.getInputValue("Type");
            SSLConfigContainer sslContainerConfig = null;
            boolean ssl3Prop = false;
            boolean ssl2Prop = false;
            boolean tlsProp = false;
            boolean clientAuth = false;
            boolean isEdit = ((Boolean)handlerCtx.getInputValue("Edit")).booleanValue();
            Map sslProps = (Map)handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("sslProps");
            if(isEdit){
                if(type.equals("jmx")){
                    JMXConnectorConfig jmxConfig = config.getAdminServiceConfig().getJMXConnectorConfigMap().get(oName);
                    sslContainerConfig = (SSLConfigContainer)jmxConfig;
                }else if(type.equals("iiop")){
                    IIOPListenerConfig iiopConfig = config.getIIOPServiceConfig().getIIOPListenerConfigMap().get(oName);
                    sslContainerConfig = (SSLConfigContainer)iiopConfig;
                }else if(type.equals("http")){
                    HTTPListenerConfig httpConfig = config.getHTTPServiceConfig().getHTTPListenerConfigMap().get(oName);
                    sslContainerConfig = (SSLConfigContainer)httpConfig;
                }else if(type.equals("nodeagent")){
                    NodeAgentConfig agentConfig = AMXUtil.getDomainConfig().getNodeAgentConfigMap().get(oName);
                    JMXConnectorConfig jmxConfig = agentConfig.getJMXConnectorConfig();
                    sslContainerConfig = (SSLConfigContainer)jmxConfig;
                }
                
                SSLConfig sslConfig = sslContainerConfig.getSSLConfig();
                if(sslConfig != null){
                    clientAuth = sslConfig.getClientAuthEnabled();
                    handlerCtx.setOutputValue("CertNickname", sslConfig.getCertNickname());
                    ssl3Prop = sslConfig.getSSL3Enabled();
                    ssl2Prop = sslConfig.getSSL2Enabled();
                    tlsProp = sslConfig.getTLSEnabled();
                    selectedCiphers = sslConfig.getSSL3TLSCiphers();
                }
            }else{
                if(sslProps != null){
                    clientAuth = (Boolean)sslProps.get("clientAuth");
                    ssl3Prop = (Boolean)sslProps.get("ssl3Prop");
                    ssl2Prop = (Boolean)sslProps.get("ssl2Prop");                    
                    tlsProp = (Boolean)sslProps.get("tlsProp");
                    handlerCtx.setOutputValue("CertNickname", sslProps.get("certNickname"));
                }
            }
            SSLServerSocketFactory factory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
            String[] supportedCiphers = factory.getDefaultCipherSuites();
            Vector ciphers = getCiphersVector(supportedCiphers);
            
            SelectItem[] commonCiphers = ConfigurationHandlers.getOptions(getCommonCiphers(ciphers));
            SelectItem[] ephemeralCiphers = ConfigurationHandlers.getOptions(getEphemeralCiphers(ciphers));
            SelectItem[] otherCiphers = ConfigurationHandlers.getOptions(getOtherCiphers(ciphers));
            SelectItem[] eccCiphers = ConfigurationHandlers.getOptions(getEccCiphers(ciphers));

            handlerCtx.setOutputValue("ClientAuth", clientAuth);
            handlerCtx.setOutputValue("SSL3Prop", ssl3Prop);            
            handlerCtx.setOutputValue("SSL2Prop", ssl2Prop);
            handlerCtx.setOutputValue("TLSProp", tlsProp);
            handlerCtx.setOutputValue("CommonCiphersList", commonCiphers); //NOI18N
            handlerCtx.setOutputValue("EphemeralCiphersList", ephemeralCiphers); //NOI18N
            handlerCtx.setOutputValue("OtherCiphersList", otherCiphers); //NOI18N
            handlerCtx.setOutputValue("EccCiphersList", eccCiphers); //NOI18N
            if(sslProps == null){
                String[] ciphersArr = getSelectedCiphersList(selectedCiphers);
                Vector selValues = getCiphersVector(ciphersArr);
                handlerCtx.setOutputValue("SelectedCommon", getCommonCiphers(selValues)); //NOI18N
                handlerCtx.setOutputValue("SelectedEph", getEphemeralCiphers(selValues)); //NOI18N
                handlerCtx.setOutputValue("SelectedOther", getOtherCiphers(selValues)); //NOI18N
                handlerCtx.setOutputValue("SelectedEcc", getEccCiphers(selValues)); //NOI18N
            }else{
                handlerCtx.setOutputValue("SelectedCommon", sslProps.get("selectedCommon")); //NOI18N
                handlerCtx.setOutputValue("SelectedEph", sslProps.get("selectedEph")); //NOI18N
                handlerCtx.setOutputValue("SelectedOther", sslProps.get("selectedOther")); //NOI18N
                handlerCtx.setOutputValue("SelectedEcc", sslProps.get("selectedEcc")); //NOI18N
            }
        }
        
        /**
         *  <p> This handler returns the default values for all the attributes in
         *      Edit JMX Connector Page </p>
         *  <p> Input value: "ConfigName"     -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "JmxName"       -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "JmxProtocol"   -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "JmxServUrl"    -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "Address"       -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "Port"          -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "RealmName"     -- Type: <code>java.lang.String</code></p>
         *  @param context The HandlerContext.
         */
        @Handler(id="getDefaultJmxConnectorSettings",
            input={
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="JmxName",       type=String.class, required=true)   },
            output={
            @HandlerOutput(name="JmxProtocol",  type=String.class),
            @HandlerOutput(name="JmxServUrl",   type=String.class),
            @HandlerOutput(name="Address",      type=String.class),
            @HandlerOutput(name="Port",         type=String.class),
            @HandlerOutput(name="RealmName",    type=String.class) })
            
            public static void getDefaultJmxConnectorSettings(HandlerContext handlerCtx) {
            String jmxConnName = ((String)handlerCtx.getInputValue("JmxName"));
            String configName = ((String)handlerCtx.getInputValue("ConfigName"));
            ConfigConfig config = AMXUtil.getConfig(configName);
            JMXConnectorConfig connectorConfig = config.getAdminServiceConfig().getJMXConnectorConfigMap().get(jmxConnName);

            handlerCtx.setOutputValue("JmxProtocol", connectorConfig.getDefaultValue("Protocol"));
            handlerCtx.setOutputValue("JmxServUrl", getJMXConnectorServURL(jmxConnName, configName));
            handlerCtx.setOutputValue("Address", connectorConfig.getDefaultValue("Address"));
            handlerCtx.setOutputValue("Port", connectorConfig.getDefaultValue("Port"));
            handlerCtx.setOutputValue("RealmName", connectorConfig.getDefaultValue("AuthRealmName"));
        }   
               
        /**
         *  <p> This handler saves the values for all the attributes in
         *      Edit JMX Connector Page </p>
         *  <p> Input value: "ConfigName"     -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "JmxName"        -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Address"       -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Port"          -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "security"      -- Type: <code>java.lang.Boolean</code></p>
         *  <p> Input value: "RealmName"     -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Security"      -- Type: <code>java.lang.Boolean</code></p>
         *  @param context The HandlerContext.
         */
        @Handler(id="saveJmxConnectorSettings",
            input={
            @HandlerInput(name="ConfigName", type=String.class, required=true),
            @HandlerInput(name="JmxName",       type=String.class, required=true),
            @HandlerInput(name="Address",      type=String.class),
            @HandlerInput(name="Port",         type=String.class),
            @HandlerInput(name="security",         type=Boolean.class),
            @HandlerInput(name="RealmName",    type=String.class),
            @HandlerInput(name="clientHostname",    type=String.class)})
            
            public static void saveJmxConnectorSettings(HandlerContext handlerCtx) {
            String jmxConnName = ((String)handlerCtx.getInputValue("JmxName"));
            String configName = ((String)handlerCtx.getInputValue("ConfigName"));
            ConfigConfig config = AMXUtil.getConfig(configName);
            JMXConnectorConfig connectorConfig = config.getAdminServiceConfig().getJMXConnectorConfigMap().get(jmxConnName);
            String objName = "com.sun.appserv:type=jmx-connector,name=" + jmxConnName +",config=" + configName + ",category=config"; //NOI18N
            try{
                Boolean security = (Boolean) handlerCtx.getInputValue("security");
                JMXUtil.setAttribute(objName, new Attribute("security-enabled", (security == null) ? Boolean.FALSE:  security ));
                JMXUtil.setAttribute(objName, new Attribute("port", handlerCtx.getInputValue("Port")));
                JMXUtil.setAttribute(objName, new Attribute("address", handlerCtx.getInputValue("Address")));
                JMXUtil.setAttribute(objName, new Attribute("auth-realm-name", handlerCtx.getInputValue("RealmName")));

                if( AMXUtil.isEE()){
                    String clientHostname = (String) handlerCtx.getInputValue("clientHostname");
                    AMXUtil.changeProperty(connectorConfig, "client-hostname", clientHostname);
                }
            }catch ( Exception ex){
                GuiUtil.handleException(handlerCtx, ex);
            }
        }   
        
        /**
         *  <p> This handler saves the values for all the attributes in
         *      SSL Page </p>
         *  <p> Input value: "ConfigName"            -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Name"                  -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Type"                  -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Edit"                  -- Type: <code>java.lang.Boolean</code></p>
         *  <p> Input value: "ClientAuth"            -- Type: <code>java.lang.Boolean</code></p>
         *  <p> Input value: "CertNickname"          -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "CommonCiphersList"     -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "EphemeralCiphersList"  -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "OtherCiphersList"      -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "EccCiphersList"        -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "SelectedCommon"        -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "SelectedEphemeral"     -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "SelectedOther"         -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "SelectedEcc"           -- Type: <code>java.util.Array</code></p>
         *  @param context The HandlerContext.
         */
        @Handler(id="saveSSLProperties",
            input={
            @HandlerInput(name="ConfigName",            type=String.class, required=true),
            @HandlerInput(name="Name",                  type=String.class, required=true),
            @HandlerInput(name="Type",                  type=String.class, required=true),
            @HandlerInput(name="Edit",                  type=Boolean.class, required=true),
            @HandlerInput(name="ClientAuth",            type=Boolean.class),
            @HandlerInput(name="CertNickname",          type=String.class),
            @HandlerInput(name="SSL3Prop",              type=Boolean.class),
            @HandlerInput(name="SSL2Prop",              type=Boolean.class),
            @HandlerInput(name="TLSProp",               type=Boolean.class),
            @HandlerInput(name="CommonCiphersList",     type=SelectItem[].class),
            @HandlerInput(name="EphemeralCiphersList",  type=SelectItem[].class),
            @HandlerInput(name="OtherCiphersList",      type=SelectItem[].class),
            @HandlerInput(name="EccCiphersList",        type=SelectItem[].class),
            @HandlerInput(name="SelectedCommon",        type=String[].class),
            @HandlerInput(name="SelectedEph",           type=String[].class),
            @HandlerInput(name="SelectedOther",         type=String[].class),
            @HandlerInput(name="SelectedEcc",           type=String[].class) })
            
        public static void saveSSLProperties(HandlerContext handlerCtx) {
            try{
                String oName = (String)handlerCtx.getInputValue("Name");
                String configName = (String)handlerCtx.getInputValue("ConfigName");
                ConfigConfig config = AMXUtil.getConfig(configName);
                String certNickname = (String)handlerCtx.getInputValue("CertNickname");
                String type = (String)handlerCtx.getInputValue("Type");
                SSLConfigContainer sslContainerConfig = null;
                boolean isEdit = ((Boolean)handlerCtx.getInputValue("Edit")).booleanValue();
                if(isEdit){
                    if(type.equals("jmx")){
                        JMXConnectorConfig jmxConfig = config.getAdminServiceConfig().getJMXConnectorConfigMap().get(oName);
                        sslContainerConfig = (SSLConfigContainer)jmxConfig;
                    }else if(type.equals("iiop")){
                        IIOPListenerConfig iiopConfig = config.getIIOPServiceConfig().getIIOPListenerConfigMap().get(oName);
                        sslContainerConfig = (SSLConfigContainer)iiopConfig;
                    }else if(type.equals("http")){
                        HTTPListenerConfig httpConfig = config.getHTTPServiceConfig().getHTTPListenerConfigMap().get(oName);
                        sslContainerConfig = (SSLConfigContainer)httpConfig;
                    }else if(type.equals("nodeagent")){
                        NodeAgentConfig agentConfig = AMXUtil.getDomainConfig().getNodeAgentConfigMap().get(oName);
                        JMXConnectorConfig jmxConfig = agentConfig.getJMXConnectorConfig();
                        sslContainerConfig = (SSLConfigContainer)jmxConfig;
                    }
                    if((sslContainerConfig != null) && (sslContainerConfig.getSSLConfig() != null)){
                        sslContainerConfig.removeSSLConfig();
                    }
                }else{
                    if(type.equals("iiop")){
                        Map props = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("iiopProps");
                        Map options = AMXUtil.convertToPropertiesOptionMap((Map)props.get("options"), null);
                        IIOPListenerConfig iiopConfig = config.getIIOPServiceConfig().createIIOPListenerConfig(
                                (String) props.get("iiopName"),
                                (String) props.get("address"),
                                options);
                        iiopConfig.setPort((String) props.get("port"));
                        iiopConfig.setEnabled((Boolean)props.get("listener"));
                        iiopConfig.setSecurityEnabled((Boolean)props.get("security"));
                        sslContainerConfig = (SSLConfigContainer)iiopConfig;
                    }else if(type.equals("http")){
                        Map props = (Map) handlerCtx.getFacesContext().getExternalContext().getSessionMap().get("httpProps");
                        Map options = AMXUtil.convertToPropertiesOptionMap((Map)props.get("options"), null);
                        int port = Integer.parseInt((String)props.get("port"));
                        String vs = (String) props.get("virtualServer");
                        String httpName = (String) props.get("httpName");
                        String serverName = (String)props.get("serverName");
                        //issue 3714. we need to pass in a String with a space, instead of empty string so backend will take it.
                        //server-name is allowed to be empty. see admin-core/admin/dtds/admin-mbeans-descriptors.xml
                        if (GuiUtil.isEmpty(serverName))serverName = " ";
                        
                        HTTPListenerConfig httpConfig = config.getHTTPServiceConfig().createHTTPListenerConfig(
                                httpName,
                                (String) props.get("address"),
                                port,
                                vs,
                                serverName,
                                options);
                        httpConfig.setEnabled((Boolean)props.get("enabled"));
                        httpConfig.setSecurityEnabled((Boolean)props.get("securityEnabled"));
                        httpConfig.setRedirectPort((String)props.get("redirectPort"));
                        httpConfig.setAcceptorThreads((String)props.get("acceptor-threads"));
                        httpConfig.setXpoweredBy((Boolean)props.get("xpowered-by"));
                        httpConfig.setBlockingEnabled((Boolean)props.get("blocking-enabled"));
                        VirtualServerConfig vsConfig= config.getHTTPServiceConfig().getVirtualServerConfigMap().get(vs);
                        String listeners = vsConfig.getHTTPListeners();
                        if (GuiUtil.isEmpty(listeners))
                            vsConfig.setHTTPListeners(httpName);
                        else
                            vsConfig.setHTTPListeners(listeners+","+httpName);
                        sslContainerConfig = (SSLConfigContainer)httpConfig;
                    }
                }
                
                if((certNickname != null) && (!certNickname.equals(""))){
                    SSLConfig sslConfig = sslContainerConfig.getSSLConfig();
                    if(sslConfig == null){
                        sslConfig = sslContainerConfig.createSSLConfig(certNickname, new HashMap());
                    }else{
                        sslConfig.setCertNickname(certNickname);
                    }
                    sslConfig.setClientAuthEnabled((Boolean)handlerCtx.getInputValue("ClientAuth"));
                    boolean ssl3Prop = ((Boolean)handlerCtx.getInputValue("SSL3Prop")).booleanValue();
                    sslConfig.setSSL3Enabled(ssl3Prop);                   
                    if(!type.equals("iiop")) {
                        boolean ssl2Prop = ((Boolean)handlerCtx.getInputValue("SSL2Prop")).booleanValue();
                        sslConfig.setSSL2Enabled(ssl2Prop);                                            
                    }
                    boolean tlsProp = ((Boolean)handlerCtx.getInputValue("TLSProp")).booleanValue();
                    sslConfig.setTLSEnabled(tlsProp);
                    if(ssl3Prop || tlsProp){
                        SSLServerSocketFactory factory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
                        String[] supportedCiphers = factory.getDefaultCipherSuites();
                        Vector ciphersVector = getCiphersVector(supportedCiphers);
                        String[] selectedCiphers = getSelectedCiphersList(sslConfig.getSSL3TLSCiphers());
                        String[] selectedCommon = (String[])handlerCtx.getInputValue("SelectedCommon");
                        String[] selectedEph = (String[])handlerCtx.getInputValue("SelectedEph");
                        String[] selectedOther = (String[])handlerCtx.getInputValue("SelectedOther");
                        String[] selectedEcc = (String[])handlerCtx.getInputValue("SelectedEcc");
                        
                        //TODO Nitya - SSL Ciphers settings has issues. Has errors and does not save
                        // properly when selections are removed
                        String ciphers = processSelectedCiphers(selectedCommon, "");
                        ciphers = processSelectedCiphers(selectedEph, ciphers);
                        ciphers = processSelectedCiphers(selectedOther, ciphers);
                        ciphers = processSelectedCiphers(selectedEcc, ciphers);
                        //ciphers = processDeletedCiphers(selectedCiphers, ciphers);
                        
                        sslConfig.setSSL3TLSCiphers(ciphers);
                    }
                }
            }catch(Exception ex){
                GuiUtil.handleException(handlerCtx, ex);
            }
        }
        
        /**
         *  <p> This handler saves the values for all the attributes in
         *      SSL Page </p>
         *  <p> Input value: "ConfigName"            -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Name"                  -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Type"                  -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Edit"                  -- Type: <code>java.lang.Boolean</code></p>
         *  <p> Input value: "ClientAuth"            -- Type: <code>java.lang.Boolean</code></p>
         *  <p> Input value: "CertNickname"          -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "CommonCiphersList"     -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "EphemeralCiphersList"  -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "OtherCiphersList"      -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "EccCiphersList"        -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "SelectedCommon"        -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "SelectedEphemeral"     -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "SelectedOther"         -- Type: <code>java.util.Array</code></p>
         *  <p> Input value: "SelectedEcc"           -- Type: <code>java.util.Array</code></p>
         *  @param context The HandlerContext.
         */
        @Handler(id="updateSSLProperties",
            input={
            @HandlerInput(name="ClientAuth",            type=Boolean.class),
            @HandlerInput(name="CertNickname",          type=String.class),
            @HandlerInput(name="SSL3Prop",              type=Boolean.class),
            @HandlerInput(name="SSL2Prop",              type=Boolean.class),
            @HandlerInput(name="TLSProp",               type=Boolean.class),
            @HandlerInput(name="CommonCiphersList",     type=SelectItem[].class),
            @HandlerInput(name="EphemeralCiphersList",  type=SelectItem[].class),
            @HandlerInput(name="OtherCiphersList",      type=SelectItem[].class),
            @HandlerInput(name="EccCiphersList",        type=SelectItem[].class),
            @HandlerInput(name="SelectedCommon",        type=String[].class),
            @HandlerInput(name="SelectedEph",           type=String[].class),
            @HandlerInput(name="SelectedOther",         type=String[].class),
            @HandlerInput(name="SelectedEcc",           type=String[].class) })
            
            public static void updateSSLProperties(HandlerContext handlerCtx) {
            Map sslPropsMap = new HashMap();
            sslPropsMap.put("certNickname", (String)handlerCtx.getInputValue("CertNickname"));
            sslPropsMap.put("clientAuth", ((Boolean)handlerCtx.getInputValue("ClientAuth")).booleanValue());
            sslPropsMap.put("ssl3Prop", ((Boolean)handlerCtx.getInputValue("SSL3Prop")).booleanValue());
            sslPropsMap.put("ssl2Prop", ((Boolean)handlerCtx.getInputValue("SSL2Prop")).booleanValue());
            sslPropsMap.put("tlsProp", ((Boolean)handlerCtx.getInputValue("TLSProp")).booleanValue());
            sslPropsMap.put("selectedCommon", (String[])handlerCtx.getInputValue("SelectedCommon"));
            sslPropsMap.put("selectedEph", (String[])handlerCtx.getInputValue("SelectedEph"));
            sslPropsMap.put("selectedOther", (String[])handlerCtx.getInputValue("SelectedOther"));
            sslPropsMap.put("selectedEcc", (String[])handlerCtx.getInputValue("SelectedEcc"));
            handlerCtx.getFacesContext().getExternalContext().getSessionMap().put("sslProps", sslPropsMap);
        }
        
        /**
         *  <p> This handler returns the values for all the attributes in
         *      JMX Connector Tab Page of a Node Agent </p>
         *  <p> Input value: "NodeAgentName"      -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "JmxName"           -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "JmxProtocol"       -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "Address"           -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "Port"              -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "RealmName"         -- Type: <code>java.lang.String</code></p>
         *  <p> Output value: "Accept"            -- Type: <code>java.lang.Boolean</code></p>
         *  @param context The HandlerContext.
         */
        @Handler(id="getNodeAgentJmxSettings",
            input={
            @HandlerInput(name="NodeAgentName", type=String.class, required=true)},
            output={
            @HandlerOutput(name="JmxName",      type=String.class),
            @HandlerOutput(name="JmxProtocol",  type=String.class),
            @HandlerOutput(name="Address",      type=String.class),
            @HandlerOutput(name="Port",         type=String.class),
            @HandlerOutput(name="RealmName",    type=String.class),
            @HandlerOutput(name="Accept",       type=Boolean.class),
            @HandlerOutput(name="security",       type=Boolean.class),
            @HandlerOutput(name="clientHostname",       type=String.class)})
            
            public static void getNodeAgentJmxSettings(HandlerContext handlerCtx) {
            String ndAgentName = ((String)handlerCtx.getInputValue("NodeAgentName"));
            Map <String, NodeAgentConfig> nodeAgentMap = AMXUtil.getDomainConfig().getNodeAgentConfigMap();
            NodeAgentConfig agentConfig = nodeAgentMap.get(ndAgentName);
            JMXConnectorConfig connectorConfig = agentConfig.getJMXConnectorConfig();
            handlerCtx.setOutputValue("JmxName", agentConfig.getSystemJMXConnectorName());
            handlerCtx.setOutputValue("JmxProtocol", connectorConfig.getProtocol());
            handlerCtx.setOutputValue("Address", connectorConfig.getAddress());
            handlerCtx.setOutputValue("Port", connectorConfig.getPort());
            handlerCtx.setOutputValue("RealmName", connectorConfig.getAuthRealmName());
            handlerCtx.setOutputValue("Accept", connectorConfig.getAcceptAll());
            handlerCtx.setOutputValue("security", connectorConfig.getSecurityEnabled());
            String client="";
            if (connectorConfig.existsProperty("client-hostname"))
                client = connectorConfig.getPropertyValue("client-hostname");
            handlerCtx.setOutputValue("clientHostname", client);
        }
        
        /**
         *  <p> This handler returns the values for all the attributes in
         *      JMX Connector Tab Page of a Node Agent </p>
         *  <p> Input value: "NodeAgentName"     -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "JmxName"           -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "JmxProtocol"       -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Address"           -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Port"              -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "RealmName"         -- Type: <code>java.lang.String</code></p>
         *  <p> Input value: "Accept"            -- Type: <code>java.lang.Boolean</code></p>
         *  @param context The HandlerContext.
         */
        @Handler(id="saveNodeAgentJmxSettings",
            input={
            @HandlerInput(name="NodeAgentName", type=String.class, required=true),
            @HandlerInput(name="JmxName",       type=String.class, required=true),
            @HandlerInput(name="JmxProtocol",   type=String.class),
            @HandlerInput(name="Address",       type=String.class),
            @HandlerInput(name="Port",          type=String.class),
            @HandlerInput(name="RealmName",     type=String.class),
            @HandlerInput(name="clientHostname",        type=String.class),
            @HandlerInput(name="security",        type=Boolean.class),
            @HandlerInput(name="Accept",        type=Boolean.class) })
            
            public static void saveNodeAgentJmxSettings(HandlerContext handlerCtx) {
            try{
                String ndAgentName = ((String)handlerCtx.getInputValue("NodeAgentName"));
                Map <String, NodeAgentConfig> nodeAgentMap = AMXUtil.getDomainConfig().getNodeAgentConfigMap();
                NodeAgentConfig agentConfig = nodeAgentMap.get(ndAgentName);
                JMXConnectorConfig connectorConfig = agentConfig.getJMXConnectorConfig();
                String objName = "com.sun.appserv:type=jmx-connector,node-agent=" + ndAgentName + ",category=config";
                JMXUtil.setAttribute(objName, new Attribute("address", handlerCtx.getInputValue("Address")));
                JMXUtil.setAttribute(objName, new Attribute("port", handlerCtx.getInputValue("Port")));
                JMXUtil.setAttribute(objName, new Attribute("auth-realm-name", handlerCtx.getInputValue("RealmName")));
                connectorConfig.setProtocol((String)handlerCtx.getInputValue("JmxProtocol"));
                connectorConfig.setAcceptAll((Boolean)handlerCtx.getInputValue("Accept"));
                connectorConfig.setSecurityEnabled((Boolean)handlerCtx.getInputValue("security"));
                String clientHostname = (String) handlerCtx.getInputValue("clientHostname");
                AMXUtil.changeProperty(connectorConfig, "client-hostname", clientHostname);
            }catch(Exception ex){
                GuiUtil.handleException(handlerCtx, ex);
            }
        }   
        
        private static String[] getSelectedCiphersList(String selectedCiphers){
            Vector selItems = new Vector();
            if(selectedCiphers != null){
                String[] sel = selectedCiphers.split(","); //NOI18N
                for(int i=0; i<sel.length; i++){
                    String cName = sel[i];
                    if(cName.startsWith("+")){ //NOI18N
                        cName = cName.substring(1, cName.length());
                        selItems.add(cName);
                    }
                }    
            }
            return (String[])selItems.toArray(new String[selItems.size()]);
        }
        
        private static String processSelectedCiphers(String[] selectedCiphers, String ciphers){
            if(selectedCiphers != null){
                for (int i = 0; i < selectedCiphers.length; i++) {
                    if(! ciphers.equals("")){
                        ciphers += ",";
                    }
                    ciphers += "+" + selectedCiphers[i];
                }
            }
            return ciphers;
        }
        
        private static String processDeletedCiphers(String[] oldCiphers, String ciphers){
            if(oldCiphers != null){
                for (int i = 0; i < oldCiphers.length; i++) {
                    String cipVal = oldCiphers[i];
                    if(ciphers.indexOf(cipVal) != -1){
                        if(! ciphers.equals("")){
                            ciphers += ",";
                        }
                        ciphers += "-" + cipVal;
                    }
                }
            }
            return ciphers;
        }
        
        private static Vector getCiphersVector(String[] allCiphers){
            Vector ciphers = new Vector();
            for(int i=0; i<allCiphers.length; i++){
                ciphers.add(allCiphers[i]);
            }
            return ciphers;
        }
        
        private static String[] getCommonCiphers(Vector ciphers){
            Vector commonCiphers = filterCiphers(ciphers, COMMON_CIPHERS);
            String[] ciphersList = (String[])commonCiphers.toArray(new String[commonCiphers.size()]);;
            return ciphersList;
        }
        
        private static String[] getEccCiphers(Vector ciphers){
            Vector eccCiphers = breakUpCiphers(new Vector(), ciphers, "ECDH"); //NOI18N
            eccCiphers = breakUpCiphers(eccCiphers, ciphers, "ECDHE"); //NOI18N
            String[] ciphersList = (String[])eccCiphers.toArray(new String[eccCiphers.size()]);
            return ciphersList;
        }    
        
        private static String[] getEphemeralCiphers(Vector ciphers){
            Vector ephmCiphers = breakUpCiphers(new Vector(), ciphers, "DHE_RSA"); //NOI18N
            ephmCiphers = breakUpCiphers(ephmCiphers, ciphers, "DHE_DSS"); //NOI18N
            String[] ciphersList = (String[])ephmCiphers.toArray(new String[ephmCiphers.size()]);
            return ciphersList;
        }
        
        private static String[] getOtherCiphers(Vector ciphers){
            Vector bitCiphers = filterCiphers(ciphers, BIT_CIPHERS);
            String[] ciphersList = (String[])bitCiphers.toArray(new String[bitCiphers.size()]);
            return ciphersList;
        }
        
        private static Vector filterCiphers(Vector ciphers, String[] filterList){
            Vector listCiphers = new Vector();
            for(int i=0; i<ciphers.size(); i++){
                String cipherName = ciphers.get(i).toString();
                if (Arrays.asList(filterList).contains(cipherName)){
                    listCiphers.add(ciphers.get(i));
                }
            }
            return listCiphers;
        }
        
        private static Vector breakUpCiphers(Vector cipherSubset, Vector allCiphers, String type){
            for(int i=0; i<allCiphers.size(); i++){
                String cipherName = allCiphers.get(i).toString();
                if(cipherName.indexOf(type) != -1) {
                    if(! Arrays.asList(BIT_CIPHERS).contains(cipherName)){
                        cipherSubset.add(cipherName);
                    }
                }
            }
            return cipherSubset;
        }
        
        private static String getJMXConnectorServURL(String jmxConnName, String configName){
            String objName = "com.sun.appserv:type=jmx-connector,name=" + jmxConnName +",config=" + configName + ",category=config"; //NOI18N
            String jmxServiceUrl = (String)JMXUtil.getAttribute(objName, "JMXServiceURL"); 
            return jmxServiceUrl;
        }
        
    /**
     *	<p> Returns the value of JMX attribute</p>
     * 
     *  <p> Input value: "ObjectName" -- Type: <code>java.lang.String</code></p>
     *  <p> Input value: "AttributeName" -- Type: <code>java.lang.String</code></p>
     *  <p> Output value: "AttributeValue" -- Type: <code>java.util.String</code></p>
     *          
     *	@param	context	The HandlerContext.
     */
    @Handler(id="getJMXAttribute",
        input={
            @HandlerInput(name="ObjectName", type=String.class, required=true),
            @HandlerInput(name="AttributeName", type=String.class, required=true)},
        output={
            @HandlerOutput(name="AttributeValue", type=Object.class)})
    public void getJMXAttribute(HandlerContext handlerCtx) {
        String objectName = (String)handlerCtx.getInputValue("ObjectName");
        String attributeName = (String)handlerCtx.getInputValue("AttributeName");
        Object attributeValue = JMXUtil.getAttribute(objectName, attributeName);
        handlerCtx.setOutputValue("AttributeValue", attributeValue);
    }
}

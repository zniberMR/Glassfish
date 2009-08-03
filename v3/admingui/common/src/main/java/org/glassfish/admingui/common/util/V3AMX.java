/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.admingui.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;
import javax.management.Attribute;
import javax.management.AttributeList;

import javax.servlet.ServletContext;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.glassfish.admin.amx.base.DomainRoot;
import org.glassfish.admin.amx.base.Realms;
import org.glassfish.admin.amx.base.Runtime;
import org.glassfish.admin.amx.base.ConnectorRuntimeAPIProvider;
import org.glassfish.admin.amx.config.AMXConfigProxy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.api.amx.AMXBooter;
import org.glassfish.admin.amx.core.proxy.ProxyFactory;
import org.glassfish.admin.amx.intf.config.AMXConfigHelper;
import org.glassfish.admin.amx.intf.config.Application;
import org.glassfish.admin.amx.intf.config.ApplicationRef;
import org.glassfish.admin.amx.intf.config.Applications;
import org.glassfish.admin.amx.intf.config.Config;
import org.glassfish.admin.amx.intf.config.ConfigTools;
import org.glassfish.admin.amx.intf.config.Configs;
import org.glassfish.admin.amx.intf.config.Domain;
import org.glassfish.admin.amx.intf.config.PropertiesAccess;
import org.glassfish.admin.amx.intf.config.Property;

import org.glassfish.admin.amx.intf.config.Resources;
import org.glassfish.admin.amx.intf.config.Server;
import org.jvnet.hk2.component.Habitat;

/**
 *
 * @author anilam
 */
public class V3AMX {
    private static V3AMX v3amx  ;
    private final DomainRoot domainRoot;
    private final Domain domain;
    private final ProxyFactory proxyFactory;
    private final MBeanServerConnection mbeanServer;

    private V3AMX(DomainRoot dd, MBeanServerConnection msc) {
        proxyFactory = ProxyFactory.getInstance(msc);
        domainRoot = dd;
        domain =  domainRoot.getDomain().as(Domain.class);
        mbeanServer = msc;
    }

     /**
     *	<p> Use this method to get the singleton instance of this object.</p>
     *
     *	<p> On the first invokation of this method, it will obtain the official
     *	    MBeanServer from the <code>Habitat</code>.  This will cause the
     *	    MBeanServer to initialize when this is called, if it hasn't already
     *	    been initialized.</p>
     */
    public synchronized static V3AMX getInstance() {
	if (v3amx == null) {
	    // Get the ServletContext
	    ServletContext servletCtx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();

        // Get the Habitat from the ServletContext
        Habitat habitat = (Habitat) servletCtx.getAttribute(
            org.glassfish.admingui.common.plugin.ConsoleClassLoader.HABITAT_ATTRIBUTE);

	    // Get the MBeanServer via the Habitat, we want the "official" one
	    MBeanServer mbs = (MBeanServer) habitat.getComponent(MBeanServer.class);
            if (mbs == null){
                 mbs = java.lang.management.ManagementFactory.getPlatformMBeanServer();
            }
            if (mbs == null){
                System.out.println("!!!!!!!!!!!!!!  Cannot get to MBeanServer");
            }
            AMXBooter.bootAMX(mbs);
            DomainRoot domainRoot = ProxyFactory.getInstance(mbs).getDomainRootProxy();
            v3amx = new V3AMX( domainRoot, mbs);
	}
        return v3amx;
    }


    public Domain getDomain(){
        return domain;
    }

    public DomainRoot getDomainRoot(){
        return domainRoot;
    }

    public Configs getConfigs(){
        return domain.getConfigs();
    }

    public ProxyFactory getProxyFactory(){
        return proxyFactory;
    }

    public AMXProxy getProxy( ObjectName objName){
        return proxyFactory.getProxy(objName);
    }

    public MBeanServerConnection getMbeanServerConnection(){
        return mbeanServer;
    }

    public Realms getRealmsMgr(){
        return domainRoot.getExt().getRealms();
    }
    
    public Runtime getRuntime(){
        return domainRoot.getExt().getRuntime();
    }
    
    public ConnectorRuntimeAPIProvider getConnectorRuntime(){
        return domainRoot.getExt().getConnectorRuntimeAPIProvider();
    }    

    public Applications getApplications(){
        return domain.getApplications();
    }

    public Application getApplication(String appName){
        return getApplications().childrenMap(Application.class).get(appName);
    }

    public ApplicationRef getApplicationRef(String server, String appName){
        return getDomain().getServers().getServer().get(server).getApplicationRef().get(appName);
    }

    public Server getServer(String server) {
        return domain.getServers().getServer().get(server);
    }


    public Config getConfig(String configName){
        return domain.getConfigs().getConfig().get(configName);
    }

    public Resources getResources(){
        return domain.getResources();
    }

    public AMXProxy getAdminListener(){
        return getConfig("server-config").getNetworkConfig().child("network-listeners").childrenMap("network-listener").get("admin-listener");
    }


    public  boolean isEE(){
        return false;
    }


    public  boolean supportCluster(){
        return false;
    }


    public static Config getServerConfig(String configName){
        if ((configName == null) || configName.equals("")) {
            configName = "server-config";
        }
        return V3AMX.getInstance().getConfigs().getConfig().get(configName);
    }

    public static void setAttribute(String objectName, Attribute attributeName) {
	try {
	    setAttribute(new ObjectName(objectName), attributeName);
	}catch (javax.management.MalformedObjectNameException ex){
            GuiUtil.getLogger().severe("MalformedObjectNameException: " + objectName);
            throw new RuntimeException(ex);
        }
    }

    public static void setAttribute(ObjectName objectName, Attribute attributeName) {
	try {
	    V3AMX.getInstance().getMbeanServerConnection().setAttribute(objectName, attributeName);
	} catch (Exception  ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void setAttributes(Object name, Map<String, Object> attrMap){
        try{
            ObjectName objectName = null;
            if (name instanceof String){
                objectName = new ObjectName((String) name);
            }else
                objectName = (ObjectName) name;
            AttributeList attrList = new AttributeList();
            removeElement(attrMap);
            for(String key : attrMap.keySet()){
                if (skipAttr.contains(key))
                    continue;
                Object val = attrMap.get(key);
                if ((val != null) && (!(val instanceof String)) && (!(val instanceof Boolean))){
                    //skip any element.
                    continue;
                }
                Attribute attr = new Attribute(key, (val==null)?  (String)val  : val.toString());
                attrList.add(attr);
            }
            setAttributes(objectName, attrList);
        }catch(Exception ex){
            throw new RuntimeException (ex);
        }
    }
    
    public static void setAttributes(ObjectName objectName, AttributeList attrList) {
	try {
	    V3AMX.getInstance().getMbeanServerConnection().setAttributes(objectName, attrList);
	} catch (Exception  ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static Map getAttrsMap(Object objectNameStr){
        
        ObjectName amxObjectName = null;
        try{
            if (objectNameStr instanceof ObjectName){
                amxObjectName = (ObjectName) objectNameStr;
            }else{
                amxObjectName = new ObjectName((String)objectNameStr);
            }
            AMXProxy  amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(amxObjectName);
            AMXConfigHelper helper = new AMXConfigHelper((AMXConfigProxy) amx);
            final Map<String,Object> attrs = helper.simpleAttributesMap();
            return attrs;
        }catch(Exception ex){
            ex.printStackTrace();
            return new HashMap();
        }
    }

    public static Object getAttribute(Object objectNameStr, String key){
        ObjectName amxObjectName = null;
        try{
            if (objectNameStr instanceof ObjectName){
                amxObjectName = (ObjectName) objectNameStr;
            }else{
                amxObjectName = new ObjectName((String)objectNameStr);
            }
            AMXProxy  amx = (AMXProxy) V3AMX.getInstance().getProxyFactory().getProxy(amxObjectName);
            return amx.attributesMap().get(key);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

        /**
     *  Change the property of a config mbean
     *  If the property Value is null or empty, this property will be removed.
     *  If the property  already exist,  the property value will be updated if the value is different.
     *  If the property doesn't exist, a new property will be created.
     *
     * @param config    mbean whose Property element to be changed
     * @param propName  name of the property
     * @param propValue value of this property
     *
     */
    public static void setPropertyValue(AMXConfigProxy config, String propName, String propValue) {
        Map<String, Property> pMap = ((PropertiesAccess)config).getProperty();
        if (pMap.containsKey(propName)) {
            if (GuiUtil.isEmpty(propValue)) {
                config.removeChild("property", propName);
            } else {
                //don't change the value if it is equal.
                Property cp = ((PropertiesAccess)config).getProperty().get(propName);
                if (!cp.getValue().equals(propValue)) {
                    cp.setValue(propValue);
                }
            }
        } else {
            if (!GuiUtil.isEmpty(propValue)) {
                Map attrs =new HashMap();
                attrs.put(PROPERTY_NAME, propName);
                attrs.put(PROPERTY_VALUE, propValue);
                config.createChild("property", attrs);
            }
        }
    }

    public static void setProperties(String objectNameStr, List<Map<String,String>> propertyList, boolean sysProp){
        try{
            ObjectName objectName = new ObjectName(objectNameStr);
            final ConfigTools configTools = V3AMX.getInstance().getDomainRoot().getExt().child(ConfigTools.class);
            if (propertyList.size()==0){
                if (sysProp){
                    configTools.clearSystemProperties(objectName);
                }else{
                    configTools.clearProperties(objectName);
                }
            }else{
                List newList = verifyPropertyList(propertyList);
                if (sysProp){
                    configTools.setSystemProperties(objectName, newList, true);
                }else{
                    configTools.setProperties(objectName, newList, true);
                }
            }
        }catch(Exception ex){
            throw new RuntimeException (ex);
        }
    }

    static public List verifyPropertyList(List<Map<String, String>> propertyList){

        List newList = new ArrayList();
        Set propertyNames = new HashSet();
        for(Map<String, String> oneRow : propertyList){
            Map newRow = new HashMap();
            final String  name = oneRow.get(PROPERTY_NAME);
            if (GuiUtil.isEmpty(name)){
                continue;
            }
            if (propertyNames.contains(name)){
                throw new RuntimeException(GuiUtil.getMessage("msg.duplicatePropTableKey", new Object[]{name}));
                //GuiUtil.handleError(handlerCtx, GuiUtil.getMessage("msg.duplicatePropTableKey", new Object[]{name}));
            }else{
                propertyNames.add(name);
            }

            String value = oneRow.get(PROPERTY_VALUE);
            if (GuiUtil.isEmpty(value)){
                value = "";
            }
            newRow.put(PROPERTY_NAME,name);
            newRow.put(PROPERTY_VALUE,value);
            String desc = (String) oneRow.get(PROPERTY_DESC);
            if (! GuiUtil.isEmpty(desc)){
                newRow.put( PROPERTY_DESC,  desc);
            }
            newList.add(newRow);
        }
        return newList;
    }

    /*
     * update the properties of a config.
     */
    public static void updateProperties(AMXConfigProxy config, Map<String, String> newProps, List ignore) {

        Map<String, Property> oldProps = ((PropertiesAccess)config).getProperty();
        if (ignore == null) {
            ignore = new ArrayList();
        }
        //Remove any property that is no longer in the new list
        Iterator iter = oldProps.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            if (ignore.contains(key)) {
                continue;
            }
            if (!newProps.containsKey(key)) {
                config.removeChild("property", (String)key);
            }
        }

        //update the value if the value is different or create a new property if it doesn't exist before
        Map<String, Property> pMap = ((PropertiesAccess)config).getProperty();
        for (String propName : newProps.keySet()) {
            String propValue = newProps.get(propName);
            if (pMap.containsKey(propName)) {
                pMap.get(propName).setValue(propValue);
            } else {
                Map attrs =new HashMap();
                attrs.put(PROPERTY_NAME, propName);
                attrs.put(PROPERTY_VALUE, propValue);
                config.createChild("property", attrs);
            }
        }
    }


    public static List getChildrenMapForTableList(AMXProxy amx, String childType, List skipList){
        boolean hasSkip = true;
        if (skipList == null ){
            hasSkip = false;
        }
        List result = new ArrayList();
        if (amx != null) {
            Map<String, AMXProxy> children = amx.childrenMap(childType);
            for(AMXProxy oneChild : children.values()){
                try{
                    AMXConfigHelper helper = new AMXConfigHelper((AMXConfigProxy) oneChild);
                    final Map<String,Object> attrs = helper.simpleAttributesMap();
                    HashMap oneRow = new HashMap();
                    if ( hasSkip && skipList.contains(oneChild.getName())){
                        continue;
                    }
                    oneRow.put("selected", false);
                    for(String attrName : attrs.keySet()){
                        oneRow.put(attrName, getA(attrs, attrName));
                    }
                    result.add(oneRow);
                }catch(Exception ex){
                    GuiUtil.getLogger().info("getChildrenMapForTableList():  ");
                    GuiUtil.getLogger().info("Proxy = " + amx.objectName().toString() + "; childType="+childType + ", skipList="+skipList);
                    ex.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * returns the Properties of a config, skipping those specified in the list thats passed in.
     * This is mostly for edit where we want to treat particular properties differently, and don't
     * show that in the Properties table.
     */
    public static List getNonSkipPropertiesMap(AMXConfigProxy amx, List skipList) {
        boolean noSkip = false;
        if (skipList == null || skipList.size()==0){
            noSkip = true;
        }
        Map<String, AMXProxy> children = amx.childrenMap("property");
        List result = new ArrayList();
        for(AMXProxy oneChild : children.values()){
            HashMap oneRow = new HashMap();
            oneRow.put("selected", false);
            String name = oneChild.getName();
            if ( noSkip || skipList.contains(name)){
                continue;
            }
            oneRow.put(PROPERTY_NAME, name);
            oneRow.put(PROPERTY_VALUE, oneChild.attributesMap().get(PROPERTY_VALUE));
            final Object desc = oneChild.attributesMap().get(PROPERTY_DESC);
            oneRow.put(PROPERTY_DESC, (desc == null) ? "" : desc);
        }
        return result;
    }

    
    public static String getPropValue( AMXProxy amx, String key){
        Map<String, AMXProxy> props = amx.childrenMap("property");
        if (props.containsKey(key)){
            return (String) props.get(key).attributesMap().get(PROPERTY_VALUE);
        }else{
            return null;
        }
    }

    public static String getPropValue(Map<String, Property> propMap, String key){
        if (propMap.containsKey(key)){
            return (String) propMap.get(key).attributesMap().get(PROPERTY_VALUE);
        }else{
            return null;
        }
    }

    /*  converts a Property Map to a Map where the name is preceded by PropertiesAccess.PROPERTY_PREFIX.   // FIXME no longer works
     *  This conversion is required when this Map is used as the optional parameter when creating a config.
     *  refer to the java doc of PropertiesAccess in AMX javadoc
     */
    public static Map convertToPropertiesOptionMap(Map<String, String> props, Map<String, String> convertedMap) {
        if (convertedMap == null) {
            convertedMap = new HashMap();
        }
        if (props == null) {
            return convertedMap;
        }
        Set<String> keySet = props.keySet();
        for (String key : keySet) {
            if (!GuiUtil.isEmpty((String) props.get(key))) {
                convertedMap.put( "property." + key, (String) props.get(key));  // FIXME no longer works
            }
        }
        return convertedMap;
    }

    /* A utility method to remove Element before calling create or set attribute of a proxy */
    static public void removeElement(Map<String, Object> attrs){
        if (attrs==null || attrs.size() <=0 )
            return;
        Set<Map.Entry <String, Object>> attrSet = attrs.entrySet();
        Iterator<Map.Entry<String, Object>> iter = attrSet.iterator();
        while (iter.hasNext()){
             Map.Entry<String, Object> oneEntry = iter.next();
             Object val = oneEntry.getValue();
             if ( val instanceof ObjectName || val instanceof ObjectName[]){
                 iter.remove();
            }
        }
    }


    static public void removeSpecifiedAttr(Map<String, Object> attrs,  List removeList){
        if (removeList==null || removeList.size() <=0 )
            return;
        Set<Map.Entry <String, Object>> attrSet = attrs.entrySet();
        Iterator<Map.Entry<String, Object>> iter = attrSet.iterator();
        while (iter.hasNext()){
             Map.Entry<String, Object> oneEntry = iter.next();
             if (removeList.contains(oneEntry.getKey())){
                 iter.remove();
            }
        }
    }

    
     public static AMXProxy objectNameToProxy(String objectNameStr){
         try {
            AMXProxy amx = V3AMX.getInstance().getProxyFactory().getProxy(new ObjectName(objectNameStr));
            return amx;
         }catch(Exception ex){
             System.out.println("Cannot find object: " + objectNameStr);
             return null;
         }
     }

    private static String getA(Map<String, Object> attrs,  String key){
        Object val = attrs.get(key);
        return (val == null) ? "" : val.toString();
    }

    final private static List skipAttr = new ArrayList();
    static{
        skipAttr.add("Parent");
        skipAttr.add("Name");
        skipAttr.add("Children");
    }
    private static final String PROPERTY_NAME = "Name";
    private static final String PROPERTY_VALUE = "Value";
    private static final String PROPERTY_DESC = "Description";


}

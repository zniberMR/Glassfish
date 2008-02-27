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

package com.sun.enterprise.glassfish.bootstrap.launcher;

import com.sun.enterprise.module.bootstrap.ArgumentManager;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author bnevins
 */
public class GFLauncherInfo 
{
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    public static void main(String[] args)
    {
        try
        {
            LocalStringsImpl lsi = new LocalStringsImpl(GFLauncherInfo.class);
            System.out.println("FOO= " + lsi.get("foo"));
            System.out.println("FOO2= " + lsi.get("foo2", "xxxxx", "yyyy"));
            GFLauncherInfo gfli = new GFLauncherInfo();
            gfli.finalSetup();
        }
        catch (GFLauncherException ex)
        {
            Logger.getLogger(GFLauncherInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP
    // TEMP TEMP TEMP TEMP TEMP

    
    
    
    
    public void addArgs(String... args)
    {
        for(String s : args)
            argsRaw.add(s);
    }

    private void setup() throws GFLauncherException
    {
        setupFromArgs();
        finalSetup();
    }
    
    private void setupFromArgs()
    {
        argsMap = ArgumentManager.argsToMap(argsRaw);
        
        File f = null;
        String s = null;
        Boolean b = null;
        
        // pick out file props
        // annoying -- cli uses "domaindir" to represent the parent of the 
        // domain root dir.  I'm sticking with the same syntax for now...
        if((f = getFile("domainDir")) != null)
            domainParentDir = f;
        
        if((f = getFile("instanceDir")) != null)
            instanceDir = f;
        
        // Now do the same thing with known Strings
        if((s = getString("domain")) != null)
            domainName = s;
        
        if((s = getString("instanceName")) != null)
            instanceName = s;

        // finally, do the booleans
        if((b = getBoolean("debug")) != null)
            debug = b;

        if((b = getBoolean("verbose")) != null)
            verbose = b;

        if((b = getBoolean("embedded")) != null)
            embedded = b;
    }
    
    private void finalSetup() throws GFLauncherException
    {
        installDir = GFLauncherUtils.getInstallDir();
        
        // temp temp temp temp
        // temp temp temp temp
        // temp temp temp temp
        // temp temp temp temp
        if(installDir != null)
            throw new GFLauncherException("noInstallDir", installDir);
        // temp temp temp temp
        // temp temp temp temp
        // temp temp temp temp
        // temp temp temp temp

        if(!GFLauncherUtils.safeIsDirectory(installDir))
            throw new GFLauncherException("noInstallDir", installDir);
       
        
        

        
    }
    
    private Boolean getBoolean(String key) 
    {
        // 3 return values -- true, false, null
        if(argsMap.containsKey(key))
        {
            String s = argsMap.get(key);
            //argsMap.remove(key);
            return Boolean.valueOf(s);
        }
        return null;
    }

    private File getFile(String key)
    {
        if(argsMap.containsKey(key))
        {
            File f = new File(argsMap.get(key));
            //argsMap.remove(key);
            return f;
        }
        return null;
    }
    
    private String getString(String key) 
    {
        if(argsMap.containsKey(key))
        {
            String s = argsMap.get(key);
            //argsMap.remove(key);
            return s;
        }
        return null;
    }

    // yes these variables are all accessible from any class in the package...
    boolean                     verbose         = false;
    boolean                     debug           = false;
    boolean                     embedded        = false;
    File                        installDir;
    File                        domainParentDir;
    File                        domainRootDir;
    File                        instanceDir;
    File                        configDir;
    String                      domainName;
    String                      instanceName;

    private boolean             valid           = false;
    private Map<String,String>  argsMap;
    private ArrayList<String>   argsRaw = new ArrayList<String>();
    
}


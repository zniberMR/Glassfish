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

package com.sun.enterprise.web.connector.grizzly.ssl;

import com.sun.enterprise.web.connector.grizzly.FileCache;
import com.sun.enterprise.web.connector.grizzly.FileCache.FileCacheEntry;
import com.sun.enterprise.web.connector.grizzly.FileCacheFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * File cache extension used to support SSL.
 *
 * @author Jeanfrancois Arcand
 */
public class SSLFileCacheFactory extends FileCacheFactory{
    
    public SSLFileCacheFactory() {
    }
    
    /**
     * Configure the factory.
     */
    public static FileCacheFactory newInstance(int currentPort){
        FileCacheFactory fileCacheFactory= new SSLFileCacheFactory();

        fileCacheFactory.port = currentPort;
        cache.put(currentPort, fileCacheFactory);

        ConcurrentLinkedQueue<FileCacheEntry> cacheManager =
            new  ConcurrentLinkedQueue<FileCacheEntry>();
        fileCacheFactory.setCacheManager(cacheManager);  

        return fileCacheFactory;
    }    
     
    
    /**
     * Return an instance of this Factory.
     */
    public static FileCacheFactory getFactory(int currentPort){
                
        FileCacheFactory fileCacheFactory = cache.get(currentPort);
        if ( fileCacheFactory == null ){
            fileCacheFactory = newInstance(currentPort); 
        }

        return fileCacheFactory;
    }
    
    
    /**
     * Return an instance of a <code>FileCache</code>
     */
    public FileCache getFileCache(){
        if (fileCache == null){
            fileCache = new FileCache(){
                
                protected void sendCache(SocketChannel socketChannel,  
                        FileCacheEntry entry,
                    boolean keepAlive) throws IOException{

                    SSLOutputWriter.flushChannel(socketChannel, 
                            entry.headerBuffer.slice());
                    ByteBuffer keepAliveBuf = keepAlive ? connectionKaBB.slice():
                    connectionCloseBB.slice();
                    SSLOutputWriter.flushChannel(socketChannel, keepAliveBuf);        
                    SSLOutputWriter.flushChannel(socketChannel, entry.bb.slice());
                }  
            };
            fileCache.setIsEnabled(isEnabled);
            fileCache.setLargeFileCacheEnabled(isLargeFileCacheEnabled);
            fileCache.setSecondsMaxAge(secondsMaxAge);
            fileCache.setMaxCacheEntries(maxCacheEntries);
            fileCache.setMinEntrySize(minEntrySize);
            fileCache.setMaxEntrySize(maxEntrySize);
            fileCache.setMaxLargeCacheSize(maxLargeFileCacheSize);
            fileCache.setMaxSmallCacheSize(maxSmallFileCacheSize);         
            fileCache.setCacheManager(cacheManager);
            fileCache.setIsMonitoringEnabled(isMonitoringEnabled);
        }
        
        return fileCache;
    }     
 
}

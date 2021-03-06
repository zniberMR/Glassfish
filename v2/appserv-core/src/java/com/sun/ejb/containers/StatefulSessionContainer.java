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
package com.sun.ejb.containers;

import java.util.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

import javax.ejb.*;
import javax.transaction.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.rmi.PortableRemoteObject;
import javax.ejb.ConcurrentAccessException;

import javax.persistence.PersistenceContextType;

import com.sun.ejb.*;
import com.sun.enterprise.util.Utility;
import com.sun.enterprise.deployment.*;

import static com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;
import com.sun.enterprise.*;
import com.sun.enterprise.util.EntityManagerFactoryWrapper;

import com.sun.enterprise.iiop.SFSBClientVersionManager;
import com.sun.enterprise.appverification.factory.AppVerification;

import com.sun.enterprise.admin.monitor.*;
import com.sun.enterprise.util.io.FileUtils;

import java.util.logging.*;
import com.sun.logging.*;

import com.sun.ejb.containers.util.cache.*;

import com.sun.appserv.util.cache.CacheListener;

import com.sun.enterprise.util.threadpool.Servicable;

import com.sun.ejb.base.io.IOUtils;

import com.sun.ejb.base.stats.StatefulSessionStoreMonitor;

import com.sun.ejb.containers.util.ContainerWorkPool;

import com.sun.ejb.containers.util.cache.LruSessionCache;

import com.sun.ejb.spi.container.ContainerService;
import com.sun.ejb.spi.container.SFSBContainerCallback;
import com.sun.ejb.spi.container.StatefulEJBContext;

import com.sun.ejb.spi.sfsb.initialization.SFSBContainerInitialization;

import com.sun.ejb.spi.sfsb.store.SFSBBeanState;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManagerException;

import com.sun.ejb.spi.sfsb.util.CheckpointPolicy;
import com.sun.ejb.spi.sfsb.util.SFSBUUIDUtil;

import com.sun.ejb.spi.stats.StatefulSessionBeanStatsProvider;

import com.sun.enterprise.deployment.runtime.IASEjbExtraDescriptors;
import com.sun.enterprise.deployment.runtime.CheckpointAtEndOfMethodDescriptor;
import com.sun.enterprise.admin.monitor.callflow.ComponentType;

import com.sun.ejb.spi.io.IndirectlySerializable;
import com.sun.ejb.spi.io.SerializableObjectFactory;

/** This class provides container functionality specific to stateful
 *  SessionBeans.
 *  At deployment time, one instance of the StatefulSessionContainer is created
 *  for each stateful SessionBean type (i.e. deployment descriptor) in a JAR.
 * <P>
 * The 5 states of a Stateful EJB (an EJB can be in only 1 state at a time):
 * 1. PASSIVE : has been passivated
 * 2. READY : ready for invocations, no transaction in progress
 * 3. INVOKING : processing an invocation
 * 4. INCOMPLETE_TX : ready for invocations, transaction in progress
 * 5. DESTROYED : does not exist
 *
 * @author Mahesh Kannan
 */

public final class StatefulSessionContainer
    extends BaseContainer
    implements CacheListener, SFSBContainerCallback,
	StatefulSessionBeanStatsProvider, SFSBContainerInitialization
{
    
    private static final Logger _logger =
        LogDomains.getLogger(LogDomains.EJB_LOGGER);
    
    static final int PASSIVE=1, READY=2, INVOKING=3;
    static final int INCOMPLETE_TX=4, DESTROYED=5;
    
    // We do not want too many ORB task for passivation
    public static final int  MIN_PASSIVATION_BATCH_COUNT = 8;
    
    private long		instanceCount = 1;
    
    protected ArrayList		passivationCandidates = new ArrayList();
    private Object		asyncTaskSemaphore = new Object();
    
    
    private int			asyncTaskCount = 0;
    private int			asyncCummTaskCount = 0;
    
    private int			passivationBatchCount
				    = MIN_PASSIVATION_BATCH_COUNT;
    
    private int			containerTrimCount=0;
        
    private LruSessionCache	sessionBeanCache;
    private SFSBStoreManager	sfsbStoreManager;
    private SFSBUUIDUtil	uuidGenerator;
    private ArrayList		scheduledTimerTasks = new ArrayList();

    protected int		statMethodReadyCount = 0;

    private Level		TRACE_LEVEL = Level.FINE;

    private String		ejbName;

    private CheckpointPolicy	checkpointPolicy;
    private int			removalGracePeriodInSeconds;

    private StatefulSessionStoreMonitor	sfsbStoreMonitor;

    private final String	traceInfoPrefix;

    /*
     * Cache for keeping ref count for shared extended entity manager.
     * The key in this map is the physical entity manager
     */
    private static Map<EntityManager, EEMRefInfo> extendedEMReferenceCountMap
        = new HashMap<EntityManager, EEMRefInfo>();

    private static Map<EEMRefInfoKey, EntityManager> eemKey2EEMMap
        = new HashMap<EEMRefInfoKey, EntityManager>();
    
    /**
     * This constructor is called from the JarManager when a Jar is deployed.
     * @exception Exception on error
     */
    public StatefulSessionContainer(EjbDescriptor desc,
            ClassLoader loader)
        throws Exception
    {
        super(desc, loader);
        super.createCallFlowAgent(ComponentType.SFSB);
	this.ejbName = desc.getName();
	this.traceInfoPrefix = "sfsb-" + ejbName + ": ";
    }
    
    protected void initializeHome()
        throws Exception
    {
        super.initializeHome();

	loadCheckpointInfo();

	registerMonitorableComponents();

    }

    protected void loadCheckpointInfo() {
	try {
	    if (checkpointPolicy.isHAEnabled()) {
		Iterator iter = invocationInfoMap.values().iterator();
		while (iter.hasNext()) {
		    InvocationInfo info = (InvocationInfo) iter.next();
		    info.checkpointEnabled = false;
		    MethodDescriptor md = new MethodDescriptor(
			    info.method, info.methodIntf);
		    IASEjbExtraDescriptors extraDesc =
			ejbDescriptor.getIASEjbExtraDescriptors();
		    if (extraDesc != null) {
			CheckpointAtEndOfMethodDescriptor cpDesc =
			    extraDesc.getCheckpointAtEndOfMethodDescriptor();
			if (cpDesc != null) {
			    info.checkpointEnabled =
				cpDesc.isCheckpointEnabledFor(md);
			}
		    }

		    if (info.checkpointEnabled) {
			if (_logger.isLoggable(Level.FINE)) {
			    _logger.log(Level.FINE, "[SFSBContainer] "
				+ info.method + " MARKED for "
				+ "end-of-method-checkpoint");
			}
		    }
		}
	    }
	} catch (Exception ex) {
	    _logger.log(Level.WARNING, "[SFSBContainer] Exception while "
		    + " loading checkpoint info", ex);
	}
    }

    protected void registerMonitorableComponents() {
	registryMediator.registerProvider(this);
	registryMediator.registerProvider(sessionBeanCache);
        super.registerMonitorableComponents();
	super.populateMethodMonitorMap();
	sfsbStoreMonitor = registryMediator.registerProvider(
	    sfsbStoreManager.getMonitorableSFSBStoreManager(),
	    checkpointPolicy.isHAEnabled());
	sessionBeanCache.setStatefulSessionStoreMonitor(sfsbStoreMonitor);
	_logger.log(Level.FINE, "[SFSBContainer] registered monitorable");
    }

    public String getMonitorAttributeValues() {
        StringBuffer sbuf = new StringBuffer();
	//sbuf.append(storeHelper.getMonitorAttributeValues());
        sbuf.append(" { asyncTaskCount=").append(asyncTaskCount)
        .append("; asyncCummTaskCount=").append(asyncCummTaskCount)
        .append("; passivationBatchCount=").append(passivationBatchCount)
        .append("; passivationQSz=").append(passivationCandidates.size())
        .append("; trimEventCount=").append(containerTrimCount)
        .append(" }");
        return sbuf.toString();
    }
    
    public void appendStats(StringBuffer sbuf) {
	sbuf.append("\nStatefulContainer: ")
	    .append("CreateCount=").append(statCreateCount).append("; ")
	    .append("RemoveCount=").append(statRemoveCount).append("; ")
	    .append("Size=")
	    .append(sessionBeanCache.getNumBeansInCache()).append("; ")
	    .append("ReadyCount=")
	    .append(statMethodReadyCount).append("; ");
	sbuf.append("]");
    }

    private static final String convertCtxStateToString(
	SessionContextImpl sc)
    {
	switch (sc.getState()) {
	    case PASSIVE: return "PASSIVE";
	    case READY: return "READY";
	    case INVOKING: return "INVOKING";
	    case INCOMPLETE_TX: return "INCOMPLETE_TX";
	    case DESTROYED: return "DESTROYED";
	}
	return "UNKNOWN-STATE";
    }

    boolean isIdentical(EJBObjectImpl ejbo, EJBObject other)
        throws RemoteException
    {
        if ( other == ejbo.getStub() )
            return true;
        else {
            try {
                // other may be a stub for a remote object.
                if ( protocolMgr.isIdentical(ejbo.getStub(), other) )
                    return true;
                else
                    return false;
            } catch ( Exception ex ) {
                _logger.log(Level.FINE,
                "Exception while getting stub for ejb",ex);
                throw new RemoteException("Error during isIdentical.", ex);
            }
        }
    }
    
    /**
     * This is called from the generated "HelloEJBHomeImpl" create method
     * via EJBHomeImpl.createEJBObject.
     * Note: for stateful beans, the HelloEJBHomeImpl.create calls
     * ejbCreate on the new bean after createEJBObject() returns.
     * Return the EJBObject for the bean.
     */
    EJBObjectImpl createEJBObjectImpl()
        throws CreateException, RemoteException
    {
        try {
            SessionContextImpl context = createBeanInstance();
            EJBObjectImpl ejbObjImpl = createEJBObjectImpl(context);
            afterInstanceCreation(context);
            return ejbObjImpl;
        }
        catch (Exception ex) {
            
            _logger.log(Level.WARNING, "ejb.create_ejbobject_exception", 
                        ejbDescriptor.getName());
            _logger.log(Level.WARNING, "create object exception", ex);
                        
            if ( ex instanceof EJBException )
                throw (EJBException)ex;
            else {
                CreateException ce =
                    new CreateException("ERROR creating stateful SessionBean");
                ce.initCause(ex);
                throw ce;
            }
        }
    }

    EJBObjectImpl createRemoteBusinessObjectImpl()
        throws CreateException, RemoteException
    {
        try {
            SessionContextImpl context = createBeanInstance();
            EJBObjectImpl ejbBusinessObjImpl = 
                createRemoteBusinessObjectImpl(context);
            afterInstanceCreation(context);
            return ejbBusinessObjImpl;
        }
        catch (Exception ex) {
            
            _logger.log(Level.WARNING, "ejb.create_ejbobject_exception", 
                        ejbDescriptor.getName());
            _logger.log(Level.WARNING, "create object exception", ex);
                        
            if ( ex instanceof EJBException )
                throw (EJBException)ex;
            else {
                CreateException ce =
                    new CreateException("ERROR creating stateful SessionBean");
                ce.initCause(ex);
                throw ce;
            }
        }
    }
    
    
    /**
     * This is called from the generated "HelloEJBLocalHomeImpl" create method
     * via EJBLocalHomeImpl.createEJBObject.
     * Note: for stateful beans, the HelloEJBLocalHomeImpl.create calls
     * ejbCreate on the new bean after createEJBLocalObjectImpl() returns.
     * Return the EJBLocalObject for the bean.
     */
    EJBLocalObjectImpl createEJBLocalObjectImpl()
        throws CreateException
    {
        try {
            SessionContextImpl context = createBeanInstance();
            
            EJBLocalObjectImpl localObjImpl = 
                createEJBLocalObjectImpl(context);

            afterInstanceCreation(context);
            
            return localObjImpl;
        }
        catch (Exception ex) {

            _logger.log(Level.WARNING, "ejb.create_ejblocalobject_exception", 
                        ejbDescriptor.getName());
            _logger.log(Level.WARNING, "create ejblocal object exception", ex);

            if ( ex instanceof EJBException )
                throw (EJBException)ex;
            else {
                CreateException ce =
                new CreateException("ERROR creating stateful SessionBean");
                ce.initCause(ex);
                throw ce;
            }
        }
    }

    /**
     * Internal creation event for Local Business view of SFSB
     */
    EJBLocalObjectImpl createEJBLocalBusinessObjectImpl()
        throws CreateException
    {
        try {
            SessionContextImpl context = createBeanInstance();
            
            EJBLocalObjectImpl localBusinessObjImpl = 
                createEJBLocalBusinessObjectImpl(context);

            afterInstanceCreation(context);
            
            return localBusinessObjImpl;
        }
        catch (Exception ex) {

            _logger.log(Level.WARNING, "ejb.create_ejblocalobject_exception", 
                        ejbDescriptor.getName());
            _logger.log(Level.WARNING, "create ejblocal object exception", ex);

            if ( ex instanceof EJBException )
                throw (EJBException)ex;
            else {
                CreateException ce =
                new CreateException("ERROR creating stateful SessionBean");
                ce.initCause(ex);
                throw ce;
            }
        }
    }
    
    /**
     * Create a new Session Bean and set Session Context.
     */
    private SessionContextImpl createBeanInstance()
        throws Exception
    {
        Invocation i=null;
        try {
            // create new (stateful) EJB
            Object ejb = ejbClass.newInstance();
            
            // create SessionContext and set it in the EJB
            SessionContextImpl context = new SessionContextImpl(ejb, this);
            context.setInterceptorInstances(
                    interceptorManager.createInterceptorInstances());
            
            Object sessionKey = uuidGenerator.createSessionKey();
            createExtendedEMs(context, sessionKey);
            
            // Need to do preInvoke because setSessionContext can access JNDI
            i = new Invocation(ejb, this);
            i.context = context;
            invocationManager.preInvoke(i);
            
            // setSessionContext will be called without a Tx as required
            // by the spec, because the EJBHome.create would have been called
            // after the container suspended any client Tx.
            // setSessionContext is also called before createEJBObject because
            // the bean is not allowed to do EJBContext.getEJBObject here
            if( ejb instanceof SessionBean ) {
                ((SessionBean)ejb).setSessionContext(context);
            }

            // Perform injection right after where setSessionContext
            // would be called.  This is important since injection methods
            // have the same "operations allowed" permissions as
            // setSessionContext.
            injectionManager.injectInstance(ejb, ejbDescriptor, false);
            for (Object interceptorInstance : context.getInterceptorInstances()) {
                injectionManager.injectInstance(interceptorInstance,
                        ejbDescriptor, false);
            }
            // Set the timestamp before inserting into bean store, else
            // Recycler might go crazy and remove this bean!
            context.touch();
            
            // Add the EJB into the session store
            // and get the instanceKey for this EJB instance.
            // XXX The store operation could be avoided for local-only beans.

            sessionBeanCache.put(sessionKey, context);
            context.setInstanceKey(sessionKey);



	    if (_logger.isLoggable(TRACE_LEVEL)) {
		_logger.log(TRACE_LEVEL, "[SFSBContainer] Created "
		    + "session: " + sessionKey);
	    }
            
            return context;
        } catch (Exception ex) {
            throw ex;
        } catch (Throwable t) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(t);
            throw ejbEx;
        } finally {
            if ( i != null ) {
                invocationManager.postInvoke(i);
            }
        }
    }

    private void createExtendedEMs(SessionContextImpl ctx, Object sessionKey) {
        Set<EntityManagerReferenceDescriptor> emRefs
            = ejbDescriptor.getEntityManagerReferenceDescriptors();
        Iterator<EntityManagerReferenceDescriptor> iter = emRefs.iterator();
        Set<EEMRefInfo> eemRefInfos = new HashSet<EEMRefInfo>();
        while (iter.hasNext()) {
            EntityManagerReferenceDescriptor refDesc = iter.next();
            if (refDesc.getPersistenceContextType() == 
                PersistenceContextType.EXTENDED) {
                String unitName = refDesc.getUnitName();
                EntityManagerFactory emf = 
                    EntityManagerFactoryWrapper.lookupEntityManagerFactory(
                        ComponentInvocation.EJB_INVOCATION,
                        unitName, ejbDescriptor);
                if (emf != null) {
                    EntityManager em = findExtendedEMFromInvList(emf);

                    if (em == null) {
                        try {
                            Map properties = refDesc.getProperties();
                            em = emf.createEntityManager(properties);
                            if (em == null) {
                                throw new EJBException
                                    ("EM is null. Couldn't create EntityManager for"
                                        + " refName: " + refDesc.getName()
                                        + "; unitname: " + unitName);
                            }
                        } catch (Throwable th) {
                        	EJBException ejbEx = new EJBException
                                ("Couldn't create EntityManager for"
                                    + " refName: " + refDesc.getName()
                                    + "; unitname: " + unitName);
                        	ejbEx.initCause(th);
                        	throw ejbEx;
                        }
                    }
                
                    String emRefName = refDesc.getName();
                    long containerID = this.getContainerId();
                    EEMRefInfo refInfo = null;
                    synchronized (extendedEMReferenceCountMap) {
                        refInfo = extendedEMReferenceCountMap.get(em);
                        if (refInfo != null) {
                            refInfo.refCount++;
                        } else  {
                            refInfo = new EEMRefInfo(emRefName, refDesc.getUnitName(), containerID,
                                    sessionKey, em, emf);
                            refInfo.refCount = 1;
                            extendedEMReferenceCountMap.put(em, refInfo);
                            eemKey2EEMMap.put(refInfo.getKey(), refInfo.getEntityManager());
                        }
                    }
                    ctx.addExtendedEntityManagerMapping(emf, refInfo);
                    eemRefInfos.add(refInfo);
                } else {
                    throw new EJBException("EMF is null. Couldn't get extended EntityManager for"
                            + " refName: " + refDesc.getName()
                            + "; unitname: " + unitName);
                }
            }        
	}

	if (eemRefInfos.size() > 0) {
	    ctx.setEEMRefInfos(eemRefInfos);
	}
    }
    
    private EntityManager findExtendedEMFromInvList(EntityManagerFactory emf) {
        EntityManager em = null;

        ComponentInvocation compInv = (ComponentInvocation) 
            invocationManager.getCurrentInvocation();
        if (compInv != null) {
            if (compInv.getInvocationType() == ComponentInvocation.EJB_INVOCATION) {
                if (compInv.context instanceof SessionContextImpl) {
                    SessionContextImpl ctxImpl = (SessionContextImpl) compInv.context;
                    if (ctxImpl.container instanceof StatefulSessionContainer) {
                        em = ctxImpl.getExtendedEntityManager(emf);
                    }
                }
            }
        }

        return em;
    }
   
    private void afterInstanceCreation(SessionContextImpl context) 
        throws Exception {

        context.setState(READY);

        Invocation i = null;
        try {
            // Need to do preInvoke because setSessionContext can access JNDI
            i = new Invocation(context.getEJB(), this);
            i.context = context;
            invocationManager.preInvoke(i);
            // PostConstruct must be called after state set to something
            // other than NOT_INITIALIZED
            interceptorManager.intercept(CallbackType.POST_CONSTRUCT, context);
        } catch(Throwable t) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(t);
            throw ejbEx;
        } finally {
            if ( i != null ) {
                invocationManager.postInvoke(i);
            }
        }

        statCreateCount++;
        incrementMethodReadyStat();


    }
    
        
    // called from createEJBObject and activateEJB and createEJBLocalObjectImpl
    private EJBLocalObjectImpl createEJBLocalObjectImpl
        (SessionContextImpl context) throws Exception
    {
        if ( context.getEJBLocalObjectImpl() != null )
            return context.getEJBLocalObjectImpl();
        
        // create EJBLocalObject
        EJBLocalObjectImpl localObjImpl = instantiateEJBLocalObjectImpl();
        
        // introduce context and EJBLocalObject to each other
        context.setEJBLocalObjectImpl(localObjImpl);
        localObjImpl.setContext(context);
        localObjImpl.setKey(context.getInstanceKey());
        
        if ( hasLocalBusinessView ) {
            createEJBLocalBusinessObjectImpl(context);
        }

        if ( hasRemoteHomeView ) {
            createEJBObjectImpl(context);  // enable remote invocations too
        }

        if( hasRemoteBusinessView ) {
            createRemoteBusinessObjectImpl(context);
        }
        
        return localObjImpl;
    }

    private EJBLocalObjectImpl createEJBLocalBusinessObjectImpl
        (SessionContextImpl context) throws Exception
    {
        if ( context.getEJBLocalBusinessObjectImpl() != null )
            return context.getEJBLocalBusinessObjectImpl();
        
        EJBLocalObjectImpl localBusinessObjImpl = 
            instantiateEJBLocalBusinessObjectImpl();
        
        context.setEJBLocalBusinessObjectImpl(localBusinessObjImpl);
        localBusinessObjImpl.setContext(context);
        localBusinessObjImpl.setKey(context.getInstanceKey());
        
        if( hasLocalHomeView ) {
            createEJBLocalObjectImpl(context);
        }

        if ( hasRemoteHomeView ) {
            createEJBObjectImpl(context);  // enable remote invocations too
        }

        if( hasRemoteBusinessView ) {
            createRemoteBusinessObjectImpl(context);
        }
        
        return localBusinessObjImpl;
    }

    // called from createEJBObject and activateEJB and createEJBLocalObjectImpl
    private EJBObjectImpl createEJBObjectImpl(SessionContextImpl context)
        throws Exception
    {
        if ( context.getEJBObjectImpl() != null )
            return context.getEJBObjectImpl();
        
        // create EJBObject
        EJBObjectImpl ejbObjImpl = instantiateEJBObjectImpl();
        
        // introduce context and EJBObject to each other
        context.setEJBObjectImpl(ejbObjImpl);
        ejbObjImpl.setContext(context);
        Object sessionKey = context.getInstanceKey();
        ejbObjImpl.setKey(sessionKey);
        
        // connect the EJBObject to the ProtocolManager
        // (creates the client-side stub too)
        byte[] sessionOID = uuidGenerator.keyToByteArray(sessionKey);
        EJBObject ejbStub = (EJBObject) 
            remoteHomeRefFactory.createRemoteReference(sessionOID);

        context.setEJBStub(ejbStub);
        ejbObjImpl.setStub(ejbStub);

        if (hasRemoteBusinessView ) {
            createRemoteBusinessObjectImpl(context);
        }
        
        if ( isLocal ) {
            if( hasLocalHomeView ) {
                // enable local home invocations too
                createEJBLocalObjectImpl(context); 
            }
            if( hasLocalBusinessView ) {
                // enable local business invocations too
                createEJBLocalBusinessObjectImpl(context);
            }
        }

        return ejbObjImpl;
    }

    private EJBObjectImpl createRemoteBusinessObjectImpl
        (SessionContextImpl context) throws Exception
    {
        if ( context.getEJBRemoteBusinessObjectImpl() != null )
            return context.getEJBRemoteBusinessObjectImpl();
        
        // create EJBObject
        EJBObjectImpl ejbBusinessObjImpl = 
            instantiateRemoteBusinessObjectImpl();
        
        context.setEJBRemoteBusinessObjectImpl(ejbBusinessObjImpl);
        ejbBusinessObjImpl.setContext(context);
        Object sessionKey = context.getInstanceKey();
        ejbBusinessObjImpl.setKey(sessionKey);
        
        // connect the Remote object to the ProtocolManager
        // (creates the client-side stub too)
        byte[] sessionOID = uuidGenerator.keyToByteArray(sessionKey);
        for(RemoteBusinessIntfInfo next : remoteBusinessIntfInfo.values()) {

            java.rmi.Remote stub = next.referenceFactory.
                createRemoteReference(sessionOID); 
                
            ejbBusinessObjImpl.setStub(next.generatedRemoteIntf.getName(),
                                       stub);
        }

        if (hasRemoteHomeView ) {
            createEJBObjectImpl(context);
        }
        
        if ( isLocal ) {
            if( hasLocalHomeView ) {
                // enable local home invocations too
                createEJBLocalObjectImpl(context); 
            }
            if( hasLocalBusinessView ) {
                // enable local business invocations too
                createEJBLocalBusinessObjectImpl(context);
            }
        }

        return ejbBusinessObjImpl;
    }
    
    
    
    // Called from EJBObjectImpl.remove, EJBLocalObjectImpl.remove,
    // EJBHomeImpl.remove(Handle).
    void removeBean(EJBLocalRemoteObject ejbo, Method removeMethod,
            boolean local)
        throws RemoveException, EJBException
    {
        Invocation i = new Invocation();
        i.ejbObject = ejbo;
        i.isLocal = local;
        i.method = removeMethod;

        // Method must be a remove method defined on one of :
        // javax.ejb.EJBHome, javax.ejb.EJBObject, javax.ejb.EJBLocalHome,
        // javax.ejb.EJBLocalObject
        Class declaringClass = removeMethod.getDeclaringClass();
        i.isHome = ( (declaringClass == javax.ejb.EJBHome.class) ||
                     (declaringClass == javax.ejb.EJBLocalHome.class) );

        try {
            preInvoke(i);
            removeBean(i);
        } catch(Exception e) {
            _logger.log(Level.FINE,"ejb.preinvoke_exception",e);
            i.exception = e;
        } finally {
            if ( AppVerification.doInstrument() ) {
                AppVerification.getInstrumentLogger().doInstrumentForEjb
                (ejbDescriptor, removeMethod, i.exception);
            }
            
            postInvoke(i);
        }
        
        if(i.exception != null) {
            if(i.exception instanceof RemoveException) {
                throw (RemoveException)i.exception;
            }
            else if(i.exception instanceof RuntimeException) {
                throw (RuntimeException)i.exception;
            }
            else if(i.exception instanceof Exception) {
                throw new EJBException((Exception)i.exception);
            }
            else {
                EJBException ejbEx = new EJBException();
                ejbEx.initCause(i.exception);
                throw ejbEx;
            }
        }
    }
    
    
    /**
     * Called from EJBObjectImpl.remove().
     * Note: preInvoke and postInvoke are called for remove().
     */
    private void removeBean(Invocation inv)
        throws RemoveException
    {
        // At this point the EJB's state is always INVOKING
        // because EJBObjectImpl.remove() called preInvoke().
        
        try {
	    statRemoveCount++;
            SessionContextImpl sc = (SessionContextImpl)inv.context;
            Transaction tc = sc.getTransaction();
            
            if ( tc != null && tc.getStatus() !=
            Status.STATUS_NO_TRANSACTION ) {
                // EJB2.0 section 7.6.4: remove must always be called without
                // a transaction.
                throw new RemoveException
                ("Cannot remove EJB: transaction in progress");
            }
            
            // call ejbRemove on the EJB
	    if (_logger.isLoggable(TRACE_LEVEL)) {
		_logger.log(TRACE_LEVEL, "[SFSBContainer] Removing "
		    + "session: " + sc.getInstanceKey());
	    }
            sc.setInEjbRemove(true);
            try {
                interceptorManager.intercept(
                    CallbackType.PRE_DESTROY, sc);
            } catch(Throwable t) {
                _logger.log(Level.FINE, 
                            "exception thrown from SFSB PRE_DESTROY", t);
            } finally {
                sc.setInEjbRemove(false);
            }
            forceDestroyBean(sc);
        }
        catch ( EJBException ex ) {
            _logger.log(Level.FINE,"EJBException in removing bean",ex);
            throw ex;
        }
        catch ( RemoveException ex ) {
            _logger.log(Level.FINE,"Remove exception while removing bean",ex);
            throw ex;
        }
        catch (Exception ex) {
            _logger.log(Level.FINE,"Some exception while removing bean",ex);
            throw new EJBException(ex);
        }
    }
    
    
    /**
     * Force destroy the EJB and rollback any Tx it was associated with
     * Called from removeBean, timeoutBean and BaseContainer.postInvokeTx.
     * Note: EJB2.0 section 18.3.1 says that discarding an EJB
     * means that no methods other than finalize() should be invoked on it.
     */
    void forceDestroyBean(EJBContextImpl ctx) {
        SessionContextImpl sc = (SessionContextImpl)ctx;
        
        synchronized ( sc ) {
            if ( sc.getState() == DESTROYED )
                return;
            
            // mark context as destroyed so no more invocations happen on it
            sc.setState(DESTROYED);
            if (_logger.isLoggable(TRACE_LEVEL)) {
		_logger.log(TRACE_LEVEL, "[SFSBContainer] (Force)Destroying "
		    + "session: " + sc.getInstanceKey());
	    }

            
            Transaction prevTx = sc.getTransaction();
            try {
                if ( prevTx != null && prevTx.getStatus() !=
                Status.STATUS_NO_TRANSACTION ) {
                    prevTx.setRollbackOnly();
                }
            } catch (SystemException ex) {
                throw new EJBException(ex);
            } catch (IllegalStateException ex) {
                throw new EJBException(ex);
            }
            
            // remove the bean from the session store
            Object sessionKey = sc.getInstanceKey();
            sessionBeanCache.remove(sessionKey, sc.existsInStore());

            if ( isRemote ) {

                if( hasRemoteHomeView ) {
                    // disconnect the EJBObject from the context and vice versa
                    EJBObjectImpl ejbObjImpl = sc.getEJBObjectImpl();
                    ejbObjImpl.clearContext();
                    ejbObjImpl.setRemoved(true);
                    sc.setEJBObjectImpl(null);
                    
                    // disconnect the EJBObject from the ProtocolManager
                    // so that no remote invocations can reach the EJBObject
                    remoteHomeRefFactory.destroyReference
                        (ejbObjImpl.getStub(), ejbObjImpl.getEJBObject());
                }

                if( hasRemoteBusinessView ) {

                    EJBObjectImpl ejbBusinessObjImpl = 
                        sc.getEJBRemoteBusinessObjectImpl();
                    ejbBusinessObjImpl.clearContext();
                    ejbBusinessObjImpl.setRemoved(true);
                    sc.setEJBRemoteBusinessObjectImpl(null);
                    
                    for(RemoteBusinessIntfInfo next : 
                            remoteBusinessIntfInfo.values()) {
                        // disconnect from the ProtocolManager
                        // so that no remote invocations can get through
                        next.referenceFactory.destroyReference
                            (ejbBusinessObjImpl.getStub
                                (next.generatedRemoteIntf.getName()), 
                             ejbBusinessObjImpl.getEJBObject
                                (next.generatedRemoteIntf.getName()));
                    }
                }
            }

            if ( isLocal ) {
                if( hasLocalHomeView ) {
                    // disconnect the EJBLocalObject from the context 
                    // and vice versa
                    EJBLocalObjectImpl localObjImpl =
                        (EJBLocalObjectImpl)sc.getEJBLocalObjectImpl();
                    localObjImpl.clearContext();
                    localObjImpl.setRemoved(true);
                    sc.setEJBLocalObjectImpl(null);
                }
                if( hasLocalBusinessView ) {
                    // disconnect the EJBLocalObject from the context 
                    // and vice versa
                    EJBLocalObjectImpl localBusinessObjImpl =
                        (EJBLocalObjectImpl)sc.getEJBLocalBusinessObjectImpl();
                    localBusinessObjImpl.clearContext();
                    localBusinessObjImpl.setRemoved(true);
                    sc.setEJBLocalBusinessObjectImpl(null);
                }
            }
            
            destroyExtendedEMsForContext(sc);
            
            // tell the TM to release resources held by the bean
            transactionManager.ejbDestroyed(sc);
            
            if (checkpointPolicy.isHAEnabled()) {
                //Remove any SFSBClientVersions
                SFSBClientVersionManager.removeClientVersion(getContainerId(), sessionKey);
            }
        }
    }

    private void destroyExtendedEMsForContext(SessionContextImpl sc) {
        for (EntityManager em : sc.getExtendedEntityManagers()) {
            synchronized (extendedEMReferenceCountMap) {
                if (extendedEMReferenceCountMap.containsKey(em)) {
                    EEMRefInfo refInfo = extendedEMReferenceCountMap.get(em);
                    if (refInfo.refCount > 1) {
                        refInfo.refCount--;
                        _logger.log(Level.FINE,
                                "Decremented RefCount ExtendedEM em: " + em);
                    } else {
                        _logger.log(Level.FINE, "DESTROYED ExtendedEM em: "
                                + em);
                        refInfo = extendedEMReferenceCountMap.remove(em);
                        eemKey2EEMMap.remove(refInfo.getKey());
                        try {
                            em.close();
                        } catch (Throwable th) {
                            _logger.log(Level.FINE,
                                    "Exception during em.close()", th);
                        }
                    }
                }
            }
        }
    }
    
    public boolean userTransactionMethodsAllowed(ComponentInvocation inv) {
        boolean utMethodsAllowed = false;
        
        if( isBeanManagedTran ) {
            if( inv instanceof Invocation ) {
                Invocation i = (Invocation) inv;
                SessionContextImpl sc = (SessionContextImpl) i.context;
                // This will prevent setSessionContext access to
                // UserTransaction methods.
                utMethodsAllowed = (sc.getInstanceKey() != null);
            } else {
                utMethodsAllowed = true;
            }
        }
        
        return utMethodsAllowed;
    }
    
    
    public void removeTimedoutBean(EJBContextImpl ctx) {
        // check if there is an invocation in progress for
        // this instance.
        synchronized (ctx) {
            if (ctx.getState() != INVOKING) {
                try {
                    // call ejbRemove on the bean
                    Object ejb = ctx.getEJB();
                    ctx.setInEjbRemove(true);
                    interceptorManager.intercept(
                        CallbackType.PRE_DESTROY, ctx);
                } catch ( Throwable t ) {
                    _logger.log(Level.FINE, "ejbRemove exception", t);
                } finally {
                    ctx.setInEjbRemove(false);
                }
	    
		if (_logger.isLoggable(TRACE_LEVEL)) {
		    SessionContextImpl sc = (SessionContextImpl) ctx;
		    _logger.log(TRACE_LEVEL, "[SFSBContainer] Removing TIMEDOUT "
			+ "session: " + sc.getInstanceKey());
		}
                
                forceDestroyBean(ctx);
            }
        }
    }
    
    /**
     * Called when a remote invocation arrives for an EJB.
     * @exception NoSuchObjectLocalException if the target object does not exist
     */
    private SessionContextImpl _getContextForInstance(byte[] instanceKey) {

        Object sessionKey = uuidGenerator.byteArrayToKey(instanceKey, 0, -1);
        
	if (_logger.isLoggable(TRACE_LEVEL)) {
	    _logger.log(TRACE_LEVEL, "[SFSBContainer] Got request for: "
		+ sessionKey);
	}
        while (true) {
            SessionContextImpl sc = (SessionContextImpl)
                sessionBeanCache.lookupEJB(sessionKey, this, null);

            if ( sc == null ) {
                // EJB2.0 section 7.6
                // Note: the NoSuchObjectLocalException gets converted to a
                // remote exception by the protocol manager.
                throw new NoSuchObjectLocalException(
                    "Invalid Session Key ( " + sessionKey + ")");
            }
    
            synchronized (sc) {
                switch (sc.getState()) {
                    case  PASSIVE:      //Next cache.lookup() == different_ctx
                    case  DESTROYED:    //Next cache.lookup() == null
                        break;
                    default:

                        return sc;
                }
            }
        }
    }
    

    EJBObjectImpl getEJBObjectImpl(byte[] instanceKey) {
        SessionContextImpl sc = _getContextForInstance(instanceKey);
        return (sc != null) ? sc.getEJBObjectImpl() : null;
    }

    EJBObjectImpl getEJBRemoteBusinessObjectImpl(byte[] instanceKey) {
        SessionContextImpl sc = _getContextForInstance(instanceKey);
        return (sc != null) ? sc.getEJBRemoteBusinessObjectImpl() : null;
    }

    /**
     * Called from EJBLocalObjectImpl.getLocalObject() while deserializing
     * a local object reference.
     */
    EJBLocalObjectImpl getEJBLocalObjectImpl(Object sessionKey) {

        // Create an EJBLocalObject reference which
        // is *not* associated with a SessionContext.  That way, the
        // session bean context lookup will be done lazily whenever
        // the reference is actually accessed.  This avoids I/O in the
        // case that the reference points to a passivated session bean.
        // It's also consistent with the deserialization approach used
        // throughout the container.  e.g. a timer reference is deserialized
        // from its handle without checking it against the timer database.
      
        EJBLocalObjectImpl localObjImpl;

        try {
            localObjImpl = instantiateEJBLocalObjectImpl();

            localObjImpl.setKey(sessionKey);
                            
        } catch ( Exception ex ) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(ex);
            throw ejbEx;
        }
                
        return localObjImpl;  
    }
    
    EJBLocalObjectImpl getEJBLocalBusinessObjectImpl(Object sessionKey) {

        // Create an EJBLocalObject reference which
        // is *not* associated with a SessionContext.  That way, the
        // session bean context lookup will be done lazily whenever
        // the reference is actually accessed.  This avoids I/O in the
        // case that the reference points to a passivated session bean.
        // It's also consistent with the deserialization approach used
        // throughout the container.  e.g. a timer reference is deserialized
        // from its handle without checking it against the timer database.
      
        EJBLocalObjectImpl localBusinessObjImpl;

        try {
            localBusinessObjImpl = instantiateEJBLocalBusinessObjectImpl();

            localBusinessObjImpl.setKey(sessionKey);
                            
        } catch ( Exception ex ) {
            EJBException ejbEx = new EJBException();
            ejbEx.initCause(ex);
            throw ejbEx;
        }
                
        return localBusinessObjImpl;  
    }
    
    /**
     * Check if the given EJBObject/LocalObject has been removed.
     * @exception NoSuchObjectLocalException if the object has been removed.
     */
    void checkExists(EJBLocalRemoteObject ejbObj) {
        if ( ejbObj.isRemoved() )
            throw new NoSuchObjectLocalException("Bean has been removed");
    }
    
    private final void logTraceInfo(Invocation inv, Object key, String message) {
	_logger.log(TRACE_LEVEL, traceInfoPrefix + message
	    + " for " + inv.method.getName() + "; key: " + key);
    }
    
    private final void logTraceInfo(SessionContextImpl sc, String message) {
	_logger.log(TRACE_LEVEL, traceInfoPrefix + message
	    + " for key: " + sc.getInstanceKey()
	    + "; " + System.identityHashCode(sc));
    }

    /**
     * Called from preInvoke which is called from the EJBObject
     * for local and remote invocations.
     */
    public ComponentContext _getContext(Invocation inv) {
        EJBLocalRemoteObject ejbo = inv.ejbObject;
        SessionContextImpl sc = ejbo.getContext();
        Object sessionKey = ejbo.getKey();
        
	if (_logger.isLoggable(TRACE_LEVEL)) {
	    logTraceInfo(inv, sessionKey, "Trying to get context");
	}
    
        if (sc == null) {
            // This is possible if the EJB was destroyed or passivated.
            // Try to activate it again.
            sc = (SessionContextImpl) sessionBeanCache.lookupEJB(
		    sessionKey, this, ejbo);
        }
        
        if ((sc == null) || (sc.getState()==DESTROYED)) {
	    if (_logger.isLoggable(TRACE_LEVEL)) {
		logTraceInfo(inv, sessionKey, "Context already destroyed");
	    }
	    // EJB2.0 section 7.6
            throw new NoSuchObjectLocalException("The EJB does not exist."
		    + " session-key: " + sessionKey);
        }
        
	SessionContextImpl context = null;
        synchronized (sc) {
	    SessionContextImpl newSC = sc;
            if ( sc.getState() == PASSIVE ) {
                // This is possible if the EJB was passivated after
                // the last lookupEJB. Try to activate it again.
		newSC = (SessionContextImpl) sessionBeanCache.lookupEJB(
		    sessionKey, this, ejbo);
		if (newSC == null) {
		    if (_logger.isLoggable(TRACE_LEVEL)) {
			logTraceInfo(inv, sessionKey, "Context does not exist");
		    }
		    // EJB2.0 section 7.6
		    throw new NoSuchObjectLocalException(
			    "The EJB does not exist. key: " + sessionKey);
		}
            }
            // acquire the lock again, in case a new sc was returned.
            synchronized (newSC) { //newSC could be same as sc
                // Check & set the state of the EJB
                if (newSC.getState()==DESTROYED ) {
		    if (_logger.isLoggable(TRACE_LEVEL)) {
			logTraceInfo(inv, sessionKey, "Got destroyed context");
		    }
                    throw new NoSuchObjectLocalException
                    ("The EJB does not exist. session-key: " + sessionKey);
                } else if (newSC.getState() == INVOKING ) {
                    handleConcurrentInvocation(inv, sessionKey);
                }
		if (newSC.getState() == READY) {
			decrementMethodReadyStat();
                }
        if (checkpointPolicy.isHAEnabled()) {
            doVersionCheck(inv, sessionKey, sc);
        }
		newSC.setState(INVOKING);
		context = newSC;
            }
        }

        // touch the context here so timestamp is set & timeout is prevented
        context.touch();

	if ((context.existsInStore()) && (removalGracePeriodInSeconds > 0)) {
	    long now = System.currentTimeMillis();
	    long threshold = now - (removalGracePeriodInSeconds * 1000);
	    if (context.getLastPersistedAt() <= threshold) {
		try {
		    sfsbStoreManager.updateLastAccessTime(sessionKey, now);
		    context.setLastPersistedAt(System.currentTimeMillis());
		} catch (SFSBStoreManagerException sfsbEx) {
		    _logger.log(Level.WARNING,
			"Couldn't update timestamp for: " + sessionKey
			+ "; Exception: " + sfsbEx);
		    _logger.log(Level.FINE,
			"Couldn't update timestamp for: " + sessionKey, sfsbEx);
		}
	    }
	}

	if (_logger.isLoggable(TRACE_LEVEL)) {
	    logTraceInfo(inv, context, "Got Context!!");
	}

        return context;
    }

    public boolean isHAEnabled() {
        return checkpointPolicy.isHAEnabled();
    }
    
    private void doVersionCheck(Invocation inv, Object sessionKey,
            SessionContextImpl sc) {
        EJBLocalRemoteObject ejbLRO = inv.ejbObject;
        long clientVersion = SFSBVersionManager.NO_VERSION;
        if (! inv.isLocal) {
            clientVersion = SFSBVersionManager.getRequestClientVersion();
            SFSBVersionManager.clearRequestClientVersion();
            SFSBVersionManager.clearResponseClientVersion();
        }
        
        if (ejbLRO != null) {
            if (clientVersion == SFSBVersionManager.NO_VERSION) {
                clientVersion = ejbLRO.getSfsbClientVersion();
            }

            long ctxVersion = sc.getVersion();
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, "doVersionCheck(): for: {" + ejbDescriptor.getName()
                    + "." + inv.method.getName() + " <=> " + sessionKey + "} clientVersion: "
                    + clientVersion + " == " + ctxVersion);
            }
            if (clientVersion > ctxVersion) {
                throw new NoSuchObjectLocalException(
                        "Found only a stale version " + " clientVersion: "
                                + clientVersion + " contextVersion: "
                                + ctxVersion);
            }            
        }
    }
    
    private void handleConcurrentInvocation(Invocation inv, Object sessionKey) {
        if (_logger.isLoggable(TRACE_LEVEL)) {
            logTraceInfo(inv, sessionKey, "Another invocation in progress");
        }

        String errMsg = "SessionBean is executing another request. "
            + "[session-key: " + sessionKey + "]";
        ConcurrentAccessException conEx = new ConcurrentAccessException(errMsg); 

        if (inv.isBusinessInterface) {
            throw conEx;
        } else {
            // there is an invocation in progress for this instance
            // throw an exception (EJB2.0 section 7.5.6).
            throw new EJBException(conEx);
        }
    }

    public void postInvokeTx(Invocation inv) throws Exception {
        
        // Intercept postInvokeTx call to perform any @Remove logic
        // before tx commits.  super.postInvokeTx() must *always*
        // be called. 

        // If this was an invocation of a remove-method
        if( inv.invocationInfo.removalInfo != null ) {
                
            InvocationInfo invInfo = inv.invocationInfo;
            EjbRemovalInfo removeInfo = invInfo.removalInfo;
                
            if( retainAfterRemoveMethod(inv, removeInfo) ) {
                // Do nothing
            } else {
                
                // If there is a tx, remove bean from ContainerSynch so it 
                // won't receive any SessionSynchronization callbacks.
                // We delay the PreDestroy callback and instance destruction
                // until releaseContext so that PreDestroy won't run within
                // the business method's tx.  
                
                SessionContextImpl sc = (SessionContextImpl)inv.context;
                Transaction tx = sc.getTransaction();

                if( tx != null ) {
                    ContainerSynchronization sync = 
                        containerFactory.getContainerSync(tx);
                    sync.removeBean(sc);
                }
                
            }
        }

        super.postInvokeTx(inv);

    }

    /**
     * Should only be called when a method is known to be a remove method.
     * @return true if the removal should be skipped, false otherwise.
     */
    private boolean retainAfterRemoveMethod(Invocation inv,
                                            EjbRemovalInfo rInfo) {

        boolean retain = 
            ( rInfo.getRetainIfException() &&
              (inv.exceptionFromBeanMethod != null) &&
              (isApplicationException(inv.exceptionFromBeanMethod)) );

        return retain;

    }

    /**
     * Called from preInvoke which is called from the EJBObject for local and
     * remote invocations.
     */
    public void releaseContext(Invocation inv) {
        SessionContextImpl sc = (SessionContextImpl) inv.context;

        // check if the bean was destroyed
        if (sc.getState() == DESTROYED)
            return;

        // we're sure that no concurrent thread can be using this
        // context, so no need to synchronize.
        Transaction tx = sc.getTransaction();
        try {

            // If this was an invocation of a remove-method
            if (inv.invocationInfo.removalInfo != null) {

                InvocationInfo invInfo = inv.invocationInfo;
                EjbRemovalInfo removeInfo = invInfo.removalInfo;

                if (retainAfterRemoveMethod(inv, removeInfo)) {
                    _logger.log(Level.INFO, "Skipping destruction of SFSB "
                            + invInfo.ejbName + " after @Remove method "
                            + invInfo.method + " due to (retainIfException"
                            + " == true) and exception " + inv.exception);
                } else {
                    try {
                        // PRE-DESTROY runs in an unspecified tx context, so
                        // we call it here in releaseContext, after
                        // postInvokeTx has completed.
                        interceptorManager.intercept(CallbackType.PRE_DESTROY, sc);
                    } catch (Throwable t) {
                        _logger.log(Level.FINE, "@Remove.preDestroy exception",
                                t);
                    }

                    forceDestroyBean(sc);
                }
            }

            if (tx == null || tx.getStatus() == Status.STATUS_NO_TRANSACTION) {
                // The Bean executed with no tx, or with a tx and
                // container.afterCompletion() was already called.
                if (sc.getState() != READY) {
                    if (sc.isAfterCompletionDelayed()) {
                        // ejb.afterCompletion was not called yet
                        // because of container.afterCompletion may have
                        // been called concurrently with this invocation.
                        if (_logger.isLoggable(TRACE_LEVEL)) {
                            logTraceInfo(inv, sc,
                                    "Calling delayed afterCompletion");
                        }
                        callEjbAfterCompletion(sc, sc.getCompletedTxStatus());
                    }

                    if (sc.getState() != DESTROYED) {
                        // callEjbAfterCompletion could make state as DESTROYED
                        sc.setState(READY);
                        handleEndOfMethodCheckpoint(sc, inv);
                    }
                }
                if ((sc.getState() != DESTROYED)
                        && (checkpointPolicy.isHAEnabled())) {
                    syncClientVersion(inv, sc);
                }
            } else {
                if ((sc.getState() != DESTROYED)
                        && (checkpointPolicy.isHAEnabled())) {
                    syncClientVersion(inv, sc);
                }
                sc.setState(INCOMPLETE_TX);
                if (_logger.isLoggable(TRACE_LEVEL)) {
                    logTraceInfo(inv, sc, "Marking state == INCOMPLETE_TX");
                }
            }
            
        } catch (SystemException ex) {
            throw new EJBException(ex);
        }
    }
    
    
    void afterBegin(EJBContextImpl context) {
        // TX_BEAN_MANAGED EJBs cannot implement SessionSynchronization
        if ( isBeanManagedTran )
            return;
        // Note: this is only called for business methods.
        // For SessionBeans non-business methods are never called with a Tx.
        Object ejb = context.getEJB();
        if ( ejb instanceof SessionSynchronization ) {
            SessionSynchronization sync = (SessionSynchronization)ejb;
            try {
                sync.afterBegin();
            } catch ( Exception ex ) {
                // Error during afterBegin, so discard bean: EJB2.0 18.3.3
                forceDestroyBean(context);
                throw new EJBException("Error during SessionSynchronization." +
                ".afterBegin(), EJB instance discarded",
                ex);
            }
        }

	//Register CMT Beans for end of Tx Checkpointing
	//Note:- We will never reach here for TX_BEAN_MANAGED
	if (checkpointPolicy.isHAEnabled()) {
	    ContainerSynchronization cSync = null;
	    try {
		cSync = containerFactory.
		    getContainerSync(context.getTransaction());
		cSync.registerForTxCheckpoint(
		    (SessionContextImpl) context);
	    } catch (javax.transaction.RollbackException rollEx) {
                _logger.log(Level.WARNING, "Cannot register bean for "
			+ "checkpointing", rollEx);
	    } catch (javax.transaction.SystemException sysEx) {
                _logger.log(Level.WARNING, "Cannot register bean for "
			+ "checkpointing", sysEx);
	    }
	}
    }
    
    
    void beforeCompletion(EJBContextImpl context) {
        // SessionSync calls on TX_BEAN_MANAGED SessionBeans
        // are not allowed
        if ( isBeanManagedTran )
            return;
        
        Object ejb = context.getEJB();
        if ( !(ejb instanceof SessionSynchronization) )
            return;
        
        // No need to check for a concurrent invocation
        // because beforeCompletion can only be called after
        // all business methods are completed.
        
        Invocation inv = new Invocation(ejb, this);
        inv.context = context;
        invocationManager.preInvoke(inv);
        try {
            transactionManager.enlistComponentResources();
            
            ((SessionSynchronization)ejb).beforeCompletion();
            
        } catch ( Exception ex ) {
            // Error during beforeCompletion, so discard bean: EJB2.0 18.3.3
            try {
                forceDestroyBean(context);
            } catch ( Exception e ) {
                _logger.log(Level.FINE, "error destroying bean", e);
            }
            throw new EJBException("Error during SessionSynchronization." +
            "beforeCompletion, EJB instance discarded",
            ex);
        } finally {
            invocationManager.postInvoke(inv);
        }
    }
    
    
    // Called from SyncImpl.afterCompletion
    // May be called asynchronously during tx timeout
    // or on the same thread as tx.commit
    void afterCompletion(EJBContextImpl context, int status) {
        if (context.getState() == DESTROYED) {
            return;
	}
        
        SessionContextImpl sc = (SessionContextImpl)context;
        Object ejb = sc.getEJB();
        boolean committed = (status == Status.STATUS_COMMITTED)
        || (status == Status.STATUS_NO_TRANSACTION);
        
        sc.setTransaction(null);
        
        // SessionSync calls on TX_BEAN_MANAGED SessionBeans
        // are not allowed.
        if ( !isBeanManagedTran && (ejb instanceof SessionSynchronization) ) {
            
            // Check for a concurrent invocation
            // because afterCompletion can be called asynchronously
            // during rollback because of transaction timeout
            if ((sc.getState() == INVOKING) && (!sc.isTxCompleting())) {
                // Cant invoke ejb.afterCompletion now because there is
                // already some invocation in progress on the ejb.
                sc.setAfterCompletionDelayed(true);
                sc.setCompletedTxStatus(committed);
		if (_logger.isLoggable(TRACE_LEVEL)) {
		    logTraceInfo(sc, "AfterCompletion delayed");
		}
		return;
            }
            
            callEjbAfterCompletion(sc, committed);
        }
        
	//callEjbAfterCompletion can set state as  DESTROYED
	if (sc.getState() != DESTROYED) {
	    if (checkpointPolicy.isHAEnabled()) {
		if (isBeanManagedTran) {
		    sc.setTxCheckpointDelayed(true);
		    if (_logger.isLoggable(TRACE_LEVEL)) {
			logTraceInfo(sc, "(BMT)Checkpoint delayed");
		    }
		}
	    } else {
		if (! isBeanManagedTran) {
		    if (_logger.isLoggable(TRACE_LEVEL)) {
			logTraceInfo(sc, "Released context");
		    }
		    sc.setState(READY);
		    incrementMethodReadyStat();
		}
	    } 
	}
    }

    SFSBBeanState getSFSBBeanState(SessionContextImpl sc) {
	//No need to synchronize
	SFSBBeanState sfsbBeanState = null;
	try {
            
            if ((containerState != CONTAINER_STARTED) && (containerState != CONTAINER_STOPPED)) {
		_logger.log(Level.FINE, "getSFSBBeanState() returning because "
			+ "containerState: " + containerState);
                return null;
            }

            if (sc.getState() == DESTROYED) {
                return null;
	    }
            
            Object ejb = sc.getEJB();
            
            ComponentInvocation ci = new ComponentInvocation(ejb, this, sc);
            invocationManager.preInvoke(ci);

            synchronized (sc) {
                try {
                    interceptorManager.intercept(
                            CallbackType.PRE_PASSIVATE, sc);
		    sc.setLastPersistedAt(System.currentTimeMillis());
                    long newCtxVersion = sc.incrementAndGetVersion();
		    byte[] serializedState = IOUtils.serializeObject(sc, true);
		    sfsbBeanState = sfsbStoreManager.createSFSBBeanState(
			sc.getInstanceKey(), System.currentTimeMillis(),
			!sc.existsInStore(), serializedState);
		    sfsbBeanState.setVersion(newCtxVersion);
                    interceptorManager.intercept(
                            CallbackType.POST_ACTIVATE, sc);
		    //Do not set sc.setExistsInStore() here
                } catch (java.io.NotSerializableException serEx) {
                    _logger.log(Level.WARNING, "Error  during checkpoint ("
                                + ejbDescriptor.getName() + ". Key: "
                                        + sc.getInstanceKey() + ") " + serEx);
                    _logger.log(Level.FINE, "sfsb checkpoint error. Key: "
                             + sc.getInstanceKey(), serEx);
                    try {
                        forceDestroyBean(sc);
                    } catch ( Exception e ) {
                        _logger.log(Level.FINE, "error destroying bean", e);
                    }
                } catch (Throwable ex) {
                    _logger.log(Level.WARNING, "ejb.sfsb_checkpoint_error",
                                new Object[] { ejbDescriptor.getName() });
                    _logger.log(Level.WARNING, "sfsb checkpoint error. key: "
                             + sc.getInstanceKey(), ex);
                    try {
                        forceDestroyBean(sc);
                    } catch ( Exception e ) {
                        _logger.log(Level.FINE, "error destroying bean", e);
                    }
                } finally {
                    invocationManager.postInvoke(ci);
                }
            } //synchronized
            
        } catch (Throwable th) {
	    _logger.log(Level.WARNING, "ejb.sfsb_checkpoint_error",
		new Object[] { ejbDescriptor.getName() });
	    _logger.log(Level.WARNING, "sfsb checkpoint error", th);
	}

	return sfsbBeanState;
    }

    void txCheckpointCompleted(SessionContextImpl sc) {
	if (sc.getState() != DESTROYED) {
            //We did persist this ctx in the store
	    sc.setExistsInStore(true);
	    sc.setState(READY);
	    incrementMethodReadyStat();
	}
    }

    private void callEjbAfterCompletion(SessionContextImpl context,
            boolean status)
    {
        Object ejb = context.getEJB();
        ComponentInvocation ci = new ComponentInvocation(ejb, this, context);
        invocationManager.preInvoke(ci);
        try {
            context.setInAfterCompletion(true);
            ((SessionSynchronization)ejb).afterCompletion(status);
            
            // reset flags
            context.setAfterCompletionDelayed(false);
            context.setTxCompleting(false);
        }
        catch (Exception ex) {
            // Error during afterCompletion, so discard bean: EJB2.0 18.3.3
            try {
                forceDestroyBean(context);
            } catch ( Exception e ) {
                _logger.log(Level.FINE, "error removing bean", e);
            }
            
            _logger.log(Level.INFO, "ejb.aftercompletion_exception",ex);
            
            // No use throwing an exception here, since the tx has already
            // completed, and afterCompletion may be called asynchronously
            // when there is no client to receive the exception.
        }
        finally {
            context.setInAfterCompletion(false);
            invocationManager.postInvoke(ci);
        }
    }
    
    public final boolean canPassivateEJB(ComponentContext context) {
        SessionContextImpl sc = (SessionContextImpl)context;
        return (sc.getState() == READY);
    }
    
    // called asynchronously from the Recycler
    public final boolean passivateEJB(ComponentContext context) {
        SessionContextImpl sc = (SessionContextImpl)context;
        
        boolean success = false;
        try {
            
            if ((containerState != CONTAINER_STARTED) && (containerState != CONTAINER_STOPPED)) {
		_logger.log(TRACE_LEVEL, "passivateEJB() returning because "
			+ "containerState: " + containerState);
                return false;
            }

            if ( sc.getState() == DESTROYED )
                return false;
            
	    if (_logger.isLoggable(TRACE_LEVEL)) {
		_logger.log(TRACE_LEVEL, traceInfoPrefix + "Passivating context "
		    + sc.getInstanceKey() + "; current-state = "
		    + convertCtxStateToString(sc));
	    }

            Object ejb = sc.getEJB();

	    long passStartTime = -1;
	    if (sfsbStoreMonitor.isMonitoringOn()) {
	        passStartTime = System.currentTimeMillis();
	    }
            
            ComponentInvocation ci = new ComponentInvocation(ejb, this, sc);
            invocationManager.preInvoke(ci);

            boolean failed = false;
            
            success = false;
            synchronized (sc) {
                try {
                    // dont passivate if there is a Tx/invocation in progress
                    // for this instance.
                    if (! sc.canBePassivated()) {
                        return false;
                    }

                    // passivate the EJB
                    sc.setState(PASSIVE);
                    decrementMethodReadyStat();
                    interceptorManager.intercept(
                            CallbackType.PRE_PASSIVATE, sc);
		    sc.setLastPersistedAt(System.currentTimeMillis());
                    boolean saved = false;
                    try {
                        saved = sessionBeanCache.passivateEJB(sc, sc
                            .getInstanceKey());
                    } catch (EMNotSerializableException emNotSerEx) {
                        _logger.log(Level.WARNING, "Extended EM not serializable. "
                                + "Exception: " + emNotSerEx);
                        _logger.log(Level.FINE, "Extended EM not serializable", emNotSerEx);
                        saved = false;
                    }
                    if (! saved) {
                        interceptorManager.intercept(
                                CallbackType.POST_ACTIVATE, sc);
                        sc.setState(READY);
                        incrementMethodReadyStat();
                        return false;
                    }
		    sfsbStoreMonitor.incrementPassivationCount(true);
		    transactionManager.ejbDestroyed( sc );

                    decrementRefCountsForEEMs(sc);
                    
                    if ( isRemote ) {

                        if( hasRemoteHomeView ) {
                            // disconnect the EJBObject from the EJB
                            EJBObjectImpl ejbObjImpl = sc.getEJBObjectImpl();
                            ejbObjImpl.clearContext();
                            sc.setEJBObjectImpl(null);
                            
                            // disconnect the EJBObject from ProtocolManager
                            // so that no state is held by ProtocolManager
                            remoteHomeRefFactory.destroyReference
                                (ejbObjImpl.getStub(), 
                                 ejbObjImpl.getEJBObject());
                        }
                        if( hasRemoteBusinessView ) {
                            // disconnect the EJBObject from the EJB
                            EJBObjectImpl ejbBusinessObjImpl = 
                                sc.getEJBRemoteBusinessObjectImpl();
                            ejbBusinessObjImpl.clearContext();
                            sc.setEJBRemoteBusinessObjectImpl(null);
                            
                            for(RemoteBusinessIntfInfo next : 
                                    remoteBusinessIntfInfo.values()) {
                                next.referenceFactory.destroyReference
                                (ejbBusinessObjImpl.getStub(), 
                                 ejbBusinessObjImpl.getEJBObject
                                     (next.generatedRemoteIntf.getName()));
                            }
                        }
                    }
                    if ( isLocal ) {
                        long version = sc.getVersion();
                        
                        if( hasLocalHomeView ) {
                            // disconnect the EJBLocalObject from the EJB
                            EJBLocalObjectImpl localObjImpl =
                                sc.getEJBLocalObjectImpl();
                            localObjImpl.setSfsbClientVersion(version);
                            localObjImpl.clearContext();
                            sc.setEJBLocalObjectImpl(null);
                        }
                        if( hasLocalBusinessView ) {
                            EJBLocalObjectImpl localBusinessObjImpl =
                                sc.getEJBLocalBusinessObjectImpl();
                            localBusinessObjImpl.setSfsbClientVersion(version);
                            localBusinessObjImpl.clearContext();
                            sc.setEJBLocalBusinessObjectImpl(null);
                        }
                    }
		    if (_logger.isLoggable(TRACE_LEVEL)) {
			logTraceInfo(sc, "Successfully passivated");
		    }
                } catch (java.io.NotSerializableException nsEx) {
                    sfsbStoreMonitor.incrementPassivationCount(false);
                    _logger.log(Level.WARNING, "Error during passivation: "
				+ sc + "; " + nsEx);
                    _logger.log(Level.FINE, "sfsb passivation error", nsEx);
                    // Error during passivate, so discard bean: EJB2.0 18.3.3
                    try {
                        forceDestroyBean(sc);
                    } catch ( Exception e ) {
                        _logger.log(Level.FINE, "error destroying bean", e);
                    }
                } catch (Throwable ex) {
                    sfsbStoreMonitor.incrementPassivationCount(false);
                    _logger.log(Level.WARNING, "ejb.sfsb_passivation_error",
                                new Object[] { ejbDescriptor.getName() + " <==> " + sc });
                    _logger.log(Level.WARNING, "sfsb passivation error. Key: "
                            + sc.getInstanceKey(), ex);
                    // Error during passivate, so discard bean: EJB2.0 18.3.3
                    try {
                        forceDestroyBean(sc);
                    } catch ( Exception e ) {
                        _logger.log(Level.FINE, "error destroying bean", e);
                    }
                } finally {
                    invocationManager.postInvoke(ci);
		    if (passStartTime != -1) {
			long timeSpent = System.currentTimeMillis()
				- passStartTime;
			sfsbStoreMonitor.setPassivationTime(timeSpent);
		    }
                }
            } //synchronized
            
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "ejb.sfsb_passivation_error",
                        new Object[] { ejbDescriptor.getName() });
            _logger.log(Level.WARNING, "sfsb passivation error", ex);
        }
        return success;
    }

    public final int getPassivationBatchCount() {
        return this.passivationBatchCount;
    }
    
    public final void setPassivationBatchCount(int count) {
        this.passivationBatchCount = count;
    }
    
    // called asynchronously from the Recycler
    public final boolean passivateEJB(StatefulEJBContext sfsbCtx) {
        return passivateEJB((ComponentContext) sfsbCtx.getSessionContext());
    }

    public long getMethodReadyCount() {
	return statMethodReadyCount;
    }

    public long getPassiveCount() {
	return (sfsbStoreMonitor == null)
        ? 0 : sfsbStoreMonitor.getNumPassivations();
    }

    // called from StatefulSessionStore
    public void activateEJB(Object sessionKey, StatefulEJBContext sfsbCtx,
            Object cookie)
    {
        SessionContextImpl context = (SessionContextImpl)
            sfsbCtx.getSessionContext();

	if (_logger.isLoggable(TRACE_LEVEL)) {
	    logTraceInfo(context, "Attempting to activate");
	}

        EJBLocalRemoteObject ejbObject = (EJBLocalRemoteObject) cookie;
        Object ejb = context.getEJB();
        
        ComponentInvocation ci = new ComponentInvocation(ejb, this, context);
        invocationManager.preInvoke(ci);
        try {
            // we're sure that no concurrent thread can be using this bean
            // so no need to synchronize.
            
            // No need to call enlistComponentResources here because
            // ejbActivate executes in unspecified tx context (spec 6.6.1)
            
            // Set the timestamp here, else Recycler might remove this bean!
            context.touch();
            
            context.setContainer(this);
            context.setState(READY);
            incrementMethodReadyStat();
            context.setInstanceKey(sessionKey);
            context.setExistsInStore(true);

            if ( ejbObject == null ) {

                // This MUST be a remote invocation
                if( hasRemoteHomeView ) {
                    createEJBObjectImpl(context);
                } else {
                    createRemoteBusinessObjectImpl(context);
                }

            }
            else if ( ejbObject instanceof EJBObjectImpl ) {

                EJBObjectImpl eo = (EJBObjectImpl) ejbObject;
                ejbObject.setContext(context);
                ejbObject.setKey(sessionKey);

                byte[] sessionOID = uuidGenerator.keyToByteArray(sessionKey);

                if( eo.isRemoteHomeView() ) {

                    // introduce context and EJBObject to each other
                    context.setEJBObjectImpl(eo);
                    
                    EJBObject ejbStub = (EJBObject)
                        remoteHomeRefFactory.createRemoteReference
                            (sessionOID);
                    eo.setStub(ejbStub);
                    context.setEJBStub(ejbStub);
                    
                    if( hasRemoteBusinessView ) {
                        createRemoteBusinessObjectImpl(context);
                    }

                } else {

                    context.setEJBRemoteBusinessObjectImpl(eo);

                    for(RemoteBusinessIntfInfo next : 
                            remoteBusinessIntfInfo.values()) {
                        java.rmi.Remote stub = next.referenceFactory
                            .createRemoteReference(sessionOID);

                        eo.setStub(next.generatedRemoteIntf.getName(), stub);
                    }

                    if( hasRemoteHomeView ) {
                        createEJBObjectImpl(context);
                    }

                }

                if ( isLocal ) { // create localObj too
                    if( hasLocalHomeView ) {
                        createEJBLocalObjectImpl(context);
                    }
                    if( hasLocalBusinessView ) {
                        createEJBLocalBusinessObjectImpl(context);
                    }
                }
            }
            else if ( ejbObject instanceof EJBLocalObjectImpl ) {

                EJBLocalObjectImpl elo = (EJBLocalObjectImpl) ejbObject;
                ejbObject.setContext(context);
                ejbObject.setKey(sessionKey);

                if( elo.isLocalHomeView() ) {                    
                    context.setEJBLocalObjectImpl(elo);
                    if( hasLocalBusinessView ) {
                        createEJBLocalBusinessObjectImpl(context);
                    }
                } else {
                    context.setEJBLocalBusinessObjectImpl(elo);
                    if( hasLocalHomeView ) {
                        createEJBLocalObjectImpl(context);
                    }

                }
                
                if ( hasRemoteHomeView ) { // create remote obj too
                    createEJBObjectImpl(context);
                }
                if ( hasRemoteBusinessView ) {
                    createRemoteBusinessObjectImpl(context);
                }
            }
            

            //Now populate the EEM maps in this context
            repopulateEEMMapsInContext(sessionKey, context);
            
            try {
                interceptorManager.intercept(
                    CallbackType.POST_ACTIVATE, context);
            } catch (Throwable  th) {
                EJBException ejbEx = new EJBException("Error during activation"
                        + sessionKey);
                ejbEx.initCause(th);
                throw ejbEx;
            }
            long now = System.currentTimeMillis();
    		try {
    		    sfsbStoreManager.updateLastAccessTime(sessionKey, now);
    		    context.setLastPersistedAt(now);
    		} catch (SFSBStoreManagerException sfsbEx) {
    		    _logger.log(Level.WARNING,
    			"Couldn't update timestamp for: " + sessionKey
    			+ ";Exception: " + sfsbEx);
    		    _logger.log(Level.FINE,
    			"Couldn't update timestamp for: " + sessionKey, sfsbEx);
    		}            

            
	    if (_logger.isLoggable(TRACE_LEVEL)) {
		logTraceInfo(context, "Successfully activated");
	    }
        _logger.log(Level.FINE, "Activated: " + sessionKey);
        }
        catch ( Exception ex ) {
	    if (_logger.isLoggable(TRACE_LEVEL)) {
		logTraceInfo(context, "Failed to activate");
	    }
	    _logger.log(Level.SEVERE, "ejb.sfsb_activation_error",
                        new Object[] { sessionKey });
            _logger.log(Level.SEVERE, "sfsb activation error. Key: "
                    + sessionKey, ex);

            throw new EJBException("Unable to activate EJB for key: " 
                                   + sessionKey, ex);
        }
        finally {
            invocationManager.postInvoke(ci);
        }
    }

    private void decrementRefCountsForEEMs(SessionContextImpl context) {
        Collection<EEMRefInfo> allRefInfos = context.getAllEEMRefInfos();
        for (EEMRefInfo refInfo : allRefInfos) {
            EEMRefInfoKey key = refInfo.getKey();
            synchronized (extendedEMReferenceCountMap) {
                EEMRefInfo cachedRefInfo = extendedEMReferenceCountMap.get(
                        refInfo.eem);
                if (cachedRefInfo != null) {
                    cachedRefInfo.refCount--;
                    if (cachedRefInfo.refCount == 0) {
                        extendedEMReferenceCountMap.remove(refInfo.eem);
                        eemKey2EEMMap.remove(key);
                    }
                }
            }
        }
    }

    private void repopulateEEMMapsInContext(Object sessionKey,
            SessionContextImpl context) {
        Collection<EEMRefInfo> allRefInfos = context.getAllEEMRefInfos();
        for (EEMRefInfo refInfo : allRefInfos) {
            EEMRefInfoKey key = refInfo.getKey();
            synchronized (extendedEMReferenceCountMap) {
                EntityManager eMgr = eemKey2EEMMap.get(key);
                EEMRefInfo newRefInfo = null;
                if (eMgr != null) {
                    EEMRefInfo cachedRefInfo = extendedEMReferenceCountMap.get(eMgr);
                    //cachedRefInfo cannot be null
                    context.addExtendedEntityManagerMapping(
                            cachedRefInfo.getEntityManagerFactory(),
                            cachedRefInfo);
                    cachedRefInfo.refCount++;
                    newRefInfo = cachedRefInfo;
                } else {
                    //Deserialize em from the byte[]
                    String emRefName = key.emRefName;
                    String unitName = refInfo.getUnitName();
                    EntityManagerFactory emf = EntityManagerFactoryWrapper
                        .lookupEntityManagerFactory(ComponentInvocation.EJB_INVOCATION,
                                unitName, ejbDescriptor);
                    if (emf != null) {
                        ObjectInputStream ois = null;
                        ByteArrayInputStream bis = null;
                        try {
                            bis = new ByteArrayInputStream(refInfo.serializedEEM);
                            ois = new ObjectInputStream(bis);
                            eMgr = (EntityManager) ois.readObject();
                            newRefInfo = new EEMRefInfo(emRefName, unitName, super.getContainerId(),
                                    sessionKey, eMgr, emf);
                            newRefInfo.refCount = 1;
                            extendedEMReferenceCountMap.put(eMgr, newRefInfo);
                            eemKey2EEMMap.put(newRefInfo.getKey(),
                                    newRefInfo.getEntityManager());
                        } catch (Throwable th) {
                        	EJBException ejbEx = new EJBException(
                                    "Couldn't create EntityManager for"
                                            + " refName: " + emRefName);
                        	ejbEx.initCause(th);
                        	throw ejbEx;
                        } finally {
                            if (ois != null) { try { ois.close(); } catch (Throwable th) {} }
                            if (bis != null) { try { bis.close(); } catch (Throwable th) {} }
                        }
                    } else {
                        throw new EJBException(
                                "EMF is null. Couldn't get extended EntityManager for"
                                        + " refName: " + emRefName);
                    }
                }
                context.addExtendedEntityManagerMapping(
                        newRefInfo.getEntityManagerFactory(), newRefInfo);
            }
        }
    }
    
    public void invokePeriodically(long delay, long periodicity, Runnable target) {
        java.util.Timer timer = ContainerFactoryImpl.getContainerService().getTimer();

        TimerTask timerTask = new PeriodicTask(super.loader, target);
        timer.scheduleAtFixedRate(timerTask, delay, periodicity);
        scheduledTimerTasks.add(timerTask);
    }

    public ContainerService getContainerService() {
        return ContainerFactoryImpl.getContainerService();
    }
    
    //Called from Cache implementation through ContainerCallback
    //  when cache.undeploy() is invoked
    public void onUndeploy(StatefulEJBContext sfsbCtx) {
        undeploy((SessionContextImpl) sfsbCtx.getSessionContext());
    }
    
    protected String[] getPre30LifecycleMethodNames() {
        return new String[] {
            null, "ejbRemove", "ejbPassivate", "ejbActivate" 
        };
    };

    /*********************************************************************/
    /***********  END SFSBContainerCallback methods    *******************/
    /*********************************************************************/

    public void onShutdown() {
        _logger.log(Level.FINE, "StatefulSessionContainer.onshutdown() called");
        ClassLoader origClassLoader = Utility.setContextClassLoader(loader);
        try {
            cancelAllTimerTasks();
    
            sessionBeanCache.shutdown();
            while (true) {
                ComponentContext ctx = null;
                synchronized (asyncTaskSemaphore) {
                    int sz = passivationCandidates.size();
                    if (sz > 0) {
                        ctx = (ComponentContext)
                            passivationCandidates.remove(sz-1);
                    } else {
                        break;
                    }
                }
                passivateEJB(ctx);
            }
            
            super.onShutdown();
            sessionBeanCache.destroy();
            try {
                sfsbStoreManager.shutdown();
            } catch (SFSBStoreManagerException sfsbEx) {
                _logger.log(Level.WARNING, "[" + ejbName + "]: Error during "
                        + "storeManager.shutdown()", sfsbEx);
            }
        } catch (Throwable th) {
            _logger.log(Level.WARNING, "[" + ejbName + "]: Error during "
                        + " onShutdown()", th);
        } finally {
            Utility.setContextClassLoader(origClassLoader);
        }
    }

    public void undeploy() {
        _logger.log(Level.FINE, "StatefulSessionContainer.undeploy() called");
        long myContainerId = getContainerId();
        super.setUndeployedState();

        ClassLoader origLoader = null;
        try {
            cancelAllTimerTasks();
            sessionBeanCache.setUndeployedState();

            origLoader = Utility.setContextClassLoader(loader);
            Iterator iter = sessionBeanCache.values();
            while (iter.hasNext()) {
                SessionContextImpl ctx = (SessionContextImpl) iter.next();
                invokePreDestroyAndUndeploy(ctx);
            }

            while (true) {
                SessionContextImpl ctx = null;
                synchronized (asyncTaskSemaphore) {
                    int sz = passivationCandidates.size();
                    if (sz > 0) {
                        ctx = (SessionContextImpl) passivationCandidates
                                .remove(sz - 1);
                        invokePreDestroyAndUndeploy(ctx);
                    } else {
                        break;
                    }
                }
            }

            sessionBeanCache.destroy();
            try {
                sfsbStoreManager.removeAll();
            } catch (SFSBStoreManagerException sfsbEx) {
                _logger.log(Level.WARNING, "[" + ejbName + "]: Error during "
                        + "storeManager.shutdown()", sfsbEx);
            }
        } finally {
            super.undeploy();
            SFSBClientVersionManager.removeAllEntries(myContainerId);

            // helps garbage collection
            // this.storeHelper = null;
            this.passivationCandidates = null;
            this.asyncTaskSemaphore = null;
            this.sessionBeanCache = null;

            if (origLoader != null) {
                Utility.setContextClassLoader(origLoader);
            }
        }
    }
    
    private void invokePreDestroyAndUndeploy(SessionContextImpl ctx) {
        try {
            ctx.setInEjbRemove(true);
            interceptorManager.intercept(CallbackType.PRE_DESTROY, ctx);
        } catch (Throwable t) {
            _logger.log(Level.FINE,
                    "exception thrown from SFSB PRE_DESTROY", t);
        } finally {
            ctx.setInEjbRemove(false);
        }
        
        try {
            this.undeploy(ctx);
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "[" + ejbName
                    + "]: Error while " + " undeploying ctx. Key: "
                    + ctx.getInstanceKey());
            _logger.log(Level.FINE, "[" + ejbName + "]: Error while "
                    + " undeploying ctx. Key: " + ctx.getInstanceKey(),
                    ex);
        }
    }
    
    private void cancelAllTimerTasks() {
        try {
            int size = scheduledTimerTasks.size();
            for (int i=0; i<size; i++) {
                TimerTask task = (TimerTask) scheduledTimerTasks.get(i);
                task.cancel();
            }
        } catch( Exception ex ) {}
    }

    public void undeploy(SessionContextImpl ctx) {
        if (ctx.getContainer() == this) {
            if (hasRemoteHomeView) {
                EJBObjectImpl ejbObjectImpl = ctx.getEJBObjectImpl();
                if( ejbObjectImpl != null ) {
                    remoteHomeRefFactory.destroyReference
                        (ejbObjectImpl.getStub(),
                         ejbObjectImpl.getEJBObject());
                }
            }
            if (hasRemoteBusinessView) {
                EJBObjectImpl ejbBusinessObjectImpl = 
                    ctx.getEJBRemoteBusinessObjectImpl();
                if( ejbBusinessObjectImpl != null ) {
                    for(RemoteBusinessIntfInfo next : 
                            remoteBusinessIntfInfo.values()) {
                        next.referenceFactory.destroyReference
                            (ejbBusinessObjectImpl.getStub
                               (next.generatedRemoteIntf.getName()),
                             ejbBusinessObjectImpl.getEJBObject
                               (next.generatedRemoteIntf.getName()));
                    }
                }
            }
            sessionBeanCache.remove(ctx.getInstanceKey(), ctx.existsInStore());
            destroyExtendedEMsForContext(ctx);
            transactionManager.ejbDestroyed(ctx);
        }
    }
    
    // CacheListener interface
    public void trimEvent(Object primaryKey, Object context) {
        boolean addTask = false;
        synchronized (asyncTaskSemaphore) {
            containerTrimCount++;
            passivationCandidates.add(context);
            int requiredTaskCount =
            (passivationCandidates.size() / passivationBatchCount);
            addTask = (asyncTaskCount < requiredTaskCount);

	    if (_logger.isLoggable(Level.FINE)) {
		_logger.log(Level.FINE, "qSize: " + passivationCandidates.size()
		    + "; batchCount: " + passivationBatchCount
		    + "; asyncTaskCount: " + asyncTaskCount
		    + "; requiredTaskCount: " + requiredTaskCount
		    + "; ADDED TASK ==> " + addTask);
	    }

            if (addTask == false) {
                return;
            }
            asyncTaskCount++;
            asyncCummTaskCount++;
        }
        
        
        try {
            ASyncPassivator work = new ASyncPassivator();
            ContainerWorkPool.addLast(work);
        } catch (Exception ex) {
            synchronized (asyncTaskSemaphore) {
                asyncTaskCount--;
            }
            _logger.log(Level.WARNING, "ejb.add_cleanup_task_error",ex);
        }
        
    }
    
    private class ASyncPassivator implements Servicable {
        
        public void prolog() { }
        
        public void epilog() { }
        
        public void service() { run(); }
        
        public void run() {
            final Thread currentThread = Thread.currentThread();
            final ClassLoader previousClassLoader =
            currentThread.getContextClassLoader();
            final ClassLoader myClassLoader = loader;
            
            boolean decrementedTaskCount = false;
            try {
                // We need to set the context class loader for
                // this (deamon) thread!!
                if(System.getSecurityManager() == null) {
                    currentThread.setContextClassLoader(loader);
                } else {
                    java.security.AccessController.doPrivileged
                            (new java.security.PrivilegedAction() {
                        public java.lang.Object run() {
                            currentThread.setContextClassLoader(loader);
                            return null;
                        }
                    });
                }
                ComponentContext ctx = null;
                
                do {
                    synchronized (asyncTaskSemaphore) {
                        int sz = passivationCandidates.size();
                        if (sz > 0) {
                            ctx = (ComponentContext)
                                passivationCandidates.remove(sz-1);
                        } else {
                            return;
                        }
                    }
                    passivateEJB(ctx);
                } while (true);
                
            } catch (Throwable th) {
                th.printStackTrace();
            } finally {
                if (! decrementedTaskCount) {
                    synchronized (asyncTaskSemaphore) {
                        asyncTaskCount--;
                    }
                }
                
                if(System.getSecurityManager() == null) {
                    currentThread.setContextClassLoader(previousClassLoader);
                } else {
                    java.security.AccessController.doPrivileged
                            (new java.security.PrivilegedAction() {
                        public java.lang.Object run() {
                            currentThread.setContextClassLoader
                                    (previousClassLoader);
                            return null;
                        }
                    });
                }
            }
        }
    }
    
    //*******************************************************//
    //**** Implementation of SFSBContainerInitialization ****//
    //*******************************************************//

    public void setSFSBUUIDUtil(SFSBUUIDUtil util) {
	this.uuidGenerator = util;
    }

    public void setCheckpointPolicy(CheckpointPolicy policy) {
	this.checkpointPolicy = policy;
    }

    public void setSFSBStoreManager(SFSBStoreManager storeManager) {
	this.sfsbStoreManager = storeManager;
    }

    public void setSessionCache(LruSessionCache cache) {
	this.sessionBeanCache = cache;
    }

    public SFSBStoreManager getSFSBStoreManager() {
	return this.sfsbStoreManager;
    }

    public void setRemovalGracePeriodInSeconds(int val) {
	this.removalGracePeriodInSeconds = val;
    }

    public void removeExpiredSessions() {
	try {
	    _logger.log(Level.FINE, "StatefulContainer Removing expired sessions....");
	    long val = sfsbStoreManager.removeExpiredSessions();
	    sfsbStoreMonitor.incrementExpiredSessionsRemoved(val);
	    _logger.log(Level.FINE, "StatefulContainer Removed " + val + " sessions....");
	} catch (SFSBStoreManagerException sfsbEx) {
	    _logger.log(Level.WARNING, "Got exception from store manager",
		sfsbEx);
	}
    }

    ///////////////////////////////////////////////////////////

    private void handleEndOfMethodCheckpoint(SessionContextImpl sc,
	    Invocation inv)
    {
	int txAttr = inv.invocationInfo.txAttr;
	switch (txAttr) {
	    case TX_NEVER:
	    case TX_SUPPORTS:
	    case TX_NOT_SUPPORTED:
		if (inv.invocationInfo.checkpointEnabled) {
		    checkpointEJB(sc);
                }
		break;
	    case TX_BEAN_MANAGED:
		if (sc.isTxCheckpointDelayed()
		    || inv.invocationInfo.checkpointEnabled)
		{
		    checkpointEJB(sc);
		    sc.setTxCheckpointDelayed(false);
		}
		break;
	    default:
		if (inv.invocationInfo.isCreateHomeFinder) {
		    if (inv.invocationInfo.checkpointEnabled) {
			checkpointEJB(sc);
		    }
		}
		break;
	}

	if (sc.getState() != DESTROYED) {
	    sc.setState(READY);
	    incrementMethodReadyStat();
	    if (_logger.isLoggable(TRACE_LEVEL)) {
		logTraceInfo(inv, sc.getInstanceKey(), "Released context");
	    }
	}
    }
    
    private void syncClientVersion(Invocation inv, SessionContextImpl sc) {
        EJBLocalRemoteObject ejbLRO = inv.ejbObject;
        if (ejbLRO != null) {
            ejbLRO.setSfsbClientVersion(sc.getVersion());
        }

        if ((! inv.isLocal) && (checkpointPolicy.isHAEnabled())) {
            long version = sc.getVersion();
            SFSBVersionManager.setResponseClientVersion(version);
            SFSBClientVersionManager.setClientVersion(getContainerId(),
                    sc.getInstanceKey(), version);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Added [synced] version: "
                        + sc.getVersion() + " for key: " + sc.getInstanceKey());
            }
        }
    }

    
    //methods for StatefulSessionBeanStatsProvider
    public int getMaxCacheSize() {
	return sessionBeanCache.getMaxCacheSize();
    }

    private boolean checkpointEJB(SessionContextImpl sc) {
        
        boolean checkpointed = false;
        try {
            
            if ((containerState != CONTAINER_STARTED) && (containerState != CONTAINER_STOPPED)) {
		_logger.log(Level.FINE, "passivateEJB() returning because "
			+ "containerState: " + containerState);
                return false;
            }

            if (sc.getState() == DESTROYED) {
                return false;
	    }
            
            Object ejb = sc.getEJB();
            
	    long checkpointStartTime = -1;
	    if (sfsbStoreMonitor.isMonitoringOn()) {
	        checkpointStartTime = System.currentTimeMillis();
	    }

            ComponentInvocation ci = new ComponentInvocation(ejb, this, sc);
            invocationManager.preInvoke(ci);

            synchronized (sc) {
                try {
                    // dont passivate if there is a Tx/invocation in progress
                    // for this instance.
                    if (sc.getState() != READY) {
                        return false;
                    }

                    // passivate the EJB
                    sc.setState(PASSIVE);
                    decrementMethodReadyStat();
                    interceptorManager.intercept(
                            CallbackType.PRE_PASSIVATE, sc);
		    sc.setLastPersistedAt(System.currentTimeMillis());
		    byte[] serializedState = null;
		    try {
                        long newCtxVersion = sc.incrementAndGetVersion();
		        serializedState = IOUtils.serializeObject(sc, true);
		        SFSBBeanState beanState =
			sfsbStoreManager.createSFSBBeanState(
				sc.getInstanceKey(), sc.getLastAccessTime(),
				!sc.existsInStore(), serializedState);
                        beanState.setVersion(newCtxVersion);
			sfsbStoreManager.checkpointSave(beanState);

			//Now that we have successfully stored.....
            
			sc.setLastPersistedAt(System.currentTimeMillis());
			sc.setExistsInStore(true);
                        checkpointed = true;
		    } catch (EMNotSerializableException emNotSerEx) {
			_logger.log(Level.WARNING,
				"Error during checkpoint(, but session not destroyed)", emNotSerEx);
		    } catch (NotSerializableException notSerEx) {
                        throw notSerEx;
		    } catch (Exception ignorableEx) {
			_logger.log(Level.WARNING,
				"Error during checkpoint", ignorableEx);
		    }

                    interceptorManager.intercept(
                            CallbackType.POST_ACTIVATE, sc);
                    sc.setState(READY);
                    incrementMethodReadyStat();
		    sfsbStoreMonitor.setCheckpointSize(serializedState.length);
		    sfsbStoreMonitor.incrementCheckpointCount(true);
                } catch (Throwable ex) {
		    sfsbStoreMonitor.incrementCheckpointCount(false);
                    _logger.log(Level.WARNING, "ejb.sfsb_checkpoint_error",
                                new Object[] { ejbDescriptor.getName() });
                    _logger.log(Level.WARNING, "sfsb checkpoint error. Key: "
                            + sc.getInstanceKey(), ex);
                    try {
                        forceDestroyBean(sc);
                    } catch ( Exception e ) {
                        _logger.log(Level.FINE, "error destroying bean", e);
                    }
                } finally {
                    invocationManager.postInvoke(ci);
		    if (checkpointStartTime != -1) {
			long timeSpent = System.currentTimeMillis()
				- checkpointStartTime;
			sfsbStoreMonitor.setCheckpointTime(timeSpent);
		    }
                }
            } //synchronized
            
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "ejb.sfsb_passivation_error",
                        new Object[] { ejbDescriptor.getName() });
            _logger.log(Level.WARNING, "sfsb passivation error", ex);
        }

        return checkpointed;
    }
    
    public void incrementMethodReadyStat() {
    	statMethodReadyCount++;
    }
    
    public void decrementMethodReadyStat() {
    	statMethodReadyCount--;
    }
    

    static class EEMRefInfoKey
        implements Serializable {
        
        private String emRefName;
        
        private long containerID;

        private Object instanceKey;

        private int hc;
        
        EEMRefInfoKey(String en, long cid, Object ikey) {
            this.emRefName = en;
            this.containerID = cid;
            this.instanceKey = ikey;
            
            this.hc = instanceKey.hashCode();
        }
        
        public int hashCode() {
            return hc;
        }
        
        public boolean equals(Object obj) {
            boolean result = false;
            if (obj instanceof EEMRefInfoKey) {
                EEMRefInfoKey other = (EEMRefInfoKey) obj;
                result = ((this.containerID == other.containerID)
                        && (this.emRefName.equals(other.emRefName))
                        && (this.instanceKey.equals(other.instanceKey))
                        );
            }
            
            return result;
        }
        
        public String toString() {
            return "<" + instanceKey + ":" + emRefName + ":" + containerID + ">";
        }
    }
    
    static class EEMRefInfo
        implements IndirectlySerializable, SerializableObjectFactory {

        private transient int refCount = 0;

        private String unitName;
        
        private EEMRefInfoKey eemRefInfoKey;

        private byte[] serializedEEM;
        
        private transient EntityManager eem;
        
        private transient EntityManagerFactory emf;
        
        private int hc;

        EEMRefInfo(String emRefName, String uName, long containerID,
                Object instanceKey, EntityManager eem,
                EntityManagerFactory emf) {
            this.eemRefInfoKey = new EEMRefInfoKey(emRefName,
                    containerID, instanceKey);
            this.eem = eem;
            this.emf = emf;
            this.unitName = uName;
        }
        
        EntityManager getEntityManager() {
            return eem;
        }
        
        EntityManagerFactory getEntityManagerFactory() {
            return this.emf;
        }
        
        EEMRefInfoKey getKey() {
            return eemRefInfoKey;
        }

        Object getSessionKey() {
            return eemRefInfoKey.instanceKey;
        }
        
        String getUnitName() {
            return unitName;
        }

        //Method of IndirectlySerializable
        public SerializableObjectFactory getSerializableObjectFactory()
            throws IOException {
            //First serialize the eem into serializedEEM
            
            ByteArrayOutputStream bos = null;
            ObjectOutputStream oos = null;
            try {
                bos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(bos);
                oos.writeObject(eem);
                oos.flush();
                bos.flush();
                serializedEEM = bos.toByteArray();
            } catch (NotSerializableException notSerEx) {
                throw new EMNotSerializableException(
                        notSerEx.toString(), notSerEx);
            } catch (IOException ioEx) {
                throw new EMNotSerializableException(
                        ioEx.toString(), ioEx);
            } finally {
                if (oos != null) { try { oos.close(); } catch (Exception ex) {} }
                if (bos != null) { try { bos.close(); } catch (Exception ex) {} }
            }
            
            return this;
        }
        
        //Method of SerializableObjectFactory
        public Object createObject()
            throws IOException {

            return this;
        }
        
    }

    static class EMNotSerializableException
        extends NotSerializableException {

        public EMNotSerializableException(String className, Throwable th) {
            super(className);
            super.initCause(th);
        }
        
    }

}

class PeriodicTask
    extends java.util.TimerTask
{
    ClassLoader         classLoader;
    AsynchronousTask    task;

    PeriodicTask(ClassLoader classLoader, Runnable target) {
        this.classLoader = classLoader;
        this.task = new AsynchronousTask(target);
    }
        
    public void run() {
        if (! task.isExecuting()) {
            ContainerFactoryImpl.getContainerService().
		scheduleWork(classLoader, task);
        }
    }

    public boolean cancel() {
        boolean cancelled = super.cancel();

        this.classLoader = null;
        this.task = null;

        return cancelled;
    }
}

class AsynchronousTask
    implements Runnable
{
    Runnable        target;
    boolean         executing;

    AsynchronousTask(Runnable target) {
        this.target = target;
        this.executing = false;
    }
        
    boolean isExecuting() {
        return executing;
    }

    //This will be called with the correct ClassLoader
    public void run() {
        try {
            target.run();
        } finally {
            executing = false;
        }
    } // end run
}


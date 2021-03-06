package com.sun.s1asdev.ejb.ee.ejb;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface CMTSession
    extends EJBObject
{

    public String getName()
	throws RemoteException;

    public int getActivateCount()
	throws RemoteException;

    public int getPassivateCount()
	throws RemoteException;

    public void incrementCount()
	throws RemoteException;

}

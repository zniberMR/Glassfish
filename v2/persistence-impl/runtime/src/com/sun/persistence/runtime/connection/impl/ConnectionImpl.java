/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * ConnectionImpl.java
 *
 * Create on March 3, 2000
 */


package com.sun.persistence.runtime.connection.impl;

import com.sun.org.apache.jdo.ejb.EJBImplHelper;
import com.sun.persistence.runtime.LogHelperSQLStore;
import com.sun.persistence.support.JDODataStoreException;
import com.sun.persistence.support.Transaction;
import com.sun.persistence.utility.Linkable;
import com.sun.persistence.utility.logging.Logger;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;

/**
 * This class implements the <code>java.sql.Connection</code> interface, which
 * is part of the JDBC API. You should use the <code>java.sql.Connection</code>
 * interface as an object type instead of this class.
 */
public class ConnectionImpl implements Connection, Linkable {
    /*
     * The associated JDBC Connection.
     */
    private Connection connection;

    /*
     * The datasource url; e.g. "jdbc:oracle:oci7:@ABYSS_ORACLE".
     */
    private String url;

    /*
     * User name.
     */
    private String userName;

    /*
     * Previous ConnectionImpl in a chain.
     */
    Linkable previous;

    /*
     * Next ConnectionImpl in a chain.
     */
    Linkable next;

    /*
     * Indicates whether this ConnectionImpl is pooled.
     */
    private boolean pooled;

    /*
     * The Transaction object for the associated transaction.
     */
    private Transaction transaction;

    /*
     * Indicates whether this ConnectionImpl is to be freed on
     * transaction termination.
     */
    boolean freePending;

    /*
     * The resource interface that is registered with a transaction
     * object.
     */
    //	private ForteJDBCResource		resource;

    /*
     * The parent ConnectionManager object.
     */
    ConnectionManager connectionManager;

    /**
     * The logger
     */
    private static Logger logger = LogHelperSQLStore.getLogger();

    /**
     * Create a new ConnectionImpl object and keep a reference to the
     * corresponding JDBC Connection.
     * @param conn Connection
     */
    public ConnectionImpl(Connection conn, String url, String userName,
            ConnectionManager connMgr) {
        super();
        this.connection = conn;
        this.url = url;
        this.userName = userName;
        this.previous = null;
        this.next = null;
        this.pooled = false;
        this.transaction = null;
        this.freePending = false;
        //		this.resource = null;
        this.connectionManager = connMgr;
    }

    //----------------------------------------------------------------------
    // Wrapper methods for JDBC Connection:

    //
    // Create a JDBC Statement to execute SQL statements without parameters.
    // <p>
    // @return     A StatementImpl wrapper object.
    // @exception  SQLException  if a database error occurs.
    //
    public synchronized Statement createStatement() throws SQLException {
        StatementImpl fstmt;

        try {
            this.checkXact();
            fstmt = new StatementImpl(this, this.connection.createStatement());
        } catch (SQLException se) {
            throw se;
        }

        return ((Statement) fstmt);
    }

    public Statement createStatement(int resultSetType,
            int resultSetConcurrency) throws SQLException {
        return (null);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        return (null);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException {
        return (null);
    }

    //
    // Prepare a SQL statement, with or without parameters, that can be
    // efficiently executed multiple times.
    // <p>
    // @param     sql   SQL statement.
    // @return    A StatementImpl wrapper object.
    // @exception SQLException  if a database error occurs.
    //
    public synchronized PreparedStatement prepareStatement(String sql)
            throws SQLException {
        PreparedStatementImpl fpstmt;

        try {
            this.checkXact();
            fpstmt = new PreparedStatementImpl(
                    this, this.connection.prepareStatement(sql));
        } catch (SQLException se) {
            throw se;
        }

        return ((PreparedStatement) fpstmt);
    }

    //
    // Prepare a SQL stored procedure call.
    // <p>
    // @parameter sql   SQL stored procedure call.
    // @return a ForteCallableStatement wrapper object.
    // @exception SQLException if a database error occurs.
    //
    public synchronized CallableStatement prepareCall(String sql)
            throws SQLException {
        CallableStatementImpl fcstmt;

        try {
            this.checkXact();
            fcstmt = new CallableStatementImpl(
                    this, this.connection.prepareCall(sql));
            return ((CallableStatement) fcstmt);
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized String nativeSQL(String sql) throws SQLException {
        try {
            return (this.connection.nativeSQL(sql));
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized void setAutoCommit(boolean autoCommit)
            throws SQLException {
        try {
            this.connection.setAutoCommit(autoCommit);
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized boolean getAutoCommit() throws SQLException {
        try {
            return (this.connection.getAutoCommit());
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized void commit() throws SQLException {

        try {
            this.connection.commit();
            if (this.freePending) {
                if (this.connectionManager.shutDownPending) {
                    try {
                        this.connection.close();
                        logger.finest("connection.connectionimpl.commit"); // NOI18N
                    } catch (SQLException se) {
                        ;
                    }
                } else {
                    this.freePending = false;
                    this.connectionManager.freeList.insertAtTail(this);
                }
            }
            if (EJBImplHelper.isManaged()) {
                // this is for use with test 'ejb' only.
                closeInternal();
            }
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized void rollback() throws SQLException {
        logger.finest("connection.connectionimpl.rollback"); // NOI18N

        try {
            this.connection.rollback();
            if (this.freePending) {
                if (this.connectionManager.shutDownPending) {
                    this.connection.close();
                    logger.finest("connection.connectionimpl.rollback.close"); // NOI18N
                } else {
                    this.freePending = false;
                    this.connectionManager.freeList.insertAtTail(this);
                }
            }
            if (EJBImplHelper.isManaged()) {
                // this is for use with test 'ejb' only.
                closeInternal();
            }
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized void close() throws SQLException {
        if (EJBImplHelper.isManaged()) {
            logger.finest("connection.connectionimpl.close"); // NOI18N

            // ignore - this can happen in test 'ejb' only
            return;
        }

        closeInternal();
    }

    private synchronized void closeInternal() throws SQLException {

        boolean debug = logger.isLoggable(Logger.FINEST);

        ConnectionImpl conn = (ConnectionImpl) this;

        if (debug) {
            logger.finest("connection.connectionimpl.close_arg", conn); // NOI18N
        }

        try {
            conn.connectionManager.busyList.removeFromList((Linkable) conn);
            if (conn.xactPending() == true) {
                conn.setFreePending(true);
                if (debug) {
                    logger.finest("connection.connectionimpl.close.freepending"); // NOI18N
                }
            } else if ((conn.getPooled() == true)
                    && (conn.connectionManager.shutDownPending == false)) {
                conn.connectionManager.freeList.insertAtTail((Linkable) conn);
                if (debug) {
                    logger.finest("connection.connectionimpl.close.putfreelist"); // NOI18N
                }
            } else {
                if (EJBImplHelper.isManaged()) {
                    // RESOLVE: do we need it here?
                    this.connection.close();
                    if (debug) {
                        logger.finest("connection.connectionimpl.close.exit"); // NOI18N
                    }
                } else {
                    // Save reference to this connection and close it only when
                    // another free becomes available. This reduces time to
                    // get a new connection.
                    this.connectionManager.replaceFreeConnection(this);
                    if (debug) {
                        logger.finest(
                                "connection.connectionimpl.close.replaced"); // NOI18N
                    }
                }
            }

        } catch (SQLException se) {
            throw se;
        }
    }

    /**
     * Called by ConnectionManager to close old connection when a new free
     * connection becomes available
     */
    protected void release() {
        try {
            this.connection.close();
        } catch (SQLException se) {
            // ignore
        }
        logger.finest("connection.connectionimpl.close.connrelease"); // NOI18N
    }

    public synchronized boolean isClosed() throws SQLException {
        try {
            return (this.connection.isClosed());
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized DatabaseMetaData getMetaData() throws SQLException {
        try {
            return (this.connection.getMetaData());
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized void setReadOnly(boolean readOnly) throws SQLException {
        try {
            this.connection.setReadOnly(readOnly);
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized boolean isReadOnly() throws SQLException {
        try {
            return (this.connection.isReadOnly());
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized void setCatalog(String catalog) throws SQLException {
        try {
            this.connection.setCatalog(catalog);
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized String getCatalog() throws SQLException {
        try {
            return (this.connection.getCatalog());
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized void setTransactionIsolation(int level)
            throws SQLException {
        try {
            this.connection.setTransactionIsolation(level);
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized int getTransactionIsolation() throws SQLException {
        try {
            return (this.connection.getTransactionIsolation());
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized SQLWarning getWarnings() throws SQLException {
        try {
            return (this.connection.getWarnings());
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized void clearWarnings() throws SQLException {
        try {
            this.connection.clearWarnings();
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized java.util.Map getTypeMap() throws SQLException {
        try {
            return (this.connection.getTypeMap());
        } catch (SQLException se) {
            throw se;
        }
    }

    public synchronized void setTypeMap(java.util.Map map) throws SQLException {
        try {
            this.connection.setTypeMap(map);
        } catch (SQLException se) {
            throw se;
        }
    }

    //-------------Begin New methods added in JDBC 3.0 --------------

    public synchronized void setHoldability(int holdability)
            throws SQLException {

        throw new UnsupportedOperationException();
    }

    public synchronized int getHoldability() throws SQLException {

        throw new UnsupportedOperationException();
    }

    public synchronized Savepoint setSavepoint() throws SQLException {

        throw new UnsupportedOperationException();
    }

    public synchronized Savepoint setSavepoint(String name)
            throws SQLException {

        throw new UnsupportedOperationException();
    }

    public synchronized void rollback(Savepoint savepoint) throws SQLException {

        throw new UnsupportedOperationException();

    }

    public synchronized void releaseSavepoint(Savepoint savepoint)
            throws SQLException {

        throw new UnsupportedOperationException();
    }

    public synchronized Statement createStatement(int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {

        throw new UnsupportedOperationException();
    }

    public synchronized PreparedStatement prepareStatement(String sql,
            int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public synchronized CallableStatement prepareCall(String sql,
            int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public synchronized PreparedStatement prepareStatement(String sql,
            int autoGeneratedKeys) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public synchronized PreparedStatement prepareStatement(String sql,
            int[] columnIndexes) throws SQLException {

        throw new UnsupportedOperationException();
    }

    public synchronized PreparedStatement prepareStatement(String sql,
            String[] columnNames) throws SQLException {
        throw new UnsupportedOperationException();
    }

    //-------------End New methods added in JDBC 3.0 --------------

    //---------------- ConnectionImpl methods ----------------

    /**
     * Check the TransactionContext.  If the current thread is in a transaction,
     * and the Connection is not participating in a transaction; the Connection
     * should register a resource with the current thread's transaction.  If the
     * current thread is in a transaction, and the Connection is participating
     * in a transaction; the thread's transaction and the Connection's
     * transaction must be the same transaction.  Anything else is an error.
     * <p>
     * @throws SQLException if the current thread's transaction and the
     * Connection's transaction do not match.
     * @ForteInternal
     */
    synchronized void checkXact() throws SQLException {
        /*	RESOLVE: Need to reimplement this
	         * 	for CMP environment if any

		Transaction		tran;

		try
		{
   			tran = ThreadContext.transactionContext().getTransaction();
		}
		catch (SystemException ex)
		{
			tran = null;
		}

		if (tran == null)
		{
			if (this.transaction != null)
			{
				throw new SQLException("Thread is no longer in transaction."); // NOI18N
			}
			else
			{
				// No transaction.
				return;
			}
		}
		else if (this.transaction == null)
		{
			// This is a new transaction.
			this.transaction = tran;
			this.resource = new ForteJDBCResource(this);
			try
			{
				TranManager tm = ThreadContext.partition().getTranManager();
				tm.enlistResource(this.resource);
				this.setAutoCommit(false);
			}
			catch (SQLException ex)
			{
			}
			catch (Throwable ex)
			{
				// XXX This shouldn't happen. XXX
			}
			this.connectionManager.associateXact(tran, this);
		}
		else if (!tran.equals(this.transaction))
		{
			throw new SQLException("Wrong Transaction."); // NOI18N
		}
		else
		{
			// Connection and thread are in the same transaction.
			return;
		}
		*/
    }

    /**
     * Mark the Connection free-pending.  A Connection gets into this state only
     * if it is freed while still participating in a transaction. <p>
     */
    synchronized void setFreePending(boolean freePending) {
        this.freePending = freePending;
    }

    /**
     * Get the value of the free-pending attribute. <p>
     */
    synchronized boolean getFreePending() {
        return (this.freePending);
    }

    /**
     * Indicates whether this Connection is participating in a transaction. <p>
     * @return True if this Connection is participating in a transaction; false,
     *         otherwise.
     */
    synchronized boolean xactPending() {
        return ((this.transaction != null) ? true : false);
    }

    /**
     * Get the previous ConnectionImpl in a chain. <p>
     * @return The previous ConnectionImpl in a chain.
     */
    public Linkable getPrevious() {
        return (this.previous);
    }

    /**
     * Hook a ConnectionImpl to a chain. <p>
     * @param conn The ConnectionImpl to hook on the chain.
     */
    public void setPrevious(Linkable conn) {
        this.previous = conn;
    }

    /**
     * Get the next ConnectionImpl in a chain. <p>
     * @return The next ConnectionImpl in a chain.
     */
    public Linkable getNext() {
        return (this.next);
    }

    /**
     * Hook a ConnectionImpl to a chain. <p>
     * @param conn The ConnectionImpl to hook on the chain.
     */
    public void setNext(Linkable conn) {
        this.next = conn;
    }

    /**
     * Indicates whether this ConnectionImpl is pooled. <p>
     * @return TRUE if this ConnectionImpl is pooled.
     */
    synchronized boolean getPooled() {
        return (this.pooled);
    }

    /**
     * Mark this ConnectionImpl as pooled. <p>
     */
    synchronized void setPooled(boolean flag) {
        this.pooled = flag;
    }

    /**
     * Get the url for this ConnectionImpl object. <p>
     * @return String containing the url for this ConnectionImpl object.
     */
    synchronized String getURL() {
        return this.url;
    }

    /**
     * Get the user name for this ConnectionImpl object. <p>
     * @return String containing the user name for this ConnectionImpl object.
     */
    synchronized String getUserName() {
        return this.userName;
    }

    /**
     * Used by TransactionImpl to commit the transaction on this Connection.
     * Also disassociates this Connection from the transaction.  Throws ...
     * (javax.transaction.xa.XAException(XA_RBROLLBACK)) on commit error. <p>
     */
    public synchronized void internalCommit() {
        logger.finest("connection.connectionimpl.internalcommit"); // NOI18N

        try {
            this.connection.commit();
        } catch (Exception e1) {
            try {
                this.connection.rollback();
            } catch (Exception e2) {
                // XXX Try to recover from bad connection. XXX
            } finally {
                this.clearXact();
            }
            throw new JDODataStoreException(null, e1); //XAException(XAException.XA_RBROLLBACK);
        } finally {
            this.clearXact();
        }
    }

    /**
     * Used by TransactionImpl to rollback the transaction on this Connection.
     * Also disassociates this Connection from the transaction. <p>
     */
    public synchronized void internalRollback() {
        logger.finest("connection.connectionimpl.internalrollback"); // NOI18N
        try {
            this.connection.rollback();
        } catch (Exception e1) {
            // XXX Try to recover from bad connection. XXX
        } finally {
            this.clearXact();
        }
    }

    /**
     * Clear this ConnectionImpl of any knowledge of a transaction. Also informs
     * the parent ConnectionManager to clear its knowledge of the transaction as
     * well. <p>
     */
    private void clearXact() {
        logger.finest("connection.connectionimpl.clearxact"); // NOI18N

        try {
            if (this.freePending) {
                this.freePending = false;
                if (this.pooled) {
                    this.connectionManager.disassociateXact(
                            this.transaction, this, true);
                    logger.finest(
                            "connection.connectionimpl.pendingdisassocxact"); // NOI18N
                } else {
                    this.connectionManager.disassociateXact(
                            this.transaction, this, false);
                    // Make sure the last things done are the only things
                    // that can throw exceptions.
                    this.connection.close();

                    logger.finest("connection.connectionimpl.clearxact.close"); // NOI18N
                }
            } else {
                this.connectionManager.disassociateXact(
                        this.transaction, this, false);
                logger.finest(
                        "connection.connectionimpl.clearxact.disassocxact"); // NOI18N
            }
            this.connection.setAutoCommit(true);
        } catch (SQLException ex) {
            // XXX Need to recover from a bad connection. XXX
        } finally {
            //			this.resource = null;
            this.transaction = null;
        }
    }

    /**
     * Return a string representation of this ConnectionImpl object. <p>
     * @return String describing contents of this ConnectionImpl object.
     */
    public synchronized String toString() {
        int xactIsolation = 0;
        String buffer = "Connect@"; // NOI18N

        String strTran = (this.transaction == null)
                ? "  NULL" : this.transaction.toString(); // NOI18N
        int hash = this.hashCode();

        try {
            xactIsolation = this.getTransactionIsolation();
        } catch (SQLException ex) {
            xactIsolation = -1;
        }

        buffer = buffer + hash + "\n" + // NOI18N
                "  pooled = " + this.pooled + "\n" + // NOI18N
                "  freePending = " + this.freePending + "\n" + // NOI18N
                "  xactIsolation = " + xactIsolation + "\n" + // NOI18N
                "  Tran = " + strTran + "\n"; // NOI18N

        return buffer;
    }

    protected void finalize() {
        try {
            this.connection.close();
            logger.finest("connection.connectionimpl.finalize"); // NOI18N
        } catch (SQLException se) {
            ;
        }
    }
}

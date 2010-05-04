/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.channelfinder;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.ws.WebServiceException;

/**
 * Utility class for dealing with database connection.
 *
 * @author rlange
 */
public class DbConnection {

    private static ThreadLocal<DbConnection> instance = new ThreadLocal<DbConnection>() {
        @Override
        protected DbConnection initialValue() {
            return new DbConnection();
        }
    };
    private Connection con;
    private DataSource ds;

    private DbConnection() {
        try {
            InitialContext ic = new InitialContext();
            ds = (DataSource) ic.lookup("java:comp/env/jdbc/channelfinder");
        } catch (Exception e) {
            throw new IllegalStateException("Cannot find JDBC DataSource named 'channelfinder' "
                    + "- check configuration", e);
        }
    }

    /**
     * Returns the DbConnection instance.
     *
     * @return DbConnection instance
     */
    public static DbConnection getInstance() {
        return instance.get();
    }

    /**
     * Returns the DataSource Connection, requesting a new one (thread local) if needed.
     *
     * @return Connection to the JDBC DataSource
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        if (con == null) {
            con = ds.getConnection();
        }
        return con;
    }

    /**
     * Release the current (thread local) DataSource Connection.
     */
    public void releaseConnection() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                throw new WebServiceException("SQLException while releasing connection", e);
            }
        }
    }

    /**
     * Begins a database transaction.
     * @throws SQLException
     */
    public void beginTransaction() throws SQLException {
        getConnection();
        con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        con.setAutoCommit(false);
    }

    /**
     * Ends a transaction by committing.
     * @throws SQLException
     */
    public void commit() throws SQLException {
        if (con != null) {
            con.commit();
        }
    }

    /**
     * Ends a transaction by rolling back.
     */
    public void rollback() {
        if (con != null) {
            try {
                con.rollback();
            } catch (Exception e) {
                throw new WebServiceException("SQLException during rollback", e);
            }
        }
    }
}

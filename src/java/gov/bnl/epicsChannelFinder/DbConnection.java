/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.epicsChannelFinder;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.sql.DataSource;

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
    
    private DataSource ds;
 
    private DbConnection() {
        try {
            InitialContext ic = new InitialContext();
            ds = (DataSource) ic.lookup("java:comp/env/jdbc/epics_directory");
        } catch (Exception e) {
            throw new IllegalStateException("Cannot find JDBC DataSource named 'epics_directory' "
                    + "- check configuration", e);
        }
    }

    /**
     * Returns the instance of DbConnection.
     * 
     * @return the instance of DbConnection
     */
    public static DbConnection getInstance() {
        return instance.get();
    }
    
    /**
     * Removes this instance.
     */
     private static void removeInstance() {
        instance.remove();
    }

    /**
     * Returns a Connection to the DataSource.
     *
     * @return a Connection
     */
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
    /**
     * Closes this instance.
     */
    public void close() {
        removeInstance();
    }
}

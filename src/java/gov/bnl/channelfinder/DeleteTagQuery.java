/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.channelfinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author rlange
 */
public class DeleteTagQuery {

    private String name;

    /**
     * Creates a new instance of DeleteChannelQuery
     *
     * @param name  channel name
     */
    public DeleteTagQuery(String name) {
    this.name = name;
    }

    /**
     * Creates and executes a JDBC based query
     *
     * @param con  connection to use
     * @throws SQLException
     */
    public void executeQuery(Connection con) throws SQLException {
        String query = "DELETE FROM property WHERE property = ? AND value IS NULL";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, name);
        ps.executeUpdate();
    }
}

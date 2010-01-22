/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.epicsChannelFinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author rlange
 */
public class ListChannelQuery {

    private String name;

    /**
     * Creates a new instance of ListChannelQuery
     *
     * @param name  channel name
     */
    public ListChannelQuery(String name) {
    this.name = name;
    }

    /**
     * Creates and executes a JDBC based query
     *
     * @param con  connection to use
     * @return  result set with columns named <tt>channel</tt>, <tt>property</tt>, <tt>value</tt>
     * @throws SQLException
     */
    public ResultSet executeQuery(Connection con) throws SQLException {
        String query = "SELECT c.name as channel, p.property, p.value FROM channel c, property p" +
                " WHERE c.id = p.channel_id and c.name = ?";

        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, name);

        return ps.executeQuery();
    }
}

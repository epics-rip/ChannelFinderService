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
public class DeleteChannelQuery {

    private String name;

    /**
     * Creates a new instance of DeleteChannelQuery
     *
     * @param name  channel name
     */
    public DeleteChannelQuery(String name) {
    this.name = name;
    }

    /**
     * Creates and executes a JDBC based query
     *
     * @param con  connection to use
     * @throws SQLException
     */
    public void executeQuery(Connection con) throws SQLException {
        String query = "SELECT id FROM channel WHERE name = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        if (!rs.first()) return;
        long id = rs.getLong(1);

        query = "DELETE FROM property WHERE channel_id = ?";
        ps = con.prepareStatement(query);
        ps.setLong(1, id);
        ps.executeUpdate();

        query = "DELETE FROM channel WHERE id = ?";
        ps = con.prepareStatement(query);
        ps.setLong(1, id);
        ps.executeUpdate();
    }
}

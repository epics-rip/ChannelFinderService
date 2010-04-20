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

    private String tag;
    private String channel = null;

    /**
     * Creates a new instance of DeleteChannelQuery
     *
     * @param tag  tag name
     */
    public DeleteTagQuery(String tag) {
        this.tag = tag;
    }

    /**
     * Creates a new instance of DeleteChannelQuery for a single channel delete
     *
     * @param tag  tag name
     */
    public DeleteTagQuery(String tag, String channel) {
        this.tag = tag;
        this.channel = channel;
    }

    /**
     * Creates and executes a JDBC based query
     *
     * @param con  connection to use
     * @throws SQLException
     */
    public void executeQuery(Connection con) throws SQLException {
        Long id = null;

        if (this.channel != null) {
            // Get Channel id
            String query = "SELECT id FROM channel WHERE name = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, this.channel);

            ResultSet rs = ps.executeQuery();
            if (rs.first())
                id = rs.getLong(1);
        }

        String query = "DELETE FROM property WHERE property = ? AND value IS NULL";
        if (id != null) query = query + " AND channel_id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, tag);
        if (id != null) ps.setLong(2, id);
        ps.executeUpdate();
    }
}

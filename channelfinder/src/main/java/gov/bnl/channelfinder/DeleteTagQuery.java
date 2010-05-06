/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.channelfinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ws.rs.core.Response;

/**
 *
 * @author rlange
 */
public class DeleteTagQuery {

    private String tag;
    private String channel = null;

    /**
     * Creates a new instance of DeleteTagQuery
     *
     * @param tag  tag name
     */
    public DeleteTagQuery(String tag) {
        this.tag = tag;
    }

    /**
     * Creates a new instance of DeleteTagQuery for a single tag delete
     *
     * @param tag tag name
     * @param channel channel to delete <tt>tag</tt> from
     */
    public DeleteTagQuery(String tag, String channel) {
        this.tag = tag;
        this.channel = channel;
    }

    /**
     * Creates and executes the JDBC based query
     *
     * @param con connection to use
     * @throws CFException wrapping an SQLException
     */
    public void executeQuery(Connection con) throws CFException {
        Long id = null;
        PreparedStatement ps;
        String query;

        if (channel != null) {                  // Get Channel id
            try {
                query = "SELECT id FROM channel WHERE name = ?";
                ps = con.prepareStatement(query);
                ps.setString(1, channel);

                ResultSet rs = ps.executeQuery();
                if (rs.first()) {
                    id = rs.getLong(1);
                }
            } catch (SQLException e) {
                throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                        "SQL Exception while preparing deletion of tag " + tag +
                        " of channel " + channel, e);
            }
        }

        try {
            query = "DELETE FROM property WHERE property = ? AND value IS NULL";
            if (id != null) {
                query = query + " AND channel_id = ?";
            }
            ps = con.prepareStatement(query);
            ps.setString(1, tag);
            if (id != null) {
                ps.setLong(2, id);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while deleting tag " + tag +
                    " of channel " + channel, e);
        }
    }
}

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
public class DeleteChannelQuery {

    private String name;

    /**
     * Creates a new instance of DeleteChannelQuery.
     *
     * @param name channel name
     */
    public DeleteChannelQuery(String name) {
        this.name = name;
    }

    /**
     * Creates and executes a JDBC based query for deleting one channel.
     *
     * @param con db connection to use
     * @throws CFException wrapping an SQLException
     */
    public void executeQuery(Connection con) throws CFException {
        String query;
        PreparedStatement ps;
        long id;
        try {
            query = "SELECT id FROM channel WHERE name = ?";
            ps = con.prepareStatement(query);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (!rs.first()) {
                return;
            }
            id = rs.getLong(1);
        } catch (Exception e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while preparing deletion of channel " + name, e);
        }

        try {
            query = "DELETE FROM property WHERE channel_id = ?";
            ps = con.prepareStatement(query);
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while deleting properties of channel " + name, e);
        }

        try {
            query = "DELETE FROM channel WHERE id = ?";
            ps = con.prepareStatement(query);
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while deleting channel " + name, e);
        }
    }
}

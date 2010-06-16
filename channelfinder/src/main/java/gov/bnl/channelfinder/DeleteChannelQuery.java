/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * Subject to license terms and conditions.
 */
package gov.bnl.channelfinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ws.rs.core.Response;

/**
 *
 * @author Ralph Lange <Ralph.Lange@bessy.de>
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
     * @param ignoreNoExist flag: true = do not generate an error if channel does not exist
     * @throws CFException wrapping an SQLException
     */
    public void executeQuery(Connection con, boolean ignoreNoExist) throws CFException {
        String query;
        PreparedStatement ps;
        long id;
        try {
            query = "SELECT id FROM channel WHERE name = ?";
            ps = con.prepareStatement(query);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (!rs.first()) {
                if (ignoreNoExist) {
                    return;
                } else {
                    throw new CFException(Response.Status.NOT_FOUND,
                            "Channel " + name + " does not exist");
                }
            }
            id = rs.getLong(1);
        } catch (SQLException e) {
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

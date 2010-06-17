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
 * JDBC query to delete a property from one or all channel(s).
 *
 * @author Ralph Lange <Ralph.Lange@bessy.de>
 */
public class DeletePropertyQuery {

    private String property;
    private String channel = null;

    /**
     * Creates a new instance of DeletePropertyQuery
     *
     * @param property  tag name
     */
    public DeletePropertyQuery(String property) {
        this.property = property;
    }

    /**
     * Creates a new instance of DeletePropertyQuery for a single property delete
     *
     * @param property property name
     * @param channel channel to delete <tt>tag</tt> from
     */
    public DeletePropertyQuery(String property, String channel) {
        this.property = property;
        this.channel = channel;
    }

    /**
     * Creates and executes the JDBC based query
     *
     * @param con connection to use
     * @param ignoreNoExist flag: true = do not generate an error if property does not exist
     * @throws CFException wrapping an SQLException
     */
    public void executeQuery(Connection con, boolean ignoreNoExist) throws CFException {
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
                        "SQL Exception while preparing deletion of property " + property +
                        " of channel " + channel, e);
            }
        }

        try {
            query = "DELETE FROM property WHERE LOWER(property) = ? AND value IS NOT NULL";
            if (id != null) {
                query = query + " AND channel_id = ?";
            }
            ps = con.prepareStatement(query);
            ps.setString(1, property.toLowerCase());
            if (id != null) {
                ps.setLong(2, id);
            }
            int rows = ps.executeUpdate();
            if (rows == 0 && !ignoreNoExist) {
                throw new CFException(Response.Status.NOT_FOUND,
                        "Property " + property + " does not exist");
            }
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while deleting property " + property +
                    " of channel " + channel, e);
        }
    }
}

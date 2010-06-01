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
                        "SQL Exception while preparing deletion of tag " + tag +
                        " of channel " + channel, e);
            }
        }

        try {
            query = "DELETE FROM property WHERE LOWER(property) = ? AND value IS NULL";
            if (id != null) {
                query = query + " AND channel_id = ?";
            }
            ps = con.prepareStatement(query);
            ps.setString(1, tag.toLowerCase());
            if (id != null) {
                ps.setLong(2, id);
            }
            int rows = ps.executeUpdate();
            if (rows == 0 && !ignoreNoExist) {
                throw new CFException(Response.Status.NOT_FOUND,
                        "Tag " + tag + " does not exist");
            }
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while deleting tag " + tag +
                    " of channel " + channel, e);
        }
    }
}

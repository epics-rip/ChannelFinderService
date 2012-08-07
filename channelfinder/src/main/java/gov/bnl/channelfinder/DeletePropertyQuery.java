package gov.bnl.channelfinder;
/**
 * #%L
 * ChannelFinder Directory Service
 * %%
 * Copyright (C) 2010 - 2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * %%
 * Copyright (C) 2010 - 2012 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 * #L%
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.ws.rs.core.Response;

/**
 * JDBC query to delete a property from one or all channel(s).
 *
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */
public class DeletePropertyQuery {

    private String name;
    private String channel;
    private boolean removeProperty = false;

    private DeletePropertyQuery(String name, boolean removeProperty) {
        this.name = name;
        this.removeProperty = removeProperty;
    }

    private DeletePropertyQuery(String name, String channel) {
        this.name = name;
        this.channel = channel;
    }

    /**
     * Creates and executes the JDBC based query.
     *
     * @param con connection to use
     * @param ignoreNoExist flag: true = do not generate an error if property/tag does not exist
     * @throws CFException wrapping an SQLException
     */
    private void executeQuery(Connection con, boolean ignoreNoExist) throws CFException {
        Long cid = null;
        PreparedStatement ps;
        String query;

        // Get property id
        Long pid = FindPropertyIdsQuery.getPropertyId(name);

        if (pid == null) {
            if (ignoreNoExist) {
                return;
            } else {
                throw new CFException(Response.Status.NOT_FOUND,
                        "Property/tag '" + name + "' does not exist");
            }
        }

        if (channel != null) {
            // Get channel id
            try {
                query = "SELECT id FROM channel WHERE name = ?";
                ps = con.prepareStatement(query);
                ps.setString(1, channel);

                ResultSet rs = ps.executeQuery();
                if (rs.first()) {
                    cid = rs.getLong(1);
                } else {
                    throw new CFException(Response.Status.NOT_FOUND,
                            "Channel '" + channel + "' does not exist");
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                        "SQL Exception while preparing deletion of property/tag '" + name
                        + "' from channel '" + channel + "'", e);
            }
            // Delete values for channel
            try {
                query = "DELETE FROM value WHERE property_id = ? AND channel_id = ?";
                ps = con.prepareStatement(query);
                ps.setLong(1, pid);
                ps.setLong(2, cid);
                int rows = ps.executeUpdate();
                if (rows == 0 && !ignoreNoExist) {
                    throw new CFException(Response.Status.NOT_FOUND,
                            "Property/tag '" + name + "' does not exist for channel '"
                            + channel + "'");
                }
                ps.close();
            } catch (SQLException e) {
                throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                        "SQL Exception while deleting property/tag '" + name +
                        "' from channel '" + channel + "'", e);
            }

        } else {

            if (removeProperty) {
                try {
                    query = "DELETE FROM property WHERE id = ?";
                    ps = con.prepareStatement(query);
                    ps.setLong(1, pid);
                    int rows = ps.executeUpdate();
                    ps.close();
                } catch (SQLException e) {
                    throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                            "SQL Exception while deleting property/tag '" + name + "'", e);
                }
            } else {
                try {
                    query = "DELETE FROM value WHERE property_id = ?";
                    ps = con.prepareStatement(query);
                    ps.setLong(1, pid);
                    int rows = ps.executeUpdate();
                    if (rows == 0 && !ignoreNoExist) {
                        throw new CFException(Response.Status.NOT_FOUND,
                                "Property/tag '" + name + "' does not exist");
                    }
                    ps.close();
                } catch (SQLException e) {
                    throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                            "SQL Exception while deleting property/tag '" + name + "'", e);
                }
            }
        }
    }

    /**
     * Creates a DeletePropertyQuery to completely remove a property/tag (from all
     * channels and the property/tag itself).
     *
     * @param name property/tag name
     */
    public static void removeProperty(String name) throws CFException {
        DeletePropertyQuery q = new DeletePropertyQuery(name, true);
        q.executeQuery(DbConnection.getInstance().getConnection(), true);
    }

    /**
     * Creates a DeletePropertyQuery to completely remove a property/tag (from all
     * channels and the property/tag itself).
     *
     * @param name property/tag name
     */
    public static void removeExistingProperty(String name) throws CFException {
        DeletePropertyQuery q = new DeletePropertyQuery(name, true);
        q.executeQuery(DbConnection.getInstance().getConnection(), false);
    }

    /**
     * Creates a DeletePropertyQuery to remove all values for the specified property/tag
     * (without removing the property/tag itself).
     *
     * @param name property/tag name
     * @throws CFException wrapping an SQLException
     */
    public static void deleteAllValues(String name) throws CFException {
        DeletePropertyQuery q = new DeletePropertyQuery(name, false);
        q.executeQuery(DbConnection.getInstance().getConnection(), true);
    }

    /**
     * Creates a DeletePropertyQuery to remove one value of the specified property/tag
     * from the specified channel.
     *
     * @param name property/tag name
     * @param channel channel to delete <tt>name</tt> from
     * @return new FindChannelsQuery instance
     */
    public static void deleteOneValue(String name, String channel) throws CFException {
        DeletePropertyQuery q = new DeletePropertyQuery(name, channel);
        q.executeQuery(DbConnection.getInstance().getConnection(), false);
    }
}

/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010-2011 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms and conditions.
 */

package gov.bnl.channelfinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * JDBC query to add a property to channel(s).
 * 
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */
public class UpdateValuesQuery {

    private XmlChannels channels;
    private boolean isTagQuery = false;
    private String oldname;
    private String name;
    private String owner;
    private String dbname;
    private String dbowner;

    private String getType() {
        if (isTagQuery) {
            return "tag";
        } else {
            return "property";
        }
    }

    /**
     * Creates a new instance of UpdateValuesQuery.
     *
     * @param data property data (containing channels to add property to)
     */
    private UpdateValuesQuery(String name, XmlProperty data) {
        this.oldname = name;
        this.name = data.getName();
        this.owner = data.getOwner();
        this.channels = data.getXmlChannels();
    }

    /**
     * Creates a new instance of UpdateValuesQuery.
     *
     * @param data property data (containing channels to add property to)
     */
    private UpdateValuesQuery(String name, XmlTag data) {
        this.oldname = name;
        this.name = data.getName();
        this.owner = data.getOwner();
        this.channels = data.getXmlChannels();
        this.isTagQuery = true;
    }

    /**
     * Creates a new instance of UpdateValuesQuery for a single tag on a single channel
     *
     * @param name name of tag to add
     * @param owner owner for tag to add
     * @param channel channel to add tag to
     */
    private UpdateValuesQuery(String name, String channel) {
        this.oldname = name;
        this.name = name;
        this.isTagQuery = true;
        this.channels = new XmlChannels(new XmlChannel(channel));
    }

    /**
     * Creates a new instance of UpdateValuesQuery for a single property on a single channel
     *
     * @param name name of tag to add
     * @param owner owner for tag to add
     * @param channel channel to add tag to
     */
    private UpdateValuesQuery(String name, String channel, XmlProperty data) {
        this.oldname = name;
        this.name = name;
        XmlChannel chan = new XmlChannel(channel);
        chan.addXmlProperty(data);
        this.channels = new XmlChannels(chan);
        
    }

    /**
     * Creates and executes a JDBC based query to add a property to the listed channels
     *
     * @param con  connection to use
     * @throws CFException wrapping an SQLException
     */
    public void executeQuery(Connection con) throws CFException {
        List<String> params = new ArrayList<String>();
        Map<String, Long> ids = new HashMap<String, Long>();
        Map<String, String> values = new HashMap<String, String>();
        PreparedStatement ps;
        int i;

        // Get property id
        Long pid = FindPropertyIdsQuery.getPropertyId(oldname);

        if (pid == null) {
            throw new CFException(Response.Status.NOT_FOUND,
                    "A " + getType() + " named '" + oldname + "' does not exist");
        }

        // Update name and owner if necessary
        if (isTagQuery) {
            XmlTag t = ListPropertiesQuery.findTag(oldname);
            dbname = t.getName();
            dbowner = t.getOwner();
        } else {
            XmlProperty p = ListPropertiesQuery.findProperty(oldname);
            dbname = p.getName();
            dbowner = p.getOwner();
        }
        if ((oldname != null && !oldname.equals(name)) || (owner != null && !dbowner.equals(owner))) {
            String q = "UPDATE property SET name = ?, owner = ? WHERE id = ?";
            try {
                ps = con.prepareStatement(q.toString());
                ps.setString(1, name);
                ps.setString(2, owner);
                ps.setLong(3, pid);

                ps.executeUpdate();
            } catch (SQLException e) {
                throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                        "SQL Exception while updating "
                        + getType() + " '" + name + "'", e);
            }
        }

        if (channels == null) return;

        // Get Channel ids
        StringBuilder query = new StringBuilder("SELECT id, name FROM channel WHERE ");
        for (XmlChannel chan : channels.getChannels()) {
            query.append("name = ? OR ");
            params.add(chan.getName());
        }
        query.setLength(query.length() - 3);

        try {
            ps = con.prepareStatement(query.toString());
            i = 1;
            for (String p : params) {
                ps.setString(i++, p);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Add key to map of matching channel ids
                ids.put(rs.getString("name"), rs.getLong("id"));
            }
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while retrieving channel ids for insertion of "
                    + getType() + " '" + name + "'", e);
        }

        if (ids.isEmpty()) {
            throw new CFException(Response.Status.NOT_FOUND,
                    "Channels specified in " + getType() + " update do not exist");
        }

        // Get values from payload
        for (XmlChannel chan : channels.getChannels()) {
            for (XmlProperty prop : chan.getXmlProperties().getProperties()) {
                if (name.equals(prop.getName())) {
                    values.put(chan.getName(), prop.getValue());
                }
            }
        }

        // Remove existing values for the specified channels
        query.setLength(0);
        params.clear();
        query.append("DELETE FROM value WHERE property_id = ? AND channel_id IN (");
        for (Long id : ids.values()) {
            query.append("?, ");
        }
        query.replace(query.length() - 2, query.length(), ")");

        try {
            ps = con.prepareStatement(query.toString());
            ps.setLong(1, pid);
            i = 2;
            for (Long id : ids.values()) {
                ps.setLong(i++, id);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while deleting values for " + getType() + " '"
                    + name + "' (before reinsert)", e);
        }

        // Add new values
        query.setLength(0);
        params.clear();
        query.append("INSERT INTO value (channel_id, property_id, value) VALUES ");
        for (Long id : ids.values()) {
            query.append("(?,?,?),");
        }
        try {
            ps = con.prepareStatement(query.substring(0, query.length() - 1));
            i = 1;
            for (String chan : ids.keySet()) {
                ps.setLong(i++, ids.get(chan));
                ps.setLong(i++, pid);
                if (values.get(chan) == null) {
                    ps.setNull(i++, java.sql.Types.NULL);
                } else {
                    ps.setString(i++, values.get(chan));
                }
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while inserting values for " + getType()
                    + " '" + name + "' ", e);
        }
    }

    /**
     * Updates a property in the database.
     *
     * @param name name of property to update
     * @param prop XmlProperty
     * @throws CFException wrapping an SQLException
     */
    public static void updateProperty(String name, XmlProperty prop) throws CFException {
        UpdateValuesQuery q = new UpdateValuesQuery(name, prop);
        q.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Updates a single property instance in the database.
     *
     * @param name name of property to update
     * @param channel name of channel to update
     * @param prop XmlProperty
     * @throws CFException wrapping an SQLException
     */
    public static void updateSingleProperty(String name, String channel, XmlProperty prop) throws CFException {
        UpdateValuesQuery q = new UpdateValuesQuery(name, channel, prop);
        q.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Updates the <tt>tag</tt> in the database, adding it to all channels in <tt>tag</tt>.
     *
     * @param tag XmlTag
     * @throws CFException wrapping an SQLException
     */
    public static void updateTag(String name, XmlTag tag) throws CFException {
        UpdateValuesQuery q = new UpdateValuesQuery(name, tag);
        q.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Updates the <tt>tag</tt> in the database, adding it to the single channel <tt>chan</tt>.
     *
     * @param tag name of tag to add
     * @param chan name of channel to add tag to
     * @throws CFException wrapping an SQLException
     */
    public static void updateTag(String tag, String chan) throws CFException {
        UpdateValuesQuery q = new UpdateValuesQuery(tag, chan);
        q.executeQuery(DbConnection.getInstance().getConnection());
    }
}

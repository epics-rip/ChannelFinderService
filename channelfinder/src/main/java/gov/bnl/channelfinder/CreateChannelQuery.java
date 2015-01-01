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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * JDBC query to create one channel.
 *
 * @author Ralph Lange {@literal <ralph.lange@gmx.de>}
 */
public class CreateChannelQuery {

    private XmlChannel chan;

    private CreateChannelQuery(XmlChannel chan) {
        this.chan = chan;
    }

    /**
     * Executes a JDBC based query to add a channel and its properties/tags.
     *
     * @param con database connection to use
     * @throws CFException wrapping an SQLException
     */
    private void executeQuery(Connection con) throws CFException {
        List<List<String>> params = new ArrayList<List<String>>();
        PreparedStatement ps;
        int i;
        long id;

        // Insert channel
        StringBuilder query = new StringBuilder("INSERT INTO channel (name, owner) VALUE (?, ?)");
        try {
            ps = con.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, chan.getName());
            ps.setString(2, chan.getOwner());
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            rs.first();
            id = rs.getLong(1);
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while adding channel '" + chan.getName() +"'", e);
        }

        // Fetch the property/tag ids
        Map<String, Integer> pids = FindPropertyIdsQuery.getPropertyIdMap(chan);

        // Insert properties/tags
        if (this.chan.getXmlProperties().getProperties().size() > 0
                || this.chan.getXmlTags().getTags().size() > 0) {
            params.clear();
            query.setLength(0);
            query.append("INSERT INTO value (channel_id, property_id, value) VALUES ");
            for (XmlProperty prop : this.chan.getXmlProperties().getProperties()) {
                if (pids.get(prop.getName()) == null) {
                    throw new CFException(Response.Status.NOT_FOUND,
                    "Property '" + prop.getName() + "' does not exist");
                }
                query.append("(?,?,?),");
                ArrayList<String> par = new ArrayList<String>();
                par.add(prop.getName());
                par.add(prop.getValue());
                params.add(par);
            }
            for (XmlTag tag : this.chan.getXmlTags().getTags()) {
                if (pids.get(tag.getName()) == null) {
                    throw new CFException(Response.Status.NOT_FOUND,
                    "Tag '" + tag.getName() + "' does not exist");
                }
                query.append("(?,?,?),");
                ArrayList<String> par = new ArrayList<String>();
                par.add(tag.getName());
                par.add(null);
                params.add(par);
            }
            try {
                ps = con.prepareStatement(query.substring(0, query.length() - 1));
                i = 1;
                for (List<String> par : params) {
                    ps.setLong(i++, id);
                    ps.setLong(i++, pids.get(par.get(0)));
                    if (par.get(1) == null) {
                        ps.setNull(i++, java.sql.Types.NULL);
                    } else {
                        ps.setString(i++, par.get(1));
                    }
                }
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                        "SQL Exception while adding properties/tags for channel '" + chan.getName() + "'", e);
            }
        }
    }

    /**
     * Creates a channel and its properties/tags in the database.
     *
     * @param chan XmlChannel object
     * @throws CFException wrapping an SQLException
     */
    public static void createChannel(XmlChannel chan) throws CFException {
        CreateChannelQuery q = new CreateChannelQuery(chan);
        q.executeQuery(DbConnection.getInstance().getConnection());
    }
}

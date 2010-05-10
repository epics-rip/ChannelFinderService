/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.channelfinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 *
 * @author rlange
 */
public class AddTagQuery {

    private XmlChannels data;
    private String name;
    private String owner;

    /**
     * Creates a new instance of AddTagQuery.
     *
     * @param name name of tag to add
     * @param data channel data (channels to add tag to)
     * @param owner owner for tag to add
     */
    public AddTagQuery(String name, String owner, XmlChannels data) {
        this.name = name;
        this.owner = owner;
        this.data = data;
    }

    /**
     * Creates a new instance of AddTagQuery for a single channel
     *
     * @param name name of tag to add
     * @param data channel data (channel to add tag to)
     * @param owner owner for tag to add
     */
    public AddTagQuery(String name, String owner, XmlChannel data) {
        this.name = name;
        this.owner = owner;
        this.data = new XmlChannels(data);
    }

    /**
     * Creates and executes a JDBC based query to add a tag to the listed channels
     *
     * @param con  connection to use
     * @throws CFException wrapping an SQLException
     */
    public void executeQuery(Connection con) throws CFException {
        List<String> params = new ArrayList<String>();
        List<Long> ids = new ArrayList<Long>();
        PreparedStatement ps;
        int i;

        // Get Channel ids
        String query = "SELECT id FROM channel WHERE ";
        for (XmlChannel chan : data.getChannels()) {
            query = query + "name = ? OR ";
            params.add(chan.getName());
        }
        query = query.substring(0, query.length() - 3);

        try {
            ps = con.prepareStatement(query);
            i = 1;
            for (String p : params) {
                ps.setString(i++, p);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Add key to list of matching channel ids
                ids.add(rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while preparing insertion of tag " + name, e);
        }

        // Insert tags
        if (ids.isEmpty()) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "No such channels");
        }
        params.clear();
        query = "INSERT INTO property (channel_id, property, owner) VALUES ";
        for (Long id : ids) {
            query = query + "(?,?,?),";
            params.add(name);
            params.add(owner);
        }
        try {
            ps = con.prepareStatement(query.substring(0, query.length() - 1));
            i = 1;
            int j = 0;
            for (Long id : ids) {
                ps.setLong(i++, id);
                ps.setString(i++, params.get(j++));
                ps.setString(i++, params.get(j++));
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while inserting tag " + name, e);
        }
    }
}

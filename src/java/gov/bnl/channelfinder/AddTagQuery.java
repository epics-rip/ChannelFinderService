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

/**
 *
 * @author rlange
 */
public class AddTagQuery {

    private XmlChannels data;
    private String name;
    private String owner;

    /**
     * Creates a new instance of AddTagQuery
     *
     * @param data  channel data
     */
    public AddTagQuery(String name, String owner, XmlChannels data) {
        this.name = name;
        this.owner = owner;
        this.data = data;
    }

    /**
     * Creates and executes a JDBC based query to add a tag to the listed channels
     *
     * @param con  connection to use
     * @throws SQLException
     */
    public void executeQuery(Connection con) throws SQLException {
        List<String> params = new ArrayList<String>();
        List<Long> ids = new ArrayList<Long>();

        // Get Channel ids
        String query = "SELECT id FROM channel WHERE ";
        for (XmlChannel chan : data.getChannels()) {
            query = query + "name = ? OR ";
            params.add(chan.getName());
        }
        query = query.substring(0, query.length() - 3);
        PreparedStatement ps = con.prepareStatement(query);
        int i = 1;
        for (String p : params) {
            ps.setString(i++, p);
        }

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            // Add key to list of matching channel ids
            ids.add(rs.getLong(1));
        }

        // Insert tags
        params.clear();
        query = "INSERT INTO property (channel_id, property, owner) VALUES ";
        for (Long id : ids) {
            query = query + "(?,?,?),";
            params.add(name);
            params.add(owner);
        }
        ps = con.prepareStatement(query.substring(0, query.length() - 1));
        i = 1;
        int j = 0;
        for (Long id : ids) {
            ps.setLong(i++, id);
            ps.setString(i++, params.get(j++));
            ps.setString(i++, params.get(j++));
        }
        ps.executeUpdate();
    }
}

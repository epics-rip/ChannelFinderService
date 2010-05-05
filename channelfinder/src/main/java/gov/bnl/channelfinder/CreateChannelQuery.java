/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.channelfinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author rlange
 */
public class CreateChannelQuery {

    private XmlChannel chan;

    /**
     * Creates a new instance of CreateChannelQuery
     *
     * @param chan  channel name
     */
    public CreateChannelQuery(XmlChannel chan) {
    this.chan = chan;
    }

    /**
     * Creates and executes a JDBC based query to create a channel and its properties
     *
     * @param con  connection to use
     * @throws SQLException
     */
    public void executeQuery(Connection con) throws SQLException {
        List<List<String>> params = new ArrayList<List<String>>();
        int i;

        // Insert channel
        String query = "INSERT INTO channel (name, owner) VALUE (?, ?)";
        PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, chan.getName());
        ps.setString(2, chan.getOwner());
        ps.execute();
        ResultSet rs = ps.getGeneratedKeys();
        rs.first();
        long id = rs.getLong(1);

        // Insert properties
        if (this.chan.getXmlProperties().size() > 0) {
            params.clear();
            query = "INSERT INTO property (channel_id, property, value, owner) VALUES ";
            for (XmlProperty prop : this.chan.getXmlProperties()) {
                query = query + "(?,?,?,?),";
                ArrayList<String> par = new ArrayList<String>();
                par.add(prop.getName());
                par.add(prop.getValue());
                par.add(prop.getOwner());
                params.add(par);
            }
            ps = con.prepareStatement(query.substring(0, query.length() - 1));
            i = 1;
            for (List<String> par : params) {
                ps.setLong(i++, id);
                for (int j = 0; j < 3; j++)
                    ps.setString(i++, par.get(j));
            }
            ps.executeUpdate();
        }

        // Insert tags
        if (this.chan.getXmlTags().size() > 0) {
            params.clear();
            query = "INSERT INTO property (channel_id, property, owner) VALUES ";
            for (XmlTag tag : this.chan.getXmlTags()) {
                query = query + "(?,?,?),";
                ArrayList<String> par = new ArrayList<String>();
                par.add(tag.getName());
                par.add(tag.getOwner());
                params.add(par);
            }
            ps = con.prepareStatement(query.substring(0, query.length() - 1));
            i = 1;
            for (List<String> par : params) {
                ps.setLong(i++, id);
                for (int j = 0; j < 2; j++)
                    ps.setString(i++, par.get(j));
            }
            ps.executeUpdate();
        }
    }
}

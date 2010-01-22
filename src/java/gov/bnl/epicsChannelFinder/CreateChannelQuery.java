/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.epicsChannelFinder;

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
public class CreateChannelQuery {

    private XmlChannel chan;

    /**
     * Creates a new instance of CreateChannelQuery
     *
     * @param name  channel name
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
        String query = "INSERT INTO channel VALUE ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, chan.getName());
        ResultSet rs = ps.executeQuery();
        rs.first();
        long id = rs.getLong(1);

        query = "INSERT INTO property VALUES ";
        for (XmlProperty prop : this.chan.getXmlProperties().getProperty()) {
            query = query + "(?,?,?),";
            ArrayList<String> par = new ArrayList<String>();
            par.add(prop.getName());
            par.add(prop.getValue());
            params.add(par);
        }
        ps = con.prepareStatement(query.substring(0, query.length() - 1));
        int i = 1;
        for (List<String> par : params) {
            ps.setLong(i++, id);
            ps.setString(i++, par.get(0));
            ps.setString(i++, par.get(1));
        }
        ps.executeUpdate();
    }
}

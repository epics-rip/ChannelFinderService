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
import java.util.Collection;
import java.util.List;

/**
 *
 * @author rlange
 */
public class FindPropertiesQuery {

    private List<String> names = new ArrayList();

    /**
     * Creates a new instance of FindPropertiesQuery.
     *
     * @param matches  the collection of name matches
     */
    private FindPropertiesQuery(Collection<String> matches) {
        names.addAll(matches);
    }

    /**
     * Creates a new instance of FindPropertiesQuery for a simple query.
     *
     * @param name the name to match
     */
    private FindPropertiesQuery(String name) {
        names.add(name);
    }

    /**
     * Creates a FindPropertiesQuery for multiple property names.
     * @param matches String collection of property names
     * @return new FindChannelsQuery instance
     */
    public static FindPropertiesQuery createFindPropertiesQuery(Collection<String> matches) {
        return new FindPropertiesQuery(matches);
    }

    /**
     * Creates a channel FindPropertiesQuery for a single channel name.
     * @param name property name
     * @return new FindChannelsQuery instance
     */
    public static FindPropertiesQuery createFindPropertiesQuery(String name) {
        return new FindPropertiesQuery(name);
    }

    /**
     * Creates and executes a JDBC based query using GROUP to collapse it by property.
     *
     * @param con connection to use
     * @return result set with columns named <tt>property</tt>, <tt>value</tt>, <tt>owner</tt>
     * @throws SQLException
     */
    public ResultSet executeQuery(Connection con) throws SQLException {
        String query = "SELECT property, value, owner FROM property p WHERE";
        List<String> name_params = new ArrayList<String>();

        for (String name : names) {
            query = query + " property = ? AND";
            name_params.add(name);
        }
        query = query.substring(0, query.length()-3) + "GROUP BY property";

        PreparedStatement ps = con.prepareStatement(query);
        int i = 1;
        for (String s : name_params) ps.setString(i++, s);

        return ps.executeQuery();
    }
}

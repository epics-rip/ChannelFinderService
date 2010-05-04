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
public class FindEntitiesQuery {

    private enum SearchType { CHANNEL, PROPERTY };
    private SearchType type;
    private List<String> names = new ArrayList();

    /**
     * Creates a new instance of FindEntitiesQuery.
     *
     * @param matches  the collection of name matches
     */
    private FindEntitiesQuery(SearchType type, Collection<String> matches) {
        this.type = type;
        names.addAll(matches);
    }

    /**
     * Creates a new instance of FindEntitiesQuery for a simple query.
     *
     * @param name the name to match
     */
    private FindEntitiesQuery(SearchType type, String name) {
        this.type = type;
        names.add(name);
    }

    /**
     * Creates a FindEntitiesQuery for multiple channel names.
     * @param names String collection of channel names
     * @return new FindEntitiesQuery instance
     */
    public static FindEntitiesQuery createFindChannelNamesQuery(Collection<String> names) {
        return new FindEntitiesQuery(FindEntitiesQuery.SearchType.CHANNEL, names);
    }

    /**
     * Creates a channel FindEntitiesQuery for a single channel name.
     * @param name channel name
     * @return new FindEntitiesQuery instance
     */
    public static FindEntitiesQuery createFindChannelNamesQuery(String name) {
        return new FindEntitiesQuery(FindEntitiesQuery.SearchType.CHANNEL, name);
    }

    /**
     * Creates a FindEntitiesQuery for multiple property or tag names.
     * @param names String collection of property/tag names
     * @return new FindEntitiesQuery instance
     */
    public static FindEntitiesQuery createFindPropertyNamesQuery(Collection<String> names) {
        return new FindEntitiesQuery(FindEntitiesQuery.SearchType.PROPERTY, names);
    }

    /**
     * Creates a channel FindEntitiesQuery for a single property or tag name.
     * @param name property/tag name
     * @return new FindEntitiesQuery instance
     */
    public static FindEntitiesQuery createFindPropertyNamesQuery(String name) {
        return new FindEntitiesQuery(FindEntitiesQuery.SearchType.PROPERTY, name);
    }

    /**
     * Creates and executes a JDBC based query using GROUP to collapse it by name.
     *
     * @param con connection to use
     * @return result set with columns named <tt>name</tt>, <tt>owner</tt>
     * @throws SQLException
     */
    public ResultSet executeQuery(Connection con) throws SQLException {
        PreparedStatement ps;
        List<String> name_params = new ArrayList<String>();

        String q_base;
        String q_clause;
        String q_group;
        if (type == FindEntitiesQuery.SearchType.PROPERTY) {
            q_base = "SELECT property as name, owner FROM property";
            q_clause = " property = ? OR";
            q_group = " GROUP BY property";
        } else {
            q_base = "SELECT name, owner FROM channel";
            q_clause = " name = ? OR";
            q_group = " GROUP BY name";
        }

        StringBuffer query = new StringBuffer(q_base);
        for (String name : names) {
            if (!query.toString().endsWith("OR")) {
                query.append(" WHERE");
            }
            query.append(q_clause);
            name_params.add(name);
        }
        query.delete(query.length()-3, query.length()).append(q_group);

        ps = con.prepareStatement(query.toString());
        int i = 1;
        for (String s : name_params) ps.setString(i++, s);

        return ps.executeQuery();    }
}

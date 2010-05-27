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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;

/**
 *
 * @author rlange
 */
public class FindEntitiesQuery {

    private enum SearchType {
        CHANNEL, PROPERTY, PROPERTY_FOR_CHANNEL
    };
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
     * Creates a FindEntitiesQuery for properties of multiple channel names.
     * @param names String collection of channel names
     * @return new FindEntitiesQuery instance
     */
    public static FindEntitiesQuery createFindPropertiesForChannelNamesQuery(Collection<String> names) {
        return new FindEntitiesQuery(FindEntitiesQuery.SearchType.PROPERTY_FOR_CHANNEL, names);
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
     * Creates and executes the property and tag string match subquery using GROUP.
     *
     * @param con connection to use
     * @return a set of channel ids that match
     */
    private Set<Long> getIdsForChannelNames(Connection con, Collection<String> names) throws CFException {
        PreparedStatement ps;
        String q_base = "SELECT id FROM channel";
        String q_clause = " name=? OR";
        Set<Long> ids = new HashSet<Long>();
        List<String> name_params = new ArrayList<String>();

        if (names.isEmpty()) {
            return ids;
        }

        StringBuffer query = new StringBuffer(q_base);
        for (String name : names) {
            if (!query.toString().endsWith(" OR")) {
                query.append(" WHERE");
            }
            query.append(q_clause);
            name_params.add(name);
        }
        if (query.toString().endsWith(" OR")) {
            query.delete(query.length() - 3, query.length());
        }

        try {
            String s2 = query.toString();
            ps = con.prepareStatement(query.toString());
            int i = 1;
            for (String s : name_params) {
                ps.setString(i++, s);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ids.add(rs.getLong(1));
            }
            return ids;
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception in entity names query (channel ids)", e);
        }
    }

    /**
     * Creates and executes a JDBC based query returning names and owners,
     * using GROUP to collapse it by name.
     */
    private ResultSet executePropertiesForChannelsQuery(Connection con) throws CFException {
        PreparedStatement ps;
        Set<Long> ids;

        String q_base = "SELECT property as name, owner FROM property";
        String q_clause = " channel_id = ? OR";
        String q_group = " GROUP BY property";

        ids = getIdsForChannelNames(con, names);

        if (ids.isEmpty()) {
            return null;
        }

        StringBuffer query = new StringBuffer(q_base);
        for (Long id : ids) {
            if (!query.toString().endsWith(" OR")) {
                query.append(" WHERE");
            }
            query.append(q_clause);
        }
        if (query.toString().endsWith(" OR")) {
            query.delete(query.length() - 3, query.length());
        }
        query.append(q_group);

        try {
            String s2 = query.toString();
            ps = con.prepareStatement(query.toString());
            int i = 1;
            for (Long id : ids) {
                ps.setLong(i++, id);
            }

            return ps.executeQuery();
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception in entity names query (channels with properties)", e);
        }
    }

    /**
     * Creates and executes a JDBC based query returning names and owners,
     * using GROUP to collapse it by name.
     *
     * @param con connection to use
     * @return result set with columns named <tt>name</tt>, <tt>owner</tt> or null if no result
     * @throws CFException wrapping an SQLException
     */
    public ResultSet executeQuery(Connection con) throws CFException {
        PreparedStatement ps;
        List<String> name_params = new ArrayList<String>();

        String q_base;
        String q_clause;
        String q_group;

        if (type == FindEntitiesQuery.SearchType.PROPERTY_FOR_CHANNEL) {
            return executePropertiesForChannelsQuery(con);
        } else if (type == FindEntitiesQuery.SearchType.PROPERTY) {
            q_base = "SELECT property as name, owner FROM property";
            q_clause = " LOWER(property) = ? OR";
            q_group = " GROUP BY property";
        } else {
            q_base = "SELECT name, owner FROM channel";
            q_clause = " name = ? OR";
            q_group = " GROUP BY name";
        }

        StringBuffer query = new StringBuffer(q_base);
        for (String name : names) {
            if (!query.toString().endsWith(" OR")) {
                query.append(" WHERE");
            }
            query.append(q_clause);
            name_params.add(name);
        }
        if (query.toString().endsWith(" OR")) {
            query.delete(query.length() - 3, query.length());
        }
        query.append(q_group);

        try {
            String s2 = query.toString();
            ps = con.prepareStatement(query.toString());
            int i = 1;
            for (String s : name_params) {
                ps.setString(i++, s);
            }

            return ps.executeQuery();
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception in entity names query", e);
        }
    }
}

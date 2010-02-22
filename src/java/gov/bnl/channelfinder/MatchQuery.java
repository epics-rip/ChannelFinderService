/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.channelfinder;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author rlange
 */
public class MatchQuery {

    private Multimap<String, String> prop_matches = ArrayListMultimap.create();
    private List<String> chan_matches = new ArrayList();
    private List<String> tag_matches = new ArrayList();

    /**
     * Creates a new instance of MatchQuery, sorting the query parameters.
     * Property and tag matches go to the inner query,
     * name matches go to the outer query.
     * Property and tag names are converted to lowercase before being matched.
     *
     * @param matches  the map of matches to apply
     */
    public MatchQuery(MultivaluedMap<String, String> matches) {
        for (Map.Entry<String, List<String>> match : matches.entrySet()) {
            String key = match.getKey().toLowerCase();
            if (key.equals("~name")) {
                chan_matches.addAll(match.getValue());
            } else if (key.equals("~tag")) {
                tag_matches.addAll(match.getValue());
            } else {
                prop_matches.putAll(key, match.getValue());
            }
        }
    }

    /**
     * Creates a new instance of MatchQuery for a channel name query.
     *
     * @param matches  the collection of name matches
     */
    public MatchQuery(Collection<String> matches) {
        chan_matches.addAll(matches);
    }

    /**
     * Creates a new instance of MatchQuery for a single channel name query.
     *
     * @param name the name to match
     */
    public MatchQuery(String name) {
        chan_matches.add(name);
    }

    /**
     * Creates and executes the property match subquery using GROUP.
     *
     * @param con  connection to use
     * @return a set of channel ids that match
     */
    private Set<Long> getIdsForPropertyMatch(Connection con) throws SQLException {
        String query = "SELECT p0.channel_id FROM property p0 WHERE";
        Set<Long> ids = new HashSet<Long>();           // set of matching channel ids
        List<String> params = new ArrayList<String>(); // parameter list for this query

        if (prop_matches.size() == 0 && tag_matches.size() == 0) return null;

        for (Map.Entry<String, Collection<String>> match : prop_matches.asMap().entrySet()) {
            String valueList = "p0.value LIKE";
            params.add(match.getKey());
            for (String value : match.getValue()) {
                valueList = valueList + " ? OR p0.value LIKE";
                params.add(convertFileGlobToSQLPattern(value));
            }
            query = query + " (p0.property = ? AND (" +
                    valueList.substring(0, valueList.length() - 17) + ")) OR";
        }

       for (String tag : tag_matches) {
            params.add(convertFileGlobToSQLPattern(tag));
            query = query + " (p0.property LIKE ? AND p0.value IS NULL) OR";
        }

        query = query.substring(0, query.length() - 2) +
                "GROUP BY p0.channel_id HAVING COUNT(p0.channel_id) = " +
                (prop_matches.size() + tag_matches.size());

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
        return ids;
    }

    /**
     * Creates and executes a JDBC based query using the GROUP based subquery
     *
     * @param con  connection to use
     * @return  result set with columns named <tt>channel</tt>, <tt>property</tt>, <tt>value</tt>
     * @throws SQLException
     */
    public ResultSet executeQuery(Connection con) throws SQLException {
        String query = "SELECT c.name as channel, c.owner as cowner, p.property, p.value, p.owner" +
                " FROM channel c, property p WHERE c.id = p.channel_id";
        List<Long> id_params = new ArrayList<Long>();       // parameter lists for the outer query
        List<String> name_params = new ArrayList<String>();

        Set<Long> ids = getIdsForPropertyMatch(con);

        if (ids != null) {
            query = query + " AND c.id IN (0,"; // 0 added to avoid SQL error with empty id list
            for (long i : ids) {
                query = query + "?,";
                id_params.add(i);
            }
            query = query.substring(0, query.length() - 1) + ")";
        }

        for (String value : chan_matches) {
            query = query + " AND c.name LIKE ?";
            name_params.add(convertFileGlobToSQLPattern(value));
        }

        PreparedStatement ps = con.prepareStatement(query);
        int i = 1;
        for (long p : id_params) ps.setLong(i++, p);
        for (String s : name_params) ps.setString(i++, s);

        return ps.executeQuery();
    }

    /* Regexp for this pattern: "((\\\\)*)((\\\*)|(\*)|(\\\?)|(\?)|(%)|(_))"
     * i.e. any number of "\\" (group 1) -> same number of "\\"
     * then any of        "\*" (group 4) -> "*"
     *                    "*"  (group 5) -> "%"
     *                    "\?" (group 6) -> "?"
     *                    "?"  (group 7) -> "_"
     *                    "%"  (group 8) -> "\%"
     *                    "_"  (group 9) -> "\_"
     */
    private static Pattern pat = Pattern.compile("((\\\\\\\\)*)((\\\\\\*)|(\\*)|(\\\\\\?)|(\\?)|(%)|(_))");
    private static final int    grp[] = {   4,   5,   6,   7,     8,     9  };
    private static final String rpl[] = { "*", "%", "?", "_", "\\%", "\\_" };

    /**
     * Translates the specified file glob pattern <tt>in</tt>
     * into the corresponding SQL pattern
     *
     * @param in  file glob pattern
     * @return  SQL pattern
     */
    private static String convertFileGlobToSQLPattern(String in) {
    StringBuffer out = new StringBuffer();
    Matcher m = pat.matcher(in);

        while (m.find()) {
            StringBuffer rep = new StringBuffer();
            if (m.group(1) != null) rep.append(m.group(1));
            for (int i = 0; i < grp.length; i++) {
                if (m.group(grp[i]) != null) {
                    rep.append(rpl[i]);
                    break;
                }
            }
            m.appendReplacement(out, rep.toString());
        }
    m.appendTail(out);
    return out.toString();
    }
}
